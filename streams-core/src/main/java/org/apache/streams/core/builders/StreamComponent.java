package org.apache.streams.core.builders;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.tasks.StreamsPersistWriterTask;
import org.apache.streams.core.tasks.StreamsProcessorTask;
import org.apache.streams.core.tasks.StreamsProviderTask;
import org.apache.streams.core.tasks.StreamsTask;
import org.apache.streams.util.SerializationUtil;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.*;

/**
 * Stores the implementations of {@link org.apache.streams.core.StreamsOperation}, the StreamsOperations it is connected
 * to and the necessary metadata to construct a data stream.
 */
public class StreamComponent {

    private static final int START = 1;
    private static final int END = 2;

    private String id;
    private Set<StreamComponent> inBound;
    private Map<StreamComponent, Queue<StreamsDatum>> outBound;
    private Queue<StreamsDatum> inQueue;
    private StreamsProvider provider;
    private StreamsProcessor processor;
    private StreamsPersistWriter writer;
    private DateTime[] dateRange;
    private BigInteger sequence;
    private int numTasks = 1;

    /**
     *
     * @param id
     * @param provider
     */
    public StreamComponent(String id, StreamsProvider provider) {
        this.id = id;
        this.provider = provider;
        initializePrivateVariables();
    }

    /**
     *
     * @param id
     * @param provider
     * @param start
     * @param end
     */
    public StreamComponent(String id, StreamsProvider provider, DateTime start, DateTime end) {
        this.id = id;
        this.provider = provider;
        this.dateRange = new DateTime[2];
        this.dateRange[START] = start;
        this.dateRange[END] = end;
        initializePrivateVariables();
    }


    /**
     *
     * @param id
     * @param provider
     * @param sequence
     */
    public StreamComponent(String id, StreamsProvider provider, BigInteger sequence) {
        this.id = id;
        this.provider = provider;
        this.sequence = sequence;
    }

    /**
     *
     * @param id
     * @param processor
     * @param inQueue
     * @param numTasks
     */
    public StreamComponent(String id, StreamsProcessor processor, Queue<StreamsDatum> inQueue, int numTasks) {
        this.id = id;
        this.processor = processor;
        this.inQueue = inQueue;
        this.numTasks = numTasks;
        initializePrivateVariables();
    }

    /**
     *
     * @param id
     * @param writer
     * @param inQueue
     * @param numTasks
     */
    public StreamComponent(String id, StreamsPersistWriter writer, Queue<StreamsDatum> inQueue, int numTasks) {
        this.id = id;
        this.writer = writer;
        this.inQueue = inQueue;
        this.numTasks = numTasks;
        initializePrivateVariables();
    }

    private void initializePrivateVariables() {
        this.inBound = new HashSet<StreamComponent>();
        this.outBound = new HashMap<StreamComponent, Queue<StreamsDatum>>();
    }

    /**
     * Add an outbound queue for this component. The queue should be an inbound queue of a downstream component.
     * @param component the component that this supplying their inbound queue
     * @param queue the queue to to put post processed/provided datums on
     */
    public void addOutBoundQueue(StreamComponent component, Queue<StreamsDatum> queue) {
        this.outBound.put(component, queue);
    }

    /**
     * Add a component that supplies data through the inbound queue.
     * @param component that supplies data through the inbound queue
     */
    public void addInboundQueue(StreamComponent component) {
        this.inBound.add(component);
    }

    /**
     * The components that are immediately downstream of this component (aka child nodes)
     * @return Collection of child nodes of this component
     */
    public Collection<StreamComponent> getDownStreamComponents() {
        return this.outBound.keySet();
    }

    /**
     * The components that are immediately upstream of this component (aka parent nodes)
     * @return Collection of parent nodes of this component
     */
    public Collection<StreamComponent> getUpStreamComponents() {
        return this.inBound;
    }

    /**
     * The inbound queue for this component
     * @return inbound queue
     */
    public Queue<StreamsDatum> getInBoundQueue() {
        return this.inQueue;
    }

    /**
     * The number of tasks this to run this component
     * @return
     */
    public int getNumTasks() {
        return this.numTasks;
    }

    /**
     * Creates a {@link org.apache.streams.core.tasks.StreamsTask} that is running a clone of this component whose
     * inbound and outbound queues are appropriately connected to the parent and child nodes.
     * @return StreamsTask for this component
     */
    public StreamsTask createConnectedTask() {
        StreamsTask task;
        if(this.processor != null) {
            task =  new StreamsProcessorTask((StreamsProcessor)SerializationUtil.cloneBySerialization(this.processor));
            task.addInputQueue(this.inQueue);
            for(Queue<StreamsDatum> q : this.outBound.values()) {
                task.addOutputQueue(q);
            }
        }
        else if(this.writer != null) {
            task = new StreamsPersistWriterTask((StreamsPersistWriter) SerializationUtil.cloneBySerialization(this.writer));
            task.addInputQueue(this.inQueue);
        }
        else if(this.provider != null) {
            StreamsProvider prov = (StreamsProvider)SerializationUtil.cloneBySerialization(this.provider);
            if(this.dateRange == null && this.sequence == null)
                task = new StreamsProviderTask(prov);
            else if(this.sequence != null)
                task = new StreamsProviderTask(prov, this.sequence);
            else
                task = new StreamsProviderTask(prov, this.dateRange[0], this.dateRange[1]);
            for(Queue<StreamsDatum> q : this.outBound.values()) {
                task.addOutputQueue(q);
            }
        }
        else {
            throw new InvalidStreamException("Underlying StreamComponoent was NULL.");
        }
        return task;
    }

    /**
     * The unique of this component
     * @return
     */
    public String getId() {
        return this.id;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof StreamComponent)
            return this.id.equals(((StreamComponent) o).id);
        else
            return false;
    }
}
