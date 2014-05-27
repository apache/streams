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
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

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

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readCurrent()}
     *
     * @param provider
     */
    public StreamsProviderTask(String id, StreamsProvider provider, boolean perpetual) {
        super(id);
        this.provider = provider;
        if (perpetual)
            this.type = Type.PERPETUAL;
        else
            this.type = Type.READ_CURRENT;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
        this.timeout = DEFAULT_TIMEOUT_MS;
    }

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readNew(BigInteger)}
     *
     * @param provider
     * @param sequence
     */
    public StreamsProviderTask(String id, StreamsProvider provider, BigInteger sequence) {
        super(id);
        this.provider = provider;
        this.type = Type.READ_NEW;
        this.sequence = sequence;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
        this.timeout = DEFAULT_TIMEOUT_MS;
    }

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readRange(DateTime,DateTime)}
     *
     * @param provider
     * @param start
     * @param end
     */
    public StreamsProviderTask(String id, StreamsProvider provider, DateTime start, DateTime end) {
        super(id);
        this.provider = provider;
        this.type = Type.READ_RANGE;
        this.dateRange = new DateTime[2];
        this.dateRange[START] = start;
        this.dateRange[END] = end;
        this.keepRunning = new AtomicBoolean(true);
        this.isRunning = new AtomicBoolean(true);
        this.timeout = DEFAULT_TIMEOUT_MS;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public void addInputQueue(Queue<StreamsDatum> inputQueue) {
        throw new UnsupportedOperationException(this.getClass().getName() + " does not support method - setInputQueue()");
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        this.config = config;
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
                        safeQuickRest();
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

            if(this.streamsResultSet != null) {
                /**
                 * We keep running while the provider tells us that we are running or we still have
                 * items in queue to be processed.
                 *
                 * IF, the keep running flag is turned to off, then we exit immediately
                 */
                while ((this.streamsResultSet.isRunning() ||
                        this.streamsResultSet.getQueue().size() > 0) && this.keepRunning.get()) {
                    if(this.streamsResultSet.getQueue().size() == 0) {
                        // The process is still running, but there is nothing on the queue...
                        // we just need to be patient and wait, we yield the execution and
                        // wait for 1ms to see if anything changes.
                        safeQuickRest();
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
            while ((datum = streamsResultSet.getQueue().poll()) != null) {
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
        try {
            super.addToOutgoingQueue(datum);
            statusCounter.incrementStatus(DatumStatus.SUCCESS);
        } catch (Throwable e) {
            statusCounter.incrementStatus(DatumStatus.FAIL);
        }
    }

    public StatusCounts getCurrentStatus() {
        return new StatusCounts(this.streamsResultSet == null ? 0 : this.streamsResultSet.getQueue().size(),
                0,
                this.statusCounter.getSuccess(),
                this.statusCounter.getFail());
    }


}
