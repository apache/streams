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

package org.apache.streams.facebook.api;

import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.facebook.Post;
import org.apache.streams.facebook.serializer.FacebookActivityUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.List;

public class FacebookPostActivitySerializer implements ActivitySerializer<org.apache.streams.facebook.Post> {

  public static final DateTimeFormatter FACEBOOK_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
  public static final DateTimeFormatter ACTIVITY_FORMAT = ISODateTimeFormat.basicDateTime();

  public static final String PROVIDER_NAME = "Facebook";

  public static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  @Override
  public String serializationFormat() {
    return "facebook_post_json_v1";
  }

  @Override
  public Post serialize(Activity deserialized) throws ActivitySerializerException {
    throw new NotImplementedException("Not currently supported by this deserializer");
  }

  @Override
  public Activity deserialize(Post post) throws ActivitySerializerException {
    Activity activity = new Activity();

    FacebookActivityUtil.updateActivity(post, activity);

    return activity;
  }

  @Override
  public List<Activity> deserializeAll(List<Post> serializedList) {
    throw new NotImplementedException("Not currently supported by this deserializer");
  }
}
