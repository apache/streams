/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.streams.builders.threaded;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.SerializationException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.jackson.StreamsJacksonModule;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 */
public abstract class BaseStreamsTask implements StreamsTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseStreamsTask.class);

    private final String id;
    protected final AtomicBoolean keepRunning = new AtomicBoolean(true);
    private final List<Queue<StreamsDatum>> inQueues = new ArrayList<Queue<StreamsDatum>>();
    private final List<Queue<StreamsDatum>> outQueues = new ArrayList<Queue<StreamsDatum>>();
    private final AtomicInteger queueCycleCounter = new AtomicInteger(0);

    public abstract StatusCounts getCurrentStatus();

    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    BaseStreamsTask(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public final void stopTask() {
        this.keepRunning.set(false);
    }

    @Override
    public void addInputQueue(Queue<StreamsDatum> inputQueue) {
        this.inQueues.add(inputQueue);
    }

    @Override
    public void addOutputQueue(Queue<StreamsDatum> outputQueue) {
        this.outQueues.add(outputQueue);
    }

    @Override
    public final List<Queue<StreamsDatum>> getInputQueues() {
        return this.inQueues;
    }

    @Override
    public final List<Queue<StreamsDatum>> getOutputQueues() {
        return this.outQueues;
    }

    /**
     * Get the next datum to be processed, if a null datum is returned,
     * then there are no more datums to be processed.
     *
     * @return the next StreamsDatum or null if all input queues are empty.
     */
    protected StreamsDatum pollNextDatum() {
        StreamsDatum datum = null;
        do {
            synchronized (queueCycleCounter) {

                if(!this.inQueues.get(queueCycleCounter.get()).isEmpty())
                    datum = this.inQueues.get(queueCycleCounter.get()).poll();

                // increment our queue counter
                if (queueCycleCounter.incrementAndGet() >= this.inQueues.size())
                    queueCycleCounter.set(0);

            }
        } while (datum == null && isDatumAvailable());

        return datum;
    }

    /**
     * Check all the inbound queues and see if there is a datum that is available
     * to be processed.
     *
     * @return whether or not there is another datum available
     */
    public boolean isDatumAvailable() {
        return getTotalInQueue() > 0;
    }

    public boolean isOutBoundQueueBackedUp() {
        for(Queue q : this.outQueues) {
            if(q.isEmpty())
                return false;
            else {
                if(q instanceof BlockingQueue) {
                    if(((BlockingQueue)q).remainingCapacity() == 0)
                        return true;
                }
            }
        }
        return false;
    }


    /**
     * The total number of items that are in the queue right now.
     * @return
     * The total number of items that are in the queue waiting to be worked right now.
     */
    public int getTotalInQueue() {
        int total = 0;
        for (Queue q : this.inQueues)
            if (q != null)
                total += q.size();

        return total;
    }

    /**
     * Adds a StreamDatum to the outgoing queues.  If there are multiple queues, it uses serialization to create
     * clones of the datum and adds a new clone to each queue.
     *
     * @param datum The datum you wish to add to an outgoing queue
     */
    protected void addToOutgoingQueue(StreamsDatum datum) {
        if (datum != null) {
            for (Queue<StreamsDatum> queue : this.outQueues)
                ComponentUtils.offerUntilSuccess(cloneStreamsDatum(datum), queue);
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
        // this is difficult to clone due to it's nature. To clone it we will use the "deepCopy" function available.
        if (datum.document instanceof ObjectNode)
            return copyMetaData(datum, new StreamsDatum(((ObjectNode) datum.getDocument()).deepCopy(), datum.getTimestamp(), datum.getSequenceid()));
        else {
            try {
                // Try to serialize the document using standard serialization methods
                return (StreamsDatum) org.apache.commons.lang.SerializationUtils.clone(datum);
            }
            catch(SerializationException ser) {
                try {
                    // Use the bruce force method for serialization.
                    Object object = MAPPER.readValue(MAPPER.writeValueAsString(datum.document), datum.getDocument().getClass());
                    return copyMetaData(datum, new StreamsDatum(object, datum.getId(), datum.timestamp, datum.sequenceid));
                } catch (JsonMappingException e) {
                    LOGGER.warn("Unable to clone datum Mapper Error: {} - {}", e.getMessage(), datum);
                } catch (JsonParseException e) {
                    LOGGER.warn("Unable to clone datum Parser Error: {} - {}", e.getMessage(), datum);
                } catch (JsonProcessingException e) {
                    LOGGER.warn("Unable to clone datum Processing Error: {} - {}", e.getMessage(), datum);
                } catch (IOException e) {
                    LOGGER.warn("Unable to clone datum IOException Error: {} - {}", e.getMessage(), datum);
                }
                throw new SerializationException("Unable to clone datum");
            }
        }
    }


    protected void safeQuickRest(int waitTime) {
        // The queue is empty, we might as well sleep.
        Thread.yield();
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ie) {
            // No Operation
        }
    }

    /**
     * A quick rest for 1 ms that yields the execution of the processor.
     */
    protected void safeQuickRest() {
        safeQuickRest(2);
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
