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
import org.apache.streams.core.StreamsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Processor task that is multi-threaded
 */
public class StreamsProcessorTask extends BaseStreamsTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProcessorTask.class);

    protected final StreamsProcessor processor;
    protected Map<String, Object> streamConfig;
    protected final DatumStatusCounter statusCounter = new DatumStatusCounter();

    /**
     * Default constructor, uses default sleep time of 500ms when inbound queue is empty
     *
     * @param processor process to run in task
     */
    public StreamsProcessorTask(String id, BlockingQueue<StreamsDatum> inQueue, Map<String, BaseStreamsTask> ctx, StreamsProcessor processor, ThreadingController threadingController) {
        super(id, inQueue, ctx, threadingController);
        this.processor = processor;
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        if (this.streamConfig != null)
            throw new RuntimeException("This variable has already been set, you cannot set it.");
        this.streamConfig = config;
    }

    @Override
    public boolean isRunning() {
        return getWorkingCount() > 0 || this.inQueue.size() > 0;
    }

    @Override
    public void run() {

        try {
            this.processor.prepare(this.streamConfig);

            while (shouldKeepRunning() || this.inQueue.size() > 0) {

                waitForIncoming();

                if(this.inQueue.size() > 0) {
                    final Collection<ThreadingController.LockCounter> locks = waitForOutBoundQueueToBeFree();
                    if(locks != null) {
                        final StreamsDatum datum = super.pollNextDatum();

                        Callable<List<StreamsDatum>> command = new Callable<List<StreamsDatum>>() {
                            @Override
                            public List<StreamsDatum> call() throws Exception {
                                return processor.process(datum);
                            }
                        };

                        FutureCallback<List<StreamsDatum>> callback = new FutureCallback<List<StreamsDatum>>() {
                            @Override
                            public void onSuccess(List<StreamsDatum> ds) {
                                if (ds != null)
                                    for (StreamsDatum d : ds)
                                        addToOutgoingQueue(d);

                                statusCounter.incrementStatus(DatumStatus.SUCCESS);
                                reportCompleted(locks);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                statusCounter.incrementStatus(DatumStatus.FAIL);
                                reportCompleted(locks);
                            }
                        };

                        this.threadingController.execute(command, callback);
                    }
                }
                // loop back around.
            }
        } finally {
            // clean everything up
            this.processor.cleanUp();
        }
    }

    public StatusCounts getCurrentStatus() {
        return new StatusCounts(this.inQueue.size(),
                getWorkingCount(),
                this.statusCounter.getSuccess(),
                this.statusCounter.getFail());
    }
}