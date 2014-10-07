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

import org.apache.streams.core.*;
import org.apache.streams.core.util.DatumUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class StreamsPersistWriterTask extends BaseStreamsTask implements DatumStatusCountable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPersistWriterTask.class);

    private StreamsPersistWriter writer;
    private long sleepTime;
    private AtomicBoolean keepRunning;
    private Map<String, Object> streamConfig;
    private BlockingQueue<StreamsDatum> inQueue;
    private AtomicBoolean isRunning;

    private DatumStatusCounter statusCounter = new DatumStatusCounter();

    @Override
    public DatumStatusCounter getDatumStatusCounter() {
        return this.statusCounter;
    }


    /**
     * Default constructor.  Uses default sleep of 500ms when inbound queue is empty.
     * @param writer writer to execute in task
     */
    public StreamsPersistWriterTask(StreamsPersistWriter writer) {
        this(writer, DEFAULT_SLEEP_TIME_MS);
    }

    /**
     *
     * @param writer writer to execute in task
     * @param sleepTime time to sleep when inbound queue is empty.
     */
    public StreamsPersistWriterTask(StreamsPersistWriter writer, long sleepTime) {
        this.writer = writer;
        this.sleepTime = sleepTime;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        this.streamConfig = config;
    }

    @Override
    public void addInputQueue(BlockingQueue<StreamsDatum> inputQueue) {
        this.inQueue = inputQueue;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }

    @Override
    public void run() {
        try {
            this.writer.prepare(this.streamConfig);
            while(this.keepRunning.get()) {
                StreamsDatum datum = null;
                try {
                    datum = this.inQueue.take();
                } catch (InterruptedException ie) {
                    LOGGER.error("Received InterruptedException. Shutting down and re-applying interrupt status.");
                    this.keepRunning.set(false);
                    Thread.currentThread().interrupt();
                }
                if(datum != null) {
                    try {
                        this.writer.write(datum);
                        statusCounter.incrementStatus(DatumStatus.SUCCESS);
                    } catch (Exception e) {
                        LOGGER.error("Error writing to persist writer {}", this.writer.getClass().getSimpleName(), e);
                        this.keepRunning.set(false); // why do we shutdown on a failed write ?
                        statusCounter.incrementStatus(DatumStatus.FAIL);
                        DatumUtils.addErrorToMetadata(datum, e, this.writer.getClass());
                    }
                } else { //datums should never be null
                    LOGGER.warn("Received null StreamsDatum @ writer : {}", this.writer.getClass().getName());
                }
            }
//            StreamsDatum datum = this.inQueue.poll();
//            while(this.keepRunning.get()) {
//                if(datum != null) {
//                    try {
//                        this.writer.write(datum);
//                        statusCounter.incrementStatus(DatumStatus.SUCCESS);
//                    } catch (Exception e) {
//                        LOGGER.error("Error writing to persist writer {}", this.writer.getClass().getSimpleName(), e);
//                        this.keepRunning.set(false);
//                        statusCounter.incrementStatus(DatumStatus.FAIL);
//                        DatumUtils.addErrorToMetadata(datum, e, this.writer.getClass());
//                    }
//                }
//                else {
//                    try {
//                        Thread.sleep(this.sleepTime);
//                    } catch (InterruptedException e) {
//                        LOGGER.warn("Thread interrupted in Writer task for {}",this.writer.getClass().getSimpleName(), e);
//                        this.keepRunning.set(false);
//                    }
//                }
//                datum = this.inQueue.poll();
//            }

        } catch(Exception e) {
            LOGGER.error("Failed to execute Persist Writer {}",this.writer.getClass().getSimpleName(), e);
        } finally {
            this.writer.cleanUp();
            this.isRunning.set(false);
        }
    }

    @Override
    public void stopTask() {
        this.keepRunning.set(false);
    }


    @Override
    public void addOutputQueue(BlockingQueue<StreamsDatum> outputQueue) {
        throw new UnsupportedOperationException(this.getClass().getName()+" does not support method - setOutputQueue()");
    }

    @Override
    public List<BlockingQueue<StreamsDatum>> getInputQueues() {
        List<BlockingQueue<StreamsDatum>> queues = new LinkedList<>();
        queues.add(this.inQueue);
        return queues;
    }

}
