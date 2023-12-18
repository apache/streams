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
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Tweet;

import org.apache.commons.lang3.NotImplementedException;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.streams.twitter.converter.util.TwitterActivityUtil.updateActivity;

/**
 * TwitterJsonDeleteActivityConverter.
 */
//TODO: use class explicitly somewhere
public class TwitterJsonDeleteActivityConverter implements ActivityConverter<Delete>, Serializable {

  public static Class requiredClass = Delete.class;

  @Override
  public Class requiredClass() {
    return requiredClass;
  }

  private static TwitterJsonDeleteActivityConverter instance = new TwitterJsonDeleteActivityConverter();

  public static TwitterJsonDeleteActivityConverter getInstance() {
    return instance;
  }

  @Override
  public String serializationFormat() {
    return null;
  }

  @Override
  public Delete fromActivity(Activity deserialized) throws ActivityConversionException {
    throw new NotImplementedException();
  }

  @Override
  public List<Delete> fromActivityList(List<Activity> list) {
    throw new NotImplementedException();
  }

  @Override
  public List<Activity> toActivityList(List<Delete> serializedList) {
    throw new NotImplementedException();
  }

  @Override
  public List<Activity> toActivityList(Delete delete) throws ActivityConversionException {

    Activity activity = new Activity();
    updateActivity(delete, activity);
    return Stream.of(activity).collect(Collectors.toList());
  }

  public ActivityObject buildTarget(Tweet tweet) {
    return null;
  }

}
