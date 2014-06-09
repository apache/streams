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

package com.google.gplus.provider;

import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.google.gplus.GPlusConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.*;

/**
 * Created by sblackmon on 12/10/13.
 */
public class GPlusProvider implements StreamsProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(GPlusProvider.class);

    private GPlusConfiguration config;

    private Class klass;

    public GPlusConfiguration getConfig() {
        return config;
    }

    public void setConfig(GPlusConfiguration config) {
        this.config = config;
    }

    protected BlockingQueue inQueue = new LinkedBlockingQueue<String>(10000);

    protected volatile Queue<StreamsDatum> providerQueue = new ConcurrentLinkedQueue<StreamsDatum>();

    public BlockingQueue<Object> getInQueue() {
        return inQueue;
    }

    private static final HttpTransport TRANSPORT = new NetHttpTransport();
    private static final JacksonFactory JSON_FACTORY = new JacksonFactory();
    private static final Gson GSON = new Gson();

    protected GoogleClientSecrets clientSecrets;
    protected GoogleCredential credential;
    protected Plus plus;

    protected ListeningExecutorService executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(5, 20));

    ListenableFuture providerTaskComplete;

    private static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    public GPlusProvider() {
        Config config = StreamsConfigurator.config.getConfig("gplus");
        this.config = GPlusConfigurator.detectConfiguration(config);
    }

    public GPlusProvider(GPlusConfiguration config) {
        this.config = config;
    }

    public GPlusProvider(Class klass) {
        Config config = StreamsConfigurator.config.getConfig("gplus");
        this.config = GPlusConfigurator.detectConfiguration(config);
        this.klass = klass;
    }

    public GPlusProvider(GPlusConfiguration config, Class klass) {
        this.config = config;
        this.klass = klass;
    }

    @Override
    public void startStream() {

        providerTaskComplete = executor.submit(new GPlusHistoryProviderTask(this, "me", "public"));

        for (int i = 0; i < 1; i++) {
            new Thread(new GPlusEventProcessor(inQueue, providerQueue, klass));
        }

    }

    @Override
    public StreamsResultSet readCurrent() {

        startStream();

        while( !providerTaskComplete.isDone()) {
            try {
                Thread.sleep(new Random().nextInt(100));
            } catch (InterruptedException e) { }
        }

        return new StreamsResultSet(providerQueue);

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
    public void prepare(Object configurationObject) {

        Preconditions.checkNotNull(this.klass);

        Preconditions.checkNotNull(config.getOauth().getConsumerKey());
        Preconditions.checkNotNull(config.getOauth().getConsumerSecret());
        Preconditions.checkNotNull(config.getOauth().getAccessToken());
        Preconditions.checkNotNull(config.getOauth().getAccessTokenSecret());

        try {
            credential = new GoogleCredential.Builder()
                    .setJsonFactory(JSON_FACTORY)
                    .setTransport(TRANSPORT)
                    .setClientSecrets(config.getOauth().getConsumerKey(), config.getOauth().getConsumerSecret()).build()
                    .setFromTokenResponse(JSON_FACTORY.fromString(
                            config.getOauth().getAccessToken(), GoogleTokenResponse.class));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void cleanUp() {
        for (int i = 0; i < 1; i++) {
            inQueue.add(GPlusEventProcessor.TERMINATE);
        }

        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
