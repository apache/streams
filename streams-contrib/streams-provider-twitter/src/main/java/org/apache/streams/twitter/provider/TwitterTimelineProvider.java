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

package org.apache.streams.twitter.provider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.twitter.TwitterUserInformationConfiguration;
import org.apache.streams.util.ComponentUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *  Retrieve recent posts from a list of user ids or names.
 */
public class TwitterTimelineProvider implements StreamsProvider, Serializable {

    public final static String STREAMS_ID = "TwitterTimelineProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterTimelineProvider.class);
    public static final int MAX_NUMBER_WAITING = 10000;

    private TwitterUserInformationConfiguration config;

    private Class klass;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public TwitterUserInformationConfiguration getConfig() {
        return config;
    }

    public void setConfig(TwitterUserInformationConfiguration config) {
        this.config = config;
    }

    protected Collection<String[]> screenNameBatches;
    protected Collection<Long> ids;

    protected volatile Queue<StreamsDatum> providerQueue;

    protected int idsCount;
    protected Twitter client;

    protected ExecutorService executor;

    protected DateTime start;
    protected DateTime end;

    protected final AtomicBoolean running = new AtomicBoolean();

    Boolean jsonStoreEnabled;
    Boolean includeEntitiesEnabled;

    private static ExecutorService getExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    public TwitterTimelineProvider(TwitterUserInformationConfiguration config) {
        this.config = config;
    }

    public TwitterTimelineProvider(TwitterUserInformationConfiguration config, Class klass) {
        this.config = config;
        this.klass = klass;
    }

    public Queue<StreamsDatum> getProviderQueue() {
        return this.providerQueue;
    }

    @Override
    public void startStream() {
        LOGGER.debug("{} startStream", STREAMS_ID);

        Preconditions.checkArgument(!ids.isEmpty());

        LOGGER.info("readCurrent");

        submitTimelineThreads(ids.toArray(new Long[0]));

        running.set(true);

        executor.shutdown();
    }

    public boolean shouldContinuePulling(List<Status> statuses) {
        return (statuses != null) && (statuses.size() > 0);
    }

    protected void submitTimelineThreads(Long[] ids) {
        Twitter client = getTwitterClient();

        for(int i = 0; i < ids.length; i++) {

            TwitterTimelineProviderTask providerTask = new TwitterTimelineProviderTask(this, client, ids[i]);
            executor.submit(providerTask);

        }

    }

    private Collection<Long> retrieveIds(String[] screenNames) {
        Twitter client = getTwitterClient();

        List<Long> ids = Lists.newArrayList();
        try {
            for (User tStat : client.lookupUsers(screenNames)) {
                ids.add(tStat.getId());
            }
        } catch (TwitterException e) {
            LOGGER.error("Failure retrieving user details.", e.getMessage());
        }
        return ids;
    }

    public StreamsResultSet readCurrent() {

        LOGGER.info("Providing {} docs", providerQueue.size());

        StreamsResultSet result;

        try {
            lock.writeLock().lock();
            result = new StreamsResultSet(providerQueue);
            result.setCounter(new DatumStatusCounter());
            providerQueue = constructQueue();
        } finally {
            lock.writeLock().unlock();
        }

        if( providerQueue.isEmpty() && executor.isTerminated()) {
            LOGGER.info("Finished.  Cleaning up...");

            running.set(false);

            LOGGER.info("Exiting");
        }

        return result;

    }

    protected Queue<StreamsDatum> constructQueue() {
        return Queues.synchronizedQueue(new LinkedBlockingQueue<StreamsDatum>(MAX_NUMBER_WAITING));
    }

    public StreamsResultSet readNew(BigInteger sequence) {
        LOGGER.debug("{} readNew", STREAMS_ID);
        throw new NotImplementedException();
    }

    public StreamsResultSet readRange(DateTime start, DateTime end) {
        LOGGER.debug("{} readRange", STREAMS_ID);
        throw new NotImplementedException();
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(10, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void prepare(Object o) {

        executor = getExecutor();

        try {
            lock.writeLock().lock();
            providerQueue = constructQueue();
        } finally {
            lock.writeLock().unlock();
        }

        Preconditions.checkNotNull(providerQueue);
        Preconditions.checkNotNull(this.klass);
        Preconditions.checkNotNull(config.getOauth().getConsumerKey());
        Preconditions.checkNotNull(config.getOauth().getConsumerSecret());
        Preconditions.checkNotNull(config.getOauth().getAccessToken());
        Preconditions.checkNotNull(config.getOauth().getAccessTokenSecret());
        Preconditions.checkNotNull(config.getInfo());

        consolidateToIDs();
    }

    /**
     * Using the "info" list that is contained in the configuration, ensure that all
     * account identifiers are converted to IDs (Longs) instead of screenNames (Strings)
     */
    protected void consolidateToIDs() {
        List<String> screenNames = Lists.newArrayList();
        ids = Lists.newArrayList();

        for(String account : config.getInfo()) {
            try {
                if (new Long(account) != null) {
                    ids.add(Long.parseLong(Objects.toString(account, null)));
                } else {
                    screenNames.add(account);
                }
            } catch (Exception e) {
                LOGGER.error("Exception while trying to add ID: {{}}, {}", account, e);
            }
        }

        // Twitter allows for batches up to 100 per request, but you cannot mix types
        screenNameBatches = new ArrayList<String[]>();
        while(screenNames.size() >= 100) {
            screenNameBatches.add(screenNames.subList(0, 100).toArray(new String[0]));
            screenNames = screenNames.subList(100, screenNames.size());
        }

        if(screenNames.size() > 0)
            screenNameBatches.add(screenNames.toArray(new String[ids.size()]));

        Iterator<String[]> screenNameBatchIterator = screenNameBatches.iterator();

        while(screenNameBatchIterator.hasNext()) {
            Collection<Long> batchIds = retrieveIds(screenNameBatchIterator.next());
            ids.addAll(batchIds);
        }
    }

    public Twitter getTwitterClient() {
        String baseUrl = "https://api.twitter.com:443/1.1/";

        ConfigurationBuilder builder = new ConfigurationBuilder()
                .setOAuthConsumerKey(config.getOauth().getConsumerKey())
                .setOAuthConsumerSecret(config.getOauth().getConsumerSecret())
                .setOAuthAccessToken(config.getOauth().getAccessToken())
                .setOAuthAccessTokenSecret(config.getOauth().getAccessTokenSecret())
                .setIncludeEntitiesEnabled(true)
                .setJSONStoreEnabled(true)
                .setAsyncNumThreads(3)
                .setRestBaseURL(baseUrl)
                .setIncludeMyRetweetEnabled(Boolean.TRUE)
                .setPrettyDebugEnabled(Boolean.TRUE);

        return new TwitterFactory(builder.build()).getInstance();
    }

    @Override
    public void cleanUp() {
        shutdownAndAwaitTermination(executor);
    }

    public void addDatum(StreamsDatum datum) {
        try {
            lock.readLock().lock();
            ComponentUtils.offerUntilSuccess(datum, providerQueue);
        } finally {
            lock.readLock().unlock();
        }
    }
}
