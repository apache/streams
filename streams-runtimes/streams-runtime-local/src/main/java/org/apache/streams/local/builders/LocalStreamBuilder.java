package org.apache.streams.local.builders;

import org.apache.streams.core.*;
import org.apache.streams.local.monitors.MonitorJVMTimerTask;
import org.apache.streams.local.monitors.StatusCounterMonitorThread;
import org.apache.streams.local.tasks.*;
import org.apache.streams.util.SerializationUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

/**
 * {@link org.apache.streams.local.builders.LocalStreamBuilder} implementation to run a data processing stream in a single
 * JVM across many threads.  Depending on your data stream, the JVM heap may need to be set to a high value. Default
 * implementation uses unbound {@link java.util.concurrent.ConcurrentLinkedQueue} to connect stream components.
 */
public class LocalStreamBuilder implements StreamBuilder {

    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LocalStreamBuilder.class);

    public static final String TIMEOUT_KEY = "TIMEOUT";
    private final Queue<StreamsDatum> queue;
    private final Map<String, StreamComponent> providers;
    private final Map<String, StreamComponent> components;
    private final Map<String, Object> streamConfig;
    private Map<String, BaseStreamsTask> tasks;

    private Thread shutDownHandler;

    /**
     *
     */
    public LocalStreamBuilder() {
        this(new ArrayBlockingQueue<StreamsDatum>(50), null);
    }

    /**
     * @param streamConfig
     */
    public LocalStreamBuilder(Map<String, Object> streamConfig) {
        this(new ArrayBlockingQueue<StreamsDatum>(50), streamConfig);
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
    }

    @Override
    public StreamBuilder newPerpetualStream(String id, StreamsProvider provider) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, true));
        return this;
    }

    @Override
    public StreamBuilder newReadCurrentStream(String id, StreamsProvider provider) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, false));
        return this;
    }

    @Override
    public StreamBuilder newReadNewStream(String id, StreamsProvider provider, BigInteger sequence) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, sequence));
        return this;
    }

    @Override
    public StreamBuilder newReadRangeStream(String id, StreamsProvider provider, DateTime start, DateTime end) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, start, end));
        return this;
    }

    @Override
    public StreamBuilder addStreamsProcessor(String id, StreamsProcessor processor, int numTasks, String... inBoundIds) {
        validateId(id);
        StreamComponent comp = new StreamComponent(id, processor, cloneQueue(), numTasks);
        this.components.put(id, comp);
        connectToOtherComponents(inBoundIds, comp);
        return this;
    }

    @Override
    public StreamBuilder addStreamsPersistWriter(String id, StreamsPersistWriter writer, int numTasks, String... inBoundIds) {
        validateId(id);
        StreamComponent comp = new StreamComponent(id, writer, cloneQueue(), numTasks);
        this.components.put(id, comp);
        connectToOtherComponents(inBoundIds, comp);
        return this;
    }


    private ExecutorService executor;

    /**
     * Runs the data stream in the this JVM and blocks till completion.
     */
    @Override
    public synchronized void start() {

        final Timer timer = new Timer(true);

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

        this.tasks = createTasks();
        this.executor = Executors.newFixedThreadPool(tasks.size());

        try {
            LOGGER.info("----------------------------------- Starting LocalStream Builder -----------------------------------");

            for(StreamsTask task : this.tasks.values()) {
                this.executor.execute(task);

                // Count whatever we are telling ourselves
                if(task instanceof DatumStatusCountable)
                    timer.schedule(new StatusCounterMonitorThread((DatumStatusCountable)task), 0, 1000);
            }

            // track the JVM every 500ms
            timer.schedule(new MonitorJVMTimerTask(), 0, 500);

            boolean isRunning = true;

            while (isRunning) {
                isRunning = false;

                // check to see if it is running, if it is, then set the flag and break.
                for (BaseStreamsTask task : this.tasks.values()) {
                    if(task instanceof StreamsProviderTask) {
                        if(task.isRunning()) {
                            isRunning = true;
                            break;
                        }
                    }
                    else {
                        if(task.isRunning() || task.isDatumAvailable()) {
                            isRunning = true;
                            break;
                        }
                    }
                }

                if (isRunning) {
                    Thread.yield();
                    Thread.sleep(1);
                }
            }

            LOGGER.debug("Components are no longer running, we can turn it off");
            shutdown();

        } catch (Throwable e) {
            // No Operation
            try {
                shutdown();
            }
            catch (Throwable omgE) {
                LOGGER.error("Unexpected Error: {}", omgE);
            }
        } finally {
            if (!Runtime.getRuntime().removeShutdownHook(this.shutDownHandler))
                LOGGER.warn("We should have removed the shutdown handler...");

            this.shutDownHandler = null;

            // Kill the timer
            timer.cancel();
        }

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

    protected void shutdown() throws InterruptedException {
        LOGGER.debug("Shutting down...");
        //give the stream 30secs to try to shutdown gracefully, then force shutdown otherwise
        for(BaseStreamsTask task : this.tasks.values())
            task.stopTask();

        shutdownExecutor();
    }

    protected Map<String, BaseStreamsTask> createTasks() {
        Map<String, BaseStreamsTask> toReturn = new LinkedHashMap<String, BaseStreamsTask>();

        for (StreamComponent prov : this.providers.values()) {
            BaseStreamsTask task = prov.createConnectedTask(getTimeout());
            task.setStreamConfig(this.streamConfig);
            toReturn.put(prov.getId(), task);
        }

        for (StreamComponent comp : this.components.values()) {
            BaseStreamsTask task = comp.createConnectedTask(getTimeout());
            task.setStreamConfig(this.streamConfig);
            toReturn.put(comp.getId(), task);
        }

        return toReturn;
    }

    public void stop() {
        try {
            shutdown();
        } catch (Exception e) {
            LOGGER.warn("Forcing Shutdown: There was an error stopping: {}", e.getMessage());
        }
    }

    private void connectToOtherComponents(String[] connectToIds, StreamComponent toBeConnected) {
        for (String id : connectToIds) {
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

    @SuppressWarnings("unchecked")
    private Queue<StreamsDatum> cloneQueue() {
        Object toReturn = SerializationUtil.cloneBySerialization(this.queue);
        if(toReturn instanceof Queue)
            return (Queue<StreamsDatum>)toReturn;
        else
            throw new RuntimeException("Unable to clone the queue");
    }

    protected int getTimeout() {
        return streamConfig != null && streamConfig.containsKey(TIMEOUT_KEY) ? (Integer) streamConfig.get(TIMEOUT_KEY) : 3000;
    }

}
