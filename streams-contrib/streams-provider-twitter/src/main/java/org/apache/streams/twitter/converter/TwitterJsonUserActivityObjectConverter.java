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

import org.apache.streams.data.ActivityObjectConverter;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.twitter.pojo.User;

import org.apache.commons.lang.NotImplementedException;

import static org.apache.streams.twitter.converter.util.TwitterActivityUtil.buildActor;

public class TwitterJsonUserActivityObjectConverter implements ActivityObjectConverter<User> {

  public static Class requiredClass = User.class;

  @Override
  public Class requiredClass() {
    return requiredClass;
  }

  private static TwitterJsonUserActivityObjectConverter instance = new TwitterJsonUserActivityObjectConverter();

  public static TwitterJsonUserActivityObjectConverter getInstance() {
    return instance;
  }

  @Override
  public String serializationFormat() {
    return null;
  }

  @Override
  public User fromActivityObject(ActivityObject deserialized) throws ActivityConversionException {
    throw new NotImplementedException();
  }

  @Override
  public ActivityObject toActivityObject(User serialized) throws ActivityConversionException {
    return buildActor(serialized);
  }
}
