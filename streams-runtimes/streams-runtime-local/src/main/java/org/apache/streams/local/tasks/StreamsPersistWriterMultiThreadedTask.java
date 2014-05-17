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

import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Mult-threaded version of Streamsbuilder
 */
public class StreamsPersistWriterMultiThreadedTask extends StreamsPersistWriterSingleThreadedTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPersistWriterMultiThreadedTask.class);
    private final int numThreads;

    public StreamsPersistWriterMultiThreadedTask(StreamsPersistWriter writer, int numThreads) {
        super(writer);
        this.numThreads = numThreads;
    }

    @Override
    public void run() {

        ThreadPoolExecutor executorService = new ThreadPoolExecutor(this.numThreads,
                this.numThreads,
                0L,
                TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(this.numThreads, false),
                new WaitUntilAvailableExecutionHandler());

        try {

            this.writer.prepare(this.streamConfig);

            while(this.keepRunning.get() || super.isDatumAvailable()) {

                // we don't have anything to do, let's yield
                // and take a quick rest and wait for people to
                // catch up
                if (!isDatumAvailable())
                    safeQuickRest();

                StreamsDatum datum;
                while((datum = pollNextDatum()) != null) {
                    final StreamsDatum workingDatum = datum;
                    executorService.execute(new Runnable() {
                        public void run() {
                            processThisDatum(workingDatum);
                        }
                    });
                }
            }

            LOGGER.info("Shutting down threaded processor");
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
            this.isRunning.set(false);
        }
    }

}
