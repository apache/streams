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

package org.apache.streams.twitter.converter;

import org.apache.streams.data.ActivityConverter;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.twitter.pojo.UserstreamEvent;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.streams.twitter.converter.util.TwitterActivityUtil.formatId;
import static org.apache.streams.twitter.converter.util.TwitterActivityUtil.getProvider;


/**
 * TwitterJsonUserstreameventActivityConverter.
 */
// TODO: Use this class explicitely somewhere
public class TwitterJsonUserstreameventActivityConverter implements ActivityConverter<UserstreamEvent> {

  public static Class requiredClass = UserstreamEvent.class;

  @Override
  public Class requiredClass() {
    return requiredClass;
  }

  private static TwitterJsonUserstreameventActivityConverter instance = new TwitterJsonUserstreameventActivityConverter();

  public static TwitterJsonUserstreameventActivityConverter getInstance() {
    return instance;
  }

  @Override
  public String serializationFormat() {
    return null;
  }

  @Override
  public UserstreamEvent fromActivity(Activity deserialized) throws ActivityConversionException {
    throw new NotImplementedException();
  }

  @Override
  public List<UserstreamEvent> fromActivityList(List<Activity> list) {
    throw new NotImplementedException();
  }

  @Override
  public List<Activity> toActivityList(UserstreamEvent userstreamEvent) throws ActivityConversionException {

    Activity activity = convert(userstreamEvent);
    return Stream.of(activity).collect(Collectors.toList());

  }

  @Override
  public List<Activity> toActivityList(List<UserstreamEvent> serializedList) {
    return null;
  }

  /**
   * convert UserstreamEvent to Activity.
   * @param event UserstreamEvent
   * @return Activity
   * @throws ActivityConversionException ActivityConversionException
   */
  public Activity convert(UserstreamEvent event) throws ActivityConversionException {

    Activity activity = new Activity();
    activity.setActor(buildActor(event));
    activity.setVerb(detectVerb(event));
    activity.setObject(buildActivityObject(event));
    activity.setId(formatId(activity.getVerb()));
    if (StringUtils.isEmpty(activity.getId())) {
      throw new ActivityConversionException("Unable to determine activity id");
    }
    activity.setProvider(getProvider());
    return activity;
  }

  /**
   * build ActivityObject from UserstreamEvent
   * @param event UserstreamEvent
   * @return $.actor
   */
  public ActivityObject buildActor(UserstreamEvent event) {
    return new ActivityObject();
  }

  /**
   * build ActivityObject from UserstreamEvent
   * @param event UserstreamEvent
   * @return $.object
   */
  public ActivityObject buildActivityObject(UserstreamEvent event) {
    return new ActivityObject();
  }

  public String detectVerb(UserstreamEvent event) {
    return null;
  }

  public ActivityObject buildTarget(UserstreamEvent event) {
    return null;
  }

}
