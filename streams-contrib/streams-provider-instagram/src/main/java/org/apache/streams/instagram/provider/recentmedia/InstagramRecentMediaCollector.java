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
package org.apache.streams.instagram.provider.recentmedia;

import com.google.common.annotations.VisibleForTesting;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.instagram.InstagramConfiguration;
import org.apache.streams.instagram.User;
import org.apache.streams.instagram.provider.InstagramDataCollector;
import org.jinstagram.entity.common.Pagination;
import org.jinstagram.entity.users.feed.MediaFeed;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.jinstagram.exceptions.InstagramBadRequestException;
import org.jinstagram.exceptions.InstagramRateLimitException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * Executes on all of the Instagram requests to collect the media feed data.
 * <p/>
 * If errors/exceptions occur when trying to gather data for a particular user, that user is skipped and the collector
 * move on to the next user.  If a rate limit exception occurs it employs an exponential back off strategy.
 */
public class InstagramRecentMediaCollector extends InstagramDataCollector<MediaFeedData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramRecentMediaCollector.class);
    protected static final int MAX_ATTEMPTS = 5;

    private int consecutiveErrorCount;


    public InstagramRecentMediaCollector(Queue<StreamsDatum> queue, InstagramConfiguration config) {
        super(queue, config);
    }

    @Override
    protected StreamsDatum convertToStreamsDatum(MediaFeedData item) {
        return new StreamsDatum(item, item.getId());
    }

    /**
     * Pull Recement Media for a user and queues the resulting data. Will try a single call 5 times before failing and
     * moving on to the next call or returning.
     * @param user
     * @throws Exception
     */
    @Override
    protected void collectInstagramDataForUser(User user) throws Exception {
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
                    if(this.consecutiveErrorCount > Math.max(this.numAvailableTokens(), MAX_ATTEMPTS*2)) {
                        throw new Exception("InstagramCollector failed to successfully connect to instagram on "+this.consecutiveErrorCount+" attempts.");
                    }
                }
                if(succesfullDataPull = feed != null) {
                    this.consecutiveErrorCount = 0;
                    this.backOffStrategy.reset();
                    pagination = feed.getPagination();
                    queueData(feed.getData(), user.getUserId());
                }
            }
            if(!succesfullDataPull) {
                LOGGER.error("Failed to get data from instagram for user id, {}, skipping user.", user.getUserId());
            }
        } while (pagination != null && pagination.hasNextPage());
    }


}
