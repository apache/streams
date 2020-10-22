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

import com.google.common.base.Strings;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.api.ThirtyDaySearchRequest;
import org.apache.streams.twitter.api.ThirtyDaySearchResponse;
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.util.ComponentUtils;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 *  Retrieve recent posts from premium thirty day search.
 */
public class ThirtyDaySearchProviderTask implements Callable<Iterator<Tweet>>, Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ThirtyDaySearchProviderTask.class);

  private static ObjectMapper MAPPER = new StreamsJacksonMapper(Stream.of(TwitterDateTimeFormat.TWITTER_FORMAT).collect(Collectors.toList()));

  protected ThirtyDaySearchProvider provider;
  protected Twitter client;
  protected ThirtyDaySearchRequest request;
  protected List<Tweet> responseList = new ArrayList<>();

  /**
   * ThirtyDaySearchProviderTask constructor.
   * @param provider ThirtyDaySearchProvider
   * @param twitter Twitter
   * @param request ThirtyDaySearchRequest
   */
  public ThirtyDaySearchProviderTask(ThirtyDaySearchProvider provider, Twitter twitter, ThirtyDaySearchRequest request) {
    this.provider = provider;
    this.client = twitter;
    this.request = request;
  }

  int item_count = 0;
  int last_count = 0;
  int page_count = 0;
  String next = null;

  @Override
  public Iterator<Tweet> call() throws Exception {

    LOGGER.info("Thread Starting: {}", request.toString());

    do {

      ThirtyDaySearchResponse response = client.thirtyDaySearch(provider.getConfig().getEnvironment(), request);

      List<Tweet> statuses = response.getResults();

      last_count = statuses.size();

      // count items but dont truncate response b/c we already paid for them
      item_count += statuses.size();

      page_count++;

      responseList.addAll(statuses);

      next = response.getNext();

      request.setNext(next);

      LOGGER.info("item_count: {} last_count: {} page_count: {} next: {} ", item_count, last_count, page_count, next);

    }
    while (shouldContinuePulling(last_count, page_count, item_count, next));

    return responseList.iterator();
  }

  public boolean shouldContinuePulling(int count, int page_count, int item_count, String next) {
    boolean shouldContinuePulling = count > 0;
    if (Strings.isNullOrEmpty(next)) {
      shouldContinuePulling = false;
    }
    if (item_count >= provider.getConfig().getMaxItems()) {
      shouldContinuePulling = false;
    } else if (page_count >= provider.getConfig().getMaxPages()) {
      shouldContinuePulling = false;
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
