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
import com.google.common.collect.Queues;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.twitter.TwitterUserInformationConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
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

    protected Iterator<Long[]> idsBatches;
    protected Iterator<String[]> screenNameBatches;

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

        Preconditions.checkArgument(idsBatches.hasNext() || screenNameBatches.hasNext());

        LOGGER.info("readCurrent");

        while(idsBatches.hasNext())
            loadBatch(idsBatches.next());

        while(screenNameBatches.hasNext())
            loadBatch(screenNameBatches.next());

        executor.shutdown();
    }

    protected boolean shouldContinuePulling(List<Status> statuses) {
        return (statuses != null) && (statuses.size() > 0);
    }

    private void loadBatch(Long[] ids) {
        Twitter client = getTwitterClient();
        int keepTrying = 0;

        // keep trying to load, give it 5 attempts.
        //while (keepTrying < 10)
        while (keepTrying < 1)
        {
            try
            {
                long[] toQuery = new long[ids.length];
                for(int i = 0; i < ids.length; i++)
                    toQuery[i] = ids[i];

                for (User tStat : client.lookupUsers(toQuery)) {

                    TwitterTimelineProviderTask providerTask = new TwitterTimelineProviderTask(this, client, tStat.getId());
                    executor.submit(providerTask);

                }
                keepTrying = 10;
            }
            catch(TwitterException twitterException) {
                keepTrying += TwitterErrorHandler.handleTwitterError(client, twitterException);
            }
            catch(Exception e) {
                keepTrying += TwitterErrorHandler.handleTwitterError(client, e);
            }
        }
    }

    private void loadBatch(String[] ids) {
        Twitter client = getTwitterClient();
        int keepTrying = 0;

        // keep trying to load, give it 5 attempts.
        //while (keepTrying < 10)
        while (keepTrying < 1)
        {
            try
            {
                for (User tStat : client.lookupUsers(ids)) {

                    TwitterTimelineProviderTask providerTask = new TwitterTimelineProviderTask(this, client, tStat.getId());
                    executor.submit(providerTask);

                }
                keepTrying = 10;
            }
            catch(TwitterException twitterException) {
                keepTrying += TwitterErrorHandler.handleTwitterError(client, twitterException);
            }
            catch(Exception e) {
                keepTrying += TwitterErrorHandler.handleTwitterError(client, e);
            }
        }
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
        running.set(true);
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

        List<String> screenNames = new ArrayList<String>();
        List<String[]> screenNameBatches = new ArrayList<String[]>();

        List<Long> ids = new ArrayList<Long>();
        List<Long[]> idsBatches = new ArrayList<Long[]>();

        for(String s : config.getInfo()) {
            if(s != null)
            {
                String potentialScreenName = s.replaceAll("@", "").trim().toLowerCase();

                // See if it is a long, if it is, add it to the user iD list, if it is not, add it to the
                // screen name list
                try {
                    ids.add(Long.parseLong(potentialScreenName));
                } catch (NumberFormatException e) {
                    screenNames.add(potentialScreenName);
                }

                // Twitter allows for batches up to 100 per request, but you cannot mix types

                if(ids.size() >= 100) {
                    // add the batch
                    idsBatches.add(ids.toArray(new Long[ids.size()]));
                    // reset the Ids
                    ids = new ArrayList<Long>();
                }

                if(screenNames.size() >= 100) {
                    // add the batch
                    screenNameBatches.add(screenNames.toArray(new String[ids.size()]));
                    // reset the Ids
                    screenNames = new ArrayList<String>();
                }
            }
        }


        if(ids.size() > 0)
            idsBatches.add(ids.toArray(new Long[ids.size()]));

        if(screenNames.size() > 0)
            screenNameBatches.add(screenNames.toArray(new String[ids.size()]));

        this.idsBatches = idsBatches.iterator();
        this.screenNameBatches = screenNameBatches.iterator();

    }

    protected Twitter getTwitterClient()
    {
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
}
