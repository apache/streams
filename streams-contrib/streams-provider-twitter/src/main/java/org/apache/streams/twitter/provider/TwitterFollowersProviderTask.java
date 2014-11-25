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
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.util.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.PagableResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.io.IOException;

/**
 *  Retrieve recent posts for a single user id.
 */
public class TwitterFollowersProviderTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterFollowersProviderTask.class);

    private final static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    protected TwitterFollowingProvider provider;
    protected Twitter client;
    protected Long id;

    public TwitterFollowersProviderTask(TwitterFollowingProvider provider, Twitter twitter, Long id) {
        this.provider = provider;
        this.client = twitter;
        this.id = id;
    }

    @Override
    public void run() {

        getFollowers(id);

        LOGGER.info(id + " Thread Finished");

    }

    protected void getFollowers(Long id) {

        int keepTrying = 0;

        long curser = -1;

        do
        {
            try
            {
                twitter4j.User followee4j;
                String followeeJson;
                try {
                    followee4j = client.users().showUser(id);
                    followeeJson = TwitterObjectFactory.getRawJSON(followee4j);
                } catch (TwitterException e) {
                    LOGGER.error("Failure looking up " + id);
                    break;
                }

                PagableResponseList<twitter4j.User> followerList = client.friendsFollowers().getFollowersList(id.longValue(), curser);

                for (twitter4j.User follower4j : followerList) {

                    String followerJson = TwitterObjectFactory.getRawJSON(follower4j);

                    try {
                        Follow follow = new Follow()
                                .withFollowee(mapper.readValue(followeeJson, User.class))
                                .withFollower(mapper.readValue(followerJson, User.class));

                        ComponentUtils.offerUntilSuccess(new StreamsDatum(follow), provider.providerQueue);
                    } catch (JsonParseException e) {
                        LOGGER.warn(e.getMessage());
                    } catch (JsonMappingException e) {
                        LOGGER.warn(e.getMessage());
                    } catch (IOException e) {
                        LOGGER.warn(e.getMessage());
                    }
                }
                curser = followerList.getNextCursor();
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
