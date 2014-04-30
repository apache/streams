/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.sysomos.provider;

import com.google.common.collect.Queues;
import com.sysomos.SysomosConfiguration;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Streams Provider for the Sysomos Heartbeat API
 */
public class SysomosProvider implements StreamsProvider {

    public final static String STREAMS_ID = "SysomosProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(SysomosProvider.class);

    public static final int LATENCY = 10000;  //Default minLatency for querying the Sysomos API in milliseconds
    public static final long PROVIDER_BATCH_SIZE = 10000L; //Default maximum size of the queue
    public static final long API_BATCH_SIZE = 1000L; //Default maximum size of an API request

    protected volatile Queue<StreamsDatum> providerQueue;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final long maxQueued;
    private final long minLatency;
    private final long scheduledLatency;
    private final long maxApiBatch;

    private SysomosClient client;
    private SysomosConfiguration config;
    private ScheduledExecutorService stream;
    private boolean started = false;

    public SysomosProvider(SysomosConfiguration sysomosConfiguration) {
        this.config = sysomosConfiguration;
        this.client = new SysomosClient(sysomosConfiguration.getApiKey());
        this.maxQueued = sysomosConfiguration.getMaxBatchSize() == null ? PROVIDER_BATCH_SIZE : sysomosConfiguration.getMaxBatchSize();
        this.minLatency = sysomosConfiguration.getMinDelayMs() == null ? LATENCY : sysomosConfiguration.getMinDelayMs();
        this.scheduledLatency = sysomosConfiguration.getScheduledDelayMs() == null ? (LATENCY * 15) : sysomosConfiguration.getScheduledDelayMs();
        this.maxApiBatch = sysomosConfiguration.getMinDelayMs() == null ? API_BATCH_SIZE : sysomosConfiguration.getApiBatchSize();
    }

    public SysomosConfiguration getConfig() {
        return config;
    }

    public void setConfig(SysomosConfiguration config) {
        this.config = config;
    }

    @Override
    public void startStream() {
        LOGGER.trace("Starting Producer");
        if (!started) {
            LOGGER.trace("Producer not started.  Initializing");
            stream = Executors.newScheduledThreadPool(getConfig().getHeartbeatIds().size() + 1);
            for (String heartbeatId : getConfig().getHeartbeatIds()) {
                Runnable task = new SysomosHeartbeatStream(this, heartbeatId);
                stream.scheduleWithFixedDelay(task, 0, this.scheduledLatency, TimeUnit.MILLISECONDS);
                LOGGER.info("Started producer task for heartbeat {}", heartbeatId);
            }
            started = true;
        }
    }

    @Override
    public StreamsResultSet readCurrent() {
        StreamsResultSet current;
        try {
            lock.writeLock().lock();
            LOGGER.debug("Creating new result set for {} items", providerQueue.size());
            current = new StreamsResultSet(providerQueue);
            providerQueue = constructQueue();
        } finally {
            lock.writeLock().unlock();
        }

        return current;
    }

    @Override
    public StreamsResultSet readNew(BigInteger bigInteger) {
        throw new NotImplementedException("readNew not currently implemented");
    }

    @Override
    public StreamsResultSet readRange(DateTime dateTime, DateTime dateTime2) {
        throw new NotImplementedException("readRange not currently implemented");
    }

    @Override
    public void prepare(Object configurationObject) {
        this.providerQueue = constructQueue();
    }

    @Override
    public void cleanUp() {
        stream.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!stream.awaitTermination(60, TimeUnit.SECONDS)) {
                stream.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!stream.awaitTermination(60, TimeUnit.SECONDS)) {
                    LOGGER.error("Stream did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            stream.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    public long getMinLatency() {
        return minLatency;
    }

    public long getMaxApiBatch() {
        return maxApiBatch;
    }

    public SysomosClient getClient() {
        return client;
    }

    protected void enqueueItem(StreamsDatum datum) {
        boolean success;
        do {
            try {
                pauseForSpace(); //Dont lock before this pause. We don't want to block the readCurrent method
                lock.readLock().lock();
                success = providerQueue.offer(datum);
                Thread.yield();
            }finally {
                lock.readLock().unlock();
            }
        }
        while (!success);
    }

    /**
     * Wait for the queue size to be below threshold before allowing execution to continue on this thread
     */
    private void pauseForSpace() {
        while(this.providerQueue.size() >= maxQueued) {
            LOGGER.trace("Sleeping the current thread due to a full queue");
            try {
                Thread.sleep(100);
                LOGGER.trace("Resuming thread after wait period");
            } catch (InterruptedException e) {
                LOGGER.warn("Thread was interrupted", e);
            }
        }
    }

    private Queue<StreamsDatum> constructQueue() {
        return Queues.newConcurrentLinkedQueue();
    }
}
