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

import org.apache.streams.core.*;
import org.apache.streams.core.util.DatumUtils;
import org.apache.streams.local.counters.StreamsTaskCounter;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class StreamsProviderTask extends BaseStreamsTask implements DatumStatusCountable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProviderTask.class);

    public DatumStatusCounter getDatumStatusCounter() {
        return this.statusCounter;
    }

    private static enum Type {
        PERPETUAL,
        READ_CURRENT,
        READ_NEW,
        READ_RANGE
    }

    private static final int START = 0;
    private static final int END = 1;

    private StreamsProvider provider;
    private final AtomicBoolean keepRunning = new AtomicBoolean(true);
    private final AtomicBoolean flushing = new AtomicBoolean(false);
    private final AtomicBoolean started = new AtomicBoolean(false);
    private Type type;
    private BigInteger sequence;
    private DateTime[] dateRange;
    private Map<String, Object> config;

    private int timeout;
    private long sleepTime;
    private int zeros = 0;
    private DatumStatusCounter statusCounter = new DatumStatusCounter();
    private StreamsTaskCounter counter;

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readCurrent()}
     * @param provider
     */
    public StreamsProviderTask(StreamsProvider provider, boolean perpetual) {
        this.provider = provider;
        if( perpetual )
            this.type = Type.PERPETUAL;
        else
            this.type = Type.READ_CURRENT;
        this.timeout = DEFAULT_TIMEOUT_MS;
        this.sleepTime = DEFAULT_SLEEP_TIME_MS;
    }

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readNew(BigInteger)}
     * @param provider
     * @param sequence
     */
    public StreamsProviderTask(StreamsProvider provider, BigInteger sequence) {
        this.provider = provider;
        this.type = Type.READ_NEW;
        this.sequence = sequence;
        this.timeout = DEFAULT_TIMEOUT_MS;
        this.sleepTime = DEFAULT_SLEEP_TIME_MS;
    }

    /**
     * Constructor for a StreamsProvider to execute {@link org.apache.streams.core.StreamsProvider:readRange(DateTime,DateTime)}
     * @param provider
     * @param start
     * @param end
     */
    public StreamsProviderTask(StreamsProvider provider, DateTime start, DateTime end) {
        this.provider = provider;
        this.type = Type.READ_RANGE;
        this.dateRange = new DateTime[2];
        this.dateRange[START] = start;
        this.dateRange[END] = end;
        this.timeout = DEFAULT_TIMEOUT_MS;
        this.sleepTime = DEFAULT_SLEEP_TIME_MS;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    @Override
    public boolean isWaiting() {
        return false; //providers don't have inbound queues
    }

    @Override
    public void stopTask() {
        LOGGER.debug("Stopping Provider Task for {}", this.provider.getClass().getSimpleName());
        this.keepRunning.set(false);
    }

    @Override
    public void addInputQueue(BlockingQueue<StreamsDatum> inputQueue) {
        throw new UnsupportedOperationException(this.getClass().getName()+" does not support method - setInputQueue()");
    }

    @Override
    public void setStreamConfig(Map<String, Object> config) {
        this.config = config;
    }


    @Override
    public void run() {
        try {
            this.provider.prepare(this.config); //TODO allow for configuration objects
            StreamsResultSet resultSet = null;
            //Negative values mean we want to run forever
            long maxZeros = timeout < 0 ? Long.MAX_VALUE : (timeout / sleepTime);
            if(this.counter == null) { //should never be null
                this.counter = new StreamsTaskCounter(this.provider.getClass().getName()+ UUID.randomUUID().toString());
            }
            switch(this.type) {
                case PERPETUAL: {
                    provider.startStream();
                    this.started.set(true);
                    while(this.isRunning()) {
                        try {
                            long startTime = System.currentTimeMillis();
                            resultSet = provider.readCurrent();
                            this.counter.addTime(System.currentTimeMillis() - startTime);
                            if( resultSet.size() == 0 )
                                zeros++;
                            else {
                                zeros = 0;
                            }
                            flushResults(resultSet);
                            // the way this works needs to change...
                            if(zeros > maxZeros)
                                this.keepRunning.set(false);
                            if(zeros > 0)
                                Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                            this.counter.incrementErrorCount();
                            LOGGER.warn("Thread interrupted");
                            this.keepRunning.set(false);
                        }
                    }
                }
                    break;
                case READ_CURRENT:
                    resultSet = this.provider.readCurrent();
                    this.started.set(true);
                    break;
                case READ_NEW:
                    resultSet = this.provider.readNew(this.sequence);
                    this.started.set(true);
                    break;
                case READ_RANGE:
                    resultSet = this.provider.readRange(this.dateRange[START], this.dateRange[END]);
                    this.started.set(true);
                    break;
                default: throw new RuntimeException("Type has not been added to StreamsProviderTask.");
            }
            if( resultSet != null )
                flushResults(resultSet);

        } catch( Exception e ) {
            LOGGER.error("Error in processing provider stream", e);
        } finally {
            LOGGER.debug("Complete Provider Task execution for {}", this.provider.getClass().getSimpleName());
            this.provider.cleanUp();
            //Setting started to 'true' here will allow the isRunning() method to return false in the event of an exception
            //before started would normally be set to true n the run method.
            this.started.set(true);
            this.keepRunning.set(false);
        }
    }

    @Override
    public boolean isRunning() {
        //We want to make sure that we never return false if it is flushing, regardless of the state of the provider
        //or whether we have been told to shut down.  If someone really wants us to shut down, they will interrupt the
        //thread and force us to shutdown.  We also want to make sure we have had the opportunity to run before the
        //runtime kills us.
        return !this.started.get() || this.flushing.get() || (this.provider.isRunning() && this.keepRunning.get());
    }

    public void flushResults(StreamsResultSet resultSet) {
        Queue<StreamsDatum> queue = resultSet.getQueue();
        this.flushing.set(true);
        while(!queue.isEmpty()) {
            StreamsDatum datum = queue.poll();
            if(!this.keepRunning.get()) {
                break;
            }
            if(datum != null) {
                try {
                    super.addToOutgoingQueue(datum);
                    this.counter.incrementEmittedCount();
                    statusCounter.incrementStatus(DatumStatus.SUCCESS);
                } catch( Exception e ) {
                    this.counter.incrementErrorCount();
                    statusCounter.incrementStatus(DatumStatus.FAIL);
                    DatumUtils.addErrorToMetadata(datum, e, this.provider.getClass());
                }
            }
        }
        this.flushing.set(false);
    }

    @Override
    public void setStreamsTaskCounter(StreamsTaskCounter counter) {
        this.counter = counter;
    }
}
