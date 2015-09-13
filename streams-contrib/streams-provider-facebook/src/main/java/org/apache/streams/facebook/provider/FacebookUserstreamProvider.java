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

package org.apache.streams.facebook.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import facebook4j.*;
import facebook4j.Post;
import facebook4j.conf.ConfigurationBuilder;
import facebook4j.json.DataObjectFactory;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.facebook.FacebookUserInformationConfiguration;
import org.apache.streams.facebook.FacebookUserstreamConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.ComponentUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.NotImplementedException;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FacebookUserstreamProvider implements StreamsProvider, Serializable {

    public static final String STREAMS_ID = "FacebookUserstreamProvider";

    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookUserstreamProvider.class);

    private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    private static final String ALL_PERMISSIONS = "read_stream";
    private FacebookUserstreamConfiguration configuration;

    private Class klass;
    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    protected volatile Queue<StreamsDatum> providerQueue = new LinkedBlockingQueue<StreamsDatum>();

    public FacebookUserstreamConfiguration getConfig() {
        return configuration;
    }

    public void setConfig(FacebookUserstreamConfiguration config) {
        this.configuration = config;
    }

    protected ListeningExecutorService executor;

    protected DateTime start;
    protected DateTime end;

    protected final AtomicBoolean running = new AtomicBoolean();

    private DatumStatusCounter countersCurrent = new DatumStatusCounter();
    private DatumStatusCounter countersTotal = new DatumStatusCounter();

    protected Facebook client;

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public FacebookUserstreamProvider() {
        Config config = StreamsConfigurator.config.getConfig("facebook");
        FacebookUserInformationConfiguration facebookUserInformationConfiguration;
        try {
            facebookUserInformationConfiguration = mapper.readValue(config.root().render(ConfigRenderOptions.concise()), FacebookUserInformationConfiguration.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }

    public FacebookUserstreamProvider(FacebookUserstreamConfiguration config) {
        this.configuration = config;
    }

    public FacebookUserstreamProvider(Class klass) {
        Config config = StreamsConfigurator.config.getConfig("facebook");
        FacebookUserInformationConfiguration facebookUserInformationConfiguration;
        try {
            facebookUserInformationConfiguration = mapper.readValue(config.root().render(ConfigRenderOptions.concise()), FacebookUserInformationConfiguration.class);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
        this.klass = klass;
    }

    public FacebookUserstreamProvider(FacebookUserstreamConfiguration config, Class klass) {
        this.configuration = config;
        this.klass = klass;
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

        client = getFacebookClient();

        if( configuration.getInfo() != null &&
            configuration.getInfo().size() > 0 ) {
            for( String id : configuration.getInfo()) {
                executor.submit(new FacebookFeedPollingTask(this, id));
            }
            running.set(true);
        } else {
            try {
                String id = client.getMe().getId();
                executor.submit(new FacebookFeedPollingTask(this, id));
                running.set(true);
            } catch (FacebookException e) {
                LOGGER.error(e.getMessage());
                running.set(false);
            }
        }
    }

    public StreamsResultSet readCurrent() {

        StreamsResultSet current;

        synchronized (FacebookUserstreamProvider.class) {
            current = new StreamsResultSet(Queues.newConcurrentLinkedQueue(providerQueue));
            current.setCounter(new DatumStatusCounter());
            current.getCounter().add(countersCurrent);
            countersTotal.add(countersCurrent);
            countersCurrent = new DatumStatusCounter();
            providerQueue.clear();
        }

        return current;

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
        StreamsResultSet result = (StreamsResultSet) providerQueue.iterator();
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

        executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(5, 20));

        Preconditions.checkNotNull(providerQueue);
        Preconditions.checkNotNull(this.klass);
        Preconditions.checkNotNull(configuration.getOauth().getAppId());
        Preconditions.checkNotNull(configuration.getOauth().getAppSecret());
        Preconditions.checkNotNull(configuration.getOauth().getUserAccessToken());

        client = getFacebookClient();

        if( configuration.getInfo() != null &&
            configuration.getInfo().size() > 0 ) {

            List<String> ids = new ArrayList<String>();
            List<String[]> idsBatches = new ArrayList<String[]>();

            for (String s : configuration.getInfo()) {
                if (s != null) {
                    ids.add(s);

                    if (ids.size() >= 100) {
                        // add the batch
                        idsBatches.add(ids.toArray(new String[ids.size()]));
                        // reset the Ids
                        ids = new ArrayList<String>();
                    }

                }
            }
        }
    }

    protected Facebook getFacebookClient() {
        ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true)
                .setOAuthAppId(configuration.getOauth().getAppId())
                .setOAuthAppSecret(configuration.getOauth().getAppSecret())
                .setOAuthAccessToken(configuration.getOauth().getUserAccessToken())
                .setOAuthPermissions(ALL_PERMISSIONS)
                .setJSONStoreEnabled(true);

        FacebookFactory ff = new FacebookFactory(cb.build());
        Facebook facebook = ff.getInstance();

        return facebook;
    }

    @Override
    public void cleanUp() {
        shutdownAndAwaitTermination(executor);
    }

    private class FacebookFeedPollingTask implements Runnable {

        FacebookUserstreamProvider provider;
        Facebook client;
        String id;

        private Set<Post> priorPollResult = Sets.newHashSet();

        public FacebookFeedPollingTask(FacebookUserstreamProvider facebookUserstreamProvider) {
            this.provider = facebookUserstreamProvider;
        }

        public FacebookFeedPollingTask(FacebookUserstreamProvider facebookUserstreamProvider, String id) {
            this.provider = facebookUserstreamProvider;
            this.client = provider.client;
            this.id = id;
        }
        @Override
        public void run() {
            while (provider.isRunning()) {
                ResponseList<Post> postResponseList;
                try {
                    postResponseList = client.getFeed(id);

                    Set<Post> update = Sets.newHashSet(postResponseList);
                    Set<Post> repeats = Sets.intersection(priorPollResult, Sets.newHashSet(update));
                    Set<Post> entrySet = Sets.difference(update, repeats);
                    LOGGER.debug(this.id + " response: " + update.size() + " previous: " + repeats.size() + " new: " + entrySet.size());
                    for (Post item : entrySet) {
                        String json = DataObjectFactory.getRawJSON(item);
                        org.apache.streams.facebook.Post post = mapper.readValue(json, org.apache.streams.facebook.Post.class);
                        try {
                            lock.readLock().lock();
                            ComponentUtils.offerUntilSuccess(new StreamsDatum(post), providerQueue);
                            countersCurrent.incrementAttempt();
                        } finally {
                            lock.readLock().unlock();
                        }
                    }
                    priorPollResult = update;
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    try {
                        Thread.sleep(configuration.getPollIntervalMillis());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        }
    }
}
