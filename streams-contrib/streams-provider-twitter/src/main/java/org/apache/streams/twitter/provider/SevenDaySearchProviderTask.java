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

import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.api.SevenDaySearchRequest;
import org.apache.streams.twitter.api.SevenDaySearchResponse;
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.Tweet;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *  Retrieve recent posts from standard seven day search.
 */
public class SevenDaySearchProviderTask implements Callable<Iterator<Tweet>>, Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(SevenDaySearchProviderTask.class);

  private static ObjectMapper MAPPER = new StreamsJacksonMapper(Stream.of(TwitterDateTimeFormat.TWITTER_FORMAT).collect(Collectors.toList()));

  protected SevenDaySearchProvider provider;
  protected Twitter client;
  protected SevenDaySearchRequest request;
  protected List<Tweet> responseList = new ArrayList<>();

  /**
   * SevenDaySearchProviderTask constructor.
   * @param provider SevenDaySearchProvider
   * @param twitter Twitter
   * @param request SevenDaySearchRequest
   */
  public SevenDaySearchProviderTask(SevenDaySearchProvider provider, Twitter twitter, SevenDaySearchRequest request) {
    this.provider = provider;
    this.client = twitter;
    this.request = request;
  }

  int item_count = 0;
  int last_count = 0;
  int page_count = 0;
  Long maxId = null;

  @Override
  public Iterator<Tweet> call() throws Exception {

    LOGGER.info("Thread Starting: {}", request.toString());

    do {

      SevenDaySearchResponse response = client.sevenDaySearch(request);

      List<Tweet> statuses = response.getStatuses();

      last_count = statuses.size();

      page_count++;

      responseList.addAll(statuses);

      if( statuses.size() > 0 ) {

        for (Tweet status : statuses) {

          if (item_count < provider.getConfig().getMaxItems()) {
            responseList.add(status);
            item_count++;
          }

        }

        Stream<Long> statusIds = statuses.stream().map(status -> status.getId());
        maxId = statusIds.reduce(Math::min).get();
        request.setMaxId(maxId.toString());

      }

      LOGGER.info("item_count: {} last_count: {} page_count: {} ", item_count, last_count, page_count);

    }
    while (shouldContinuePulling(last_count, page_count, item_count));

    return responseList.iterator();
  }

  public boolean shouldContinuePulling(int item_count, int last_count, int page_count) {
    boolean shouldContinuePulling = last_count > 0;
    if ( item_count >= provider.getConfig().getMaxItems() ) {
      return false;
    } else if (page_count >= provider.getConfig().getMaxPages()) {
      return false;
    }
    LOGGER.info("shouldContinuePulling: ", shouldContinuePulling);
    return shouldContinuePulling;
  }

  @Override
  public void run() {
    try {
      this.call();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

}
