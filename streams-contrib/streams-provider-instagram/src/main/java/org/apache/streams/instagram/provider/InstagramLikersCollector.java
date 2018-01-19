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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.instagram.api.Instagram;
import org.apache.streams.instagram.api.UsersInfoResponse;
import org.apache.streams.instagram.config.InstagramLikersProviderConfiguration;
import org.apache.streams.instagram.pojo.UserInfo;

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
public class InstagramLikersCollector extends InstagramDataCollector<UserInfo> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramLikersCollector.class);
  protected static final int MAX_ATTEMPTS = 5;

  private int consecutiveErrorCount;

  InstagramLikersProviderConfiguration config;

  public InstagramLikersCollector(Instagram instagram, Queue<StreamsDatum> queue, InstagramLikersProviderConfiguration config) {
    super(instagram, queue, config);
    this.config = config;
  }

  @Override
  public void run() {
    for (String mediaId : this.config.getInfo()) {
      try {
        collectInstagramCommentsForMedia(mediaId);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      } catch (Exception ex) {
        LOGGER.error("Exception thrown while polling for media, {}, skipping media.", mediaId);
        LOGGER.error("Exception thrown while polling for media : ", ex);
      }
    }
    this.isCompleted.set(true);
  }

  /**
   * Pull Comments on a specific media and queues the resulting data.
   * @param mediaId mediaId
   * @throws Exception Exception
   */
  protected void collectInstagramCommentsForMedia(String mediaId) throws Exception {

    int item_count = 0;
    int last_count = 0;
    int page_count = 0;

    UsersInfoResponse response = getNextInstagramClient().likes(mediaId);
    if ( response != null && response.getData() != null) {
      last_count = response.getData().size();
      List<UserInfo> data = new LinkedList<>();
      data.addAll(response.getData());
      super.queueData(data, mediaId);
      item_count += last_count;
    }
    page_count++;

    LOGGER.info("item_count: {} last_count: {} page_count: {} ", item_count, last_count, page_count);

  }

  @Override
  protected StreamsDatum convertToStreamsDatum(UserInfo item) {
    return new StreamsDatum(item, item.getId());
  }
}
