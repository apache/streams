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
import org.apache.streams.core.StreamsProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Processor task that is multi-threaded
 */
public class StreamsProcessorTask extends BaseStreamsTask {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProcessorTask.class);

    protected final StreamsProcessor processor;
    protected Map<String, Object> streamConfig;
    protected final DatumStatusCounter statusCounter = new DatumStatusCounter();

    private final SimpleCondition simpleCondition = new SimpleCondition();
    private final ThreadingController threadingController;

    private final AtomicInteger workingCounter = new AtomicInteger(0);

    /**
     * Default constructor, uses default sleep time of 500ms when inbound queue is empty
     *
     * @param processor process to run in task
     */
    public StreamsProcessorTask(String id, Map<String, BaseStreamsTask> ctx, StreamsProcessor processor, ThreadingController threadingController) {
        super(id, ctx);
        this.processor = processor;
        this.threadingController = threadingController;
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        if (this.streamConfig != null)
            throw new RuntimeException("This variable has already been set, you cannot set it.");
        this.streamConfig = config;
    }

    @Override
    public boolean isRunning() {
        return this.workingCounter.get() > 0 || this.isDatumAvailable();
    }

    @Override
    public void run() {

        try {
            this.processor.prepare(this.streamConfig);

            while (this.keepRunning.get() || super.isDatumAvailable()) {

                StreamsDatum datum;
                while ((datum = super.pollNextDatum()) != null) {
                    final StreamsDatum workingDatum = datum;

                    Queue q = null;
                    while((q = getHaltingOutboundQueue()) != null) {
                        try {
                            CONDITIONS.get(q).await();
                        }
                        catch(InterruptedException ioe) {
                            /* no op */
                        }
                    }

                    this.threadingController.execute(new Runnable() {
                        public void run() {
                            processThisDatum(workingDatum);
                        }
                    });
                }

                // we don't have anything to do, let's yield
                // and take a quick rest and wait for people to
                // catch up
                if (wasTapped()) {
                    try {
                        this.simpleCondition.await(10, TimeUnit.MILLISECONDS);
                    }
                    catch(InterruptedException ioe) {
                            /* no op */
                    }
                }
                else {
                    safeQuickRest(5);
                }
            }
        } finally {
            // clean everything up
            this.processor.cleanUp();
        }
    }

    protected final void processThisDatum(StreamsDatum datum) {
        try {
            workingCounter.incrementAndGet();
            // get the outputs from the queue and pass them down the row
            List<StreamsDatum> output = this.processor.process(datum);
            if (output != null)
                for (StreamsDatum outDatum : output)
                    super.addToOutgoingQueue(outDatum);

            this.statusCounter.incrementStatus(DatumStatus.SUCCESS);
        } catch (Throwable e) {
            LOGGER.warn("{} - There was an error({}) processing datum: {}", this.getId(), e.getMessage(), datum);
            this.statusCounter.incrementStatus(DatumStatus.FAIL);
        }
        finally {
            workingCounter.decrementAndGet();
            this.threadingController.getItemPoppedCondition().signal();
            for(StreamsTask t : downStreamTasks)
                t.knock();
        }
    }

    public StatusCounts getCurrentStatus() {
        return new StatusCounts(getTotalInQueue(),
                this.workingCounter.get(),
                this.statusCounter.getSuccess(),
                this.statusCounter.getFail());
    }
}