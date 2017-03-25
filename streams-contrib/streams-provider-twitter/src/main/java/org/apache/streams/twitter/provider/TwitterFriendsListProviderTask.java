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
import org.apache.streams.twitter.api.FriendsListRequest;
import org.apache.streams.twitter.api.FriendsListResponse;
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
public class TwitterFriendsListProviderTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFriendsListProviderTask.class);

  private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  protected Twitter client;
  protected TwitterFollowingProvider provider;
  protected FriendsListRequest request;

  private int count = 0;

  /**
   * TwitterFollowingProviderTask constructor.
   * @param provider TwitterFollowingProvider
   * @param twitter Twitter
   * @param request FriendsListRequest
   */
  public TwitterFriendsListProviderTask(TwitterFollowingProvider provider, Twitter twitter, FriendsListRequest request) {
    this.provider = provider;
    this.client = twitter;
    this.request = request;
  }

  int last_count = 0;
  int page_count = 1;
  int item_count = 0;


  @Override
  public void run() {

    Preconditions.checkArgument(request.getId() != null || request.getScreenName() != null);

    LOGGER.info(request.getId() != null ? request.getId().toString() : request.getScreenName() + " Thread Finished");

  }

  private void getFriendsList(FriendsListRequest request) {

    do {

      this.client = provider.getTwitterClient();

      FriendsListResponse response = client.list(request);

      last_count = response.getUsers().size();

      if (response.getUsers().size() > 0) {

        for (User friend : response.getUsers()) {

          Follow follow = new Follow()
              .withFollower(friend)
              .withFollowee(
                  new User()
                      .withId(request.getId())
                      .withScreenName(request.getScreenName()));

          if (item_count < provider.getConfig().getMaxItems()) {
            ComponentUtils.offerUntilSuccess(new StreamsDatum(follow), provider.providerQueue);
            item_count++;
          }

        }

      }
      page_count++;
      request.setCurser(response.getNextCursor());

    }
    while (shouldContinuePulling(last_count, page_count, item_count));
  }

  public boolean shouldContinuePulling(int count, int page_count, int item_count) {
    return (
        count > 0
            && item_count < provider.getConfig().getMaxItems()
            && page_count <= provider.getConfig().getMaxPages());
  }

}
