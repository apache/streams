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

package org.apache.streams.twitter.test.data;

import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.converter.TwitterDocumentClassifier;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.apache.streams.twitter.converter.TwitterDateTimeFormat.TWITTER_FORMAT;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertThat;

/**
 * Tests serialization / deserialization of twitter jsons.
 */
public class TwitterObjectMapperIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterObjectMapperIT.class);

  private ObjectMapper mapper = StreamsJacksonMapper.getInstance(Lists.newArrayList(TWITTER_FORMAT));

  @Test
  public void tests() {

    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.TRUE);
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
    mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

    InputStream is = TwitterObjectMapperIT.class.getResourceAsStream("/testtweets.txt");
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);

    int tweetlinks = 0;
    int retweetlinks = 0;

    try {
      while (br.ready()) {
        String line = br.readLine();
        if (!StringUtils.isEmpty(line)) {

          LOGGER.info("raw: {}", line);

          Class detected = new TwitterDocumentClassifier().detectClasses(line).get(0);

          ObjectNode event = (ObjectNode) mapper.readTree(line);

          assertThat(event, is(not(nullValue())));

          if ( detected == Tweet.class ) {

            Tweet tweet = mapper.convertValue(event, Tweet.class);

            assertThat(tweet, is(not(nullValue())));
            assertThat(tweet.getCreatedAt(), is(not(nullValue())));
            assertThat(tweet.getText(), is(not(nullValue())));
            assertThat(tweet.getUser(), is(not(nullValue())));

            tweetlinks += Optional.fromNullable(tweet.getEntities().getUrls().size()).or(0);

          } else if ( detected == Retweet.class ) {

            Retweet retweet = mapper.convertValue(event, Retweet.class);

            assertThat(retweet.getRetweetedStatus(), is(not(nullValue())));
            assertThat(retweet.getRetweetedStatus().getCreatedAt(), is(not(nullValue())));
            assertThat(retweet.getRetweetedStatus().getText(), is(not(nullValue())));
            assertThat(retweet.getRetweetedStatus().getUser(), is(not(nullValue())));
            assertThat(retweet.getRetweetedStatus().getUser().getId(), is(not(nullValue())));
            assertThat(retweet.getRetweetedStatus().getUser().getCreatedAt(), is(not(nullValue())));

            retweetlinks += Optional.fromNullable(retweet.getRetweetedStatus().getEntities().getUrls().size()).or(0);

          } else if ( detected == Delete.class ) {

            Delete delete = mapper.convertValue(event, Delete.class);

            assertThat(delete.getDelete(), is(not(nullValue())));
            assertThat(delete.getDelete().getStatus(), is(not(nullValue())));
            assertThat(delete.getDelete().getStatus().getId(), is(not(nullValue())));
            assertThat(delete.getDelete().getStatus().getUserId(), is(not(nullValue())));

          } else {
            Assert.fail();
          }

        }
      }
    } catch ( Exception ex ) {
      LOGGER.error("Exception: ", ex);
      Assert.fail();
    }

    assertThat(tweetlinks, is(greaterThan(0)));
    assertThat(retweetlinks, is(greaterThan(0)));

  }
}
