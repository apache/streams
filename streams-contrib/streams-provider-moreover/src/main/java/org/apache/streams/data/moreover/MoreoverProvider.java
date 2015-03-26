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

package org.apache.streams.data.moreover;

import com.google.common.base.Predicates;
import com.google.common.collect.*;
import net.jcip.annotations.Immutable;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.moreover.MoreoverConfiguration;
import org.apache.streams.moreover.MoreoverKeyData;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.*;

public class MoreoverProvider implements StreamsProvider {

    public final static String STREAMS_ID = "MoreoverProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(MoreoverProvider.class);
    private final static int MAX_BATCH_SIZE = 1000;

    protected  BlockingQueue<StreamsDatum> providerQueue = new LinkedBlockingQueue<>(MAX_BATCH_SIZE);

    private List<MoreoverKeyData> keys;
    private List<MoreoverProviderTask> providerTasks;

    private MoreoverConfiguration config;

    private ExecutorService executor;

    public MoreoverProvider(MoreoverConfiguration moreoverConfiguration) {
        this.config = moreoverConfiguration;
        this.keys = Lists.newArrayList();
        for( MoreoverKeyData apiKey : config.getApiKeys()) {
            this.keys.add(apiKey);
        }
        this.providerTasks = Lists.newLinkedList();
    }

    public void startStream() {

        for(MoreoverKeyData key : keys) {
            MoreoverProviderTask task = getMoreoverProviderTask(key, this.providerQueue);
            this.providerTasks.add(task);
            executor.submit(task);
            LOGGER.info("Started producer for {}", key.getKey());
        }
        this.executor.shutdown();
    }

    protected MoreoverProviderTask getMoreoverProviderTask(MoreoverKeyData keyData, BlockingQueue<StreamsDatum> queue) {
        return new MoreoverProviderTask(keyData.getId(), keyData.getKey(), queue, keyData.getStartingSequence(), keyData.getPerpetual());
    }

    @Override
    public synchronized StreamsResultSet readCurrent() {

        LOGGER.debug("readCurrent: {}", providerQueue.size());

        Collection<StreamsDatum> currentIterator = Lists.newArrayList();
        int batchSize = 0;
        BlockingQueue<StreamsDatum> batch = Queues.newLinkedBlockingQueue();
        while(!this.providerQueue.isEmpty() && batchSize < MAX_BATCH_SIZE) {
            StreamsDatum datum = this.providerQueue.poll();
            if(datum != null) {
                ++batchSize;
                try {
                    batch.put(datum);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException(ie);
                }
            }
        }
        return new StreamsResultSet(batch);
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return null;
    }

    @Override
    public boolean isRunning() {
        return !executor.isTerminated() || !this.providerQueue.isEmpty();
    }

    @Override
    public void prepare(Object configurationObject) {
        LOGGER.debug("Prepare");
        executor = Executors.newFixedThreadPool(this.keys.size());
    }

    @Override
    public void cleanUp() {
        for(MoreoverProviderTask task : this.providerTasks) {
            task.stopTask();
        }
    }
}
