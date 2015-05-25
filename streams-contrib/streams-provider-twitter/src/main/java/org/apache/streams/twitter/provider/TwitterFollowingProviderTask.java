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
import com.google.common.base.Preconditions;
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
public class TwitterFollowingProviderTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterFollowingProviderTask.class);

    private final static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    protected TwitterFollowingProvider provider;
    protected Twitter client;
    protected Long id;
    protected String screenName;
    protected String endpoint;

    private int max_per_page = 200;

    public TwitterFollowingProviderTask(TwitterFollowingProvider provider, Twitter twitter, Long id, String endpoint) {
        this.provider = provider;
        this.client = twitter;
        this.id = id;
        this.endpoint = endpoint;
    }

    public TwitterFollowingProviderTask(TwitterFollowingProvider provider, Twitter twitter, String screenName, String endpoint) {
        this.provider = provider;
        this.client = twitter;
        this.screenName = screenName;
        this.endpoint = endpoint;
    }


    @Override
    public void run() {

        Preconditions.checkArgument(id != null || screenName != null);

        if( id != null )
            getFollowing(id);
        else if( screenName != null)
            getFollowing(screenName);

        LOGGER.info(id != null ? id.toString() : screenName + " Thread Finished");

    }

    protected void getFollowing(Long id) {

        Preconditions.checkArgument(endpoint.equals("friends") || endpoint.equals("followers"));

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

                PagableResponseList<twitter4j.User> list = null;
                if( endpoint.equals("followers") )
                    list = client.friendsFollowers().getFollowersList(id.longValue(), curser, max_per_page);
                else if( endpoint.equals("friends") )
                    list = client.friendsFollowers().getFriendsList(id.longValue(), curser, max_per_page);

                Preconditions.checkNotNull(list);
                Preconditions.checkArgument(list.size() > 0);

                for (twitter4j.User follower4j : list) {

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
                if( list.size() == max_per_page )
                    curser = list.getNextCursor();
                else break;
            }
            catch(TwitterException twitterException) {
                keepTrying += TwitterErrorHandler.handleTwitterError(client, twitterException);
            }
            catch(Exception e) {
                keepTrying += TwitterErrorHandler.handleTwitterError(client, e);
            }
        } while (curser != 0 && keepTrying < 10);
    }

    protected void getFollowing(String screenName) {

        twitter4j.User user = null;
        try {
            user = client.users().showUser(screenName);
        } catch (TwitterException e) {
            LOGGER.error("Failure looking up " + id);
        }
        Preconditions.checkNotNull(user);
        getFollowing(user.getId());
    }


}
