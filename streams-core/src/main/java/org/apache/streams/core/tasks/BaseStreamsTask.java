package org.apache.streams.core.tasks;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

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
     * NOTE NECCESSARY AT THE MOMENT.  MAY BECOME NECESSARY AS WE LOOK AT MAKING JOIN TASKS. CURRENTLY ALL TASK HAVE MAX
     * OF 1 INPUT QUEUE.
     * Round Robins through input queues to get the next StreamsDatum. If all input queues are empty, it will return null.
     * @return the next StreamsDatum or null if all input queues are empty.
     */
    protected StreamsDatum getNextDatum() {
        int startIndex = this.inIndex;
        int index = startIndex;
        StreamsDatum datum = null;
        do {
            datum = this.inQueues.get(index).poll();
            index = getNextInputQueueIndex();
        } while( datum == null && startIndex != index);
        return datum;
    }

    /**
     * Adds a StreamDatum to the outgoing queues.  If there are multiple queues, it uses serialization to create
     * clones of the datum and adds a new clone to each queue.
     * @param datum
     */
    protected void addToOutgoingQueue(StreamsDatum datum) {
        if(this.outQueues.size() == 1) {
            this.outQueues.get(0).offer(datum);
        }
        else {
            for(Queue<StreamsDatum> queue : this.outQueues) {
                try {
                    queue.offer((StreamsDatum) SerializationUtil.deserialize(SerializationUtil.serialize(datum)));
                } catch (RuntimeException e) {
                    LOGGER.debug("Failed to add StreamsDatum to outgoing queue : {}", datum);
                    LOGGER.error("Exception while offering StreamsDatum to outgoing queue: {}", e);
                }
            }
        }
    }

    private int getNextInputQueueIndex() {
        ++this.inIndex;
        if(this.inIndex >= this.inQueues.size()) {
            this.inIndex = 0;
        }
        return this.inIndex;
    }
}
