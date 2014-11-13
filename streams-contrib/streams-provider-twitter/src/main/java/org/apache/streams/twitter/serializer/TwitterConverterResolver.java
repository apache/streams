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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.ActivityConverterResolver;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.FriendList;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.pojo.UserstreamEvent;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by sblackmon on 12/13/13.
 */
public class TwitterConverterResolver implements ActivityConverterResolver {

    public TwitterConverterResolver() {

    }

    private static TwitterConverterResolver instance = new TwitterConverterResolver();

    public static TwitterConverterResolver getInstance() {
        return instance;
    }

    private static ObjectMapper mapper = new StreamsJacksonMapper(StreamsTwitterMapper.TWITTER_FORMAT);

    @Override
    public Class bestSerializer(Class documentClass) throws ActivitySerializerException {

        if (documentClass == Retweet.class)
            return TwitterJsonRetweetActivityConverter.class;
        else if (documentClass == Delete.class)
            return TwitterJsonDeleteActivityConverter.class;
        else if (documentClass == User.class)
            return TwitterJsonUserActivityConverter.class;
        else if (documentClass == UserstreamEvent.class)
            return TwitterJsonUserstreameventActivityConverter.class;
        else return TwitterJsonTweetActivityConverter.class;

    }
}
