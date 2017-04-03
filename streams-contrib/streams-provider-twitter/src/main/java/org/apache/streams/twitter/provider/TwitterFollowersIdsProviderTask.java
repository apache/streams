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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.api.FollowersIdsRequest;
import org.apache.streams.twitter.api.FollowersIdsResponse;
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.pojo.Follow;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.util.ComponentUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  Retrieve friend or follower connections for a single user id.
 */
public class TwitterFollowersIdsProviderTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFollowersIdsProviderTask.class);

  private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  protected Twitter client;
  protected TwitterFollowingProvider provider;
  protected FollowersIdsRequest request;

  private int count = 0;

  /**
   * TwitterFollowingProviderTask constructor.
   * @param provider TwitterFollowingProvider
   * @param twitter Twitter
   * @param request FollowersIdsRequest
   */
  public TwitterFollowersIdsProviderTask(TwitterFollowingProvider provider, Twitter twitter, FollowersIdsRequest request) {
    this.provider = provider;
    this.client = twitter;
    this.request = request;
  }

  @Override
  public void run() {

    Preconditions.checkArgument(request.getId() != null || request.getScreenName() != null);

    LOGGER.info("Thread Starting: {}", request.toString());

    getFollowersIds(request);

    LOGGER.info("Thread Finished: {}", request.toString());

  }

  int last_count = 0;
  int page_count = 1;
  int item_count = 0;
  long curser = 0;

  private void getFollowersIds(FollowersIdsRequest request) {

    do {

      FollowersIdsResponse response = client.ids(request);

      last_count = response.getIds().size();

      if (response.getIds().size() > 0) {

        for (Long id : response.getIds()) {

          Follow follow = new Follow()
              .withFollowee(
                  new User()
                      .withId(request.getId())
                      .withScreenName(request.getScreenName()))
              .withFollower(
                  new User()
                      .withId(id));

          if (item_count < provider.getConfig().getMaxItems()) {
            ComponentUtils.offerUntilSuccess(new StreamsDatum(follow), provider.providerQueue);
            item_count++;
          }

        }

      }
      page_count++;
      curser = response.getNextCursor();
      request.setCurser(curser);
    }
    while (shouldContinuePulling(curser, last_count, page_count, item_count));
  }

  public boolean shouldContinuePulling(long curser, int count, int page_count, int item_count) {
    return (
        curser > 0
            && count > 0
            && item_count < provider.getConfig().getMaxItems()
            && page_count <= provider.getConfig().getMaxPages());
  }

}
