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

import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.pojo.*;
import org.apache.streams.twitter.provider.TwitterEventClassifier;

import java.util.List;
import java.io.Serializable;

public class TwitterJsonActivitySerializer implements ActivitySerializer<String>, Serializable
{

    public TwitterJsonActivitySerializer() {

    }

    TwitterJsonTweetActivitySerializer tweetActivitySerializer = new TwitterJsonTweetActivitySerializer();
    TwitterJsonRetweetActivitySerializer retweetActivitySerializer = new TwitterJsonRetweetActivitySerializer();
    TwitterJsonDeleteActivitySerializer deleteActivitySerializer = new TwitterJsonDeleteActivitySerializer();

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

        Class documentSubType = TwitterEventClassifier.detectClass(serialized);

        Activity activity;
        if( documentSubType == Tweet.class )
            activity = tweetActivitySerializer.deserialize(serialized);
        else if( documentSubType == Retweet.class )
            activity = retweetActivitySerializer.deserialize(serialized);
        else if( documentSubType == Delete.class )
            activity = deleteActivitySerializer.deserialize(serialized);
        else throw new ActivitySerializerException("unrecognized type");

        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        throw new NotImplementedException();
    }
}
