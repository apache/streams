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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.TwitterFollowingConfiguration;
import org.apache.streams.twitter.TwitterUserInformationConfiguration;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.util.ComponentUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

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

import static java.util.concurrent.Executors.newSingleThreadExecutor;

public class TwitterUserInformationProvider implements StreamsProvider, Serializable
{

    public static final String STREAMS_ID = "TwitterUserInformationProvider";

    private static ObjectMapper MAPPER = new StreamsJacksonMapper(Lists.newArrayList(TwitterDateTimeFormat.TWITTER_FORMAT));

    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterUserInformationProvider.class);

    public static final int MAX_NUMBER_WAITING = 1000;

    private TwitterUserInformationConfiguration config;

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    protected volatile Queue<StreamsDatum> providerQueue;

    public TwitterUserInformationConfiguration getConfig()              { return config; }

    public void setConfig(TwitterUserInformationConfiguration config)   { this.config = config; }

    protected Iterator<Long[]> idsBatches;
    protected Iterator<String[]> screenNameBatches;

    protected ListeningExecutorService executor;

    protected DateTime start;
    protected DateTime end;

    protected final AtomicBoolean running = new AtomicBoolean();

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public TwitterUserInformationProvider() {
        this.config = new ComponentConfigurator<>(TwitterUserInformationConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter"));
    }

    public TwitterUserInformationProvider(TwitterUserInformationConfiguration config) {
        this.config = config;
    }

    public Queue<StreamsDatum> getProviderQueue() {
        return this.providerQueue;
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    @Override
    public void startStream() {

        Preconditions.checkArgument(idsBatches.hasNext() || screenNameBatches.hasNext());

        LOGGER.info("{}{} - startStream", idsBatches, screenNameBatches);

        while(idsBatches.hasNext())
            loadBatch(idsBatches.next());

        while(screenNameBatches.hasNext())
            loadBatch(screenNameBatches.next());

        running.set(true);

        executor.shutdown();
    }

    protected void loadBatch(Long[] ids) {
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

                for (twitter4j.User tUser : client.lookupUsers(toQuery)) {
                    String json = DataObjectFactory.getRawJSON(tUser);
                    try {
                        User user = MAPPER.readValue(json, org.apache.streams.twitter.pojo.User.class);
                        ComponentUtils.offerUntilSuccess(new StreamsDatum(user), providerQueue);
                    } catch(Exception exception) {
                        LOGGER.warn("Failed to read document as User ", tUser);
                    }
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

    protected void loadBatch(String[] ids) {
        Twitter client = getTwitterClient();
        int keepTrying = 0;

        // keep trying to load, give it 5 attempts.
        //while (keepTrying < 10)
        while (keepTrying < 1)
        {
            try
            {
                for (twitter4j.User tUser : client.lookupUsers(ids)) {
                    String json = DataObjectFactory.getRawJSON(tUser);
                    try {
                        User user = MAPPER.readValue(json, org.apache.streams.twitter.pojo.User.class);
                        ComponentUtils.offerUntilSuccess(new StreamsDatum(user), providerQueue);
                    } catch(Exception exception) {
                        LOGGER.warn("Failed to read document as User ", tUser);
                    }
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

        LOGGER.info("{}{} - readCurrent", idsBatches, screenNameBatches);

        StreamsResultSet result;

        try {
            lock.writeLock().lock();
            result = new StreamsResultSet(providerQueue);
            result.setCounter(new DatumStatusCounter());
            providerQueue = constructQueue();
            LOGGER.info("{}{} - providing {} docs", idsBatches, screenNameBatches, result.size());
        } finally {
            lock.writeLock().unlock();
        }

        if( providerQueue.isEmpty() && executor.isTerminated()) {
            LOGGER.info("{}{} - completed", idsBatches, screenNameBatches);

            running.set(false);
        }

        return result;

    }

    protected Queue<StreamsDatum> constructQueue() {
        return new LinkedBlockingQueue<StreamsDatum>();
    }

    public StreamsResultSet readNew(BigInteger sequence) {
        LOGGER.debug("{} readNew", STREAMS_ID);
        throw new NotImplementedException();
    }

    public StreamsResultSet readRange(DateTime start, DateTime end) {
        LOGGER.debug("{} readRange", STREAMS_ID);
        this.start = start;
        this.end = end;
        readCurrent();
        StreamsResultSet result = (StreamsResultSet)providerQueue.iterator();
        return result;
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

        if( o instanceof TwitterFollowingConfiguration )
            config = (TwitterUserInformationConfiguration) o;

        try {
            lock.writeLock().lock();
            providerQueue = constructQueue();
        } finally {
            lock.writeLock().unlock();
        }

        Preconditions.checkNotNull(providerQueue);
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
                } catch (Exception e) {
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

        if(ids.size() + screenNames.size() > 0)
            executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(5, (ids.size() + screenNames.size())));
        else
            executor = MoreExecutors.listeningDecorator(newSingleThreadExecutor());

        this.idsBatches = idsBatches.iterator();
        this.screenNameBatches = screenNameBatches.iterator();
    }

    protected Twitter getTwitterClient()
    {
        String baseUrl = TwitterProviderUtil.baseUrl(config);

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
