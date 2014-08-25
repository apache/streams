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
import org.apache.streams.core.StreamsDatum;
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

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Executes on all of the Instagram requests to collect the Instagram data.
 * <p/>
 * If errors/exceptions occur when trying to gather data for a particular user, that user is skipped and the collector
 * move on to the next user.  If a rate limit exception occurs it employs an exponential back off strategy.
 */
public abstract class InstagramDataCollector<T> implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramDataCollector.class);

    protected Queue<StreamsDatum> dataQueue; //exposed for testing
    private InstagramConfiguration config;
    private AtomicBoolean isCompleted;
    private SimpleTokenManager<InstagramOauthToken> tokenManger;
    protected int consecutiveErrorCount;
    protected BackOffStrategy backOffStrategy;
    private Instagram instagram;


    public InstagramDataCollector(Queue<StreamsDatum> queue, InstagramConfiguration config) {
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
    protected Instagram getNextInstagramClient() {
        if(this.tokenManger.numAvailableTokens() > 0) {
            this.instagram.setAccessToken(this.tokenManger.getNextAvailableToken());
        }
        return this.instagram;
    }

    /**
     * Return the number of available tokens for this data collector
     * @return numbeer of available tokens
     */
    protected int numAvailableTokens() {
        return this.tokenManger.numAvailableTokens();
    }

    /**
     * Queues the Instagram data to be output by the provider.
     * @param userData data to queue
     * @param userId user id who the data came from
     */
    protected void queueData(Collection<T> userData, String userId) {
        if (userData == null) {
            LOGGER.warn("User id, {}, returned a NULL data from instagram.", userId);
        } else {
            for (T data : userData) {
                ComponentUtils.offerUntilSuccess(convertToStreamsDatum(data), this.dataQueue);
            }
        }
    }

    /**
     * @return true when the collector has queued all of the available Instagram data for the provided users.
     */
    public boolean isCompleted() {
        return this.isCompleted.get();
    }

    @Override
    public void run() {
        for (User user : this.config.getUsersInfo().getUsers()) {
            try {
                collectInstagramDataForUser(user);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOGGER.error("Exception thrown while polling for user, {}, skipping user.", user.getUserId());
                LOGGER.error("Exception thrown while polling for user : ", e);
            }
        }
        this.isCompleted.set(true);
    }

    /**
     * Pull instagram data for a user and queues the resulting data.
     * @param user
     * @throws Exception
     */
    protected abstract void collectInstagramDataForUser(User user) throws Exception;

    /**
     * Takes an Instagram Object and sets it as the document of a streams datum and sets the id of the streams datum.
     * @param item
     * @return
     */
    protected abstract StreamsDatum convertToStreamsDatum(T item);


}
