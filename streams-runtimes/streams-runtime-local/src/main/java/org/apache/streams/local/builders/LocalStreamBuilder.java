package org.apache.streams.local.builders;

import org.apache.streams.core.*;
import org.apache.streams.local.monitors.MonitorJVMTask;
import org.apache.streams.local.monitors.StatusCounterMonitorThread;
import org.apache.streams.local.tasks.*;
import org.apache.streams.util.SerializationUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * {@link org.apache.streams.local.builders.LocalStreamBuilder} implementation to run a data processing stream in a single
 * JVM across many threads.  Depending on your data stream, the JVM heap may need to be set to a high value. Default
 * implementation uses unbound {@link java.util.concurrent.ConcurrentLinkedQueue} to connect stream components.
 */
public class LocalStreamBuilder implements StreamBuilder {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LocalStreamBuilder.class);

    public static final String TIMEOUT_KEY = "TIMEOUT";
    private Map<String, StreamComponent> providers;
    private Map<String, StreamComponent> components;
    private Queue<StreamsDatum> queue;
    private Map<String, Object> streamConfig;
    private ExecutorService executor;
    private int totalTasks;

    private final Map<String, List<StreamsTask>> tasks = new HashMap<String, List<StreamsTask>>();
    private Thread shutDownHandler;

    /**
     *
     */
    public LocalStreamBuilder() {
        this(new ConcurrentLinkedQueue<StreamsDatum>(), null);
    }

    /**
     * @param streamConfig
     */
    public LocalStreamBuilder(Map<String, Object> streamConfig) {
        this(new ConcurrentLinkedQueue<StreamsDatum>(), streamConfig);
    }

    /**
     * @param queueType
     */
    public LocalStreamBuilder(Queue<StreamsDatum> queueType) {
        this(queueType, null);
    }

    /**
     * @param queueType
     * @param streamConfig
     */
    public LocalStreamBuilder(Queue<StreamsDatum> queueType, Map<String, Object> streamConfig) {
        this.queue = queueType;
        this.providers = new HashMap<String, StreamComponent>();
        this.components = new HashMap<String, StreamComponent>();
        this.streamConfig = streamConfig;
        this.totalTasks = 0;
    }

    @Override
    public StreamBuilder newPerpetualStream(String id, StreamsProvider provider) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, true));
        ++this.totalTasks;
        return this;
    }

    @Override
    public StreamBuilder newReadCurrentStream(String id, StreamsProvider provider) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, false));
        ++this.totalTasks;
        return this;
    }

    @Override
    public StreamBuilder newReadNewStream(String id, StreamsProvider provider, BigInteger sequence) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, sequence));
        ++this.totalTasks;
        return this;
    }

    @Override
    public StreamBuilder newReadRangeStream(String id, StreamsProvider provider, DateTime start, DateTime end) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, start, end));
        ++this.totalTasks;
        return this;
    }

    @Override
    public StreamBuilder addStreamsProcessor(String id, StreamsProcessor processor, int numTasks, String... inBoundIds) {
        validateId(id);
        StreamComponent comp = new StreamComponent(id, processor, cloneQueue(), numTasks);
        this.components.put(id, comp);
        connectToOtherComponents(inBoundIds, comp);
        this.totalTasks += numTasks;
        return this;
    }

    @Override
    public StreamBuilder addStreamsPersistWriter(String id, StreamsPersistWriter writer, int numTasks, String... inBoundIds) {
        validateId(id);
        StreamComponent comp = new StreamComponent(id, writer, cloneQueue(), numTasks);
        this.components.put(id, comp);
        connectToOtherComponents(inBoundIds, comp);
        this.totalTasks += numTasks;
        return this;
    }

    /**
     * Runs the data stream in the this JVM and blocks till completion.
     */
    @Override
    public synchronized void start() {

        Timer timer = new Timer(true);

        if (this.shutDownHandler != null) {
            String message = "The stream builder has already been started and has not been successfully shutdown. Nothing will execute.";
            LOGGER.warn(message);
            throw new RuntimeException(message);
        }

        // Notice, we are making a reference to 'self' we need to remove this handler
        // once we are completed ot ensure we don't hold onto this object reference
        final LocalStreamBuilder self = this;
        this.shutDownHandler = new Thread() {
            @Override
            public void run() {
                LOGGER.debug("Shutdown hook received.  Beginning shutdown");
                self.stop();
            }
        };

        Runtime.getRuntime().addShutdownHook(shutDownHandler);

        this.executor = Executors.newFixedThreadPool(this.totalTasks);

        Map<String, StreamsProviderTask> provTasks = new HashMap<String, StreamsProviderTask>();

        try {
            // track the JVM
            timer.schedule(new MonitorJVMTask(), 0);

            setupComponentTasks(tasks, timer);
            setupProviderTasks(provTasks, timer);

            LOGGER.info("----------------------------------- Starting LocalStream Builder -----------------------------------");
            LOGGER.info("Worker Counts - Components[{}] - Providers[{}] - Tasks[{}]", this.components.size(), this.providers.size(), tasks.size());
            LOGGER.info("----------------------------------------------------------------------------------------------------");

            /*
            if(this.totalTasks != this.providers.size() + tasks.size())
                throw new RuntimeException("Not Equal");
            */

            boolean isRunning = true;

            while (isRunning) {
                isRunning = false;

                // check to see if it is running, if it is, then set the flag and break.
                for (StreamsProviderTask task : provTasks.values()) {
                    if (task.isRunning()) {
                        isRunning = true;
                        break;
                    }
                }

                // if we haven't already asserted that we are running, check these too.
                if (!isRunning) {
                    for (StreamComponent task : components.values())
                        if (task.getInBoundQueue().size() > 0) {
                            isRunning = true;
                            break;
                        }
                }

                if (isRunning) {
                    Thread.yield();
                }
            }

            LOGGER.debug("Components are no longer running, we can turn it off");
            shutdown(tasks);
        } catch (InterruptedException e) {
            forceShutdown(tasks);
        } finally {
            if (!Runtime.getRuntime().removeShutdownHook(this.shutDownHandler))
                LOGGER.warn("We should have removed the shutdown handler...");

            this.shutDownHandler = null;

            // Kill the timer
            timer.cancel();
        }

    }

    protected void forceShutdown(Map<String, List<StreamsTask>> streamsTasks) {

        LOGGER.warn("Shutdown failed.  Forcing shutdown");
        //give the stream 30secs to try to shutdown gracefully, then force shutdown otherwise
        for (List<StreamsTask> tasks : streamsTasks.values()) {
            for (StreamsTask task : tasks) {
                task.stopTask();
            }
        }

        shutdownExecutor();
    }

    private void shutdownExecutor() {
        // make sure that
        try {
            if (!this.executor.isShutdown()) {
                // tell the executor to shutdown.
                this.executor.shutdown();

                if (!this.executor.awaitTermination(5, TimeUnit.MINUTES))
                    this.executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            this.executor.shutdownNow();
            throw new RuntimeException(ie);
        }
    }

    protected void shutdown(Map<String, List<StreamsTask>> streamsTasks) throws InterruptedException {

        LOGGER.info("Shutting down LocalStreamsBuilder");

        //complete stream shut down gracefully, ask them to shutdown
        for (StreamComponent prov : this.providers.values())
            shutDownTask(prov, streamsTasks);

        shutdownExecutor();
    }

    protected void setupProviderTasks(Map<String, StreamsProviderTask> provTasks, Timer timer) {
        for (StreamComponent prov : this.providers.values()) {
            StreamsTask task = prov.createConnectedTask(getTimeout());
            task.setStreamConfig(this.streamConfig);
            this.executor.submit(task);
            provTasks.put(prov.getId(), (StreamsProviderTask) task);
            if (prov.isOperationCountable()) {
                timer.schedule(new StatusCounterMonitorThread((DatumStatusCountable)prov.getOperation()), 0, 2);
                timer.schedule(new StatusCounterMonitorThread((DatumStatusCountable)task), 0, 2);
            }
        }
    }

    protected void setupComponentTasks(Map<String, List<StreamsTask>> streamsTasks, Timer timer) {
        for (StreamComponent comp : this.components.values()) {
            int tasks = comp.getNumTasks();
            List<StreamsTask> compTasks = new LinkedList<StreamsTask>();
            for (int i = 0; i < tasks; ++i) {
                StreamsTask task = comp.createConnectedTask(getTimeout());
                task.setStreamConfig(this.streamConfig);
                this.executor.submit(task);
                compTasks.add(task);
                if (comp.isOperationCountable()) {
                    timer.schedule(new StatusCounterMonitorThread((DatumStatusCountable) comp.getOperation()), 0, 2);
                    timer.schedule(new StatusCounterMonitorThread((DatumStatusCountable) task), 0, 2);
                }
            }
            streamsTasks.put(comp.getId(), compTasks);
        }
    }

    /**
     * Shutdown the running tasks in sudo depth first search kind of way. Checks that the upstream components have
     * finished running before shutting down. Waits till inbound queue is empty to shutdown.
     *
     * @param comp        StreamComponent to shut down.
     * @param streamTasks the list of non-StreamsProvider tasks for this stream.
     * @throws InterruptedException
     */
    private void shutDownTask(StreamComponent comp, Map<String, List<StreamsTask>> streamTasks) throws InterruptedException {
        List<StreamsTask> tasks = streamTasks.get(comp.getId());
        if (tasks != null) { //not a StreamProvider
            boolean parentsShutDown = true;
            for (StreamComponent parent : comp.getUpStreamComponents()) {
                List<StreamsTask> parentTasks = streamTasks.get(parent.getId());
                //if parentTask == null, its a provider and is not running anymore
                if (parentTasks != null) {
                    for (StreamsTask task : parentTasks) {
                        parentsShutDown = parentsShutDown && !task.isRunning();
                    }
                }
            }
            if (parentsShutDown) {
                for (StreamsTask task : tasks) {
                    task.stopTask();
                }
                for (StreamsTask task : tasks) {
                    int count = 0;
                    while (count++ < 2000 && task.isRunning()) {
                        Thread.yield();
                        Thread.sleep(5);
                    }
                    if (task.isRunning()) {
                        LOGGER.warn("Task {} failed to terminate in allotted time-frame", task.toString());
                    }
                }
            }
        }
        Collection<StreamComponent> children = comp.getDownStreamComponents();
        if (children != null) {
            for (StreamComponent child : comp.getDownStreamComponents()) {
                shutDownTask(child, streamTasks);
            }
        }
    }

    public void stop() {
        try {
            shutdown(tasks);
        } catch (Exception e) {
            LOGGER.warn("Forcing Shutdown: There was an error stopping: {}", e.getMessage());
            forceShutdown(tasks);
        }
    }

    private void connectToOtherComponents(String[] conntectToIds, StreamComponent toBeConnected) {
        for (String id : conntectToIds) {
            StreamComponent upStream = null;
            if (this.providers.containsKey(id)) {
                upStream = this.providers.get(id);
            } else if (this.components.containsKey(id)) {
                upStream = this.components.get(id);
            } else {
                throw new InvalidStreamException("Cannot connect to id, " + id + ", because id does not exist.");
            }
            upStream.addOutBoundQueue(toBeConnected, toBeConnected.getInBoundQueue());
            toBeConnected.addInboundQueue(upStream);
        }
    }

    private void validateId(String id) {
        if (this.providers.containsKey(id) || this.components.containsKey(id)) {
            throw new InvalidStreamException("Duplicate id. " + id + " is already assigned to another component");
        }
    }


    private Queue<StreamsDatum> cloneQueue() {
        return (Queue<StreamsDatum>) SerializationUtil.cloneBySerialization(this.queue);
    }

    protected int getTimeout() {
        return streamConfig != null && streamConfig.containsKey(TIMEOUT_KEY) ? (Integer) streamConfig.get(TIMEOUT_KEY) : 3000;
    }

}
