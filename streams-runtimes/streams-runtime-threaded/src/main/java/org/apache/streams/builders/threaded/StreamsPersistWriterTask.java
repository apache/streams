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

import org.apache.streams.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Streams persist writer
 */
public class StreamsPersistWriterTask extends BaseStreamsTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPersistWriterTask.class);
    private final ThreadPoolExecutor executorService;

    protected final StreamsPersistWriter writer;
    protected Map<String, Object> streamConfig;
    protected final DatumStatusCounter statusCounter = new DatumStatusCounter();

    public StreamsPersistWriterTask(String id, StreamsPersistWriter writer, int numThreads) {
        super(id);
        this.writer = writer;
        this.executorService = new ThreadPoolExecutor(numThreads,
                numThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(numThreads, false),
                new WaitUntilAvailableExecutionHandler());
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        this.streamConfig = config;
    }

    @Override
    public boolean isRunning() {
        return  executorService.getActiveCount() > 0 ||
                this.isDatumAvailable();
    }

    public StatusCounts getCurrentStatus() {
        return new StatusCounts(getTotalInQueue(),
                this.executorService.getActiveCount(),
                this.statusCounter.getSuccess(),
                this.statusCounter.getFail());
    }

    @Override
    public void run() {

        try {

            this.writer.prepare(this.streamConfig);

            while (this.keepRunning.get() || super.isDatumAvailable()) {

                // we don't have anything to do, let's yield
                // and take a quick rest and wait for people to
                // catch up
                if (!isDatumAvailable())
                    safeQuickRest();

                StreamsDatum datum;
                while ((datum = pollNextDatum()) != null) {
                    final StreamsDatum workingDatum = datum;
                    executorService.execute(new Runnable() {
                        public void run() {
                            processThisDatum(workingDatum);
                        }
                    });
                }
            }

            LOGGER.debug("Shutting down threaded writer");
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
            this.writer.cleanUp();
        }
    }

    protected final void processThisDatum(StreamsDatum datum) {
        try {
            this.writer.write(datum);
            statusCounter.incrementStatus(DatumStatus.SUCCESS);
        } catch (Throwable e) {
            LOGGER.error("{} - Error[{}] writing to persist writer {}", this.getId(), e.getMessage(), this.writer.toString());
            statusCounter.incrementStatus(DatumStatus.FAIL);
        }
    }

    @Override
    public void addOutputQueue(Queue<StreamsDatum> outputQueue) {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support method - setOutputQueue()");
    }


}
