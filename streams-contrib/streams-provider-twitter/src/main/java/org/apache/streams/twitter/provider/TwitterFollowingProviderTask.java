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
import org.apache.streams.twitter.pojo.Follow;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.util.ComponentUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.util.Objects;

/**
 *  Retrieve friend or follower connections for a single user id.
 */
public class TwitterFollowingProviderTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFollowingProviderTask.class);

  private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  protected TwitterFollowingProvider provider;
  private Twitter client;
  protected Long id;
  private String screenName;

  private int count = 0;

  /**
   * TwitterFollowingProviderTask constructor.
   * @param provider TwitterFollowingProvider
   * @param twitter Twitter
   * @param id numeric id
   */
  public TwitterFollowingProviderTask(TwitterFollowingProvider provider, Twitter twitter, Long id) {
    this.provider = provider;
    this.client = twitter;
    this.id = id;
  }

  /**
   * TwitterFollowingProviderTask constructor.
   * @param provider TwitterFollowingProvider
   * @param twitter Twitter
   * @param screenName screenName
   */
  public TwitterFollowingProviderTask(TwitterFollowingProvider provider, Twitter twitter, String screenName) {
    this.provider = provider;
    this.client = twitter;
    this.screenName = screenName;
  }


  @Override
  public void run() {

    Preconditions.checkArgument(id != null || screenName != null);

    if ( id != null ) {
      getFollowing(id);
    } else {
      getFollowing(screenName);
    }

    LOGGER.info(id != null ? id.toString() : screenName + " Thread Finished");

  }

  private void getFollowing(Long id) {

    Preconditions.checkArgument(
        provider.getConfig().getEndpoint().equals("friends")
        || provider.getConfig().getEndpoint().equals("followers")
    );

    if ( provider.getConfig().getIdsOnly() ) {
      collectIds(id);
    } else {
      collectUsers(id);
    }
  }

  private void getFollowing(String screenName) {

    twitter4j.User user = null;
    try {
      user = client.users().showUser(screenName);
    } catch (TwitterException ex) {
      LOGGER.error("Failure looking up " + id);
    }
    Objects.requireNonNull(user);
    getFollowing(user.getId());
  }

  private void collectUsers(Long id) {
    int keepTrying = 0;

    long curser = -1;

    do {
      try {
        twitter4j.User user;
        String userJson;
        try {
          user = client.users().showUser(id);
          userJson = TwitterObjectFactory.getRawJSON(user);
        } catch (TwitterException ex) {
          LOGGER.error("Failure looking up " + id);
          break;
        }

        PagableResponseList<twitter4j.User> list = null;
        if ( provider.getConfig().getEndpoint().equals("followers") ) {
          list = client.friendsFollowers().getFollowersList(id, curser, provider.getConfig().getMaxItems().intValue());
        } else if ( provider.getConfig().getEndpoint().equals("friends") ) {
          list = client.friendsFollowers().getFriendsList(id, curser, provider.getConfig().getMaxItems().intValue());
        }

        Objects.requireNonNull(list);
        Preconditions.checkArgument(list.size() > 0);

        for (twitter4j.User other : list) {

          String otherJson = TwitterObjectFactory.getRawJSON(other);

          try {
            Follow follow = null;
            if ( provider.getConfig().getEndpoint().equals("followers") ) {
              follow = new Follow()
                  .withFollowee(mapper.readValue(userJson, User.class))
                  .withFollower(mapper.readValue(otherJson, User.class));
            } else if ( provider.getConfig().getEndpoint().equals("friends") ) {
              follow = new Follow()
                  .withFollowee(mapper.readValue(otherJson, User.class))
                  .withFollower(mapper.readValue(userJson, User.class));
            }

            Objects.requireNonNull(follow);

            if ( count < provider.getConfig().getMaxItems()) {
              ComponentUtils.offerUntilSuccess(new StreamsDatum(follow), provider.providerQueue);
              count++;
            }

          } catch (Exception ex) {
            LOGGER.warn("Exception: {}", ex);
          }
        }
        if ( !list.hasNext() ) {
          break;
        }
        if ( list.getNextCursor() == 0 ) {
          break;
        }
        curser = list.getNextCursor();
      } catch (Exception ex) {
        keepTrying += TwitterErrorHandler.handleTwitterError(client, null, ex);
      }
    }
    while (curser != 0 && keepTrying < provider.getConfig().getRetryMax() && count < provider.getConfig().getMaxItems());
  }

  private void collectIds(Long id) {
    int keepTrying = 0;

    long curser = -1;

    do {
      try {
        twitter4j.IDs ids = null;
        if ( provider.getConfig().getEndpoint().equals("followers") ) {
          ids = client.friendsFollowers().getFollowersIDs(id, curser, provider.getConfig().getMaxItems().intValue());
        } else if ( provider.getConfig().getEndpoint().equals("friends") ) {
          ids = client.friendsFollowers().getFriendsIDs(id, curser, provider.getConfig().getMaxItems().intValue());
        }

        Objects.requireNonNull(ids);
        Preconditions.checkArgument(ids.getIDs().length > 0);

        for (long otherId : ids.getIDs()) {

          try {
            Follow follow = null;
            if ( provider.getConfig().getEndpoint().equals("followers") ) {
              follow = new Follow()
                  .withFollowee(new User().withId(id))
                  .withFollower(new User().withId(otherId));
            } else if ( provider.getConfig().getEndpoint().equals("friends") ) {
              follow = new Follow()
                  .withFollowee(new User().withId(otherId))
                  .withFollower(new User().withId(id));
            }

            Objects.requireNonNull(follow);

            if ( count < provider.getConfig().getMaxItems()) {
              ComponentUtils.offerUntilSuccess(new StreamsDatum(follow), provider.providerQueue);
              count++;
            }
          } catch (Exception ex) {
            LOGGER.warn("Exception: {}", ex);
          }
        }
        if ( !ids.hasNext() ) {
          break;
        }
        if ( ids.getNextCursor() == 0 ) {
          break;
        }
        curser = ids.getNextCursor();
      } catch (TwitterException twitterException) {
        keepTrying += TwitterErrorHandler.handleTwitterError(client, id, twitterException);
      } catch (Exception ex) {
        keepTrying += TwitterErrorHandler.handleTwitterError(client, null, ex);
      }
    }
    while (curser != 0 && keepTrying < provider.getConfig().getRetryMax() && count < provider.getConfig().getMaxItems());
  }

}
