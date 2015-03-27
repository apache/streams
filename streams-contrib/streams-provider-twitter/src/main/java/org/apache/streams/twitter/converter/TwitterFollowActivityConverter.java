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

import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Provider;
import org.apache.streams.twitter.pojo.Follow;
import org.apache.streams.twitter.converter.util.TwitterActivityUtil;

import java.io.Serializable;
import java.util.List;

public class TwitterFollowActivityConverter implements ActivityConverter<Follow>, Serializable {

    public TwitterFollowActivityConverter() {
    }

    private static TwitterFollowActivityConverter instance = new TwitterFollowActivityConverter();

    public static TwitterFollowActivityConverter getInstance() {
        return instance;
    }

    public static Class requiredClass = Follow.class;

    @Override
    public Class requiredClass() {
        return requiredClass;
    }

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public Follow fromActivity(Activity deserialized) throws ActivityConversionException {
        throw new NotImplementedException();
    }

    @Override
    public List<Activity> toActivityList(Follow event) throws ActivityConversionException {

        Activity activity = new Activity();
        activity.setVerb("follow");
        activity.setActor(TwitterActivityUtil.buildActor(event.getFollower()));
        activity.setObject(TwitterActivityUtil.buildActor(event.getFollowee()));
        activity.setId(activity.getActor().getId() + "-follow->" + activity.getObject().getId());
        activity.setProvider((Provider) new Provider().withId("twitter"));
        return Lists.newArrayList(activity);
    }

    @Override
    public List<Follow> fromActivityList(List<Activity> list) {
        throw new NotImplementedException();
    }

    @Override
    public List<Activity> toActivityList(List<Follow> list) {
        throw new NotImplementedException();
    }
}

