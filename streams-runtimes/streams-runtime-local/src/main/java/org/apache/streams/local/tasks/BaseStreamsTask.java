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

package org.apache.streams.local.tasks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
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
    private ObjectMapper mapper;

    public BaseStreamsTask() {
        this.mapper = new StreamsJacksonMapper();
        this.mapper.registerSubtypes(Activity.class);
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
            ComponentUtils.offerUntilSuccess(datum, outQueues.get(0));
        }
        else {
            StreamsDatum newDatum = null;
            for(Queue<StreamsDatum> queue : this.outQueues) {
                try {
                    newDatum = cloneStreamsDatum(datum);
                    if(newDatum != null) {
                        ComponentUtils.offerUntilSuccess(newDatum, queue);
                    }
                } catch (RuntimeException e) {
                    LOGGER.debug("Failed to add StreamsDatum to outgoing queue : {}", datum);
                    LOGGER.error("Exception while offering StreamsDatum to outgoing queue: {}", e);
                }
            }
        }
    }

    /**
     * //TODO LOCAL MODE HACK. Need to fix
     * In order for our data streams to ported to other data flow frame works(Storm, Hadoop, Spark, etc) we need to be able to
     * enforce the serialization required by each framework.  This needs some thought and design before a final solution is
     * made.
     *
     * In order to be able to copy/clone StreamDatums the orginal idea was to force all StreamsDatums to be java serializable.
     * This was seen as unacceptable for local mode.  So until we come up with a solution to enforce serialization and be
     * compatiable across multiple frame works, this hack is in place.
     *
     * If datum.document is Serializable, we use serialization to clone a new copy.  If it is not Serializable we attempt
     * different methods using an com.fasterxml.jackson.databind.ObjectMapper to copy/clone the StreamsDatum. If the object
     * is not clonable by these methods, an error is reported to the logging and a NULL object is returned.
     *
     * @param datum
     * @return
     */
    protected StreamsDatum cloneStreamsDatum(StreamsDatum datum) {
        try {

            if(datum.document instanceof ObjectNode) {
                return copyMetaData(datum, new StreamsDatum(((ObjectNode) datum.document).deepCopy(), datum.timestamp, datum.sequenceid));
            }
            else if(datum.document instanceof Activity) {

                return copyMetaData(datum, new StreamsDatum(this.mapper.readValue(this.mapper.writeValueAsString(datum.document), Activity.class),
                                        datum.timestamp,
                                        datum.sequenceid));
            }
//            else if(this.mapper.canSerialize(datum.document.getClass())){
//                return new StreamsDatum(this.mapper.readValue(this.mapper.writeValueAsString(datum.document), datum.document.getClass()),
//                                        datum.timestamp,
//                                        datum.sequenceid);
//            }

            else if(datum.document instanceof Serializable) {
                return (StreamsDatum) SerializationUtil.cloneBySerialization(datum);
            }
        } catch (Exception e) {
            LOGGER.error("Exception while trying to clone/copy StreamsDatum : {}", e);
        }
        LOGGER.error("Failed to clone/copy StreamsDatum with document of class : {}", datum.document.getClass().getName());
        return null;
    }

    private int getNextInputQueueIndex() {
        ++this.inIndex;
        if(this.inIndex >= this.inQueues.size()) {
            this.inIndex = 0;
        }
        return this.inIndex;
    }

    private StreamsDatum copyMetaData(StreamsDatum copyFrom, StreamsDatum copyTo) {
        Map<String, Object> fromMeta = copyFrom.getMetadata();
        Map<String, Object> toMeta = copyTo.getMetadata();
        for(String key : fromMeta.keySet()) {
            Object value = fromMeta.get(key);
            if(value instanceof Serializable)
                toMeta.put(key, SerializationUtil.cloneBySerialization(value));
            else //hope for the best - should be serializable
                toMeta.put(key, value);
        }
        return copyTo;
    }
}
