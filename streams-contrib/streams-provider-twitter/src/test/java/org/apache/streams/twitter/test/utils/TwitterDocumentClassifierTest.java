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

import org.apache.streams.twitter.converter.TwitterDocumentClassifier;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Follow;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * Tests {@link: org.apache.streams.twitter.processor.TwitterEventClassifier}
 */
public class TwitterDocumentClassifierTest {

    private String tweet = "{\"created_at\":\"Wed Dec 11 22:27:34 +0000 2013\",\"id\":12345,\"id_str\":\"12345\",\"text\":\"text\",\"source\":\"source\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":91407775,\"id_str\":\"12345\",\"name\":\"name\",\"screen_name\":\"screen_name\",\"location\":\"\",\"url\":null,\"description\":null,\"protected\":false,\"followers_count\":136,\"friends_count\":0,\"listed_count\":1,\"created_at\":\"Fri Nov 20 19:29:02 +0000 2009\",\"favourites_count\":0,\"utc_offset\":null,\"time_zone\":null,\"geo_enabled\":false,\"verified\":false,\"statuses_count\":1793,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/profile_background_image_url.png\",\"profile_background_image_url_https\":\"https:\\/\\/profile_background_image_url_https.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/profile_image_url.jpg\",\"profile_image_url_https\":\"https:\\/\\/profile_image_url_https.jpg\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":true,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/url\",\"expanded_url\":\"http:\\/\\/expanded_url\",\"display_url\":\"display_url\",\"indices\":[118,140]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"filter_level\":\"medium\",\"lang\":\"en\"}\n";
    private String retweet = "{\"created_at\":\"Wed Dec 11 22:27:34 +0000 2013\",\"id\":23456,\"id_str\":\"23456\",\"text\":\"text\",\"source\":\"web\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":163149656,\"id_str\":\"34567\",\"name\":\"name\",\"screen_name\":\"screen_name\",\"location\":\"location\",\"url\":\"http:\\/\\/www.youtube.com\\/watch?v=url\",\"description\":\"description\\u00ed\",\"protected\":false,\"followers_count\":41,\"friends_count\":75,\"listed_count\":2,\"created_at\":\"Mon Jul 05 17:35:49 +0000 2010\",\"favourites_count\":4697,\"utc_offset\":-10800,\"time_zone\":\"Buenos Aires\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":5257,\"lang\":\"es\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C4A64B\",\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/profile_background_images\\/12345\\/12345.jpeg\",\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_background_images\\/12345\\/12345.jpeg\",\"profile_background_tile\":true,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/12345\\/12345.jpeg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/12345\\/12345.jpeg\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/12345\\/12345\",\"profile_link_color\":\"BF415A\",\"profile_sidebar_border_color\":\"000000\",\"profile_sidebar_fill_color\":\"B17CED\",\"profile_text_color\":\"3D1957\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweeted_status\":{\"created_at\":\"Wed Dec 11 22:25:06 +0000 2013\",\"id\":34567,\"id_str\":\"34567\",\"text\":\"text\",\"source\":\"source\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":34567,\"id_str\":\"34567\",\"name\":\"name\",\"screen_name\":\"screen_name\",\"location\":\"\",\"url\":\"http:\\/\\/www.web.com\",\"description\":\"description\",\"protected\":false,\"followers_count\":34307,\"friends_count\":325,\"listed_count\":361,\"created_at\":\"Fri Apr 13 19:00:11 +0000 2012\",\"favourites_count\":44956,\"utc_offset\":3600,\"time_zone\":\"Madrid\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":24011,\"lang\":\"es\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"000000\",\"profile_background_image_url\":\"http:\\/\\/profile_background_image_url.jpeg\",\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_background_images\\/34567\\/34567.jpeg\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/34567\\/34567.gif\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/34567\\/34567.gif\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/34567\\/34567\",\"profile_link_color\":\"FF00E1\",\"profile_sidebar_border_color\":\"FFFFFF\",\"profile_sidebar_fill_color\":\"F3F3F3\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":9,\"favorite_count\":6,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"lang\":\"es\"},\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[{\"screen_name\":\"screen_name\",\"name\":\"name emocional\",\"id\":45678,\"id_str\":\"45678\",\"indices\":[3,14]}]},\"favorited\":false,\"retweeted\":false,\"filter_level\":\"medium\",\"lang\":\"es\"}\n";
    private String delete = "{\"delete\":{\"status\":{\"id\":56789,\"user_id\":67890,\"id_str\":\"56789\",\"user_id_str\":\"67890\"}}}\n";
    private String follow = "{\"follower\":{\"id\":12345},\"followee\":{\"id\":56789}}\n";
    private String user = "{\"location\":\"\",\"default_profile\":true,\"profile_background_tile\":false,\"statuses_count\":1,\"lang\":\"en\",\"profile_link_color\":\"0084B4\",\"id\":67890,\"following\":false,\"protected\":false,\"favourites_count\":0,\"profile_text_color\":\"333333\",\"description\":\"\",\"verified\":false,\"contributors_enabled\":false,\"profile_sidebar_border_color\":\"C0DEED\",\"name\":\"name\",\"profile_background_color\":\"C0DEED\",\"created_at\":\"Fri Apr 17 12:35:56 +0000 2009\",\"is_translation_enabled\":false,\"default_profile_image\":true,\"followers_count\":2,\"profile_image_url_https\":\"https://profile_image_url_https.png\",\"geo_enabled\":false,\"status\":{\"contributors\":null,\"text\":\"Working\",\"geo\":null,\"retweeted\":false,\"in_reply_to_screen_name\":null,\"truncated\":false,\"lang\":\"en\",\"entities\":{\"symbols\":[],\"urls\":[],\"hashtags\":[],\"user_mentions\":[]},\"in_reply_to_status_id_str\":null,\"id\":67890,\"source\":\"web\",\"in_reply_to_user_id_str\":null,\"favorited\":false,\"in_reply_to_status_id\":null,\"retweet_count\":0,\"created_at\":\"Fri Apr 17 12:37:54 +0000 2009\",\"in_reply_to_user_id\":null,\"favorite_count\":0,\"id_str\":\"67890\",\"place\":null,\"coordinates\":null},\"profile_background_image_url\":\"http://abs.twimg.com/profile_background_image_url.png\",\"profile_background_image_url_https\":\"https://abs.twimg.com/images/profile_background_image_url_https.png\",\"follow_request_sent\":false,\"entities\":{\"description\":{\"urls\":[]}},\"url\":null,\"utc_offset\":null,\"time_zone\":null,\"notifications\":false,\"profile_use_background_image\":true,\"friends_count\":1,\"profile_sidebar_fill_color\":\"DDEEF6\",\"screen_name\":\"screen_name\",\"id_str\":\"67890\",\"profile_image_url\":\"http://abs.twimg.com/sticky/default_profile_images/default_profile_1_normal.png\",\"listed_count\":0,\"is_translator\":false}";

