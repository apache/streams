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

import com.google.common.base.Splitter;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.sysomos.SysomosConfiguration;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.data.util.RFC3339Utils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Streams Provider for the Sysomos Heartbeat API
 *
 * Configuration:
 * The provider takes either a Map<String,Object> containing the mode (backfill and terminate OR continuous) and a
 * Map<String,String> of heartbeat IDs to document target ids or a string of the format ${heartbeatId}:${documentId},...,${heartbeatId}:${documentId}
 * This configuration will configure the provider to backfill to the specified document and either terminate or not
 * depending on the mode flag.  Continuous mode is assumed, and is the ony mode supported by the String configuration.
 *
 */
public class SysomosProvider implements StreamsProvider {


    public static enum Mode { CONTINUOUS, BACKFILL_AND_TERMINATE }

    private static final Logger LOGGER = LoggerFactory.getLogger(SysomosProvider.class);

    public static final String ENDING_TIME_KEY = "addedBefore";
    public static final String STARTING_TIME_KEY = "addedAfter";
    public static final String STREAMS_ID = "SysomosProvider";
    public static final String MODE_KEY = "mode";
    public static final String STARTING_DOCS_KEY = "startingDocs";
    public static final int LATENCY = 10000;  //Default minLatency for querying the Sysomos API in milliseconds
    public static final long PROVIDER_BATCH_SIZE = 10000L; //Default maximum size of the queue
    public static final long API_BATCH_SIZE = 1000L; //Default maximum size of an API request

    protected volatile Queue<StreamsDatum> providerQueue;

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Set<String> completedHeartbeats = Sets.newHashSet();
    private final long maxQueued;
    private final long minLatency;
    private final long scheduledLatency;
    private final long maxApiBatch;

    private SysomosClient client;
    private SysomosConfiguration config;
    private ScheduledExecutorService stream;
    private Map<String, String> documentIds;
    private Map<String, String> addedBefore;
    private Map<String, String> addedAfter;
    private Mode mode = Mode.CONTINUOUS;
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

    public Mode getMode() {
        return mode;
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

    @Override
    public void startStream() {
        LOGGER.trace("Starting Producer");
        if (!started) {
            LOGGER.trace("Producer not started.  Initializing");
            stream = Executors.newScheduledThreadPool(getConfig().getHeartbeatIds().size() + 1);
            for (String heartbeatId : getConfig().getHeartbeatIds()) {
                Runnable task = createStream(heartbeatId);
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
    public boolean isRunning() {
        return completedHeartbeats.size() < this.getConfig().getHeartbeatIds().size() && !(stream.isTerminated() || stream.isShutdown());
    }

    @Override
    public void prepare(Object configurationObject) {
        this.providerQueue = constructQueue();
        if(configurationObject instanceof Map) {
            extractConfigFromMap((Map) configurationObject);
        } else if(configurationObject instanceof String) {
            documentIds = Splitter.on(";").trimResults().withKeyValueSeparator("=").split((String)configurationObject);
        }
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

    public void signalComplete(String heartbeatId) {
        try {
            this.lock.writeLock().lock();
            this.completedHeartbeats.add(heartbeatId);
            if(!this.isRunning()) {
                this.cleanUp();
            }
        } finally {
            this.lock.writeLock().unlock();
        }

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

    protected SysomosHeartbeatStream createStream(String heartbeatId) {
        String afterTime = addedAfter != null && addedAfter.containsKey(heartbeatId) ? addedAfter.get(heartbeatId) : null;
        String beforeTime = addedBefore != null && addedBefore.containsKey(heartbeatId) ? addedBefore.get(heartbeatId) : null;

        if(documentIds != null && documentIds.containsKey(heartbeatId)) {
            return new SysomosHeartbeatStream(this, heartbeatId, documentIds.get(heartbeatId));
        }
        if(afterTime != null) {
            if(beforeTime != null) {
                return new SysomosHeartbeatStream(this, heartbeatId, RFC3339Utils.parseToUTC(beforeTime), RFC3339Utils.parseToUTC(afterTime));
            } else {
                return new SysomosHeartbeatStream(this, heartbeatId, null, RFC3339Utils.parseToUTC(afterTime));
            }
        }
        return new SysomosHeartbeatStream(this, heartbeatId);
    }

    /**
     * Wait for the queue size to be below threshold before allowing execution to continue on this thread
     */
    protected void pauseForSpace() {
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

    @SuppressWarnings("unchecked")
    protected void extractConfigFromMap(Map configMap) {
        if(configMap.containsKey(MODE_KEY)) {
            Object configMode = configMap.get(MODE_KEY);
            if(!(configMode instanceof Mode)) {
                throw new IllegalStateException("Invalid configuration.  Mode must be an instance of the Mode enum but was " + configMode);
            }
            this.mode = (Mode)configMode;
        }
        if(configMap.containsKey(STARTING_DOCS_KEY)) {
            Object configIds = configMap.get(STARTING_DOCS_KEY);
            if(!(configIds instanceof Map)) {
                throw new IllegalStateException("Invalid configuration.  StartingDocs must be an instance of Map<String,String> but was " + configIds);
            }
            this.documentIds = (Map)configIds;
        }
        if(configMap.containsKey(STARTING_TIME_KEY)) {
            Object configIds = configMap.get(STARTING_TIME_KEY);
            if(!(configIds instanceof Map)) {
                throw new IllegalStateException("Invalid configuration.  Added after key must be an instance of Map<String,String> but was " + configIds);
            }
            this.addedAfter = (Map)configIds;
        }
        if(configMap.containsKey(ENDING_TIME_KEY)) {
            Object configIds = configMap.get(ENDING_TIME_KEY);
            if(!(configIds instanceof Map)) {
                throw new IllegalStateException("Invalid configuration.  Added before key must be an instance of Map<String,String> but was " + configIds);
            }
            this.addedBefore = (Map)configIds;
        }
    }

    private Queue<StreamsDatum> constructQueue() {
        return Queues.newConcurrentLinkedQueue();
    }
}
