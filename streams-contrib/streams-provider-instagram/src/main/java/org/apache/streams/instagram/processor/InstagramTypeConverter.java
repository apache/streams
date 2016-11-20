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

package org.apache.streams.instagram.processor;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.instagram.serializer.InstagramMediaFeedDataConverter;
import org.apache.streams.instagram.serializer.InstagramUserInfoDataConverter;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;

import com.google.common.collect.Lists;
import org.jinstagram.entity.users.basicinfo.UserInfoData;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * This is deprecated - use ActivityConverterProcessor or ActivityObjectConverterProcessor.
 */
@Deprecated
public class InstagramTypeConverter implements StreamsProcessor {

  public static final String STREAMS_ID = "InstagramTypeConverter";

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramTypeConverter.class);

  private InstagramMediaFeedDataConverter mediaFeedDataConverter;
  private InstagramUserInfoDataConverter userInfoDataConverter;

  public static final String TERMINATE = new String("TERMINATE");

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {

    StreamsDatum result = null;

    try {
      Object item = entry.getDocument();

      LOGGER.debug("{} processing {}", STREAMS_ID, item.getClass());
      if (item instanceof MediaFeedData) {

        //We don't need to use the mapper, since we have a process to convert between
        //MediaFeedData objects and Activity objects already
        List<Activity> activity = mediaFeedDataConverter.toActivityList((MediaFeedData)item);

        if ( activity.size() > 0 ) {
          result = new StreamsDatum(activity);
        }

      } else if (item instanceof UserInfoData) {

        ActivityObject activityObject = userInfoDataConverter.toActivityObject((UserInfoData)item);

        if ( activityObject != null ) {
          result = new StreamsDatum(activityObject);
        }

      }

    } catch (Exception ex) {
      ex.printStackTrace();
      LOGGER.error("Exception while converting item: {}", ex.getMessage());
    }

    if ( result != null ) {
      return Lists.newArrayList(result);
    } else {
      return Lists.newArrayList();
    }
  }

  @Override
  public void prepare(Object configurationObject) {
    mediaFeedDataConverter = new InstagramMediaFeedDataConverter();
    userInfoDataConverter = new InstagramUserInfoDataConverter();
  }

  @Override
  public void cleanUp() {
    //noop
  }

}
