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

package com.youtube.provider;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.client.util.Lists;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.ActivityListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.youtube.pojo.YoutubeConfiguration;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class YoutubeUserActivityCollector extends YoutubeDataCollector {
    /**
     * Max results allowed per request
     * https://developers.google.com/+/api/latest/activities/list
     */
    private static final long MAX_RESULTS = 50;
    private static final int MAX_ATTEMPTS = 5;
    private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeUserActivityCollector.class);
    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    static { //set up mapper for Google Activity Object
        SimpleModule simpleModule = new SimpleModule();
        MAPPER.registerModule(simpleModule);
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private BlockingQueue<StreamsDatum> datumQueue;
    private BackOffStrategy backOff;
    private YouTube youtube;
    private UserInfo userInfo;
    private YoutubeConfiguration config;

    public YoutubeUserActivityCollector(YouTube youtube, BlockingQueue<StreamsDatum> datumQueue, BackOffStrategy backOff, UserInfo userInfo, YoutubeConfiguration config) {
        this.youtube = youtube;
        this.datumQueue = datumQueue;
        this.backOff = backOff;
        this.userInfo = userInfo;
        this.config = config;
    }

    @Override
    public void run() {
        collectActivityData();
    }

    /**
     * Iterate through all users in the Youtube configuration and collect all videos
     * associated with their accounts.
     */
    protected void collectActivityData() {
        try {
            YouTube.Activities.List request = null;
            ActivityListResponse feed = null;

            boolean tryAgain = false;
            int attempt = 0;
            DateTime afterDate = userInfo.getAfterDate();
            DateTime beforeDate = userInfo.getBeforeDate();

            do {
                try {
                    if(request == null) {
                        request = this.youtube.activities().list("contentDetails")
                                                            .setChannelId(userInfo.getUserId())
                                                            .setMaxResults(MAX_RESULTS)
                                                            .setKey(config.getApiKey());
                        feed = request.execute();
                    } else {
                        request = this.youtube.activities().list("contentDetails")
                                                            .setChannelId(userInfo.getUserId())
                                                            .setMaxResults(MAX_RESULTS)
                                                            .setPageToken(feed.getNextPageToken())
                                                            .setKey(config.getApiKey());
                        feed = request.execute();
                    }
                    this.backOff.reset(); //successful pull reset api.

                    processActivityFeed(feed, afterDate, beforeDate);
                } catch (GoogleJsonResponseException gjre) {
                    tryAgain = backoffAndIdentifyIfRetry(gjre, this.backOff);
                    ++attempt;
                }
            } while((tryAgain || (feed != null && feed.getNextPageToken() != null)) && attempt < MAX_ATTEMPTS);
        } catch (Throwable t) {
            if(t instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            t.printStackTrace();
            LOGGER.warn("Unable to pull Activities for user={} : {}",this.userInfo.getUserId(), t);
        }
    }

    /**
     * Given a feed and an after and before date, fetch all relevant user videos
     * and place them into the datumQueue for post-processing
     * @param feed
     * @param afterDate
     * @param beforeDate
     * @throws IOException
     * @throws InterruptedException
     */
    void processActivityFeed(ActivityListResponse feed, DateTime afterDate, DateTime beforeDate) throws IOException, InterruptedException {
        for(com.google.api.services.youtube.model.Activity activity : feed.getItems()) {
            List<Video> videos = getVideoList(activity.getContentDetails().getUpload().getVideoId());

            for(Video video : videos) {
                if (video != null) {
                    org.joda.time.DateTime published = new org.joda.time.DateTime(video.getSnippet().getPublishedAt().getValue());
                    if ((afterDate == null && beforeDate == null)
                            || (beforeDate == null && afterDate.isBefore(published))
                            || (afterDate == null && beforeDate.isAfter(published))
                            || ((afterDate != null && beforeDate != null) && (afterDate.isAfter(published) && beforeDate.isBefore(published)))) {
                        LOGGER.debug("Providing Youtube Activity: {}", MAPPER.writeValueAsString(video));
                        this.datumQueue.put(new StreamsDatum(MAPPER.writeValueAsString(video), activity.getId()));
                    } else if (afterDate != null && afterDate.isAfter(published)) {
                        feed.setNextPageToken(null); // do not fetch next page
                        break;
                    }
                }
            }
        }
    }

    /**
     * Given a Youtube videoId, return the relevant Youtube Video object
     * @param videoId
     * @return
     * @throws IOException
     */
    List<Video> getVideoList(String videoId) throws IOException {
        VideoListResponse videosListResponse = this.youtube.videos().list("snippet,statistics")
                                                                    .setId(videoId)
                                                                    .setKey(config.getApiKey())
                                                                    .execute();

        if(videosListResponse.getItems().size() == 0) {
            LOGGER.debug("No Youtube videos found for videoId: {}", videoId);
            return Lists.newArrayList();
        }

        return videosListResponse.getItems();
    }

    BlockingQueue<StreamsDatum> getDatumQueue() {
        return this.datumQueue;
    }
}