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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.pojo.Retweet;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static org.apache.streams.twitter.serializer.util.TwitterActivityUtil.*;

public class TwitterJsonRetweetActivityConverter implements ActivityConverter<Retweet>, Serializable {

    public TwitterJsonRetweetActivityConverter() {

    }

    private static TwitterJsonRetweetActivityConverter instance = new TwitterJsonRetweetActivityConverter();

    public static TwitterJsonRetweetActivityConverter getInstance() {
        return instance;
    }

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public Retweet serialize(Activity deserialized) throws ActivitySerializerException {
        return null;
    }

    @Override
    public Activity deserialize(Retweet retweet) throws ActivitySerializerException {

        Activity activity = new Activity();
        updateActivity(retweet, activity);

        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<Retweet> serializedList) {
        List<Activity> result = Lists.newArrayList();
        for( Retweet item : serializedList ) {
            try {
                Activity activity = deserialize(item);
                result.add(activity);
            } catch (ActivitySerializerException e) {}
        }
        return result;
    }
}
