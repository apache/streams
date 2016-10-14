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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.streams.converter.ActivityConverterUtil;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Follow;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Tests {@link: org.apache.streams.twitter.converter.*}
 */
public class TwitterActivityConvertersTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterActivityConvertersTest.class);

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance(Lists.newArrayList(TwitterDateTimeFormat.TWITTER_FORMAT));

    private ActivityConverterUtil activityConverterUtil = ActivityConverterUtil.getInstance();

    private String tweetJson = "{\"created_at\":\"Wed Dec 11 22:27:34 +0000 2013\",\"id\":12345,\"id_str\":\"12345\",\"text\":\"text\",\"source\":\"source\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":91407775,\"id_str\":\"12345\",\"name\":\"name\",\"screen_name\":\"screen_name\",\"location\":\"\",\"url\":null,\"description\":null,\"protected\":false,\"followers_count\":136,\"friends_count\":0,\"listed_count\":1,\"created_at\":\"Fri Nov 20 19:29:02 +0000 2009\",\"favourites_count\":0,\"utc_offset\":null,\"time_zone\":null,\"geo_enabled\":false,\"verified\":false,\"statuses_count\":1793,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/profile_background_image_url.png\",\"profile_background_image_url_https\":\"https:\\/\\/profile_background_image_url_https.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/profile_image_url.jpg\",\"profile_image_url_https\":\"https:\\/\\/profile_image_url_https.jpg\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":true,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/url\",\"expanded_url\":\"http:\\/\\/expanded_url\",\"display_url\":\"display_url\",\"indices\":[118,140]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"filter_level\":\"medium\",\"lang\":\"en\"}\n";
    private String retweetJson = "{\"created_at\":\"Wed Dec 11 22:27:34 +0000 2013\",\"id\":23456,\"id_str\":\"23456\",\"text\":\"text\",\"source\":\"web\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":163149656,\"id_str\":\"34567\",\"name\":\"name\",\"screen_name\":\"screen_name\",\"location\":\"location\",\"url\":\"http:\\/\\/www.youtube.com\\/watch?v=url\",\"description\":\"description\\u00ed\",\"protected\":false,\"followers_count\":41,\"friends_count\":75,\"listed_count\":2,\"created_at\":\"Mon Jul 05 17:35:49 +0000 2010\",\"favourites_count\":4697,\"utc_offset\":-10800,\"time_zone\":\"Buenos Aires\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":5257,\"lang\":\"es\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C4A64B\",\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/profile_background_images\\/12345\\/12345.jpeg\",\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_background_images\\/12345\\/12345.jpeg\",\"profile_background_tile\":true,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/12345\\/12345.jpeg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/12345\\/12345.jpeg\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/12345\\/12345\",\"profile_link_color\":\"BF415A\",\"profile_sidebar_border_color\":\"000000\",\"profile_sidebar_fill_color\":\"B17CED\",\"profile_text_color\":\"3D1957\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweeted_status\":{\"created_at\":\"Wed Dec 11 22:25:06 +0000 2013\",\"id\":34567,\"id_str\":\"34567\",\"text\":\"text\",\"source\":\"source\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":34567,\"id_str\":\"34567\",\"name\":\"name\",\"screen_name\":\"screen_name\",\"location\":\"\",\"url\":\"http:\\/\\/www.web.com\",\"description\":\"description\",\"protected\":false,\"followers_count\":34307,\"friends_count\":325,\"listed_count\":361,\"created_at\":\"Fri Apr 13 19:00:11 +0000 2012\",\"favourites_count\":44956,\"utc_offset\":3600,\"time_zone\":\"Madrid\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":24011,\"lang\":\"es\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"000000\",\"profile_background_image_url\":\"http:\\/\\/profile_background_image_url.jpeg\",\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_background_images\\/34567\\/34567.jpeg\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/34567\\/34567.gif\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/34567\\/34567.gif\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/34567\\/34567\",\"profile_link_color\":\"FF00E1\",\"profile_sidebar_border_color\":\"FFFFFF\",\"profile_sidebar_fill_color\":\"F3F3F3\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":9,\"favorite_count\":6,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"lang\":\"es\"},\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[{\"screen_name\":\"screen_name\",\"name\":\"name emocional\",\"id\":45678,\"id_str\":\"45678\",\"indices\":[3,14]}]},\"favorited\":false,\"retweeted\":false,\"filter_level\":\"medium\",\"lang\":\"es\"}\n";
    private String deleteJson = "{\"delete\":{\"status\":{\"id\":56789,\"user_id\":67890,\"id_str\":\"56789\",\"user_id_str\":\"67890\"}}}\n";
    private String followJson = "{\"follower\":{\"id\":12345},\"followee\":{\"id\":56789}}\n";

    @Test
    public void testConvertTweet() throws Exception  {
        Tweet tweet = mapper.readValue(tweetJson, Tweet.class);
        List<Activity> activityList = activityConverterUtil.convert(tweet);
        Assert.assertTrue(activityList.size() == 1);
        Activity activity = activityList.get(0);
        if( !ActivityUtil.isValid(activity) )
            Assert.fail();
    }

    @Test
    public void testConvertRetweet() throws Exception  {
        Retweet retweet = mapper.readValue(retweetJson, Retweet.class);
        List<Activity> activityList = activityConverterUtil.convert(retweet);
        Assert.assertTrue(activityList.size() == 1);
        Activity activity = activityList.get(0);
        if( !ActivityUtil.isValid(activity) )
            Assert.fail();
    }

    @Test
    public void testConvertDelete() throws Exception  {
        Delete delete = mapper.readValue(retweetJson, Delete.class);
        List<Activity> activityList = activityConverterUtil.convert(delete);
        Assert.assertTrue(activityList.size() == 1);
        Activity activity = activityList.get(0);
        if( !ActivityUtil.isValid(activity) )
            Assert.fail();
    }

    @Test
    public void testConvertFollow() throws Exception {
        Follow follow = mapper.readValue(retweetJson, Follow.class);
        List<Activity> activityList = activityConverterUtil.convert(follow);
        Assert.assertTrue(activityList.size() == 1);
        Activity activity = activityList.get(0);
        if( !ActivityUtil.isValid(activity) )
            Assert.fail();
    }

    @Test
    public void testConvertTweetString() {
        List<Activity> activityList = activityConverterUtil.convert(tweetJson);
        Assert.assertTrue(activityList.size() == 1);
        Activity activity = activityList.get(0);
        if( !ActivityUtil.isValid(activity) )
            Assert.fail();
    }

    @Test
    public void testConvertRetweetString() {
        List<Activity> activityList = activityConverterUtil.convert(retweetJson);
        Assert.assertTrue(activityList.size() == 1);
        Activity activity = activityList.get(0);
        if( !ActivityUtil.isValid(activity) )
            Assert.fail();
    }

    @Test
    public void testConvertDeleteString() {
        List<Activity> activityList = activityConverterUtil.convert(deleteJson);
        Assert.assertTrue(activityList.size() == 1);
        Activity activity = activityList.get(0);
        if( !ActivityUtil.isValid(activity) )
            Assert.fail();
    }

    @Test
    public void testConvertFollowString() {
        List<Activity> activityList = activityConverterUtil.convert(followJson);
        Assert.assertTrue(activityList.size() == 1);
        Activity activity = activityList.get(0);
        if( !ActivityUtil.isValid(activity) )
            Assert.fail();
    }
}
