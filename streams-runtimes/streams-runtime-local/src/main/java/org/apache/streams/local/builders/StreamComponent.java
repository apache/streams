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

package org.apache.streams.local.builders;

import org.apache.streams.core.*;
import org.apache.streams.local.tasks.StreamsPersistWriterTask;
import org.apache.streams.local.tasks.StreamsProcessorTask;
import org.apache.streams.local.tasks.StreamsProviderTask;
import org.apache.streams.local.tasks.StreamsTask;
import org.apache.streams.util.SerializationUtil;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.BlockingQueue;

/**
 * Stores the implementations of {@link org.apache.streams.core.StreamsOperation}, the StreamsOperations it is connected
 * to and the necessary metadata to construct a data stream.
 */
public class StreamComponent {

    private static final int START = 1;
    private static final int END = 2;

    private String id;
    private Set<StreamComponent> inBound;
    private Map<StreamComponent, BlockingQueue<StreamsDatum>> outBound;
    private BlockingQueue<StreamsDatum> inQueue;
    private StreamsProvider provider;
    private StreamsProcessor processor;
    private StreamsPersistWriter writer;
    private DateTime[] dateRange;
    private BigInteger sequence;
    private int numTasks = 1;
    private boolean perpetual;

    private Map<String, Object> streamConfig;

    /**
     *
     * @param id
     * @param provider
     */
    public StreamComponent(String id, StreamsProvider provider, boolean perpetual, Map<String, Object> streamConfig) {
        this.id = id;
        this.provider = provider;
        this.perpetual = perpetual;
        this.streamConfig = streamConfig;
        initializePrivateVariables();
    }

    /**
     *
     * @param id
     * @param provider
     * @param start
     * @param end
     */
    public StreamComponent(String id, StreamsProvider provider, DateTime start, DateTime end, Map<String, Object> streamConfig) {
        this.id = id;
        this.provider = provider;
        this.dateRange = new DateTime[2];
        this.dateRange[START] = start;
        this.dateRange[END] = end;
        this.streamConfig = streamConfig;
        initializePrivateVariables();
    }


    /**
     *
     * @param id
     * @param provider
     * @param sequence
     */
    public StreamComponent(String id, StreamsProvider provider, BigInteger sequence, Map<String, Object> streamConfig) {
        this.id = id;
        this.provider = provider;
        this.sequence = sequence;
        this.streamConfig = streamConfig;
    }

    /**
     *
     * @param id
     * @param processor
     * @param inQueue
     * @param numTasks
     */
    public StreamComponent(String id, StreamsProcessor processor, BlockingQueue<StreamsDatum> inQueue, int numTasks, Map<String, Object> streamConfig) {
        this.id = id;
        this.processor = processor;
        this.inQueue = inQueue;
        this.numTasks = numTasks;
        this.streamConfig = streamConfig;
        initializePrivateVariables();
    }

    /**
     *
     * @param id
     * @param writer
     * @param inQueue
     * @param numTasks
     */
    public StreamComponent(String id, StreamsPersistWriter writer, BlockingQueue<StreamsDatum> inQueue, int numTasks, Map<String, Object> streamConfig) {
        this.id = id;
        this.writer = writer;
        this.inQueue = inQueue;
        this.numTasks = numTasks;
        this.streamConfig = streamConfig;
        initializePrivateVariables();
    }

    private void initializePrivateVariables() {
        this.inBound = new HashSet<StreamComponent>();
        this.outBound = new HashMap<StreamComponent, BlockingQueue<StreamsDatum>>();
    }

    /**
     * Add an outbound queue for this component. The queue should be an inbound queue of a downstream component.
     * @param component the component that this supplying their inbound queue
     * @param queue the queue to to put post processed/provided datums on
     */
    public void addOutBoundQueue(StreamComponent component, BlockingQueue<StreamsDatum> queue) {
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
    public BlockingQueue<StreamsDatum> getInBoundQueue() {
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
     * Creates a {@link org.apache.streams.local.tasks.StreamsTask} that is running a clone of this component whose
     * inbound and outbound queues are appropriately connected to the parent and child nodes.
     *
     * @return StreamsTask for this component
     * @param timeout The timeout to use in milliseconds for any tasks that support configurable timeout
     */
    public StreamsTask createConnectedTask(int timeout) {
        StreamsTask task;
        if(this.processor != null) {
            if(this.numTasks > 1) {
                task =  new StreamsProcessorTask((StreamsProcessor)SerializationUtil.cloneBySerialization(this.processor), streamConfig);
                task.addInputQueue(this.inQueue);
                for(BlockingQueue<StreamsDatum> q : this.outBound.values()) {
                    task.addOutputQueue(q);
                }
            } else {
                task = new StreamsProcessorTask(this.processor, streamConfig);
                task.addInputQueue(this.inQueue);
                for(BlockingQueue<StreamsDatum> q : this.outBound.values()) {
                    task.addOutputQueue(q);
                }
            }
        }
        else if(this.writer != null) {
            if(this.numTasks > 1) {
                task = new StreamsPersistWriterTask((StreamsPersistWriter) SerializationUtil.cloneBySerialization(this.writer), streamConfig);
                task.addInputQueue(this.inQueue);
            } else {
                task = new StreamsPersistWriterTask(this.writer, streamConfig);
                task.addInputQueue(this.inQueue);
            }
        }
        else if(this.provider != null) {
            StreamsProvider prov;
            if(this.numTasks > 1) {
                prov = (StreamsProvider)SerializationUtil.cloneBySerialization(this.provider);
            } else {
                prov = this.provider;
            }
            if(this.dateRange == null && this.sequence == null)
                task = new StreamsProviderTask(prov, this.perpetual, streamConfig);
            else if(this.sequence != null)
                task = new StreamsProviderTask(prov, this.sequence, streamConfig);
            else
                task = new StreamsProviderTask(prov, this.dateRange[0], this.dateRange[1], streamConfig);
            //Adjust the timeout if necessary
            if(timeout != 0) {
                ((StreamsProviderTask)task).setTimeout(timeout);
            }
            for(BlockingQueue<StreamsDatum> q : this.outBound.values()) {
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

    protected StreamsOperation getOperation() {
        if(this.processor != null) {
            return (StreamsOperation) this.processor;
        }
        else if(this.writer != null) {
            return (StreamsOperation) this.writer;
        }
        else if(this.provider != null) {
            return (StreamsOperation) this.provider;
        }
        else {
            throw new InvalidStreamException("Underlying StreamComponoent was NULL.");
        }
    }

    @Deprecated
    protected boolean isOperationCountable() {
        return getOperation() instanceof DatumStatusCountable;
    }
}
