/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package com.youtube.provider;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.streams.util.api.requests.backoff.impl.ExponentialBackOffStrategy;
import org.apache.youtube.pojo.YoutubeConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class YoutubeProvider implements StreamsProvider {
    private final static Logger LOGGER = LoggerFactory.getLogger(YoutubeProvider.class);
    private final static int MAX_BATCH_SIZE = 1000;

    /**
     * Define a global instance of the HTTP transport.
     */
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

    /**
     * Define a global instance of the JSON factory.
     */
    public static final JsonFactory JSON_FACTORY = new JacksonFactory();

    private static final int DEFAULT_THREAD_POOL_SIZE = 5;

    private ExecutorService executor;
    private BlockingQueue<StreamsDatum> datumQueue;
    private AtomicBoolean isComplete;
    private boolean previousPullWasEmpty;

    protected YouTube youtube;
    protected YoutubeConfiguration config;

    public YoutubeProvider() {
        Config config = StreamsConfigurator.config.getConfig("youtube");
        this.config = YoutubeConfigurator.detectConfiguration(config);

        Preconditions.checkNotNull(this.config.getApiKey());
    }

    public YoutubeProvider(YoutubeConfiguration config) {
        this.config = config;

        Preconditions.checkNotNull(this.config.getApiKey());
    }

    @Override
    public void startStream() {
        BackOffStrategy backOffStrategy = new ExponentialBackOffStrategy(2);

        for(UserInfo user : this.config.getYoutubeUsers()) {
            if(this.config.getDefaultAfterDate() != null && user.getAfterDate() == null) {
                user.setAfterDate(this.config.getDefaultAfterDate());
            }
            if(this.config.getDefaultBeforeDate() != null && user.getBeforeDate() == null) {
                user.setBeforeDate(this.config.getDefaultBeforeDate());
            }
            this.executor.submit(getDataCollector(backOffStrategy, this.datumQueue, this.youtube, user));
        }

        this.executor.shutdown();
    }

    protected abstract Runnable getDataCollector(BackOffStrategy strategy, BlockingQueue<StreamsDatum> queue, YouTube youtube, UserInfo userInfo);

    @Override
    public StreamsResultSet readCurrent() {
        BlockingQueue<StreamsDatum> batch = new LinkedBlockingQueue<>();
        int batchCount = 0;
        while(!this.datumQueue.isEmpty() && batchCount < MAX_BATCH_SIZE) {
            StreamsDatum datum = ComponentUtils.pollWhileNotEmpty(this.datumQueue);
            if(datum != null) {
                ++batchCount;
                ComponentUtils.offerUntilSuccess(datum, batch);
            }
        }
        boolean pullIsEmpty = batch.isEmpty() && this.datumQueue.isEmpty() &&this.executor.isTerminated();
        this.isComplete.set(this.previousPullWasEmpty && pullIsEmpty);
        this.previousPullWasEmpty = pullIsEmpty;
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
        try {
            this.youtube = createYouTubeClient();
        } catch (IOException |GeneralSecurityException e) {
            LOGGER.error("Failed to created oauth for GPlus : {}", e);
            throw new RuntimeException(e);
        }

        this.executor = Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
        this.datumQueue = new LinkedBlockingQueue<>(1000);
        this.isComplete = new AtomicBoolean(false);
        this.previousPullWasEmpty = false;
    }

    @VisibleForTesting
    protected YouTube createYouTubeClient() throws IOException, GeneralSecurityException {
        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, null).setApplicationName("Streams Application").build();
    }

    @Override
    public void cleanUp() {
        ComponentUtils.shutdownExecutor(this.executor, 10, 10);
        this.executor = null;
    }

    public YoutubeConfiguration getConfig() {
        return config;
    }

    public void setConfig(YoutubeConfiguration config) {
        this.config = config;
    }

    /**
     * Set and overwrite the default before date that was read from the configuration file.
     * @param defaultBeforeDate
     */
    public void setDefaultBeforeDate(DateTime defaultBeforeDate) {
        this.config.setDefaultBeforeDate(defaultBeforeDate);
    }

    /**
     * Set and overwrite the default after date that was read from teh configuration file.
     * @param defaultAfterDate
     */
    public void setDefaultAfterDate(DateTime defaultAfterDate) {
        this.config.setDefaultAfterDate(defaultAfterDate);
    }

    /**
     * Sets and overwrite the user info from the configuaration file.  Uses the defaults before and after dates.
     * @param userIds
     */
    public void setUserInfoWithDefaultDates(Set<String> userIds) {
        List<UserInfo> youtubeUsers = Lists.newLinkedList();

        for(String userId : userIds) {
            UserInfo user = new UserInfo();
            user.setUserId(userId);
            user.setAfterDate(this.config.getDefaultAfterDate());
            user.setBeforeDate(this.config.getDefaultBeforeDate());
            youtubeUsers.add(user);
        }

        this.config.setYoutubeUsers(youtubeUsers);
    }

    /**
     * Set and overwrite user into from teh configuration file. Only sets after dater.
     * @param usersAndAfterDates
     */
    public void setUserInfoWithAfterDate(Map<String, DateTime> usersAndAfterDates) {
        List<UserInfo> youtubeUsers = Lists.newLinkedList();

        for(String userId : usersAndAfterDates.keySet()) {
            UserInfo user = new UserInfo();
            user.setUserId(userId);
            user.setAfterDate(usersAndAfterDates.get(userId));
            youtubeUsers.add(user);
        }

        this.config.setYoutubeUsers(youtubeUsers);
    }
}