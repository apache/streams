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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
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
    private Queue<StreamsDatum> inQueue;
    private AtomicBoolean isRunning;

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
    public void addInputQueue(Queue<StreamsDatum> inputQueue) {
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
            StreamsDatum datum = this.inQueue.poll();
            while(this.keepRunning.get()) {
                if(datum != null) {
                    try {
                        List<StreamsDatum> output = this.processor.process(datum);

                        if(output != null) {
                            for(StreamsDatum outDatum : output) {
                                super.addToOutgoingQueue(outDatum);
                                statusCounter.incrementStatus(DatumStatus.SUCCESS);
                            }
                        }
                    } catch (Throwable e) {
                        LOGGER.error("Throwable Streams Processor {}", e);
                        statusCounter.incrementStatus(DatumStatus.FAIL);
                        //Add the error to the metadata, but keep processing
                        DatumUtils.addErrorToMetadata(datum, e, this.processor.getClass());
                    }
                }
                else {
                    try {
                        Thread.sleep(this.sleepTime);
                    } catch (InterruptedException e) {
                        this.keepRunning.set(false);
                    }
                }
                datum = this.inQueue.poll();
            }

        } finally {
            this.isRunning.set(false);
            this.processor.cleanUp();
        }
    }

    @Override
    public List<Queue<StreamsDatum>> getInputQueues() {
        List<Queue<StreamsDatum>> queues = new LinkedList<Queue<StreamsDatum>>();
        queues.add(this.inQueue);
        return queues;
    }
}
