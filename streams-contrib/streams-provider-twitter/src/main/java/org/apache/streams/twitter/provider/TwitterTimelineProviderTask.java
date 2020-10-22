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

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  Retrieve recent posts for a single user id.
 */
public class TwitterTimelineProviderTask implements Callable<Iterator<Tweet>>, Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterTimelineProviderTask.class);

  private static ObjectMapper MAPPER = new StreamsJacksonMapper(Stream.of(TwitterDateTimeFormat.TWITTER_FORMAT).collect(Collectors.toList()));

  protected TwitterTimelineProvider provider;
  protected Twitter client;
  protected StatusesUserTimelineRequest request;
  protected List<Tweet> responseList;

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
  int page_count = 0;
  Date earliest_timestamp = new Date(Long.MAX_VALUE);

  @Override
  public void run() {

    LOGGER.info("Thread Starting: {}", request.toString());

    responseList = new ArrayList<>();

    do {

      List<Tweet> statuses = client.userTimeline(request);

      responseList.addAll(statuses);

      last_count = statuses.size();
      if( statuses.size() > 0 ) {

        for (Tweet status : statuses) {

          earliest_timestamp = Date.from(Instant.ofEpochMilli(Math.min(earliest_timestamp.getTime(), status.getCreatedAt().getTime())));

          if (item_count < provider.getConfig().getMaxItems()) {

            if( (provider.getConfig().getMinTimestamp() != null &&
                  earliest_timestamp.after(provider.getConfig().getMinTimestamp())) ||
                provider.getConfig().getMinTimestamp() == null ) {

              ComponentUtils.offerUntilSuccess(new StreamsDatum(status), provider.providerQueue);
              item_count++;

            }

          }

        }

        Stream<Long> statusIds = statuses.stream().map(status -> status.getId());
        long minId = statusIds.reduce(Math::min).get();
        page_count++;
        request.setMaxId(minId);

      }

    }
    while (shouldContinuePulling(last_count, page_count, item_count, earliest_timestamp));

    LOGGER.info("item_count: {} last_count: {} page_count: {} ", item_count, last_count, page_count);
    
  }

  public boolean shouldContinuePulling(int count, int page_count, int item_count, Date earliest_timestamp) {
    if (provider.getConfig().getMinTimestamp() != null &&
          earliest_timestamp.before(provider.getConfig().getMinTimestamp())) {
      return false;
    } else if (item_count == provider.getConfig().getMaxItems()) {
      return false;
    } else if (page_count == provider.getConfig().getMaxPages()) {
      return false;
    } else {
      return (count > 0);
    }
  }


  @Override
  public Iterator<Tweet> call() throws Exception {
    run();
    return responseList.iterator();
  }
}
