/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance *
http://www.apache.org/licenses/LICENSE-2.0 *
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License. */

package org.apache.streams.instagram.provider;

import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.instagram.api.Instagram;
import org.apache.streams.instagram.api.UsersInfoResponse;
import org.apache.streams.instagram.config.Endpoint;
import org.apache.streams.instagram.config.InstagramCommentsProviderConfiguration;
import org.apache.streams.instagram.config.InstagramEngagersProviderConfiguration;
import org.apache.streams.instagram.config.InstagramLikersProviderConfiguration;
import org.apache.streams.instagram.config.InstagramRecentMediaProviderConfiguration;
import org.apache.streams.instagram.pojo.Comment;
import org.apache.streams.instagram.pojo.Media;
import org.apache.streams.instagram.pojo.UserInfo;
import org.apache.streams.instagram.provider.recentmedia.InstagramRecentMediaProvider;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.juneau.BeanSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

/**
 * Executes on all of the Instagram requests to collect the media feed data.
 * <p/>
 * If errors/exceptions occur when trying to gather data for a particular user, that user is skipped and the collector
 * move on to the next user.  If a rate limit exception occurs it employs an exponential back off strategy.
 */
public class InstagramEngagersCollector extends InstagramDataCollector<UserInfo> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramEngagersCollector.class);

  private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  protected static final int MAX_ATTEMPTS = 5;

  private int consecutiveErrorCount;

  InstagramEngagersProviderConfiguration config;

  public InstagramEngagersCollector(Instagram instagram, Queue<StreamsDatum> queue, InstagramEngagersProviderConfiguration config) {
    super(instagram, queue, config);
    this.config = config;
  }

  @Override
  public void run() {

    StreamsConfiguration streamsConfiguration = StreamsConfigurator.detectConfiguration(StreamsConfigurator.getConfig());

    InstagramRecentMediaProviderConfiguration recentMediaProviderConfiguration =
      MAPPER.convertValue(config, InstagramRecentMediaProviderConfiguration.class);
    InstagramRecentMediaProvider recentMediaProvider =
      new InstagramRecentMediaProvider(recentMediaProviderConfiguration);
    recentMediaProvider.prepare(recentMediaProviderConfiguration);
    recentMediaProvider.startStream();
    List<String> mediaIdList = new ArrayList<>();
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
      Iterator<StreamsDatum> iterator = recentMediaProvider.readCurrent().iterator();
      while (iterator.hasNext()) {
        StreamsDatum datum = iterator.next();
        Media media = (Media) datum.getDocument();
        mediaIdList.add(media.getId());
      }
    } while ( recentMediaProvider.isRunning());
    recentMediaProvider.cleanUp();
    for (Endpoint endpoint : config.getEndpoints()) {
      switch(endpoint) {
        case COMMENTS: {
          InstagramCommentsProviderConfiguration commentsProviderConfiguration =
            MAPPER.convertValue(config, InstagramCommentsProviderConfiguration.class)
              .withInfo(mediaIdList);
          InstagramCommentsCollector commentsCollector =
            new InstagramCommentsCollector(instagram, dataQueue, commentsProviderConfiguration);
          commentsCollector.run();
          break;
        }
        case LIKES: {
          InstagramLikersProviderConfiguration likersProviderConfiguration =
            MAPPER.convertValue(config, InstagramLikersProviderConfiguration.class)
              .withInfo(mediaIdList);
          InstagramLikersCollector likersCollector =
            new InstagramLikersCollector(instagram, dataQueue, likersProviderConfiguration);
          likersCollector.run();
        }
      }
    }
    // after this the queue is full but may contain both UserInfo and Comment objects
    dataQueue.iterator().forEachRemaining(datum -> {
      switch( datum.getDocument().getClass().getSimpleName()) {
        case "Comment": {
          datum.setDocument(((Comment)datum.getDocument()).getFrom());
          break;
        }
        default:
      }
    });
    this.isCompleted.set(true);
  }


  @Override
  protected StreamsDatum convertToStreamsDatum(UserInfo item) {
    return new StreamsDatum(item, item.getId());
  }
}
