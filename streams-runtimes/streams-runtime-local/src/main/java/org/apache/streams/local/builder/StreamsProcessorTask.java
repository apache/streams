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
package org.apache.streams.local.builder;

import org.apache.streams.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


/**
 * Processor task that is multi-threaded
 */
public class StreamsProcessorTask extends BaseStreamsTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProcessorTask.class);

    protected final StreamsProcessor processor;
    protected Map<String, Object> streamConfig;
    protected final DatumStatusCounter statusCounter = new DatumStatusCounter();
    private final ThreadPoolExecutor executorService;

    /**
     * Default constructor, uses default sleep time of 500ms when inbound queue is empty
     *
     * @param processor process to run in task
     */
    public StreamsProcessorTask(StreamsProcessor processor, int numThreads) {
        this.processor = processor;
        this.executorService = new ThreadPoolExecutor(numThreads,
                numThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(numThreads, false),
                new WaitUntilAvailableExecutionHandler());
    }

    /**
     * Default constructor, uses default sleep time of 500ms when inbound queue is empty
     *
     * @param processor process to run in task
     */
    public StreamsProcessorTask(StreamsProcessor processor) {
        this(processor, 1);
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        if (this.streamConfig != null)
            throw new RuntimeException("This variable has already been set, you cannot set it.");
        this.streamConfig = config;
    }

    @Override
    public boolean isRunning() {
        return  executorService.getActiveCount() > 0 ||
                this.isDatumAvailable();
    }

    @Override
    public void run() {

        try {
            this.processor.prepare(this.streamConfig);

            while (this.keepRunning.get() || super.isDatumAvailable()) {

                StreamsDatum datum;
                while ((datum = super.pollNextDatum()) != null) {
                    final StreamsDatum workingDatum = datum;
                    executorService.execute(new Runnable() {
                        public void run() {
                            processThisDatum(workingDatum);
                        }
                    });
                }

                // we don't have anything to do, let's yield
                // and take a quick rest and wait for people to
                // catch up
                if (!isDatumAvailable())
                    safeQuickRest();
            }

            LOGGER.info("Shutting down threaded processor {}", this.getInputQueues());
            executorService.shutdown();
            try {
                executorService.awaitTermination(5, TimeUnit.MINUTES);
                // after 5 minutes, these are the poor souls we left in the executor pool.
                statusCounter.incrementStatus(DatumStatus.FAIL, executorService.getPoolSize());
            } catch (InterruptedException ie) {
                LOGGER.warn("There was an issue waiting for the termination of the threaded processor");
            }

        } finally {
            // clean everything up
            this.processor.cleanUp();
        }
    }

    protected final void processThisDatum(StreamsDatum datum) {
        try {
            // get the outputs from the queue and pass them down the row
            List<StreamsDatum> output = this.processor.process(datum);
            if (output != null)
                for (StreamsDatum outDatum : output)
                    super.addToOutgoingQueue(outDatum);

            this.statusCounter.incrementStatus(DatumStatus.SUCCESS);
        } catch (Throwable e) {
            LOGGER.warn("There was an error processing datum: {}", datum);
            this.statusCounter.incrementStatus(DatumStatus.FAIL);
        }
    }


    public StatusCounts getCurrentStatus() {
        return new StatusCounts(getTotalInQueue(),
                this.executorService.getActiveCount(),
                this.statusCounter.getSuccess(),
                this.statusCounter.getFail());
    }
}