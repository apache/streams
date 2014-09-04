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

import com.google.common.util.concurrent.FutureCallback;
import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutionException;

public class StreamsPersistWriterTask extends BaseStreamsTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPersistWriterTask.class);

    protected final StreamsPersistWriter writer;
    protected Map<String, Object> streamConfig;
    protected final DatumStatusCounter statusCounter = new DatumStatusCounter();


    public StreamsPersistWriterTask(String id, Map<String, BaseStreamsTask> ctx, StreamsPersistWriter writer, ThreadingController threadingController) {
        super(id, ctx, threadingController);
        this.writer = writer;
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        this.streamConfig = config;
    }

    @Override
    public boolean isRunning() {
        return  getWorkingCount() > 0 ||
                this.isDatumAvailable();
    }

    public StatusCounts getCurrentStatus() {
        return new StatusCounts(getTotalInQueue(),
                this.getWorkingCount(),
                this.statusCounter.getSuccess(),
                this.statusCounter.getFail());
    }

    @Override
    public void run() {
        try {
            this.writer.prepare(this.streamConfig);

            while (this.keepRunning.get() || super.isDatumAvailable()) {

                if(this.keepRunning.get())
                    waitForIncoming();

                final StreamsDatum datum = pollNextDatum();

                    reportWorking();


                Runnable command = new Runnable() {
                    @Override
                    public void run() {
                        writer.write(datum);
                    }
                };


                FutureCallback callback = new FutureCallback() {
                    @Override
                    public void onSuccess(Object o) {
                        statusCounter.incrementStatus(DatumStatus.SUCCESS);
                        reportCompleted();
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        statusCounter.incrementStatus(DatumStatus.FAIL);
                        reportCompleted();
                    }
                };

                this.threadingController.execute(command, callback);
            }
        } finally {
            // clean everything up
            this.writer.cleanUp();
        }
    }

    @Override
    public void addOutputQueue(String id) {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support method - setOutputQueue()");
    }


}
