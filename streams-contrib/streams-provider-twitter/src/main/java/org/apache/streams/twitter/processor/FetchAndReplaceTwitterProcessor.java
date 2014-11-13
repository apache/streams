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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.provider.TwitterConfigurator;
import org.apache.streams.twitter.serializer.TwitterDocumentClassifier;
import org.apache.streams.twitter.serializer.StreamsTwitterMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.util.List;

import static org.apache.streams.twitter.serializer.util.TwitterActivityUtil.*;

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

    private final TwitterStreamConfiguration config;
    private Twitter client;
    private ObjectMapper mapper;
    private int retryCount;

    public FetchAndReplaceTwitterProcessor() {
        this(TwitterConfigurator.detectTwitterStreamConfiguration(StreamsConfigurator.config.getConfig("twitter")));
    }

    public FetchAndReplaceTwitterProcessor(TwitterStreamConfiguration config) {
        this.config = config;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {
        if(entry.getDocument() instanceof Activity) {
            Activity doc = (Activity)entry.getDocument();
            String originalId = doc.getId();
            if(PROVIDER_ID.equals(doc.getProvider().getId())) {
                fetchAndReplace(doc, originalId);
            }
        } else {
            throw new IllegalStateException("Requires an activity document");
        }
        return Lists.newArrayList(entry);
    }


    @Override
    public void prepare(Object configurationObject) {
        this.client = getTwitterClient();
        this.mapper = StreamsTwitterMapper.getInstance();
    }

    @Override
    public void cleanUp() {

    }

    protected void fetchAndReplace(Activity doc, String originalId) {
        try {
            String json = fetch(doc);
            replace(doc, json);
            doc.setId(originalId);
            retryCount = 0;
        } catch(TwitterException tw) {
            if(tw.exceededRateLimitation()) {
                sleepAndTryAgain(doc, originalId);
            }
        } catch (Exception e) {
            LOGGER.warn("Error fetching and replacing tweet for activity {}", doc.getId());
        }
    }

    protected void replace(Activity doc, String json) throws java.io.IOException, ActivitySerializerException {
        Class documentSubType = TwitterDocumentClassifier.getInstance().detectClass(json);
        Object object = mapper.readValue(json, documentSubType);

        if(documentSubType.equals(Retweet.class) || documentSubType.equals(Tweet.class)) {
            updateActivity((Tweet)object, doc);
        } else if(documentSubType.equals(Delete.class)) {
            updateActivity((Delete)object, doc);
        } else {
            LOGGER.info("Could not determine the correct update method for {}", documentSubType);
        }
    }

    protected String fetch(Activity doc) throws TwitterException {
        String id = doc.getObject().getId();
        LOGGER.debug("Fetching status from Twitter for {}", id);
        Long tweetId = Long.valueOf(id.replace("id:twitter:tweets:", ""));
        Status status = getTwitterClient().showStatus(tweetId);
        return TwitterObjectFactory.getRawJSON(status);
    }


    protected Twitter getTwitterClient()
    {
        if(this.client == null) {
            String baseUrl = "https://api.twitter.com:443/1.1/";

            ConfigurationBuilder builder = new ConfigurationBuilder()
                    .setOAuthConsumerKey(config.getOauth().getConsumerKey())
                    .setOAuthConsumerSecret(config.getOauth().getConsumerSecret())
                    .setOAuthAccessToken(config.getOauth().getAccessToken())
                    .setOAuthAccessTokenSecret(config.getOauth().getAccessTokenSecret())
                    .setIncludeEntitiesEnabled(true)
                    .setJSONStoreEnabled(true)
                    .setAsyncNumThreads(1)
                    .setRestBaseURL(baseUrl)
                    .setIncludeMyRetweetEnabled(Boolean.TRUE)
                    .setPrettyDebugEnabled(Boolean.TRUE);

            this.client = new TwitterFactory(builder.build()).getInstance();
        }
        return this.client;
    }

    //Hardcore sleep to allow for catch up
    protected void sleepAndTryAgain(Activity doc, String originalId) {
        try {
            //Attempt to fetchAndReplace with a backoff up to the limit then just reset the count and let the process continue
            if(retryCount < MAX_ATTEMPTS) {
                retryCount++;
                LOGGER.debug("Sleeping for {} min due to excessive calls to Twitter API", (retryCount * 4));
                Thread.sleep(BACKOFF * retryCount);
                fetchAndReplace(doc, originalId);
            } else {
                retryCount = 0;
            }
        } catch (InterruptedException e) {
            LOGGER.warn("Thread sleep interrupted while waiting for twitter backoff");
        }
    }
}
