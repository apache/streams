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
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.pojo.*;

import java.io.IOException;

/**
 * Ensures twitter documents can be converted to Activity
 */
public class TwitterDocumentClassifier implements DocumentClassifier {

    public TwitterDocumentClassifier() {

    }

    private static TwitterDocumentClassifier instance;

    public static TwitterDocumentClassifier getInstance() {

        if( instance == null )
            instance = new TwitterDocumentClassifier();
        return instance;
    }

    private static ObjectMapper mapper;

    public Class detectClass(Object document) {

        Preconditions.checkNotNull(document);
        Preconditions.checkArgument(document instanceof String);

        String json = (String)document;
        Preconditions.checkArgument(StringUtils.isNotEmpty(json));

        mapper = new StreamsJacksonMapper(Lists.newArrayList(StreamsTwitterMapper.TWITTER_FORMAT));

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
