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

package org.apache.streams.twitter.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.pojo.Follow;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.converter.util.TwitterActivityUtil;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class TwitterFollowActivitySerializer implements ActivitySerializer<Follow>, Serializable {

    public TwitterFollowActivitySerializer() {}

    private static TwitterFollowActivitySerializer instance = new TwitterFollowActivitySerializer();

    public static TwitterFollowActivitySerializer getInstance() {
        return instance;
    }

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public Follow serialize(Activity deserialized) throws ActivitySerializerException {
        return null;
    }

    @Override
    public Activity deserialize(Follow event) throws ActivitySerializerException {

        Activity activity = new Activity();
        activity.setVerb("follow");
        activity.setActor(TwitterActivityUtil.buildActor(event.getFollower()));
        activity.setObject(TwitterActivityUtil.buildActor(event.getFollowee()));

        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<Follow> serializedList) {
        return null;
    }
}
