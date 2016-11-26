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

import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Follow;
import org.apache.streams.twitter.pojo.FriendList;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.pojo.UserstreamEvent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.apache.streams.twitter.converter.TwitterDateTimeFormat.TWITTER_FORMAT;

/**
 * Ensures twitter documents can be converted to Activity.
 */
public class TwitterDocumentClassifier implements DocumentClassifier {

  @Override
  public List<Class> detectClasses(Object document) {

    Objects.requireNonNull(document);

    ObjectMapper mapper = StreamsJacksonMapper.getInstance(Collections.singletonList(TWITTER_FORMAT));

    ObjectNode objectNode;
    try {
      if ( document instanceof String ) {
        objectNode = mapper.readValue((String) document, ObjectNode.class);
      } else if ( document instanceof ObjectNode ) {
        objectNode = (ObjectNode) document;
      } else {
        objectNode = mapper.convertValue(document, ObjectNode.class);
      }
    } catch (IOException ex) {
      return new ArrayList<>();
    }

    List<Class> classList = new ArrayList<>();

    if ( objectNode.findValue("retweeted_status") != null
        && objectNode.get("retweeted_status") != null) {
      classList.add(Retweet.class);
    } else if ( objectNode.findValue("delete") != null ) {
      classList.add(Delete.class);
    } else if ( objectNode.findValue("friends") != null
        || objectNode.findValue("friends_str") != null ) {
      classList.add(FriendList.class);
    } else if ( objectNode.findValue("target_object") != null ) {
      classList.add(UserstreamEvent.class);
    } else if ( objectNode.findValue("follower") != null
        && objectNode.findValue("followee") != null) {
      classList.add(Follow.class);
    } else if ( objectNode.findValue("location") != null
        && objectNode.findValue("user") == null) {
      classList.add(User.class);
    } else {
      classList.add(Tweet.class);
    }

    return classList;
  }

}
