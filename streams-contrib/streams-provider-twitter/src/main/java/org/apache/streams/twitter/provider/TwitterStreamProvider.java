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

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.*;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.BasicAuth;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.typesafe.config.Config;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.*;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.util.ComponentUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterStreamProvider implements StreamsProvider, Serializable, DatumStatusCountable {

    public final static String STREAMS_ID = "TwitterStreamProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterStreamProvider.class);

    private TwitterStreamConfiguration config;

    public TwitterStreamConfiguration getConfig() {
        return config;
    }

    public void setConfig(TwitterStreamConfiguration config) {
        this.config = config;
    }

    protected volatile Queue<Future<List<StreamsDatum>>> providerQueue;

    protected Hosts hosebirdHosts;
    protected Authentication auth;
    protected StreamingEndpoint endpoint;
    protected BasicClient client;
    protected AtomicBoolean running = new AtomicBoolean(false);
    protected TwitterStreamProcessor processor = new TwitterStreamProcessor(this);
    private DatumStatusCounter countersCurrent = new DatumStatusCounter();
    private DatumStatusCounter countersTotal = new DatumStatusCounter();

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public TwitterStreamProvider() {
        Config config = StreamsConfigurator.config.getConfig("twitter");
        this.config = TwitterStreamConfigurator.detectConfiguration(config);
    }

    public TwitterStreamProvider(TwitterStreamConfiguration config) {
        this.config = config;
    }

    @Override
    public void startStream() {
        client.connect();
        running.set(true);
    }

    @Override
    public synchronized StreamsResultSet readCurrent() {

        StreamsResultSet current;

        synchronized( TwitterStreamProvider.class ) {
            Queue<StreamsDatum> drain = Queues.newLinkedBlockingDeque();
            drainTo(drain);
            current = new StreamsResultSet(drain);
            current.setCounter(new DatumStatusCounter());
            current.getCounter().add(countersCurrent);
            countersTotal.add(countersCurrent);
            countersCurrent = new DatumStatusCounter();
        }

        return current;
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        throw new NotImplementedException();
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end)  {
        throw new NotImplementedException();
    }

    @Override
    public boolean isRunning() {
        return this.running.get() && !client.isDone();
    }

    @Override
    public void prepare(Object o) {

        Preconditions.checkNotNull(config.getEndpoint());

        if(config.getEndpoint().equals("userstream") ) {

            hosebirdHosts = new HttpHosts(Constants.USERSTREAM_HOST);

            UserstreamEndpoint userstreamEndpoint = new UserstreamEndpoint();
            userstreamEndpoint.withFollowings(true);
            userstreamEndpoint.withUser(false);
            userstreamEndpoint.allReplies(false);
            endpoint = userstreamEndpoint;
        }
        else if(config.getEndpoint().equals("sample") ) {

            hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);

            boolean track = config.getTrack() != null && !config.getTrack().isEmpty();
            boolean follow = config.getFollow() != null && !config.getFollow().isEmpty();

            if( track || follow ) {
                LOGGER.debug("***\tPRESENT\t***");
                StatusesFilterEndpoint statusesFilterEndpoint = new StatusesFilterEndpoint();
                if( track ) {
                    statusesFilterEndpoint.trackTerms(config.getTrack());
                }
                if( follow ) {
                    statusesFilterEndpoint.followings(config.getFollow());
                }
                this.endpoint = statusesFilterEndpoint;
            } else {
                endpoint = new StatusesSampleEndpoint();
            }

        }
        else if( config.getEndpoint().endsWith("firehose")) {
            hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
            endpoint = new StatusesFirehoseEndpoint();
        } else {
            LOGGER.error("NO ENDPOINT RESOLVED");
            return;
        }

        if( config.getBasicauth() != null ) {

            Preconditions.checkNotNull(config.getBasicauth().getUsername());
            Preconditions.checkNotNull(config.getBasicauth().getPassword());

            auth = new BasicAuth(
                    config.getBasicauth().getUsername(),
                    config.getBasicauth().getPassword()
            );

        } else if( config.getOauth() != null ) {

            Preconditions.checkNotNull(config.getOauth().getConsumerKey());
            Preconditions.checkNotNull(config.getOauth().getConsumerSecret());
            Preconditions.checkNotNull(config.getOauth().getAccessToken());
            Preconditions.checkNotNull(config.getOauth().getAccessTokenSecret());

            auth = new OAuth1(config.getOauth().getConsumerKey(),
                    config.getOauth().getConsumerSecret(),
                    config.getOauth().getAccessToken(),
                    config.getOauth().getAccessTokenSecret());

        } else {
            LOGGER.error("NO AUTH RESOLVED");
            return;
        }

        LOGGER.debug("host={}\tendpoint={}\taut={}", new Object[] {hosebirdHosts,endpoint,auth});

        providerQueue = new LinkedBlockingQueue<Future<List<StreamsDatum>>>(1000);

        client = new ClientBuilder()
            .name("apache/streams/streams-contrib/streams-provider-twitter")
            .hosts(hosebirdHosts)
            .endpoint(endpoint)
            .authentication(auth)
            .connectionTimeout(1200000)
            .processor(processor)
            .build();

    }

    @Override
    public void cleanUp() {
        this.client.stop();
        this.processor.cleanUp();
        this.running.set(true);
    }

    @Override
    public DatumStatusCounter getDatumStatusCounter() {
        return countersTotal;
    }

    protected boolean addDatum(Future<List<StreamsDatum>> future) {
        ComponentUtils.offerUntilSuccess(future, providerQueue);
        return true;
    }

    protected void drainTo(Queue<StreamsDatum> drain) {
        while(!providerQueue.isEmpty()) {
            for(StreamsDatum datum : pollForDatum()) {
                ComponentUtils.offerUntilSuccess(datum, drain);
            }
        }
    }

    protected List<StreamsDatum> pollForDatum()  {
        try {
            return providerQueue.poll().get();
        } catch (InterruptedException e) {
            LOGGER.warn("Interrupted while waiting for future.  Initiate shutdown.");
            this.cleanUp();
            Thread.currentThread().interrupt();
            return Lists.newArrayList();
        } catch (ExecutionException e) {
            LOGGER.warn("Error getting tweet from future");
            return Lists.newArrayList();
        }
    }
}
