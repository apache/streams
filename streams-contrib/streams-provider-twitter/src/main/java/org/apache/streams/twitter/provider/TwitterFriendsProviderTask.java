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

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.pojo.Follow;
import org.apache.streams.util.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;
import twitter4j.User;

import java.io.IOException;

/**
 *  Retrieve recent posts for a single user id.
 */
public class TwitterFriendsProviderTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterFriendsProviderTask.class);

    private final static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    protected TwitterFollowingProvider provider;
    protected Twitter client;
    protected Long id;

    public TwitterFriendsProviderTask(TwitterFollowingProvider provider, Twitter twitter, Long id) {
        this.provider = provider;
        this.client = twitter;
        this.id = id;
    }

    @Override
    public void run() {

        getFriends(id);

        LOGGER.info(id + " Thread Finished");

    }

    protected void getFriends(Long id) {

        int keepTrying = 0;

        long curser = -1;

        do
        {
            try
            {
                twitter4j.User follower4j;
                String followerJson;
                try {
                    follower4j = client.users().showUser(id);
                    followerJson = TwitterObjectFactory.getRawJSON(follower4j);
                } catch (TwitterException e) {
                    LOGGER.error("Failure looking up " + id);
                    break;
                }

                PagableResponseList<User> followeeList = client.friendsFollowers().getFriendsList(id.longValue(), curser);

                for (twitter4j.User followee4j : followeeList ) {

                    String followeeJson = TwitterObjectFactory.getRawJSON(followee4j);

                    try {
                        Follow follow = new Follow()
                                .withFollowee(mapper.readValue(followeeJson, org.apache.streams.twitter.pojo.User.class))
                                .withFollower(mapper.readValue(followerJson, org.apache.streams.twitter.pojo.User.class));

                        ComponentUtils.offerUntilSuccess(new StreamsDatum(follow), provider.providerQueue);
                    } catch (JsonParseException e) {
                        LOGGER.warn(e.getMessage());
                    } catch (JsonMappingException e) {
                        LOGGER.warn(e.getMessage());
                    } catch (IOException e) {
                        LOGGER.warn(e.getMessage());
                    }
                }
                curser = followeeList.getNextCursor();
            }
            catch(TwitterException twitterException) {
                keepTrying += TwitterErrorHandler.handleTwitterError(client, twitterException);
            }
            catch(Exception e) {
                keepTrying += TwitterErrorHandler.handleTwitterError(client, e);
            }
        } while (curser != 0 && keepTrying < 10);
    }

}
