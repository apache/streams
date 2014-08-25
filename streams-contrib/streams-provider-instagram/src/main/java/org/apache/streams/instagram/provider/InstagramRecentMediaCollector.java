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

import com.google.common.annotations.VisibleForTesting;
import org.apache.streams.instagram.InstagramConfiguration;
import org.apache.streams.instagram.User;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.streams.util.api.requests.backoff.impl.ExponentialBackOffStrategy;
import org.apache.streams.util.oauth.tokens.tokenmanager.SimpleTokenManager;
import org.apache.streams.util.oauth.tokens.tokenmanager.impl.BasicTokenManger;
import org.jinstagram.Instagram;
import org.jinstagram.entity.common.Pagination;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramBadRequestException;
import org.jinstagram.exceptions.InstagramRateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Executes on all of the Instagram requests to collect the media feed data.
 * <p/>
 * If errors/exceptions occur when trying to gather data for a particular user, that user is skipped and the collector
 * move on to the next user.  If a rate limit exception occurs it employs an exponential back off strategy.
 */
public class InstagramRecentMediaCollector implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramRecentMediaCollector.class);
    protected static final int MAX_ATTEMPTS = 5;
    protected static final int SLEEP_SECS = 5; //5 seconds

    protected Queue<MediaFeedData> dataQueue; //exposed for testing
    private InstagramConfiguration config;
    private AtomicBoolean isCompleted;
    private SimpleTokenManager<InstagramOauthToken> tokenManger;
    private int consecutiveErrorCount;
    private BackOffStrategy backOffStrategy;
    private Instagram instagram;


    public InstagramRecentMediaCollector(Queue<MediaFeedData> queue, InstagramConfiguration config) {
        this.dataQueue = queue;
        this.config = config;
        this.isCompleted = new AtomicBoolean(false);
        this.tokenManger = new BasicTokenManger<InstagramOauthToken>();
        for (String tokens : this.config.getUsersInfo().getAuthorizedTokens()) {
            this.tokenManger.addTokenToPool(new InstagramOauthToken(tokens));
        }
        this.consecutiveErrorCount = 0;
        this.backOffStrategy = new ExponentialBackOffStrategy(2);
        this.instagram = new Instagram(this.config.getClientId());
    }


    /**
     * If there are authorized tokens available, it sets a new token for the client and returns
     * the client.  If there are no available tokens, it simply returns the client that was
     * initialized in the constructor with client id.
     * @return
     */
    @VisibleForTesting
    protected Instagram getNextInstagramClient() {
        if(this.tokenManger.numAvailableTokens() > 0) {
            this.instagram.setAccessToken(this.tokenManger.getNextAvailableToken());
        }
        return this.instagram;
    }

    private void queueData(MediaFeed userFeed, String userId) {
        if (userFeed == null) {
            LOGGER.warn("User id, {}, returned a NULL media feed from instagram.", userId);
        } else {
            for (MediaFeedData data : userFeed.getData()) {
                ComponentUtils.offerUntilSuccess(data, this.dataQueue);
            }
        }
    }

    /**
     * @return true when the collector has queued all of the available media feed data for the provided users.
     */
    public boolean isCompleted() {
        return this.isCompleted.get();
    }

    @Override
    public void run() {
        try {
            for (User user : this.config.getUsersInfo().getUsers()) {
                collectMediaFeed(user);
            }
        } catch (Exception e) {
            LOGGER.error("Shutting down InstagramCollector. Exception occured: {}", e.getMessage());
        }
        this.isCompleted.set(true);
    }

    /**
     * Pull Recement Media for a user and queues the resulting data. Will try a single call 5 times before failing and
     * moving on to the next call or returning.
     * @param user
     * @throws Exception
     */
    @VisibleForTesting
    protected void collectMediaFeed(User user) throws Exception {
        Pagination pagination = null;
        do {
            int attempts = 0;
            boolean succesfullDataPull = false;
            while (!succesfullDataPull && attempts < MAX_ATTEMPTS) {
                ++attempts;
                MediaFeed feed = null;
                try {
                    if (pagination == null) {
                        feed = getNextInstagramClient().getRecentMediaFeed(Long.valueOf(user.getUserId()),
                                0,
                                null,
                                null,
                                user.getBeforeDate() == null ? null : user.getBeforeDate().toDate(),
                                user.getAfterDate() == null ? null : user.getAfterDate().toDate());
                    } else {
                        feed = getNextInstagramClient().getRecentMediaNextPage(pagination);
                    }
                } catch (Exception e) {
                    if(e instanceof InstagramRateLimitException) {
                        LOGGER.warn("Received rate limit exception from Instagram, backing off. : {}", e);
                        this.backOffStrategy.backOff();
                    } else if(e instanceof InstagramBadRequestException) {
                        LOGGER.error("Received Bad Requests exception form Instagram: {}", e);
                        attempts = MAX_ATTEMPTS; //don't repeat bad requests.
                        ++this.consecutiveErrorCount;
                    } else {
                        LOGGER.error("Received Expection while attempting to poll Instagram: {}", e);
                        ++this.consecutiveErrorCount;
                    }
                    if(this.consecutiveErrorCount > Math.max(this.tokenManger.numAvailableTokens(), MAX_ATTEMPTS*2)) {
                        throw new Exception("InstagramCollector failed to successfully connect to instagram on "+this.consecutiveErrorCount+" attempts.");
                    }
                }
                if(succesfullDataPull = feed != null) {
                    this.consecutiveErrorCount = 0;
                    this.backOffStrategy.reset();
                    pagination = feed.getPagination();
                    queueData(feed, user.getUserId());
                }
            }
            if(!succesfullDataPull) {
                LOGGER.error("Failed to get data from instagram for user id, {}, skipping user.", user.getUserId());
            }
        } while (pagination != null && pagination.hasNextPage());
    }


}
