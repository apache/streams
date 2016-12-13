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
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.util.ComponentUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterObjectFactory;

import java.util.List;

/**
 *  Retrieve recent posts for a single user id.
 */
public class TwitterTimelineProviderTask implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterTimelineProviderTask.class);

  private static ObjectMapper MAPPER = new StreamsJacksonMapper(Lists.newArrayList(TwitterDateTimeFormat.TWITTER_FORMAT));

  protected TwitterTimelineProvider provider;
  protected Twitter client;
  protected Long id;

  /**
   * TwitterTimelineProviderTask constructor.
   * @param provider TwitterTimelineProvider
   * @param twitter Twitter
   * @param id Long
   */
  public TwitterTimelineProviderTask(TwitterTimelineProvider provider, Twitter twitter, Long id) {
    this.provider = provider;
    this.client = twitter;
    this.id = id;
  }

  int page_count = 1;
  int item_count = 0;
  List<Status> lastPage = null;

  @Override
  public void run() {

    Paging paging = new Paging(page_count, provider.getConfig().getPageSize().intValue());

    LOGGER.info(id + " Thread Starting");

    do {
      int keepTrying = 0;

      // keep trying to load, give it 5 attempts.
      //This value was chosen because it seemed like a reasonable number of times
      //to retry capturing a timeline given the sorts of errors that could potentially
      //occur (network timeout/interruption, faulty client, etc.)
      while (keepTrying < 5) {

        try {
          this.client = provider.getTwitterClient();

          ResponseList<Status> statuses = client.getUserTimeline(id, paging);

          for (Status twitterStatus : statuses) {

            String json = TwitterObjectFactory.getRawJSON(twitterStatus);

            if ( item_count < provider.getConfig().getMaxItems() ) {
              try {
                org.apache.streams.twitter.pojo.Tweet tweet = MAPPER.readValue(json, org.apache.streams.twitter.pojo.Tweet.class);
                ComponentUtils.offerUntilSuccess(new StreamsDatum(tweet), provider.providerQueue);
              } catch (Exception exception) {
                LOGGER.warn("Failed to read document as Tweet ", twitterStatus);
              }
              item_count++;
            }

          }

          lastPage = statuses;
          page_count = paging.getPage() + 1;
          paging.setPage(page_count);

          keepTrying = 10;
        } catch (TwitterException twitterException) {
          keepTrying += TwitterErrorHandler.handleTwitterError(client, id, twitterException);
        } catch (Exception ex) {
          keepTrying += TwitterErrorHandler.handleTwitterError(client, id, ex);
        }
      }
    }
    while (shouldContinuePulling());

    LOGGER.info(id + " Thread Finished");

  }

  public boolean shouldContinuePulling() {
    return (lastPage != null)
        && item_count < provider.getConfig().getMaxItems()
        && page_count <= provider.getConfig().getMaxPages();
  }



}
