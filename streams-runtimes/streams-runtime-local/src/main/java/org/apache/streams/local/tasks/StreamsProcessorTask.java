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

import com.google.common.collect.Maps;
import org.apache.streams.core.*;
import org.apache.streams.core.util.DatumUtils;
import org.apache.streams.local.counters.StreamsTaskCounter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class StreamsProcessorTask extends BaseStreamsTask implements DatumStatusCountable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProcessorTask.class);


    private StreamsProcessor processor;
    private long sleepTime;
    private AtomicBoolean keepRunning;
    private Map<String, Object> streamConfig;
    private BlockingQueue<StreamsDatum> inQueue;
    private AtomicBoolean isRunning;
    private AtomicBoolean blocked;
    private StreamsTaskCounter counter;

    private DatumStatusCounter statusCounter = new DatumStatusCounter();

    @Override
    public DatumStatusCounter getDatumStatusCounter() {
        return this.statusCounter;
    }

    /**
     * Default constructor, uses default sleep time of 500ms when inbound queue is empty
     * @param processor process to run in task
     */
    public StreamsProcessorTask(StreamsProcessor processor) {
        this(processor, DEFAULT_SLEEP_TIME_MS);
    }

    /**
     *
     * @param processor processor to run in task
     * @param sleepTime time to sleep when incoming queue is empty
     */
    public StreamsProcessorTask(StreamsProcessor processor, long sleepTime) {
        this.processor = processor;
        this.sleepTime = sleepTime;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
        this.blocked = new AtomicBoolean(true);
    }

    @Override
    public boolean isWaiting() {
        return this.inQueue.isEmpty() && this.blocked.get();
    }

    @Override
    public void stopTask() {
        this.keepRunning.set(false);
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
            this.processor.prepare(this.streamConfig);
            if(this.counter == null) {
                this.counter = new StreamsTaskCounter(this.processor.getClass().getName()+ UUID.randomUUID().toString());
            }
            while(this.keepRunning.get()) {
                StreamsDatum datum = null;
                try {
                    this.blocked.set(true);
                    datum = this.inQueue.poll(5, TimeUnit.SECONDS);
                    this.blocked.set(false);
                } catch (InterruptedException ie) {
                    LOGGER.warn("Received InteruptedException, shutting down and re-applying interrupt status.");
                    this.keepRunning.set(false);
                    Thread.currentThread().interrupt();
                }
                if(datum != null) {
                    this.counter.incrementReceivedCount();
                    try {
                        long startTime = System.currentTimeMillis();
                        List<StreamsDatum> output = this.processor.process(datum);
                        this.counter.addTime(System.currentTimeMillis() - startTime);
                        if(output != null) {
                            for(StreamsDatum outDatum : output) {
                                super.addToOutgoingQueue(outDatum);
                                this.counter.incrementEmittedCount();
                                statusCounter.incrementStatus(DatumStatus.SUCCESS);
                            }
                        }
                    } catch (InterruptedException ie) {
                        LOGGER.warn("Received InteruptedException, shutting down and re-applying interrupt status.");
                        this.keepRunning.set(false);
                        Thread.currentThread().interrupt();
                    } catch (Throwable t) {
                        this.counter.incrementErrorCount();
                        LOGGER.warn("Caught Throwable in processor, {} : {}", this.processor.getClass().getName(), t.getMessage());
                        statusCounter.incrementStatus(DatumStatus.FAIL);
                        //Add the error to the metadata, but keep processing
                        DatumUtils.addErrorToMetadata(datum, t, this.processor.getClass());
                    }
                } else {
                    LOGGER.debug("Removed NULL datum from queue at processor : {}", this.processor.getClass().getName());
                }
            }
        } finally {
            this.isRunning.set(false);
            this.processor.cleanUp();
        }
    }

    @Override
    public List<BlockingQueue<StreamsDatum>> getInputQueues() {
        List<BlockingQueue<StreamsDatum>> queues = new LinkedList<BlockingQueue<StreamsDatum>>();
        queues.add(this.inQueue);
        return queues;
    }

    @Override
    public void setStreamsTaskCounter(StreamsTaskCounter counter) {
        this.counter = counter;
    }
}
