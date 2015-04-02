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
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.twitter.TwitterUserInformationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by sblackmon on 11/25/14.
 */
public class TwitterFollowingProvider extends TwitterUserInformationProvider {

    public static final String STREAMS_ID = "TwitterFollowingProvider";
    private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFollowingProvider.class);

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public static final int MAX_NUMBER_WAITING = 10000;

    public TwitterFollowingProvider() {
        super(TwitterConfigurator.detectTwitterUserInformationConfiguration(StreamsConfigurator.config.getConfig("twitter")));
    }

    public TwitterFollowingProvider(TwitterUserInformationConfiguration config) {
        super(config);
    }

    @Override
    public void startStream() {

        running.set(true);

        Preconditions.checkArgument(idsBatches.hasNext() || screenNameBatches.hasNext());

        LOGGER.info("startStream");

        while (idsBatches.hasNext()) {
            submitFollowingThreads(idsBatches.next());
        }
        while (screenNameBatches.hasNext()) {
            submitFollowingThreads(screenNameBatches.next());
        }

        running.set(true);

        executor.shutdown();

    }

    protected void submitFollowingThreads(Long[] ids) {
        Twitter client = getTwitterClient();

        for (int i = 0; i < ids.length; i++) {
            TwitterFollowingProviderTask providerTask = new TwitterFollowingProviderTask(this, client, ids[i], getConfig().getEndpoint());
            executor.submit(providerTask);
        }
    }

    protected void submitFollowingThreads(String[] screenNames) {
        Twitter client = getTwitterClient();

        for (int i = 0; i < screenNames.length; i++) {
            TwitterFollowingProviderTask providerTask = new TwitterFollowingProviderTask(this, client, screenNames[i], getConfig().getEndpoint());
            executor.submit(providerTask);
        }

    }

    @Override
    public StreamsResultSet readCurrent() {

        LOGGER.debug("Providing {} docs", providerQueue.size());

        StreamsResultSet result;

        try {
            lock.writeLock().lock();
            result = new StreamsResultSet(providerQueue);
            result.setCounter(new DatumStatusCounter());
            providerQueue = constructQueue();
        } finally {
            lock.writeLock().unlock();
        }

        if (providerQueue.isEmpty() && executor.isTerminated()) {
            LOGGER.info("Finished.  Cleaning up...");

            running.set(false);

            LOGGER.info("Exiting");
        }

        return result;

    }

    protected Queue<StreamsDatum> constructQueue() {
        return Queues.synchronizedQueue(new LinkedBlockingQueue<StreamsDatum>(MAX_NUMBER_WAITING));
    }

    @Override
    public void prepare(Object o) {
        super.prepare(o);
        Preconditions.checkNotNull(getConfig().getEndpoint());
        Preconditions.checkArgument(getConfig().getEndpoint().equals("friends") || getConfig().getEndpoint().equals("followers"));
        return;
    }
}
