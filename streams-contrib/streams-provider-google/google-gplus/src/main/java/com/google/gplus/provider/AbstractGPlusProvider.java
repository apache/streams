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

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.google.gplus.GPlusConfiguration;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.streams.util.api.requests.backoff.impl.ExponentialBackOffStrategy;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provider that creates a GPlus client and will run task that queue data to an outing queue
 */
public abstract class AbstractGPlusProvider implements StreamsProvider {

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractGPlusProvider.class);
    private final static Set<String> SCOPE = new HashSet<String>() {{ add("https://www.googleapis.com/auth/plus.login");}};
    private final static int MAX_BATCH_SIZE = 1000;

    private static final HttpTransport TRANSPORT = new NetHttpTransport();
    private static final JacksonFactory JSON_FACTORY = new JacksonFactory();
    private static final Gson GSON = new Gson();

    private GPlusConfiguration config;
    private ExecutorService executor;
    private BlockingQueue<StreamsDatum> datumQueue;
    private BlockingQueue<Runnable> runnables;
    private AtomicBoolean isComplete;
    private boolean previousPullWasEmpty;

    protected GoogleClientSecrets clientSecrets;
    protected GoogleCredential credential;
    protected Plus plus;

    public AbstractGPlusProvider() {
        Config config = StreamsConfigurator.config.getConfig("gplus");
        this.config = GPlusConfigurator.detectConfiguration(config);
    }

    public AbstractGPlusProvider(GPlusConfiguration config) {
        this.config = config;
    }

    @Override
    public void startStream() {

        BackOffStrategy backOffStrategy = new ExponentialBackOffStrategy(2);
        for(UserInfo user : this.config.getGooglePlusUsers()) {
            if(this.config.getDefaultAfterDate() != null && user.getAfterDate() == null) {
                user.setAfterDate(this.config.getDefaultAfterDate());
            }
            if(this.config.getDefaultBeforeDate() != null && user.getBeforeDate() == null) {
                user.setBeforeDate(this.config.getDefaultBeforeDate());
            }
            this.executor.submit(getDataCollector(backOffStrategy, this.datumQueue, this.plus, user));
        }
        this.executor.shutdown();
    }

    protected abstract Runnable getDataCollector(BackOffStrategy strategy, BlockingQueue<StreamsDatum> queue, Plus plus, UserInfo userInfo);

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

        Preconditions.checkNotNull(config.getOauth().getPathToP12KeyFile());
        Preconditions.checkNotNull(config.getOauth().getAppName());
        Preconditions.checkNotNull(config.getOauth().getServiceAccountEmailAddress());

        try {
            this.plus = createPlusClient();
        } catch (IOException|GeneralSecurityException e) {
            LOGGER.error("Failed to created oauth for GPlus : {}", e);
            throw new RuntimeException(e);
        }
        // GPlus rate limits you to 5 calls per second, so there is not a need to execute more than one
        // collector unless you have multiple oauth tokens
        //TODO make this configurable based on the number of oauth tokens
        this.executor = Executors.newFixedThreadPool(1);
        this.datumQueue = new LinkedBlockingQueue<>(1000);
        this.isComplete = new AtomicBoolean(false);
        this.previousPullWasEmpty = false;
    }

    @VisibleForTesting
    protected Plus createPlusClient() throws IOException, GeneralSecurityException {
        credential = new GoogleCredential.Builder()
                .setJsonFactory(JSON_FACTORY)
                .setTransport(TRANSPORT)
                .setServiceAccountScopes(SCOPE)
                .setServiceAccountId(this.config.getOauth().getServiceAccountEmailAddress())
                .setServiceAccountPrivateKeyFromP12File(new File(this.config.getOauth().getPathToP12KeyFile()))
                .build();
        return new Plus.Builder(TRANSPORT,JSON_FACTORY, credential).setApplicationName(this.config.getOauth().getAppName()).build();
    }

    @Override
    public void cleanUp() {
        ComponentUtils.shutdownExecutor(this.executor, 10, 10);
        this.executor = null;
    }

    public GPlusConfiguration getConfig() {
        return config;
    }

    public void setConfig(GPlusConfiguration config) {
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
        List<UserInfo> gPlusUsers = Lists.newLinkedList();
        for(String userId : userIds) {
            UserInfo user = new UserInfo();
            user.setUserId(userId);
            user.setAfterDate(this.config.getDefaultAfterDate());
            user.setBeforeDate(this.config.getDefaultBeforeDate());
            gPlusUsers.add(user);
        }
        this.config.setGooglePlusUsers(gPlusUsers);
    }

    /**
     * Set and overwrite user into from teh configuration file. Only sets after dater.
     * @param usersAndAfterDates
     */
    public void setUserInfoWithAfterDate(Map<String, DateTime> usersAndAfterDates) {
        List<UserInfo> gPlusUsers = Lists.newLinkedList();
        for(String userId : usersAndAfterDates.keySet()) {
            UserInfo user = new UserInfo();
            user.setUserId(userId);
            user.setAfterDate(usersAndAfterDates.get(userId));
            gPlusUsers.add(user);
        }
        this.config.setGooglePlusUsers(gPlusUsers);
    }

}
