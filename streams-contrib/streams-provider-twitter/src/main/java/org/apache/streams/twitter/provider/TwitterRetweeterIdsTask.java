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
import org.apache.streams.twitter.api.RetweeterIdsRequest;
import org.apache.streams.twitter.api.RetweeterIdsResponse;
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.util.ComponentUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  Retrieve recent posts for a single user id.
 */
public class TwitterRetweeterIdsTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterRetweeterIdsTask.class);

  private static ObjectMapper MAPPER = new StreamsJacksonMapper(Stream.of(TwitterDateTimeFormat.TWITTER_FORMAT).collect(Collectors.toList()));

  protected TwitterEngagersProvider provider;
  protected Twitter client;
  protected RetweeterIdsRequest request;

  /**
   * TwitterTimelineProviderTask constructor.
   * @param provider TwitterEngagersProvider
   * @param twitter Twitter
   * @param request RetweeterIdsRequest
   */
  public TwitterRetweeterIdsTask(TwitterEngagersProvider provider, Twitter twitter, RetweeterIdsRequest request) {
    this.provider = provider;
    this.client = twitter;
    this.request = request;
  }

  @Override
  public void run() {

    LOGGER.info("Thread Starting: {}", request.toString());

    RetweeterIdsResponse response = client.retweeterIds(request);

    List<String> userIds = response.getIds();

    for (String userId : userIds) {
      User user = new User().withIdStr(userId);
      ComponentUtils.offerUntilSuccess(new StreamsDatum(user), provider.providerQueue);
    }

    LOGGER.info("Thread Finished: {}", request.toString());

  }

}
