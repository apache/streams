/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance *
http://www.apache.org/licenses/LICENSE-2.0 *
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License. */
package org.apache.streams.instagram.provider;

import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.instagram.InstagramConfiguration;
import org.apache.streams.instagram.InstagramConfigurator;
import org.apache.streams.instagram.User;
import org.apache.streams.instagram.UsersInfo;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.SerializationUtil;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Instagram {@link org.apache.streams.core.StreamsProvider} that provides Instagram data for a group of users
 */
public abstract class InstagramAbstractProvider implements StreamsProvider {

    private static final int MAX_BATCH_SIZE = 2000;

    protected InstagramConfiguration config;
    private InstagramDataCollector dataCollector;
    protected Queue<StreamsDatum> dataQueue; //exposed for testing
    private ExecutorService executorService;
    private AtomicBoolean isCompleted;

    public InstagramAbstractProvider() {
        this.config = InstagramConfigurator.detectInstagramConfiguration(StreamsConfigurator.config.getConfig("instagram"));
    }

    public InstagramAbstractProvider(InstagramConfiguration config) {
        this.config = SerializationUtil.cloneBySerialization(config);
    }

    @Override
    public void startStream() {
        this.dataCollector = getInstagramDataCollector();
        this.executorService = Executors.newSingleThreadExecutor();
        this.executorService.submit(this.dataCollector);
    }

    /**
     * Return the data collector to use to connect to instagram.
     * @return
     */
    protected abstract InstagramDataCollector getInstagramDataCollector();


    @Override
    public StreamsResultSet readCurrent() {
        Queue<StreamsDatum> batch = Queues.newConcurrentLinkedQueue();
        int count = 0;
        while(!this.dataQueue.isEmpty() && count < MAX_BATCH_SIZE) {
            ComponentUtils.offerUntilSuccess(ComponentUtils.pollWhileNotEmpty(this.dataQueue), batch);
            ++count;
        }
        this.isCompleted.set(batch.size() == 0 && this.dataQueue.isEmpty() && this.dataCollector.isCompleted());
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
        return !this.isCompleted.get();
    }

    @Override
    public void prepare(Object configurationObject) {
        this.dataQueue = Queues.newConcurrentLinkedQueue();
        this.isCompleted = new AtomicBoolean(false);
    }

    @Override
    public void cleanUp() {
        try {
            ComponentUtils.shutdownExecutor(this.executorService, 5, 5);
        } finally {
            this.executorService = null;
        }
    }

    /**
     * Add default start and stop points if necessary.
     */
    private void updateUserInfoList() {
        UsersInfo usersInfo = this.config.getUsersInfo();
        if(usersInfo.getDefaultAfterDate() == null && usersInfo.getDefaultBeforeDate() == null) {
            return;
        }
        DateTime defaultAfterDate = usersInfo.getDefaultAfterDate();
        DateTime defaultBeforeDate = usersInfo.getDefaultBeforeDate();
        for(User user : usersInfo.getUsers()) {
            if(defaultAfterDate != null && user.getAfterDate() == null) {
                user.setAfterDate(defaultAfterDate);
            }
            if(defaultBeforeDate != null && user.getBeforeDate() == null) {
                user.setBeforeDate(defaultBeforeDate);
            }
        }
    }

    /**
     * Overrides the client id in the configuration.
     * @param clientId client id to use
     */
    public void setInstagramClientId(String clientId) {
        this.config.setClientId(clientId);
    }

    /**
     * Overrides authroized user tokens in the configuration.
     * @param tokenStrings
     */
    public void setAuthorizedUserTokens(Collection<String> tokenStrings) {
        ensureUsersInfo(this.config).setAuthorizedTokens(Sets.newHashSet(tokenStrings));
    }

    /**
     * Overrides the default before date in the configuration
     * @param beforeDate
     */
    public void setDefaultBeforeDate(DateTime beforeDate) {
        ensureUsersInfo(this.config).setDefaultBeforeDate(beforeDate);
    }

    /**
     * Overrides the default after date in the configuration
     * @param afterDate
     */
    public void setDefaultAfterDate(DateTime afterDate) {
        ensureUsersInfo(this.config).setDefaultAfterDate(afterDate);
    }

    /**
     * Overrides the users in the configuration and sets the after date for each user. A NULL DateTime implies
     * pull data from as early as possible.  If default before or after DateTimes are set, they will applied to all
     * NULL DateTimes.
     * @param usersWithAfterDate instagram user id mapped to BeforeDate time
     */
    public void setUsersWithAfterDate(Map<String, DateTime> usersWithAfterDate) {
        Set<User> users = Sets.newHashSet();
        for(String userId : usersWithAfterDate.keySet()) {
            User user = new User();
            user.setUserId(userId);
            user.setAfterDate(usersWithAfterDate.get(userId));
            users.add(user);
        }
        ensureUsersInfo(this.config).setUsers(users);
    }

    private UsersInfo ensureUsersInfo(InstagramConfiguration config) {
        UsersInfo usersInfo = config.getUsersInfo();
        if(usersInfo == null) {
            usersInfo = new UsersInfo();
            config.setUsersInfo(usersInfo);
        }
        return usersInfo;
    }

}
