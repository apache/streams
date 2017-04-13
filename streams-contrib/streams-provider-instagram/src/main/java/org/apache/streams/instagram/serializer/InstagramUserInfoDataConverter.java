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

package org.apache.streams.instagram.serializer;

import org.apache.streams.data.ActivityObjectConverter;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.instagram.pojo.UserInfo;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Image;
import org.apache.streams.pojo.json.Provider;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * InstagramUserInfoDataConverter
 */
public class InstagramUserInfoDataConverter implements ActivityObjectConverter<UserInfo> {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramUserInfoDataConverter.class);

  private static final String STREAMS_ID_PREFIX = "id:instagram:";
  private static final String PROVIDER_ID = "id:provider:instagram";
  private static final String DISPLAY_NAME = "Instagram";

  @Override
  public Class requiredClass() {
    return UserInfo.class;
  }

  @Override
  public String serializationFormat() {
    return null;
  }

  @Override
  public UserInfo fromActivityObject(ActivityObject deserialized) throws ActivityConversionException {
    return null;
  }

  @Override
  public ActivityObject toActivityObject(UserInfo serialized) throws ActivityConversionException {
    ActivityObject activityObject = new ActivityObject();
    activityObject.setObjectType("page");
    Provider provider = new Provider();
    provider.setId(PROVIDER_ID);
    provider.setDisplayName(DISPLAY_NAME);
    activityObject.getAdditionalProperties().put("provider", provider);
    activityObject.setPublished(DateTime.now().withZone(DateTimeZone.UTC));
    Image image = new Image();
    image.setUrl(serialized.getProfilePicture());
    activityObject.setImage(image);
    activityObject.setId(STREAMS_ID_PREFIX + serialized.getId());
    activityObject.setSummary(serialized.getBio());
    activityObject.setAdditionalProperty("handle", serialized.getUsername());
    activityObject.setDisplayName(serialized.getFullName());
    activityObject.setUrl(serialized.getWebsite());
    Map<String, Object> extensions = new HashMap<>();
    activityObject.setAdditionalProperty("extensions", extensions);
    extensions.put("screenName", serialized.getUsername());
    extensions.put("posts", serialized.getCounts().getMedia());
    extensions.put("followers", serialized.getCounts().getFollowedBy());
    extensions.put("website", serialized.getWebsite());
    extensions.put("following", serialized.getCounts().getFollows());
    return activityObject;
  }

}
