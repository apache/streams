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

import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Streams persist writer
 */
public class StreamsPersistWriterTask extends BaseStreamsTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPersistWriterTask.class);

    protected final StreamsPersistWriter writer;
    protected Map<String, Object> streamConfig;
    protected final DatumStatusCounter statusCounter = new DatumStatusCounter();
    private final ThreadingController threadingController;
    private final AtomicInteger workingCounter = new AtomicInteger(0);


    public StreamsPersistWriterTask(String id, Map<String, BaseStreamsTask> ctx, StreamsPersistWriter writer, ThreadingController threadingController) {
        super(id, ctx);
        this.writer = writer;
        this.threadingController = threadingController;
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        this.streamConfig = config;
    }

    @Override
    public boolean isRunning() {
        return  workingCounter.get() > 0 ||
                this.isDatumAvailable();
    }

    public StatusCounts getCurrentStatus() {
        return new StatusCounts(getTotalInQueue(),
                this.workingCounter.get(),
                this.statusCounter.getSuccess(),
                this.statusCounter.getFail());
    }

    @Override
    public void run() {
        try {
            this.writer.prepare(this.streamConfig);

            while (this.keepRunning.get() || super.isDatumAvailable()) {

                waitForIncoming();

                StreamsDatum datum;
                while ((datum = pollNextDatum()) != null) {
                    final StreamsDatum workingDatum = datum;
                    this.threadingController.execute(new Runnable() {
                        public void run() {
                            processThisDatum(workingDatum);
                        }
                    });

                    waitForIncoming();
                }
            }
        } finally {
            // clean everything up
            this.writer.cleanUp();
        }
    }

    protected final void processThisDatum(StreamsDatum datum) {
        try
        {
            this.workingCounter.incrementAndGet();
            this.writer.write(datum);
            statusCounter.incrementStatus(DatumStatus.SUCCESS);
        }
        catch (Throwable e) {
            LOGGER.error("{} - Error[{}] writing to persist writer {}", this.getId(), e.getMessage(), this.writer.toString());
            statusCounter.incrementStatus(DatumStatus.FAIL);
        }
        finally {
            this.workingCounter.decrementAndGet();
            this.threadingController.getItemPoppedCondition().signal();
        }
    }

    @Override
    public void addOutputQueue(String id, Queue<StreamsDatum> outputQueue) {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support method - setOutputQueue()");
    }


}
