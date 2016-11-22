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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.youtube.pojo.YoutubeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Collects YoutubeChannelData on behalf of YoutubeChannelProvider.
 */
public class YoutubeChannelDataCollector extends YoutubeDataCollector {

  private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeChannelDataCollector.class);
  private static final String CONTENT = "snippet,contentDetails,statistics,topicDetails";
  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();
  private static final int MAX_ATTEMPTS = 5;

  private YouTube youTube;
  private BlockingQueue<StreamsDatum> queue;
  private BackOffStrategy strategy;
  private UserInfo userInfo;
  private YoutubeConfiguration youtubeConfig;

  /**
   * YoutubeChannelDataCollector constructor.
   * @param youTube YouTube
   * @param queue BlockingQueue of StreamsDatum
   * @param strategy BackOffStrategy
   * @param userInfo UserInfo
   * @param youtubeConfig YoutubeConfiguration
   */
  public YoutubeChannelDataCollector(
      YouTube youTube,
      BlockingQueue<StreamsDatum> queue,
      BackOffStrategy strategy,
      UserInfo userInfo,
      YoutubeConfiguration youtubeConfig) {
    this.youTube = youTube;
    this.queue = queue;
    this.strategy = strategy;
    this.userInfo = userInfo;
    this.youtubeConfig = youtubeConfig;
  }

  @Override
  public void run() {
    Gson gson = new Gson();
    try {
      int attempt = 0;
      YouTube.Channels.List channelLists = this.youTube.channels().list(CONTENT).setId(this.userInfo.getUserId()).setKey(this.youtubeConfig.getApiKey());
      boolean tryAgain = false;
      do {
        try {
          List<Channel> channels = channelLists.execute().getItems();
          for (Channel channel : channels) {
            String json = gson.toJson(channel);
            this.queue.put(new StreamsDatum(json, channel.getId()));
          }
          if (StringUtils.isEmpty(channelLists.getPageToken())) {
            channelLists = null;
          } else {
            channelLists = this.youTube.channels().list(CONTENT).setId(this.userInfo.getUserId()).setOauthToken(this.youtubeConfig.getApiKey())
                .setPageToken(channelLists.getPageToken());
          }
        } catch (GoogleJsonResponseException gjre) {
          LOGGER.warn("GoogleJsonResposneException caught : {}", gjre);
          tryAgain = backoffAndIdentifyIfRetry(gjre, this.strategy);
          ++attempt;
        } catch (Throwable throwable) {
          LOGGER.warn("Unable to get channel info for id : {}", this.userInfo.getUserId());
          LOGGER.warn("Excpection thrown while trying to get channel info : {}", throwable);
        }
      }
      while ((tryAgain && attempt < MAX_ATTEMPTS) || channelLists != null);

    } catch (Throwable throwable) {
      LOGGER.warn(throwable.getMessage());
    }
  }


}
