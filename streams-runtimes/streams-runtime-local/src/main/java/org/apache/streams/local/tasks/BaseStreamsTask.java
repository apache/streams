package org.apache.streams.local.tasks;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.SerializationException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.*;

/**
 *
 */
public abstract class BaseStreamsTask implements StreamsTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseStreamsTask.class);

    private List<Queue<StreamsDatum>> inQueues = new ArrayList<Queue<StreamsDatum>>();
    private List<Queue<StreamsDatum>> outQueues = new LinkedList<Queue<StreamsDatum>>();
    private int inIndex = 0;

    @Override
    public void addInputQueue(Queue<StreamsDatum> inputQueue) {
        this.inQueues.add(inputQueue);
    }

    @Override
    public void addOutputQueue(Queue<StreamsDatum> outputQueue) {
        this.outQueues.add(outputQueue);
    }

    @Override
    public List<Queue<StreamsDatum>> getInputQueues() {
        return this.inQueues;
    }

    @Override
    public List<Queue<StreamsDatum>> getOutputQueues() {
        return this.outQueues;
    }

    /**
     * NOTE NECESSARY AT THE MOMENT.  MAY BECOME NECESSARY AS WE LOOK AT MAKING JOIN TASKS. CURRENTLY ALL TASK HAVE MAX
     * OF 1 INPUT QUEUE.
     * Round Robins through input queues to get the next StreamsDatum. If all input queues are empty, it will return null.
     *
     * @return the next StreamsDatum or null if all input queues are empty.
     */
    protected StreamsDatum getNextDatum() {
        int startIndex = this.inIndex;
        int index = startIndex;
        StreamsDatum datum = null;
        do {
            datum = this.inQueues.get(index).poll();
            index = getNextInputQueueIndex();
        } while (datum == null && startIndex != index);
        return datum;
    }

    /**
     * Adds a StreamDatum to the outgoing queues.  If there are multiple queues, it uses serialization to create
     * clones of the datum and adds a new clone to each queue.
     *
     * @param datum The datum you wish to add to an outgoing queue
     */
    protected void addToOutgoingQueue(StreamsDatum datum) {
        if (datum != null) {
            if (this.outQueues.size() == 1) {
                ComponentUtils.offerUntilSuccess(datum, outQueues.get(0));
            } else {
                try {
                    for (Queue<StreamsDatum> queue : this.outQueues)
                        ComponentUtils.offerUntilSuccess(cloneStreamsDatum(datum), queue);
                } catch (SerializationException e) {
                    LOGGER.error("Exception while offering StreamsDatum to outgoing queue: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * In order for our data streams to ported to other data flow frame works(Storm, Hadoop, Spark, etc) we need to be able to
     * enforce the serialization required by each framework.  This needs some thought and design before a final solution is
     * made.
     * <p/>
     * The object must be either marked as serializable OR be of instance ObjectNode in order to be cloned
     *
     * @param datum The datum you wish to clone
     * @return A Streams datum
     * @throws SerializationException (runtime) if the serialization fails
     */
    private StreamsDatum cloneStreamsDatum(StreamsDatum datum) throws SerializationException {
        if (datum.document instanceof ObjectNode)
            return copyMetaData(datum, new StreamsDatum(((ObjectNode) datum.getDocument()).deepCopy(), datum.getTimestamp(), datum.getSequenceid()));
        else
            return (StreamsDatum) org.apache.commons.lang.SerializationUtils.clone(datum);
    }

    private int getNextInputQueueIndex() {
        ++this.inIndex;
        if (this.inIndex >= this.inQueues.size()) {
            this.inIndex = 0;
        }
        return this.inIndex;
    }

    /**
     * A quick rest for 1 ms that yields the execution of the processor.
     */
    protected void safeQuickRest() {
        // The queue is empty, we might as well sleep.
        Thread.yield();
        try {
            Thread.sleep(1);
        } catch(InterruptedException ie) {
            // No Operation
        }
    }

    private StreamsDatum copyMetaData(StreamsDatum copyFrom, StreamsDatum copyTo) {
        Map<String, Object> fromMeta = copyFrom.getMetadata();
        Map<String, Object> toMeta = copyTo.getMetadata();
        for (String key : fromMeta.keySet()) {
            Object value = fromMeta.get(key);
            if (value instanceof Serializable)
                toMeta.put(key, SerializationUtil.cloneBySerialization(value));
            else //hope for the best - should be serializable
                toMeta.put(key, value);
        }
        return copyTo;
    }
}
