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

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.instagram.InstagramConfiguration;
import org.apache.streams.instagram.User;
import org.apache.streams.instagram.UsersInfo;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.SerializationUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Instagram {@link org.apache.streams.core.StreamsProvider} that provides Instagram data for a group of users
 */
public abstract class InstagramAbstractProvider implements StreamsProvider {

    public static final String STREAMS_ID = "InstagramAbstractProvider";

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramAbstractProvider.class);

    private static final int MAX_BATCH_SIZE = 2000;

    protected InstagramConfiguration config;
    protected Queue<StreamsDatum> dataQueue;
    private ListeningExecutorService executorService;

    private List<ListenableFuture<Object>> futures = new ArrayList<>();

    private AtomicBoolean isCompleted;

    public InstagramAbstractProvider() {
        this.config = new ComponentConfigurator<>(InstagramConfiguration.class)
          .detectConfiguration(StreamsConfigurator.getConfig().getConfig("instagram"));
    }

    public InstagramAbstractProvider(InstagramConfiguration config) {
        this.config = SerializationUtil.cloneBySerialization(config);
    }

    public static ExecutorService newFixedThreadPoolWithQueueSize(int nThreads, int queueSize) {
        return new ThreadPoolExecutor(nThreads, nThreads,
                5000L, TimeUnit.MILLISECONDS,
                new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    @Override
    public void startStream() {
        InstagramDataCollector dataCollector = getInstagramDataCollector();
        this.executorService = MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor());
        ListenableFuture future = this.executorService.submit(dataCollector);
        this.futures.add(future);
        executorService.shutdown();
    }

    /**
     * Return the data collector to use to connect to instagram.
     * @return {@link InstagramDataCollector}
     */
    protected abstract InstagramDataCollector getInstagramDataCollector();


    @Override
    public StreamsResultSet readCurrent() {
        Queue<StreamsDatum> batch = new ConcurrentLinkedQueue<>();
        int count = 0;
        while(!this.dataQueue.isEmpty() && count < MAX_BATCH_SIZE) {
            ComponentUtils.offerUntilSuccess(ComponentUtils.pollWhileNotEmpty(this.dataQueue), batch);
            ++count;
        }
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
    public void prepare(Object configurationObject) {
        this.dataQueue = new ConcurrentLinkedQueue<>();
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
        ensureUsersInfo(this.config).setAuthorizedTokens(new HashSet<>(tokenStrings));
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
        Set<User> users = new HashSet<>();
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

    @Override
    public boolean isRunning() {
        if (dataQueue.isEmpty() && executorService.isTerminated() && Futures.allAsList(futures).isDone()) {
            LOGGER.info("Completed");
            isCompleted.set(true);
            LOGGER.info("Exiting");
        }
        return !isCompleted.get();
    }

}
