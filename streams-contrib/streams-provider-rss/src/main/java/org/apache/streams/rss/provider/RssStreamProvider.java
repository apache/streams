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

package org.apache.streams.rss.provider;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.rss.FeedDetails;
import org.apache.streams.rss.RssStreamConfiguration;
import org.apache.streams.rss.provider.perpetual.RssFeedScheduler;
import org.apache.streams.util.ComponentUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *
 */
public class RssStreamProvider implements StreamsProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(RssStreamProvider.class);
    private final static int MAX_SIZE = 1000;

    private RssStreamConfiguration config;
    private boolean perpetual;
    private Set<String> urlFeeds;
    private ExecutorService executor;
    private BlockingQueue<StreamsDatum> dataQueue;
    private AtomicBoolean isComplete;
    private int consecutiveEmptyReads;

    @VisibleForTesting
    protected RssFeedScheduler scheduler;

    public RssStreamProvider() {
        this(RssStreamConfigurator.detectConfiguration(StreamsConfigurator.config.getConfig("rss")), false);
    }

    public RssStreamProvider(boolean perpetual) {
        this(RssStreamConfigurator.detectConfiguration(StreamsConfigurator.config.getConfig("rss")), perpetual);
    }

    public RssStreamProvider(RssStreamConfiguration config) {
        this(config, false);
    }

    public RssStreamProvider(RssStreamConfiguration config, boolean perpetual) {
        this.perpetual = perpetual;
        this.config = config;
    }

    public void setConfig(RssStreamConfiguration config) {
        this.config = config;
    }

    public void setRssFeeds(Set<String> urlFeeds) {
        this.urlFeeds = urlFeeds;
    }

    public void setRssFeeds(Map<String, Long> feeds) {
        if(this.config == null) {
            this.config = new RssStreamConfiguration();
        }
        List<FeedDetails> feedDetails = Lists.newLinkedList();
        for(String feed : feeds.keySet()) {
            Long delay = feeds.get(feed);
            FeedDetails detail = new FeedDetails();
            detail.setUrl(feed);
            detail.setPollIntervalMillis(delay);
            feedDetails.add(detail);
        }
        this.config.setFeeds(feedDetails);
    }

    @Override
    public void startStream() {
        LOGGER.trace("Starting Rss Scheduler");
        this.executor.submit(this.scheduler);
    }

    @Override
    public StreamsResultSet readCurrent() {
        Queue<StreamsDatum> batch = Queues.newConcurrentLinkedQueue();
        int batchSize = 0;
        while(!this.dataQueue.isEmpty() && batchSize < MAX_SIZE) {
            StreamsDatum datum = ComponentUtils.pollWhileNotEmpty(this.dataQueue);
            if(datum != null) {
                ++batchSize;
                batch.add(datum);
            }
        }
        this.isComplete.set(this.scheduler.isComplete() && batch.isEmpty() && this.dataQueue.isEmpty());
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
        return !this.isComplete.get();
    }

    @Override
    public void prepare(Object configurationObject) {
        this.executor = new ThreadPoolExecutor(1, 4, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        this.dataQueue = Queues.newLinkedBlockingQueue();
        this.scheduler = getScheduler(this.dataQueue);
        this.isComplete = new AtomicBoolean(false);
        this.consecutiveEmptyReads = 0;
    }

    @VisibleForTesting
    protected RssFeedScheduler getScheduler(BlockingQueue<StreamsDatum> queue) {
        if(this.perpetual)
            return new RssFeedScheduler(this.executor, this.config.getFeeds(), queue);
        else
            return new RssFeedScheduler(this.executor, this.config.getFeeds(), queue, 0);
    }

    @Override
    public void cleanUp() {
        this.scheduler.stop();
        ComponentUtils.shutdownExecutor(this.executor, 10, 10);
    }


}
