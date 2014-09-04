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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.SerializationException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;

/**
 *
 */
public abstract class BaseStreamsTask implements StreamsTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseStreamsTask.class);

    private final Condition conditionIncoming = new SimpleCondition();
    private final String id;
    private final Map<String, BaseStreamsTask> ctx;
    private final AtomicBoolean keepRunning = new AtomicBoolean(true);
    private final ArrayBlockingQueue<StreamsDatum> inQueue = new ArrayBlockingQueue<StreamsDatum>(50);
    private final Set<String> downStreamIds = new HashSet<String>();
    private final AtomicInteger workingCounter = new AtomicInteger(0);

    protected final Set<StreamsTask> downStreamTasks = new HashSet<StreamsTask>();
    protected final ThreadingController threadingController;

    public abstract StatusCounts getCurrentStatus();

    BaseStreamsTask(String id, Map<String, BaseStreamsTask> ctx, ThreadingController threadingController) {
        this.id = id;
        this.ctx = ctx;
        this.threadingController = threadingController;
    }

    public void initialize() {
        for(String id : this.downStreamIds) {
            if(!this.ctx.containsKey(id)) {
                LOGGER.warn("Cannot find connected iD: {}", id);
            }
            else {
                this.downStreamTasks.add(this.ctx.get(id));
            }
        }
    }

    public boolean shouldKeepRunning() {
        return this.keepRunning.get();
    }

    public String getId() {
        return this.id;
    }

    public void knock() {
        this.conditionIncoming.signal();
    }

    public int getWorkingCount() {
        return this.workingCounter.get();
    }

    @Override
    public final void stopTask() {
        this.keepRunning.set(false);
        this.conditionIncoming.signal();
    }

    @Override
    public void addInputQueue(String id) {

    }

    @Override
    public void addOutputQueue(String id) {
        this.downStreamIds.add(id);
    }

    @Override
    public BlockingQueue<StreamsDatum> getInQueue() {
        return this.inQueue;
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
            // Randomize the processing so it evenly distributes and to not prefer one queue over another
            datum = this.inQueue.poll();
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

    /**
     * The total number of items that are in the queue right now.
     * @return
     * The total number of items that are in the queue waiting to be worked right now.
     */
    public synchronized int getTotalInQueue() {
        return this.inQueue.size();
    }

    protected void waitForOutBoundQueueToBeFree() {
        for (StreamsTask t : this.downStreamTasks) {
            while(t.getInQueue().remainingCapacity() == 0) {
                Thread.yield();
            }
        }
    }

    /**
     * Adds a StreamDatum to the outgoing queues.  If there are multiple queues, it uses serialization to create
     * clones of the datum and adds a new clone to each queue.
     *
     * @param datum The datum you wish to add to an outgoing queue
     */
    protected void addToOutgoingQueue(final StreamsDatum datum) {
        if (datum != null) {
            for (StreamsTask t : this.downStreamTasks) {
                ComponentUtils.offerUntilSuccess(cloneStreamsDatum(datum), t.getInQueue());
                t.knock();
            }
        }
    }

    public String toString() {
        return this.getClass().getName() + "[" + this.getId() + "]: " + this.getCurrentStatus().toString();
    }


    protected void reportWorking() {
        this.threadingController.flagWorking(this);
        workingCounter.incrementAndGet();
    }

    protected void reportCompleted() {
        synchronized (this) {
            this.workingCounter.decrementAndGet();
            if (this.workingCounter.get() == 0)
                this.threadingController.flagNotWorking(this);
            this.threadingController.getItemPoppedCondition().signal();
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
                    String value = StreamsJacksonMapper.getInstance().writeValueAsString(datum.document);
                    Object object = StreamsJacksonMapper.getInstance().readValue(value, datum.getDocument().getClass());
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

    protected void notifyAllDownStreamMembers() {
        for(StreamsTask t : downStreamTasks)
            t.knock();
    }

    protected void waitForIncoming() {
        // we don't have anything to do, let's yield
        // and take a quick rest and wait for people to
        // catch up
        synchronized (this.conditionIncoming) {
            if(shouldKeepRunning()) {
                if (!isDatumAvailable()) {
                    try {
                        this.conditionIncoming.await();
                    } catch (InterruptedException ioe) {
                        /* no op */
                    }
                }
            }
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
