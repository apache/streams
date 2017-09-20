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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.instagram.api.Instagram;
import org.apache.streams.instagram.api.RecentMediaResponse;
import org.apache.streams.instagram.config.InstagramRecentMediaProviderConfiguration;
import org.apache.streams.instagram.pojo.Media;
import org.apache.streams.instagram.pojo.UserRecentMediaRequest;
import org.apache.streams.instagram.provider.InstagramDataCollector;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Executes on all of the Instagram requests to collect the media feed data.
 * <p/>
 * If errors/exceptions occur when trying to gather data for a particular user, that user is skipped and the collector
 * move on to the next user.  If a rate limit exception occurs it employs an exponential back off strategy.
 */
public class InstagramRecentMediaCollector extends InstagramDataCollector<Media> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramRecentMediaCollector.class);
  protected static final int MAX_ATTEMPTS = 5;

  private int consecutiveErrorCount;

  InstagramRecentMediaProviderConfiguration config;

  public InstagramRecentMediaCollector(Instagram instagram, Queue<StreamsDatum> queue, InstagramRecentMediaProviderConfiguration config) {
    super(instagram, queue, config);
    this.config = config;
  }

  @Override
  public void run() {
    for (String userId : this.config.getInfo()) {
      try {
        collectInstagramDataForUser(userId);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      } catch (Exception ex) {
        LOGGER.error("Exception thrown while polling for user, {}, skipping user.", userId);
        LOGGER.error("Exception thrown while polling for user : ", ex);
      }
    }
    this.isCompleted.set(true);
  }

  protected StreamsDatum convertToStreamsDatum(Media item) {
    return new StreamsDatum(item, item.getId());
  }

  /**
   * Pull Recement Media for a user and queues the resulting data.
   * @param userId userId
   * @throws Exception Exception
   */
  protected void collectInstagramDataForUser(String userId) throws Exception {

    int item_count = 0;
    int last_count = 0;
    int page_count = 0;

    UserRecentMediaRequest request = (UserRecentMediaRequest) new UserRecentMediaRequest()
        .withUserId(userId)
        .withMinId(null)
        .withMaxId(0L)
        .withCount(33L);
    RecentMediaResponse response;
    do {
      response = getNextInstagramClient().userMediaRecent(request);
      if ( response != null && response.getData() != null) {
        last_count = response.getData().size();
        List<Media> data = new LinkedList<>();
        data.addAll(response.getData());
        super.queueData(data, userId);
        item_count += last_count;
      }
      page_count++;
      if ( shouldContinuePulling(response) ) {
        request.setMaxId(new Long(response.getPagination().getNextMaxId()));
      }
    }
    while (shouldContinuePulling(response));

    LOGGER.info("item_count: {} last_count: {} page_count: {} ", item_count, last_count, page_count);

  }

  private boolean shouldContinuePulling(RecentMediaResponse response) {
    if ( response != null
        || response.getData() != null
        || response.getData().size() > 0
        || response.getPagination() != null
        || StringUtils.isBlank(response.getPagination().getNextMaxId())) {
      return false;
    } else {
      return true;
    }
  }


}
