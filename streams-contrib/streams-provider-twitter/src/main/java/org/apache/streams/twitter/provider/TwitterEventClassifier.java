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

package org.apache.streams.twitter.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.pojo.*;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;

import java.io.IOException;
import java.io.Serializable;

/**
 * TwitterEventClassifier classifies twitter events
 *
 * @Deprecated - replaced by TwitterDocumentClassifier - use ActivityConverterProcessor
 */
public class TwitterEventClassifier implements Serializable {

    private static ObjectMapper mapper = new StreamsJacksonMapper(Lists.newArrayList(TwitterDateTimeFormat.TWITTER_FORMAT));

    public static Class detectClass( String json ) {
        Preconditions.checkNotNull(json);
        Preconditions.checkArgument(StringUtils.isNotEmpty(json));

        ObjectNode objectNode;
        try {
            objectNode = (ObjectNode) mapper.readTree(json);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        if( objectNode.findValue("retweeted_status") != null && objectNode.get("retweeted_status") != null)
            return Retweet.class;
        else if( objectNode.findValue("delete") != null )
            return Delete.class;
        else if( objectNode.findValue("friends") != null ||
                objectNode.findValue("friends_str") != null )
            return FriendList.class;
        else if( objectNode.findValue("target_object") != null )
            return UserstreamEvent.class;
        else if ( objectNode.findValue("location") != null && objectNode.findValue("user") == null)
            return User.class;
        else
            return Tweet.class;
    }

}
