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

import org.apache.streams.data.ActivityConverter;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.instagram.pojo.Media;
import org.apache.streams.pojo.json.Activity;

import org.apache.commons.lang3.NotImplementedException;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

import static org.apache.streams.instagram.serializer.util.InstagramActivityUtil.updateActivity;

public class InstagramMediaFeedDataConverter implements ActivityConverter<Media>, Serializable {

  public static Class requiredClass = Media.class;

  public InstagramMediaFeedDataConverter() {

  }

  @Override
  public Class requiredClass() {
    return requiredClass;
  }

  @Override
  public String serializationFormat() {
    return null;
  }

  @Override
  public Media fromActivity(Activity deserialized) throws ActivityConversionException {
    throw new NotImplementedException();
  }

  @Override
  public List<Media> fromActivityList(List<Activity> list) throws ActivityConversionException {
    throw new NotImplementedException();
  }

  @Override
  public List<Activity> toActivityList(Media item) throws ActivityConversionException {

    Activity activity = new Activity();

    updateActivity(item, activity);

    return Collections.singletonList(activity);
  }

  @Override
  public List<Activity> toActivityList(List<Media> list) throws ActivityConversionException {
    throw new NotImplementedException();
  }

}
