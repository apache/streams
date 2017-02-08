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

package org.apache.streams.youtube.processor;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.youtube.serializer.YoutubeActivityUtil;
import org.apache.streams.youtube.serializer.YoutubeChannelDeserializer;
import org.apache.streams.youtube.serializer.YoutubeEventClassifier;
import org.apache.streams.youtube.serializer.YoutubeVideoDeserializer;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.Video;
import org.apache.commons.lang.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class YoutubeTypeConverter implements StreamsProcessor {

  public static final String STREAMS_ID = "YoutubeTypeConverter";

  private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeTypeConverter.class);

  private StreamsJacksonMapper mapper;
  private Queue<Video> inQueue;
  private Queue<StreamsDatum> outQueue;
  private int count = 0;

  public YoutubeTypeConverter() {}

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum streamsDatum) {
    StreamsDatum result = null;

    try {
      Object item = streamsDatum.getDocument();

      LOGGER.debug("{} processing {}", STREAMS_ID, item.getClass());
      Activity activity;

      if (item instanceof String) {
        item = deserializeItem(item);
      }

      if (item instanceof Video) {
        activity = new Activity();
        YoutubeActivityUtil.updateActivity((Video)item, activity, streamsDatum.getId());
      } else if (item instanceof Channel) {
        activity = new Activity();
        YoutubeActivityUtil.updateActivity((Channel)item, activity, null);
      } else {
        throw new NotImplementedException("Type conversion not implement for type : " + item.getClass().getName());
      }

      if (activity != null) {
        result = new StreamsDatum(activity);
        count++;
      }
    } catch (Exception ex) {
      LOGGER.error("Exception while converting Video to Activity: {}", ex);
    }

    if (result != null) {
      List<StreamsDatum> streamsDatumList = new ArrayList<>();
      streamsDatumList.add(result);
      return streamsDatumList;
    } else {
      return new ArrayList<>();
    }
  }

  private Object deserializeItem(Object item) {
    try {
      Class klass = YoutubeEventClassifier.detectClass((String) item);
      if (klass.equals(Video.class)) {
        item = mapper.readValue((String) item, Video.class);
      } else if (klass.equals(Channel.class)) {
        item = mapper.readValue((String) item, Channel.class);
      }
    } catch (Exception ex) {
      LOGGER.error("Exception while trying to deserializeItem: {}", ex);
    }

    return item;
  }

  @Override
  public void prepare(Object configurationObject) {
    mapper = StreamsJacksonMapper.getInstance();

    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer(Video.class, new YoutubeVideoDeserializer());
    mapper.registerModule(simpleModule);
    simpleModule = new SimpleModule();
    simpleModule.addDeserializer(Channel.class, new YoutubeChannelDeserializer());
    mapper.registerModule(simpleModule);
  }

  @Override
  public void cleanUp() {}
}