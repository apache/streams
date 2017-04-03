/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.twitter.processor;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.TwitterConfiguration;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.api.StatusesShowRequest;
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.converter.TwitterDocumentClassifier;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.provider.TwitterProviderUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.streams.twitter.converter.util.TwitterActivityUtil.getProvider;
import static org.apache.streams.twitter.converter.util.TwitterActivityUtil.updateActivity;

/**
 *  Given an Activity, fetches the tweet by the activity object id and replaces the existing activity with the converted activity
 *  from what is returned by the twitter API.
 */
public class FetchAndReplaceTwitterProcessor implements StreamsProcessor {

  private static final String PROVIDER_ID = getProvider().getId();
  private static final Logger LOGGER = LoggerFactory.getLogger(FetchAndReplaceTwitterProcessor.class);

  //Default number of attempts before allowing the document through
  private static final int MAX_ATTEMPTS = 5;
  //Start the backoff at 4 minutes.  This results in a wait period of 4, 8, 12, 16 & 20 min with an attempt of 5
  public static final int BACKOFF = 1000 * 60 * 4;

  private final TwitterConfiguration config;
  private Twitter client;
  private ObjectMapper mapper;
  private int retryCount;

  public FetchAndReplaceTwitterProcessor() {
    this(new ComponentConfigurator<>(TwitterStreamConfiguration.class).detectConfiguration(StreamsConfigurator.config, "twitter"));
  }

  public FetchAndReplaceTwitterProcessor(TwitterStreamConfiguration config) {
    this.config = config;
  }

  @Override
  public String getId() {
    return getProvider().getId();
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {
    if (entry.getDocument() instanceof Activity) {
      Activity doc = (Activity)entry.getDocument();
      String originalId = doc.getId();
      if (PROVIDER_ID.equals(doc.getProvider().getId())) {
        try {
          fetchAndReplace(doc, originalId);
        } catch (ActivityConversionException ex) {
          LOGGER.warn("ActivityConversionException", ex);
        } catch (IOException ex) {
          LOGGER.warn("IOException", ex);
        }
      }
    } else {
      throw new IllegalStateException("Requires an activity document");
    }
    return Stream.of(entry).collect(Collectors.toList());
  }


  @Override
  public void prepare(Object configurationObject) {
    try {
      client = getTwitterClient();
    } catch (InstantiationException e) {
      LOGGER.error("InstantiationException", e);
    }
    Objects.requireNonNull(client);
    this.mapper = StreamsJacksonMapper.getInstance();
    Objects.requireNonNull(mapper);
  }

  @Override
  public void cleanUp() {

  }

  protected void fetchAndReplace(Activity doc, String originalId) throws java.io.IOException, ActivityConversionException {
    Tweet tweet = fetch(doc);
    replace(doc, tweet);
    doc.setId(originalId);
  }

  protected void replace(Activity doc, Tweet tweet) throws java.io.IOException, ActivityConversionException {
    String json = mapper.writeValueAsString(tweet);
    Class documentSubType = new TwitterDocumentClassifier().detectClasses(json).get(0);
    Object object = mapper.readValue(json, documentSubType);

    if (documentSubType.equals(Retweet.class) || documentSubType.equals(Tweet.class)) {
      updateActivity((Tweet)object, doc);
    } else if (documentSubType.equals(Delete.class)) {
      updateActivity((Delete)object, doc);
    } else {
      LOGGER.info("Could not determine the correct update method for {}", documentSubType);
    }
  }

  protected Tweet fetch(Activity doc) {
    String id = doc.getObject().getId();
    LOGGER.debug("Fetching status from Twitter for {}", id);
    Long tweetId = Long.valueOf(id.replace("id:twitter:tweets:", ""));
    Tweet tweet = client.show(
        new StatusesShowRequest()
            .withId(tweetId)
    );
    return tweet;
  }


  protected Twitter getTwitterClient() throws InstantiationException {

    return Twitter.getInstance(config);

  }

  //Hardcore sleep to allow for catch up
//  protected void sleepAndTryAgain(Activity doc, String originalId) {
//    try {
//      //Attempt to fetchAndReplace with a backoff up to the limit then just reset the count and let the process continue
//      if (retryCount < MAX_ATTEMPTS) {
//        retryCount++;
//        LOGGER.debug("Sleeping for {} min due to excessive calls to Twitter API", (retryCount * 4));
//        Thread.sleep(BACKOFF * retryCount);
//        fetchAndReplace(doc, originalId);
//      } else {
//        retryCount = 0;
//      }
//    } catch (InterruptedException ex) {
//      LOGGER.warn("Thread sleep interrupted while waiting for twitter backoff");
//    }
//  }
}
