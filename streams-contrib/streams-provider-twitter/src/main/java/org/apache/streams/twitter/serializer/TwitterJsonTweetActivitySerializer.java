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
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.pojo.Tweet;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static org.apache.streams.twitter.serializer.util.TwitterActivityUtil.*;

public class TwitterJsonTweetActivitySerializer implements ActivitySerializer<String>, Serializable {

    private static TwitterJsonTweetActivitySerializer instance = new TwitterJsonTweetActivitySerializer();

    public static TwitterJsonTweetActivitySerializer getInstance() {
        return instance;
    }

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public String serialize(Activity deserialized) throws ActivitySerializerException {
        throw new NotImplementedException();
    }

    @Override
    public Activity deserialize(String serialized) throws ActivitySerializerException {

        ObjectMapper mapper = StreamsTwitterMapper.getInstance();
        Tweet tweet = null;
        try {
            tweet = mapper.readValue(serialized, Tweet.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Activity activity = new Activity();

        updateActivity(tweet, activity);

        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        return null;
    }
}
