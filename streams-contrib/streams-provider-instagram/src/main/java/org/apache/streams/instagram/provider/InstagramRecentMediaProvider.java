/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance *
http://www.apache.org/licenses/LICENSE-2.0 *
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License. */
package org.apache.streams.instagram.provider;

import com.google.common.collect.Queues;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.instagram.InstagramConfigurator;
import org.apache.streams.instagram.InstagramUserInformationConfiguration;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Instagram {@link org.apache.streams.core.StreamsProvider} that provides the recent media data for a group of users
 */
public class InstagramRecentMediaProvider implements StreamsProvider {

    private InstagramUserInformationConfiguration config;
    private InstagramRecentMediaCollector dataCollector;
    protected Queue<MediaFeedData> mediaFeedQueue; //exposed for testing
    private ExecutorService executorService;
    private AtomicBoolean isCompleted;

    public InstagramRecentMediaProvider() {
        this(InstagramConfigurator.detectInstagramUserInformationConfiguration(StreamsConfigurator.config.getConfig("instagram")));
    }

    public InstagramRecentMediaProvider(InstagramUserInformationConfiguration config) {
        this.config = config;
        this.mediaFeedQueue = Queues.newConcurrentLinkedQueue();
    }

    @Override
    public void startStream() {
        this.dataCollector = getInstagramRecentMediaCollector();
        this.executorService = Executors.newSingleThreadExecutor();
        this.executorService.submit(this.dataCollector);
    }

    /**
     * EXPOSED FOR TESTING
     * @return
     */
    protected InstagramRecentMediaCollector getInstagramRecentMediaCollector() {
        return new InstagramRecentMediaCollector(this.mediaFeedQueue, this.config);
    }


    @Override
    public StreamsResultSet readCurrent() {
        Queue<StreamsDatum> batch = Queues.newConcurrentLinkedQueue();
        MediaFeedData data = null;
        synchronized (this.mediaFeedQueue) {
            while(!this.mediaFeedQueue.isEmpty()) {
                data = this.mediaFeedQueue.poll();
                batch.add(new StreamsDatum(data, data.getId()));
            }
        }
        this.isCompleted.set(batch.size() == 0 && this.mediaFeedQueue.isEmpty() && this.dataCollector.isCompleted());
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
        return !this.isCompleted.get();
    }

    @Override
    public void prepare(Object configurationObject) {
        this.isCompleted = new AtomicBoolean(false);
    }

    @Override
    public void cleanUp() {
        this.executorService.shutdown();
        try {
            this.executorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        } finally {
            this.executorService = null;
        }
    }
}
