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

package org.apache.streams.twitter.test.utils;

import org.apache.streams.converter.ActivityObjectConverterProcessorConfiguration;
import org.apache.streams.converter.ActivityObjectConverterUtil;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.converter.TwitterDocumentClassifier;
import org.apache.streams.twitter.converter.TwitterJsonUserActivityObjectConverter;
import org.apache.streams.twitter.pojo.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests {org.apache.streams.twitter.converter.*}
 */
public class TwitterActivityObjectsConvertersTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterActivityObjectsConvertersTest.class);

  private ObjectMapper mapper = StreamsJacksonMapper.getInstance(Stream.of(TwitterDateTimeFormat.TWITTER_FORMAT)
      .collect(Collectors.toList()));

  private ActivityObjectConverterProcessorConfiguration activityObjectConverterProcessorConfiguration =
      new ActivityObjectConverterProcessorConfiguration()
          .withClassifiers(Stream.of(new TwitterDocumentClassifier()).collect(Collectors.toList()))
          .withConverters(Stream.of(new TwitterJsonUserActivityObjectConverter()).collect(Collectors.toList()));

  private ActivityObjectConverterUtil activityObjectConverterUtil = ActivityObjectConverterUtil.getInstance(activityObjectConverterProcessorConfiguration);

  private String userJson = "{\"id\":1663018644,\"id_str\":\"1663018644\",\"name\":\"M.R. Clark\",\"screen_name\":\"cantennisfan\",\"location\":\"\",\"url\":null,\"description\":null,\"protected\":false,\"verified\":false,\"followers_count\":0,\"friends_count\":5,\"listed_count\":0,\"favourites_count\":2,\"statuses_count\":72,\"created_at\":\"Sun Aug 11 17:23:47 +0000 2013\",\"utc_offset\":-18000,\"time_zone\":\"Eastern Time (US & Canada)\",\"geo_enabled\":false,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http://abs.twimg.com/images/themes/theme1/bg.png\",\"profile_background_image_url_https\":\"https://abs.twimg.com/images/themes/theme1/bg.png\",\"profile_background_tile\":false,\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"profile_image_url\":\"http://abs.twimg.com/sticky/default_profile_images/default_profile_0_normal.png\",\"profile_image_url_https\":\"https://abs.twimg.com/sticky/default_profile_images/default_profile_0_normal.png\",\"default_profile\":true,\"default_profile_image\":true,\"following\":null,\"follow_request_sent\":null,\"notifications\":null,\"status\":{\"created_at\":\"Thu Jan 01 14:11:48 +0000 2015\",\"id\":550655634706669568,\"id_str\":\"550655634706669568\",\"text\":\"CBC Media Centre - CBC - Air Farce New Year's Eve 2014/2015: http://t.co/lMlL9VbC5e\",\"source\":\"<a href=\\\"https://dev.twitter.com/docs/tfw\\\" rel=\\\"nofollow\\\">Twitter for Websites</a>\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"trends\":[],\"urls\":[{\"url\":\"http://t.co/lMlL9VbC5e\",\"expanded_url\":\"http://www.cbc.ca/mediacentre/air-farce-new-years-eve-20142015.html#.VKVVarDhVxR.twitter\",\"display_url\":\"cbc.ca/mediacentre/ai…\",\"indices\":[61,83]}],\"user_mentions\":[],\"symbols\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"filter_level\":\"medium\",\"lang\":\"en\",\"timestamp_ms\":\"1420121508658\"}}\n";

  @Test
  public void testConvertUser() throws Exception {
    User user = mapper.readValue(userJson, User.class);
    ActivityObject activityObject = activityObjectConverterUtil.convert(user);
    assert ( activityObject != null );
    if ( !ActivityUtil.isValid(activityObject) ) {
      Assert.fail();
    }
  }

  @Test
  public void testConvertUserString() {
    ActivityObject activityObject = activityObjectConverterUtil.convert(userJson);
    assert ( activityObject != null );
    if ( !ActivityUtil.isValid(activityObject) ) {
      Assert.fail();
    }
  }
}
