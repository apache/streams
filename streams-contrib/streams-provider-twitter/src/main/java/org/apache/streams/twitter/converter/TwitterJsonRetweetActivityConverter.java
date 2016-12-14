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
import org.apache.streams.twitter.pojo.Retweet;

import org.apache.commons.lang.NotImplementedException;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.streams.twitter.converter.util.TwitterActivityUtil.updateActivity;

public class TwitterJsonRetweetActivityConverter implements ActivityConverter<Retweet>, Serializable {

  public static Class requiredClass = Retweet.class;

  @Override
  public Class requiredClass() {
    return requiredClass;
  }

  private static TwitterJsonRetweetActivityConverter instance = new TwitterJsonRetweetActivityConverter();

  public TwitterJsonRetweetActivityConverter getInstance() {
    return instance;
  }

  @Override
  public String serializationFormat() {
    return null;
  }

  @Override
  public Retweet fromActivity(Activity deserialized) throws ActivityConversionException {
    throw new NotImplementedException();
  }

  @Override
  public List<Retweet> fromActivityList(List<Activity> list) {
    throw new NotImplementedException();
  }

  @Override
  public List<Activity> toActivityList(Retweet retweet) throws ActivityConversionException {

    Activity activity = new Activity();
    updateActivity(retweet, activity);

    return Stream.of(activity).collect(Collectors.toList());
  }

  @Override
  public List<Activity> toActivityList(List<Retweet> serializedList) {
    List<Activity> result = new ArrayList<>();
    for ( Retweet item : serializedList ) {
      try {
        List<Activity> activities = toActivityList(item);
        result.addAll(activities);
      } catch (ActivityConversionException ex) {
        //
      }
    }
    return result;
  }
}
