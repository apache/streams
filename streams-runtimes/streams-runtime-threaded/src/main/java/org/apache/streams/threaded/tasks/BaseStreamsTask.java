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
package org.apache.streams.threaded.tasks;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.SerializationException;
import org.apache.streams.core.*;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.threaded.controller.ThreadingController;
import org.apache.streams.util.SerializationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public abstract class BaseStreamsTask implements StreamsTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseStreamsTask.class);

    private final ThreadingController threadingController;
    private final StreamsOperation streamsOperation;
    private final String id;
    private String type;
    protected final Map<String, Object> config;
    private final Set<String> downStreamIds = new HashSet<String>();
    protected final Set<StreamsTask> downStreamTasks = new HashSet<StreamsTask>();
    protected final DatumStatusCounter statusCounter = new DatumStatusCounter();
    private final AtomicLong workingCounter = new AtomicLong(0);
    private final AtomicLong timeSpentSuccess = new AtomicLong(0);
    private final AtomicLong timeSpentFailure = new AtomicLong(0);

    private boolean isPrepared = false;
    private boolean isCleanedUp = false;

    BaseStreamsTask(ThreadingController threadingController, String id, Map<String, Object> config, StreamsOperation streamsOperation) {
        this.threadingController = threadingController;
        this.id = id;
        this.config = config;
        this.streamsOperation = streamsOperation;

        if(this.getClass().equals(StreamsProviderTask.class)) {
            this.type = "provider";
        } else if(this.getClass().equals(StreamsProcessorTask.class)) {
            this.type = "processor";
        } else if(this.getClass().equals(StreamsPersistWriterTask.class)) {
            this.type = "writer";
        } else {
            this.type = "unknown";
        }

    }

    public void initialize(final Map<String, StreamsTask> ctx) {
        for(String id : this.downStreamIds) {
            this.downStreamTasks.add(ctx.get(id));
        }
    }

    public Collection<StreamsTask> getChildren() {
        return this.downStreamTasks;
    }

    @Override
    public StatusCounts getCurrentStatus() {
        if(this.streamsOperation instanceof DatumStatusCountable) {
            DatumStatusCounter datumStatusCounter = ((DatumStatusCountable)this.streamsOperation).getDatumStatusCounter();
            return new StatusCounts(this.id, this.type, this.workingCounter.get(), datumStatusCounter.getSuccess(), datumStatusCounter.getFail(), this.timeSpentSuccess.get(), this.timeSpentFailure.get());
        }
        else {
            return new StatusCounts(this.id, this.type, this.workingCounter.get(), this.statusCounter.getSuccess(), this.statusCounter.getFail(), this.timeSpentSuccess.get(), this.timeSpentFailure.get());
        }
    }

    @Override
    public final String getId() {
        return this.id;
    }

    protected ThreadingController getThreadingController() {
        return this.threadingController;
    }

    public String getType() {
        return this.type;
    }

    @Override
    public void addOutputQueue(String id) {
        this.downStreamIds.add(id);
    }

    public String toString() {
        return this.getClass().getName() + "[" + this.getId() + "]: " + this.getCurrentStatus().toString();
    }

    @Override
    public final void prepare(Object configuration) {
        try {
            if(!this.isPrepared)
                this.streamsOperation.prepare(configuration);
        }
        catch(Throwable e) {
            LOGGER.warn("Problem preparing the component[{}]: {}", this.getId(), e.getMessage());
        }
        this.isPrepared = true;
    }

    @Override
    public final void cleanup() {
        try {
            if(!isCleanedUp)
                this.streamsOperation.cleanUp();
        }
        catch(Throwable e) {
            LOGGER.warn("Problem Cleaning Up Component[{}]: {}", this.getId(), e.getMessage());
        }
        this.isCleanedUp = true;
    }


    @Override
    public final void process(StreamsDatum datum) {
        Collection<StreamsDatum> myDatums = this.fetch(datum);
        if(myDatums != null) {
            for(StreamsDatum d : myDatums)
                sendToChildren(d);
        }
    }

    protected final void sendToChildren(final StreamsDatum datum) {
        for (StreamsTask t : this.downStreamTasks) {
            t.process(datum);
        }
    }

    private Collection<StreamsDatum> fetch(StreamsDatum datum) {
        // start a timer to find out how long this process takes.
        long startTime = new Date().getTime();
        Collection<StreamsDatum> toReturn = null;
        this.workingCounter.incrementAndGet();

        try {
            toReturn = this.processInternal(cloneStreamsDatum(datum));
            this.statusCounter.incrementStatus(DatumStatus.SUCCESS);
            this.timeSpentSuccess.addAndGet(new Date().getTime() - startTime);
        } catch(Throwable e) {
            this.statusCounter.incrementStatus(DatumStatus.FAIL);
            this.timeSpentFailure.addAndGet(new Date().getTime() - startTime);
        } finally  {
            this.workingCounter.decrementAndGet();
        }

        return toReturn;
    }

    protected abstract Collection<StreamsDatum> processInternal(StreamsDatum datum);

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
        if (datum.document instanceof ObjectNode) {
            return copyMetaData(datum, new StreamsDatum(((ObjectNode) datum.getDocument()).deepCopy(), datum.getTimestamp(), datum.getSequenceid()));
        } else {
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

    private StreamsDatum copyMetaData(StreamsDatum copyFrom, StreamsDatum copyTo) {
        Map<String, Object> fromMeta = copyFrom.getMetadata();
        Map<String, Object> toMeta = copyTo.getMetadata();
        for (String key : fromMeta.keySet()) {
            Object value = fromMeta.get(key);
            if (value instanceof Serializable) {
                toMeta.put(key, SerializationUtil.cloneBySerialization(value));
            } else {//hope for the best - should be serializable
                toMeta.put(key, value);
            }
        }
        return copyTo;
    }
}