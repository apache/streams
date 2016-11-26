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
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.streams.util.api.requests.backoff.impl.LinearTimeBackOffStrategy;

import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import org.apache.youtube.pojo.YoutubeConfiguration;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * YoutubeChannelDataCollectorTest tests YoutubeChannelDataCollector.
 */
public class YoutubeChannelDataCollectorTest {

  private static final String ID = "12345";

  @Test
  public void testDataCollector() throws Exception {
    YouTube youTube = createMockYoutube();
    BlockingQueue<StreamsDatum> queue = new LinkedBlockingQueue<>();
    BackOffStrategy strategy = new LinearTimeBackOffStrategy(1);
    UserInfo userInfo = new UserInfo();
    userInfo.setUserId(ID);
    YoutubeConfiguration config = new YoutubeConfiguration();
    config.setApiKey(ID);
    YoutubeChannelDataCollector collector = new YoutubeChannelDataCollector(youTube, queue, strategy, userInfo, config);
    collector.run();
    assertEquals(1, queue.size());
    StreamsDatum datum = queue.take();
    assertNotNull(datum);
    String document = (String) datum.getDocument();
    assertNotNull(document);
  }

  private YouTube createMockYoutube() throws Exception {
    YouTube mockYouTube = mock(YouTube.class);
    YouTube.Channels channels = createMockChannels();
    when(mockYouTube.channels()).thenReturn(channels);
    return mockYouTube;
  }

  private YouTube.Channels createMockChannels() throws Exception {
    YouTube.Channels mockChannels = mock(YouTube.Channels.class);
    YouTube.Channels.List channelLists = createMockChannelsList();
    when(mockChannels.list(anyString())).thenReturn(channelLists);
    return mockChannels;
  }

  private YouTube.Channels.List createMockChannelsList() throws Exception {
    YouTube.Channels.List mockList = mock(YouTube.Channels.List.class);
    when(mockList.setId(anyString())).thenReturn(mockList);
    when(mockList.setKey(anyString())).thenReturn(mockList);
    ChannelListResponse response = createMockResponse();
    when(mockList.execute()).thenReturn(response);
    return mockList;
  }

  private ChannelListResponse createMockResponse() {
    ChannelListResponse response = new ChannelListResponse();
    List<Channel> channelList = new LinkedList<>();
    response.setItems(channelList);
    Channel channel = new Channel();
    channel.setId(ID);
    channelList.add(channel);
    return response;
  }

}
