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

package org.apache.streams.instagram.provider;

import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.instagram.InstagramConfigurator;
import org.apache.streams.instagram.InstagramUserInformationConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.jinstagram.Instagram;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class InstagramTimelineProvider implements StreamsProvider, Serializable {

    public final static String STREAMS_ID = "InstagramTimelineProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(InstagramTimelineProvider.class);
    public static final int MAX_NUMBER_WAITING = 10000;

    private static StreamsJacksonMapper mapper = StreamsJacksonMapper.getInstance();
    private InstagramUserInformationConfiguration config;

    private Class klass;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public InstagramUserInformationConfiguration getConfig() {
        return config;
    }

    public void setConfig(InstagramUserInformationConfiguration config) {
        this.config = config;
    }

    protected Iterator<Long[]> idsBatches;
    protected Iterator<String[]> screenNameBatches;

    protected volatile Queue<StreamsDatum> providerQueue;

    protected int idsCount;
    protected Instagram client;


    protected ExecutorService executor;

    protected DateTime start;
    protected DateTime end;

    protected final AtomicBoolean running = new AtomicBoolean();

    private static ExecutorService getExecutor() {
        return Executors.newSingleThreadExecutor();
    }

    public InstagramTimelineProvider() {
        Config config = StreamsConfigurator.config.getConfig("instagram");
        this.config = InstagramConfigurator.detectInstagramUserInformationConfiguration(config);
    }

    public InstagramTimelineProvider(InstagramUserInformationConfiguration config) {
        this.config = config;
    }

    public InstagramTimelineProvider(Class klass) {
        Config config = StreamsConfigurator.config.getConfig("instagram");
        this.config = InstagramConfigurator.detectInstagramUserInformationConfiguration(config);
        this.klass = klass;
    }

    public InstagramTimelineProvider(InstagramUserInformationConfiguration config, Class klass) {
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

    private void loadBatch(Long[] ids) {

        // twitter4j implementation below - replace with jInstagram

//        Twitter client = getTwitterClient();
//        int keepTrying = 0;
//
//        // keep trying to load, give it 5 attempts.
//        //while (keepTrying < 10)
//        while (keepTrying < 1)
//        {
//            try
//            {
//                long[] toQuery = new long[ids.length];
//                for(int i = 0; i < ids.length; i++)
//                    toQuery[i] = ids[i];
//
//                for (User tStat : client.lookupUsers(toQuery)) {
//
//                    TwitterTimelineProviderTask providerTask = new TwitterTimelineProviderTask(this, client, tStat.getId());
//                    executor.submit(providerTask);
//
//                }
//                keepTrying = 10;
//            }
//            catch(TwitterException twitterException) {
//                keepTrying += TwitterErrorHandler.handleTwitterError(client, twitterException);
//            }
//            catch(Exception e) {
//                keepTrying += TwitterErrorHandler.handleTwitterError(client, e);
//            }
//        }
    }

    private void loadBatch(String[] ids) {

        // twitter4j implementation below - replace with jInstagram
//
//        Twitter client = getTwitterClient();
//        int keepTrying = 0;
//
//        // keep trying to load, give it 5 attempts.
//        //while (keepTrying < 10)
//        while (keepTrying < 1)
//        {
//            try
//            {
//                for (User tStat : client.lookupUsers(ids)) {
//
//                    TwitterTimelineProviderTask providerTask = new TwitterTimelineProviderTask(this, client, tStat.getId());
//                    executor.submit(providerTask);
//
//                }
//                keepTrying = 10;
//            }
//            catch(TwitterException twitterException) {
//                keepTrying += TwitterErrorHandler.handleTwitterError(client, twitterException);
//            }
//            catch(Exception e) {
//                keepTrying += TwitterErrorHandler.handleTwitterError(client, e);
//            }
//        }
    }

    public class InstagramTimelineProviderTask implements Runnable {

        // twitter4j implementation below - replace with jInstagram

        private final Logger LOGGER = LoggerFactory.getLogger(InstagramTimelineProvider.class);

        private InstagramTimelineProvider provider;
        private Instagram client;
        private Long id;

        public InstagramTimelineProviderTask(InstagramTimelineProvider provider, Instagram client, Long id) {
            this.provider = provider;
            this.client = client;
            this.id = id;
        }

        @Override
        public void run() {

            // twitter4j implementation below - replace with jInstagram

//            Paging paging = new Paging(1, 200);
//            List<Status> statuses = null;
//            boolean KeepGoing = true;
//            boolean hadFailure = false;
//
//            do
//            {
//                int keepTrying = 0;
//
//                // keep trying to load, give it 5 attempts.
//                //This value was chosen because it seemed like a reasonable number of times
//                //to retry capturing a timeline given the sorts of errors that could potentially
//                //occur (network timeout/interruption, faulty client, etc.)
//                while (keepTrying < 5)
//                {
//
//                    try
//                    {
//                        statuses = client.getUserTimeline(id, paging);
//
//                        for (Status tStat : statuses)
//                        {
//                            String json = TwitterObjectFactory.getRawJSON(tStat);
//
//                            try {
//                                provider.lock.readLock().lock();
//                                ComponentUtils.offerUntilSuccess(new StreamsDatum(json), provider.providerQueue);
//                            } finally {
//                                provider.lock.readLock().unlock();
//                            }
//                        }
//
//                        paging.setPage(paging.getPage() + 1);
//
//                        keepTrying = 10;
//                    }
//                    catch(TwitterException twitterException) {
//                        keepTrying += TwitterErrorHandler.handleTwitterError(client, twitterException);
//                    }
//                    catch(Exception e) {
//                        keepTrying += TwitterErrorHandler.handleTwitterError(client, e);
//                    }
//                }
//            }
//            while (provider.shouldContinuePulling(statuses));

            LOGGER.info(id + " Thread Finished");

        }

    }

    private Map<Long, Long> userPullInfo;

    protected boolean shouldContinuePulling(List<MediaFeedData> statuses) {
        return (statuses != null) && (statuses.size() > 0);
    }

    private void sleep()
    {
        Thread.yield();
        try {
            // wait one tenth of a millisecond
            Thread.yield();
            Thread.sleep(1);
            Thread.yield();
        }
        catch(IllegalArgumentException e) {
            // passing in static values, this will never happen
        }
        catch(InterruptedException e) {
            // noOp, there must have been an issue sleeping
        }
        Thread.yield();
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
        Preconditions.checkNotNull(config.getAccessToken());

        //idsCount = config.getFollow().size();

        client = getInstagramClient();
    }

    protected Instagram getInstagramClient()
    {
        // twitter4j -> jInstagram
//        String baseUrl = "https://api.instagram.com:443/1.1/";
//
//        ConfigurationBuilder builder = new ConfigurationBuilder()
//                .setOAuthConsumerKey(config.getOauth().getConsumerKey())
//                .setOAuthConsumerSecret(config.getOauth().getConsumerSecret())
//                .setOAuthAccessToken(config.getOauth().getAccessToken())
//                .setOAuthAccessTokenSecret(config.getOauth().getAccessTokenSecret())
//                .setIncludeEntitiesEnabled(includeEntitiesEnabled)
//                .setJSONStoreEnabled(jsonStoreEnabled)
//                .setAsyncNumThreads(3)
//                .setRestBaseURL(baseUrl)
//                .setIncludeMyRetweetEnabled(Boolean.TRUE)
//                .setPrettyDebugEnabled(Boolean.TRUE);
//
//        return new InstagramFactory(builder.build()).getInstance();
        return null;
    }

    @Override
    public void cleanUp() {
        shutdownAndAwaitTermination(executor);
    }
}
