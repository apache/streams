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
import org.apache.streams.twitter.api.StatusesUserTimelineRequest;
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.Tweet;
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
public class TwitterTimelineProviderTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterTimelineProviderTask.class);

  private static ObjectMapper MAPPER = new StreamsJacksonMapper(Stream.of(TwitterDateTimeFormat.TWITTER_FORMAT).collect(Collectors.toList()));

  protected TwitterTimelineProvider provider;
  protected Twitter client;
  protected StatusesUserTimelineRequest request;

  /**
   * TwitterTimelineProviderTask constructor.
   * @param provider TwitterTimelineProvider
   * @param twitter Twitter
   * @param request StatusesUserTimelineRequest
   */
  public TwitterTimelineProviderTask(TwitterTimelineProvider provider, Twitter twitter, StatusesUserTimelineRequest request) {
    this.provider = provider;
    this.client = twitter;
    this.request = request;
  }

  int item_count = 0;
  int last_count = 0;
  int page_count = 1;

  @Override
  public void run() {

    LOGGER.info("Thread Starting: {}", request.toString());

    do {

      List<Tweet> statuses = client.userTimeline(request);

      last_count = statuses.size();
      if( statuses.size() > 0 ) {

        for (Tweet status : statuses) {

          if (item_count < provider.getConfig().getMaxItems()) {
            ComponentUtils.offerUntilSuccess(new StreamsDatum(status), provider.providerQueue);
            item_count++;
          }

        }

        Stream<Long> statusIds = statuses.stream().map(status -> status.getId());
        long minId = statusIds.reduce(Math::min).get();
        page_count++;
        request.setMaxId(minId);

      }

    }
    while (shouldContinuePulling(last_count, page_count, item_count));

    LOGGER.info("Thread Finished: {}", request.toString());

  }

  public boolean shouldContinuePulling(int count, int page_count, int item_count) {
    return (
        count > 0
            && item_count < provider.getConfig().getMaxItems()
            && page_count <= provider.getConfig().getMaxPages());
  }



}
