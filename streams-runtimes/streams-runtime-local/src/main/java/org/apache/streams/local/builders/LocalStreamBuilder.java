package org.apache.streams.local.builders;

import org.apache.streams.core.*;
import org.apache.streams.local.tasks.LocalStreamProcessMonitorThread;
import org.apache.streams.local.tasks.StatusCounterMonitorThread;
import org.apache.streams.local.tasks.StreamsProviderTask;
import org.apache.streams.local.tasks.StreamsTask;
import org.apache.streams.util.SerializationUtil;
import org.joda.time.DateTime;

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

    private Map<String, StreamComponent> providers;
    private Map<String, StreamComponent> components;
    private Queue<StreamsDatum> queue;
    private Map<String, Object> streamConfig;
    private ExecutorService executor;
    private ExecutorService monitor;
    private int totalTasks;
    private int monitorTasks;
    private LocalStreamProcessMonitorThread monitorThread;

    /**
     *
     */
    public LocalStreamBuilder(){
        this(new ConcurrentLinkedQueue<StreamsDatum>(), null);
    }

    /**
     *
     * @param streamConfig
     */
    public LocalStreamBuilder(Map<String, Object> streamConfig) {
        this(new ConcurrentLinkedQueue<StreamsDatum>(), streamConfig);
    }

    /**
     *
     * @param queueType
     */
    public LocalStreamBuilder(Queue<StreamsDatum> queueType) {
        this(queueType, null);
    }

    /**
     *
     * @param queueType
     * @param streamConfig
     */
    public LocalStreamBuilder(Queue<StreamsDatum> queueType, Map<String, Object> streamConfig) {
        this.queue = queueType;
        this.providers = new HashMap<String, StreamComponent>();
        this.components = new HashMap<String, StreamComponent>();
        this.streamConfig = streamConfig;
        this.totalTasks = 0;
        this.monitorTasks = 0;
    }

    @Override
    public StreamBuilder newPerpetualStream(String id, StreamsProvider provider) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, true));
        ++this.totalTasks;
        if( provider instanceof DatumStatusCountable )
            ++this.monitorTasks;
        return this;
    }

    @Override
    public StreamBuilder newReadCurrentStream(String id, StreamsProvider provider) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, false));
        ++this.totalTasks;
        if( provider instanceof DatumStatusCountable )
            ++this.monitorTasks;
        return this;
    }

    @Override
    public StreamBuilder newReadNewStream(String id, StreamsProvider provider, BigInteger sequence) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, sequence));
        ++this.totalTasks;
        if( provider instanceof DatumStatusCountable )
            ++this.monitorTasks;
        return this;
    }

    @Override
    public StreamBuilder newReadRangeStream(String id, StreamsProvider provider, DateTime start, DateTime end) {
        validateId(id);
        this.providers.put(id, new StreamComponent(id, provider, start, end));
        ++this.totalTasks;
        if( provider instanceof DatumStatusCountable )
            ++this.monitorTasks;
        return this;
    }

    @Override
    public StreamBuilder addStreamsProcessor(String id, StreamsProcessor processor, int numTasks, String... inBoundIds) {
        validateId(id);
        StreamComponent comp = new StreamComponent(id, processor, cloneQueue(), numTasks);
        this.components.put(id, comp);
        connectToOtherComponents(inBoundIds, comp);
        this.totalTasks += numTasks;
        if( processor instanceof DatumStatusCountable )
            ++this.monitorTasks;
        return this;
    }

    @Override
    public StreamBuilder addStreamsPersistWriter(String id, StreamsPersistWriter writer, int numTasks, String... inBoundIds) {
        validateId(id);
        StreamComponent comp = new StreamComponent(id, writer, cloneQueue(), numTasks);
        this.components.put(id, comp);
        connectToOtherComponents(inBoundIds, comp);
        this.totalTasks += numTasks;
        if( writer instanceof DatumStatusCountable )
            ++this.monitorTasks;
        return this;
    }

    /**
     * Runs the data stream in the this JVM and blocks till completion.
     */
    @Override
    public void start() {
        boolean isRunning = true;
        this.executor = Executors.newFixedThreadPool(this.totalTasks);
        this.monitor = Executors.newFixedThreadPool(this.monitorTasks+1);
        Map<String, StreamsProviderTask> provTasks = new HashMap<String, StreamsProviderTask>();
        Map<String, List<StreamsTask>> streamsTasks = new HashMap<String, List<StreamsTask>>();
        try {
            monitorThread = new LocalStreamProcessMonitorThread(executor, 10);
            this.monitor.submit(monitorThread);
            for(StreamComponent comp : this.components.values()) {
                int tasks = comp.getNumTasks();
                List<StreamsTask> compTasks = new LinkedList<StreamsTask>();
                for(int i=0; i < tasks; ++i) {
                    StreamsTask task = comp.createConnectedTask();
                    task.setStreamConfig(this.streamConfig);
                    this.executor.submit(task);
                    compTasks.add(task);
                    if( comp.isOperationCountable() ) {
                        this.monitor.submit(new StatusCounterMonitorThread((DatumStatusCountable) comp.getOperation(), 10));
                        this.monitor.submit(new StatusCounterMonitorThread((DatumStatusCountable) task, 10));
                    }
                }
                streamsTasks.put(comp.getId(), compTasks);
            }
            for(StreamComponent prov : this.providers.values()) {
                StreamsTask task = prov.createConnectedTask();
                task.setStreamConfig(this.streamConfig);
                this.executor.submit(task);
                provTasks.put(prov.getId(), (StreamsProviderTask) task);
                if( prov.isOperationCountable() ) {
                    this.monitor.submit(new StatusCounterMonitorThread((DatumStatusCountable) prov.getOperation(), 10));
                    this.monitor.submit(new StatusCounterMonitorThread((DatumStatusCountable) task, 10));
                }
            }
            while(isRunning) {
                isRunning = false;
                for(StreamsProviderTask task : provTasks.values()) {
                    isRunning = isRunning || task.isRunning();
                }
                if(isRunning) {
                    Thread.sleep(3000);
                }
            }
            monitorThread.shutdown();
            this.executor.shutdown();
            //complete stream shut down gracfully
            for(StreamComponent prov : this.providers.values()) {
                shutDownTask(prov, streamsTasks);
            }
            //need to make this configurable
            if(!this.executor.awaitTermination(10, TimeUnit.SECONDS)) { // all threads should have terminated already.
                this.executor.shutdownNow();
                this.executor.awaitTermination(10, TimeUnit.SECONDS);
            }
            if(!this.monitor.awaitTermination(5, TimeUnit.SECONDS)) { // all threads should have terminated already.
                this.monitor.shutdownNow();
                this.monitor.awaitTermination(5, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e){
            //give the stream 30secs to try to shutdown gracefully, then force shutdown otherwise
            for(List<StreamsTask> tasks : streamsTasks.values()) {
                for(StreamsTask task : tasks) {
                    task.stopTask();
                }
            }
            this.executor.shutdown();
            this.monitor.shutdown();
            try {
                if(!this.executor.awaitTermination(3, TimeUnit.SECONDS)){
                    this.executor.shutdownNow();
                }
                if(!this.monitor.awaitTermination(3, TimeUnit.SECONDS)){
                    this.monitor.shutdownNow();
                }
            }catch (InterruptedException ie) {
                this.executor.shutdownNow();
                this.monitor.shutdownNow();
                throw new RuntimeException(ie);
            }
        }

    }

    /**
     * Shutsdown the running tasks in sudo depth first search kind of way. Checks that the upstream components have
     * finished running before shutting down. Waits till inbound queue is empty to shutdown.
     * @param comp StreamComponent to shut down.
     * @param streamTasks the list of non-StreamsProvider tasks for this stream.
     * @throws InterruptedException
     */
    private void shutDownTask(StreamComponent comp, Map<String, List<StreamsTask>> streamTasks) throws InterruptedException {
        List<StreamsTask> tasks = streamTasks.get(comp.getId());
        if(tasks != null) { //not a StreamProvider
            boolean parentsShutDown = true;
            for(StreamComponent parent : comp.getUpStreamComponents()) {
                List<StreamsTask> parentTasks = streamTasks.get(parent.getId());
                //if parentTask == null, its a provider and is not running anymore
                if(parentTasks != null) {
                    for(StreamsTask task : parentTasks) {
                        parentsShutDown = parentsShutDown && !task.isRunning();
                    }
                }
            }
            if(parentsShutDown) {
                for(StreamsTask task : tasks) {
                    task.stopTask();
                }
                for(StreamsTask task : tasks) {
                    while(task.isRunning()) {
                        Thread.sleep(500);
                    }
                }
            }
        }
        Collection<StreamComponent> children = comp.getDownStreamComponents();
        if(children != null) {
            for(StreamComponent child : comp.getDownStreamComponents()) {
                shutDownTask(child, streamTasks);
            }
        }
    }

    /**
     * NOT IMPLEMENTED.
     */
    @Override
    public void stop() {

    }

    private void connectToOtherComponents(String[] conntectToIds, StreamComponent toBeConnected) {
        for(String id : conntectToIds) {
            StreamComponent upStream = null;
            if(this.providers.containsKey(id)) {
                upStream = this.providers.get(id);
            }
            else if(this.components.containsKey(id)) {
                upStream = this.components.get(id);
            }
            else {
                throw new InvalidStreamException("Cannot connect to id, "+id+", because id does not exist.");
            }
            upStream.addOutBoundQueue(toBeConnected, toBeConnected.getInBoundQueue());
            toBeConnected.addInboundQueue(upStream);
        }
    }

    private void validateId(String id) {
        if(this.providers.containsKey(id) || this.components.containsKey(id)) {
            throw new InvalidStreamException("Duplicate id. "+id+" is already assigned to another component");
        }
    }


    private Queue<StreamsDatum> cloneQueue() {
        return (Queue<StreamsDatum>) SerializationUtil.cloneBySerialization(this.queue);
    }


}
