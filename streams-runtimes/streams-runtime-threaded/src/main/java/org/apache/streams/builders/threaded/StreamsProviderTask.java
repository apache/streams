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
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 */
public class StreamsProviderTask extends BaseStreamsTask  {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProviderTask.class);

    private static enum Type {
        PERPETUAL,
        READ_CURRENT,
        READ_NEW,
        READ_RANGE
    }

    private static final int START = 0;
    private static final int END = 1;

    private static final int DEFAULT_TIMEOUT_MS = 1000000;

    private StreamsProvider provider;
    private AtomicBoolean keepRunning;
    private Type type;
    private BigInteger sequence;
    private DateTime[] dateRange;
    private Map<String, Object> config;
    private AtomicBoolean isRunning;
    private StreamsResultSet streamsResultSet;

    private int timeout;
    private int zeros = 0;
    private DatumStatusCounter statusCounter = new DatumStatusCounter();
    private final ThreadingController threadingController;

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readCurrent()}
     *
     * @param provider
     */
    public StreamsProviderTask(ThreadingController threadingController, String id, Map<String, BaseStreamsTask> ctx, StreamsProvider provider, boolean perpetual) {
        super(id, null, ctx, threadingController);
        this.threadingController = threadingController;
        this.provider = provider;
        if (perpetual)
            this.type = Type.PERPETUAL;
        else
            this.type = Type.READ_CURRENT;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
        this.timeout = DEFAULT_TIMEOUT_MS;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public BlockingDeque<StreamsDatum> getInQueue() {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support method - setInputQueue()");
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        this.config = config;
    }

    @Override
    public void initialize() {
        super.initialize();
        this.threadingController.flagWorking(this);
    }

    @Override
    public void run() {

        // lock, yes, I am running
        this.isRunning.set(true);

        try {
            // TODO allow for configuration objects
            this.provider.prepare(this.config);
            StreamsResultSet resultSet = null;
            switch (this.type) {
                case PERPETUAL:
                    provider.startStream();
                    while (this.keepRunning.get()) {
                        resultSet = provider.readCurrent();
                        if (resultSet.size() == 0)
                            zeros++;
                        else {
                            zeros = 0;
                        }
                        this.streamsResultSet = resultSet;
                        flushResults();
                        // the way this works needs to change...
                        if (zeros > (timeout))
                            this.keepRunning.set(false);
                        safeQuickRest(10);
                    }
                    break;
                case READ_CURRENT:
                    resultSet = this.provider.readCurrent();
                    break;
                case READ_NEW:
                    resultSet = this.provider.readNew(this.sequence);
                    break;
                case READ_RANGE:
                    resultSet = this.provider.readRange(this.dateRange[START], this.dateRange[END]);
                    break;
                default:
                    throw new RuntimeException("Type has not been added to StreamsProviderTask.");
            }
            this.streamsResultSet = resultSet;

            if(this.streamsResultSet != null && this.streamsResultSet.getQueue() != null) {
                /**
                 * We keep running while the provider tells us that we are running or we still have
                 * items in queue to be processed.
                 *
                 * IF, the keep running flag is turned to off, then we exit immediately
                 */
                while ((this.streamsResultSet.isRunning() || !this.streamsResultSet.getQueue().isEmpty()) && this.keepRunning.get()) {
                    if(streamsResultSet.getQueue().isEmpty()) {
                        // The process is still running, but there is nothing on the queue...
                        // we just need to be patient and wait, we yield the execution and
                        // wait for 1ms to see if anything changes.
                        safeQuickRest(5);
                    } else {
                        flushResults();
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.error("There was an unknown error while attempting to read from the provider. This provider cannot continue with this exception.");
            LOGGER.error("The stream will continue running, but not with this provider.");
            LOGGER.error("Exception: {}", e);
            this.isRunning.set(false);
        } finally {
            this.provider.cleanUp();
            this.threadingController.flagNotWorking(this);
            this.isRunning.set(false);
        }
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }

    public void flushResults() {
        try {
            StreamsDatum datum;
            while (!streamsResultSet.getQueue().isEmpty() && (datum = streamsResultSet.getQueue().poll()) != null) {
                /**
                 * This is meant to be a hard exit from the system. If we are running
                 * and this flag gets set to false, we are to exit immediately and
                 * abandon anything that is in this queue. The remaining processors
                 * will shutdown gracefully once they have depleted their queue
                 */
                if (!this.keepRunning.get())
                    break;

                processNext(datum);
            }
        }
        catch(Throwable e) {
            LOGGER.warn("Unknown problem reading the queue, no datums affected: {}", e.getMessage());
        }
    }

    private void processNext(StreamsDatum datum) {
        Collection<ThreadingController.LockCounter> locks = waitForOutBoundQueueToBeFree();
        try {
            super.addToOutgoingQueue(datum);
            statusCounter.incrementStatus(DatumStatus.SUCCESS);
        } catch (Throwable e) {
            statusCounter.incrementStatus(DatumStatus.FAIL);
        } finally {
            releaseLocks(locks);
        }
    }

    public StatusCounts getCurrentStatus() {
        return new StatusCounts(this.streamsResultSet == null ? 0 :
                this.streamsResultSet.getQueue() == null ? 0 : this.streamsResultSet.getQueue().size(),
                0,
                this.statusCounter.getSuccess(),
                this.statusCounter.getFail());
    }

    protected void safeQuickRest(int waitTime) {

        // The queue is empty, we might as well sleep.
        Thread.yield();
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ie) {
            // No Operation
        }
    }

}
