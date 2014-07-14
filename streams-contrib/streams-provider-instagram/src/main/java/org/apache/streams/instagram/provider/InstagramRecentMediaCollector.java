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

import com.google.common.collect.Sets;
import org.apache.streams.instagram.InstagramUserInformationConfiguration;
import org.jinstagram.Instagram;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.Set;

/**
 * Executes on all of the Instagram requests to collect the media feed data.
 *
 * If errors/exceptions occur when trying to gather data for a particular user, that user is skipped and the collector
 * move on to the next user.  If a rate limit exception occurs it employs an exponential back off strategy for up to
 * 5 attempts.
 *
 */
public class InstagramRecentMediaCollector implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramRecentMediaCollector.class);
    protected static final int MAX_ATTEMPTS = 5;
    protected static final int SLEEP_SECS = 5; //5 seconds

    protected Queue dataQueue; //exposed for testing
    private InstagramUserInformationConfiguration config;
    private Instagram instagramClient;
    private volatile boolean isCompleted;


    public InstagramRecentMediaCollector(Queue<MediaFeedData> queue, InstagramUserInformationConfiguration config) {
        this.dataQueue = queue;
        this.config = config;
        this.instagramClient = new Instagram(this.config.getClientId());
        this.isCompleted = false;
    }

    /**
     * Set instagram client
     * @param instagramClient
     */
    protected void setInstagramClient(Instagram instagramClient) {
        this.instagramClient = instagramClient;
    }

    /**
     * Gets the user ids from the {@link org.apache.streams.instagram.InstagramUserInformationConfiguration} and
     * converts them to {@link java.lang.Long}
     * @return
     */
    protected Set<Long> getUserIds() {
        Set<Long> userIds = Sets.newHashSet();
        for(String id : config.getUserIds()) {
            try {
                userIds.add(Long.parseLong(id));
            } catch (NumberFormatException nfe) {
                LOGGER.error("Failed to parse user id, {}, to a long : {}", id, nfe.getMessage());
            }
        }
        return userIds;
    }

    /**
     * Determins the course of action to take when Instagram returns an exception to a request.  If it is a rate limit
     * exception, it implements an exponentional back off strategy.  If it is anyother exception, it is logged and
     * rethrown.
     * @param instaExec exception to handle
     * @param attempt number of attempts that have occured to pull this users information
     * @throws InstagramException
     */
    protected void handleInstagramException(InstagramException instaExec, int attempt) throws InstagramException {
        LOGGER.debug("RemainingApiLimitStatus: {}", instaExec.getRemainingLimitStatus());
        if(instaExec.getRemainingLimitStatus() == 0) { //rate limit exception
            long sleepTime = Math.round(Math.pow(SLEEP_SECS, attempt)) * 1000;
            try {
                LOGGER.debug("Encountered rate limit exception, sleeping for {} ms", sleepTime);
                Thread.sleep(sleepTime);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        } else {
            LOGGER.error("Instagram returned an excetpion to the user media request : {}", instaExec.getMessage());
            throw instaExec;
        }
    }

    /**
     * Gets the MediaFeedData for this particular user and adds it to the share queued.
     * @param userId
     */
    private void getUserMedia(Long userId) {
        MediaFeed feed = null;
        int attempts = 0;
        int count = 0;
        do {
            ++attempts;
            try {
                feed = this.instagramClient.getRecentMediaFeed(userId);
                queueData(feed, userId);
                count += feed.getData().size();
                while(feed != null && feed.getPagination() != null && feed.getPagination().hasNextPage()) {
                    feed = this.instagramClient.getRecentMediaNextPage(feed.getPagination());
                    queueData(feed, userId);
                    count += feed.getData().size();
                }
            } catch (InstagramException ie) {
                try {
                    handleInstagramException(ie, attempts);
                } catch (InstagramException ie2) { //not a rate limit exception, ignore user
                    attempts = MAX_ATTEMPTS;
                }
            }
        } while(feed == null && attempts < MAX_ATTEMPTS);
        LOGGER.debug("For user, {}, received {} MediaFeedData", userId, count);
    }

    private void queueData(MediaFeed userFeed, Long userId) {
        if(userFeed == null) {
            LOGGER.error("User id, {}, returned a NULL media feed from instagram.", userId);
        } else {
            for(MediaFeedData data : userFeed.getData()) {
                synchronized (this.dataQueue) { //unnecessary
                    while(!this.dataQueue.offer(data)) {
                        Thread.yield();
                    }
                }
            }
        }
    }

    /**
     *
     * @return true when the collector has queued all of available media feed data for the provided users.
     */
    public boolean isCompleted() {
        return this.isCompleted;
    }

    @Override
    public void run() {
        for(Long userId : getUserIds()) {
            getUserMedia(userId);
        }
        this.isCompleted = true;
    }
}