    @Test
    public void testDetectTweet() {
        List<Class> detected = new TwitterDocumentClassifier().detectClasses(tweet);
        Assert.assertTrue(detected.size() == 1);
        Class result = detected.get(0);
        if( !result.equals(Tweet.class) )
            Assert.fail();
    }

    @Test
    public void testDetectRetweet() {
        List<Class> detected = new TwitterDocumentClassifier().detectClasses(retweet);
        Assert.assertTrue(detected.size() == 1);
        Class result = detected.get(0);
        if( !result.equals(Retweet.class) )
            Assert.fail();
    }

    @Test
    public void testDetectDelete() {
        List<Class> detected = new TwitterDocumentClassifier().detectClasses(delete);
        Assert.assertTrue(detected.size() == 1);
        Class result = detected.get(0);
        if( !result.equals(Delete.class) )
            Assert.fail();
    }

    @Test
    public void testDetectFollow() {
        List<Class> detected = new TwitterDocumentClassifier().detectClasses(follow);
        Assert.assertTrue(detected.size() == 1);
        Class result = detected.get(0);
        if( !result.equals(Follow.class) )
            Assert.fail();
    }

    @Test
    public void testDetectUser() {
        List<Class> detected = new TwitterDocumentClassifier().detectClasses(user);
        Assert.assertTrue(detected.size() == 1);
        Class result = detected.get(0);
        if (!result.equals(User.class))
            Assert.fail();
    }

}
