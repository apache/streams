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

package org.apache.streams.twitter.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.jackson.TypeConverterProcessor;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.serializer.StreamsTwitterMapper;
import org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
* Created with IntelliJ IDEA.
* User: sblackmon
* Date: 8/20/13
* Time: 5:57 PM
* To change this template use File | Settings | File Templates.
*/
public class SimpleTweetTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleTweetTest.class);

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance(Lists.newArrayList(StreamsTwitterMapper.TWITTER_FORMAT));

    private static final String TWITTER_JSON= "{\"created_at\":\"Wed Dec 11 22:27:34 +0000 2013\",\"id\":410898682356047872,\"id_str\":\"410898682356047872\",\"text\":\"RT @ughhblog: RRome (Brooklyn, NY) \\u2013 MY GIRL http:\\/\\/t.co\\/x6uxX9PLsH via @indierapblog @RRoseRRome\",\"source\":\"\\u003ca href=\\\"https:\\/\\/about.twitter.com\\/products\\/tweetdeck\\\" rel=\\\"nofollow\\\"\\u003eTweetDeck\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":70463906,\"id_str\":\"70463906\",\"name\":\"MHM DESIGNS, LLC\",\"screen_name\":\"MHMDESIGNS\",\"location\":\"Los Angeles New York\",\"urls\":\"http:\\/\\/www.mhmdesigns.com\",\"description\":\"Multi Media Made Simple- Web desig, Graphic Design, Internet Marketing, Photography, Video Production and much much more.\",\"protected\":false,\"followers_count\":10,\"friends_count\":64,\"listed_count\":1,\"created_at\":\"Mon Aug 31 18:31:54 +0000 2009\",\"favourites_count\":0,\"utc_offset\":-28800,\"time_zone\":\"Pacific Time (US & Canada)\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":87,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"9AE4E8\",\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/profile_background_images\\/33456434\\/body.png\",\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_background_images\\/33456434\\/body.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/391494416\\/mhm_design_logo__normal.png\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/391494416\\/mhm_design_logo__normal.png\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"BDDCAD\",\"profile_sidebar_fill_color\":\"DDFFCC\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweeted_status\":{\"created_at\":\"Wed Dec 11 10:56:49 +0000 2013\",\"id\":410724848306892800,\"id_str\":\"410724848306892800\",\"text\":\"RRome (Brooklyn, NY) \\u2013 MY GIRL http:\\/\\/t.co\\/x6uxX9PLsH via @indierapblog @RRoseRRome\",\"source\":\"\\u003ca href=\\\"http:\\/\\/twitter.com\\/tweetbutton\\\" rel=\\\"nofollow\\\"\\u003eTweet Button\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":538836510,\"id_str\":\"538836510\",\"name\":\"UGHHBlog\",\"screen_name\":\"ughhblog\",\"location\":\"Los Angeles\",\"urls\":\"http:\\/\\/www.undergroundhiphopblog.com\",\"description\":\"http:\\/\\/UNDERGROUNDHIPHOPBLOG.com: A top Indie\\/Underground Hip Hop community blog. Submission Email: ughhblog@gmail.com \\/\\/\\/ Official Host: @pawz1\",\"protected\":false,\"followers_count\":2598,\"friends_count\":373,\"listed_count\":25,\"created_at\":\"Wed Mar 28 05:40:49 +0000 2012\",\"favourites_count\":423,\"utc_offset\":-28800,\"time_zone\":\"Pacific Time (US & Canada)\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":9623,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"131516\",\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/profile_background_images\\/544717772\\/UGHHBlogLogo.jpg\",\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_background_images\\/544717772\\/UGHHBlogLogo.jpg\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/2583702975\\/uas8528qzzdlnsb7igzn_normal.jpeg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/2583702975\\/uas8528qzzdlnsb7igzn_normal.jpeg\",\"profile_link_color\":\"009999\",\"profile_sidebar_border_color\":\"EEEEEE\",\"profile_sidebar_fill_color\":\"EFEFEF\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":4,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"urls\":\"http:\\/\\/t.co\\/x6uxX9PLsH\",\"expanded_url\":\"http:\\/\\/indierapblog.com\\/rrome-brooklyn-ny-my-girl\\/\",\"display_url\":\"indierapblog.com\\/rrome-brooklyn\\u2026\",\"indices\":[31,53]}],\"user_mentions\":[{\"screen_name\":\"IndieRapBlog\",\"name\":\"IndieRapBlog.com\",\"id\":922776728,\"id_str\":\"922776728\",\"indices\":[58,71]},{\"screen_name\":\"RRoseRRome\",\"name\":\"RRome\",\"id\":76371478,\"id_str\":\"76371478\",\"indices\":[72,83]}]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"lang\":\"en\"},\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"urls\":\"http:\\/\\/t.co\\/x6uxX9PLsH\",\"expanded_url\":\"http:\\/\\/indierapblog.com\\/rrome-brooklyn-ny-my-girl\\/\",\"display_url\":\"indierapblog.com\\/rrome-brooklyn\\u2026\",\"indices\":[45,67]}],\"user_mentions\":[{\"screen_name\":\"ughhblog\",\"name\":\"UGHHBlog\",\"id\":538836510,\"id_str\":\"538836510\",\"indices\":[3,12]},{\"screen_name\":\"IndieRapBlog\",\"name\":\"IndieRapBlog.com\",\"id\":922776728,\"id_str\":\"922776728\",\"indices\":[72,85]},{\"screen_name\":\"RRoseRRome\",\"name\":\"RRome\",\"id\":76371478,\"id_str\":\"76371478\",\"indices\":[86,97]}]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"filter_level\":\"medium\",\"lang\":\"en\"}";

    private TwitterJsonActivitySerializer twitterJsonActivitySerializer = new TwitterJsonActivitySerializer();

    @Test
    public void Tests()
    {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

        InputStream is = SimpleTweetTest.class.getResourceAsStream("/testtweets.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        ObjectNode event = null;
        try {
            event = (ObjectNode) mapper.readTree(TWITTER_JSON);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }

        assertThat(event, is(not(nullValue())));

        Tweet tweet = mapper.convertValue(event, Tweet.class);

        assertThat(tweet, is(not(nullValue())));
        assertThat(tweet.getCreatedAt(), is(not(nullValue())));
        assertThat(tweet.getText(), is(not(nullValue())));
        assertThat(tweet.getUser(), is(not(nullValue())));

        Activity activity = null;
        try {
            activity = twitterJsonActivitySerializer.deserialize(TWITTER_JSON);
        } catch (ActivitySerializerException e) {
            e.printStackTrace();
            Assert.fail();
        }

        try {
            TypeConverterProcessor converter = new TypeConverterProcessor(String.class, Activity.class);
            converter.prepare(null);
            converter.process(new StreamsDatum(TWITTER_JSON));
        } catch (Throwable e) {
            e.printStackTrace();
            Assert.fail();
        }

        assertThat(activity, is(not(nullValue())));

        assertThat(activity.getId(), is(not(nullValue())));
        assertThat(activity.getActor(), is(not(nullValue())));
        assertThat(activity.getActor().getId(), is(not(nullValue())));
        assertThat(activity.getVerb(), is(not(nullValue())));
        assertThat(activity.getProvider(), is(not(nullValue())));

    }

    @Test
    public void testTweetRetweetStatus() throws Exception {
        String json = "{\"text\":\"RT @cnnbrk: Ebola test for Texas deputy comes back negative, state health officials say. http://t.co/hucKJBgORA\",\"retweeted\":false,\"truncated\":false,\"entities\":{\"user_mentions\":[{\"id\":428333,\"name\":\"CNN Breaking News\",\"indices\":[3,10],\"screen_name\":\"cnnbrk\",\"id_str\":\"428333\"}],\"hashtags\":[],\"urls\":[{\"expanded_url\":\"http://cnn.it/1vUBvoa\",\"indices\":[89,111],\"display_url\":\"cnn.it/1vUBvoa\",\"url\":\"http://t.co/hucKJBgORA\"}],\"symbols\":[]},\"id\":520313386975121408,\"source\":\"<a href=\\\"http://twitter.com\\\" rel=\\\"nofollow\\\">Twitter Web Client</a>\",\"lang\":\"en\",\"favorited\":false,\"favorite_count\":0,\"possibly_sensitive\":false,\"created_at\":\"2014-10-09T20:42:33.000Z\",\"retweet_count\":404,\"id_str\":\"520313386975121408\",\"user\":{\"location\":\"\",\"default_profile\":false,\"statuses_count\":49664,\"profile_background_tile\":false,\"lang\":\"en\",\"profile_link_color\":\"004287\",\"entities\":{\"description\":{\"urls\":[]},\"url\":{\"urls\":[{\"expanded_url\":\"http://www.cnn.com\",\"indices\":[0,22],\"display_url\":\"cnn.com\",\"url\":\"http://t.co/IaghNW8Xm2\"}]}},\"id\":759251,\"protected\":false,\"favourites_count\":708,\"profile_text_color\":\"000000\",\"verified\":true,\"description\":\"It’s our job to #GoThere and tell the most difficult stories. Come with us!\",\"contributors_enabled\":false,\"name\":\"CNN\",\"profile_sidebar_border_color\":\"000000\",\"profile_background_color\":\"323232\",\"created_at\":\"2007-02-09T00:35:02.000Z\",\"default_profile_image\":false,\"followers_count\":14333004,\"geo_enabled\":false,\"profile_image_url_https\":\"https://pbs.twimg.com/profile_images/508960761826131968/LnvhR8ED_normal.png\",\"profile_background_image_url\":\"http://pbs.twimg.com/profile_background_images/515228058286952449/zVfUxqPl.jpeg\",\"profile_background_image_url_https\":\"https://pbs.twimg.com/profile_background_images/515228058286952449/zVfUxqPl.jpeg\",\"follow_request_sent\":false,\"url\":\"http://t.co/IaghNW8Xm2\",\"utc_offset\":-14400,\"time_zone\":\"Eastern Time (US & Canada)\",\"profile_use_background_image\":false,\"friends_count\":997,\"profile_sidebar_fill_color\":\"EEEEEE\",\"screen_name\":\"CNN\",\"id_str\":\"759251\",\"profile_image_url\":\"http://pbs.twimg.com/profile_images/508960761826131968/LnvhR8ED_normal.png\",\"is_translator\":false,\"listed_count\":100250,\"following\":false,\"is_translation_enabled\":true,\"notifications\":false,\"profile_banner_url\":\"https://pbs.twimg.com/profile_banners/759251/1412100311\"},\"extensions\":{\"SimpleContentRecord\":{\"title\":\"CNN\",\"description\":\"It’s our job to #GoThere and tell the most difficult stories. Come with us!\",\"content\":\"RT @cnnbrk: Ebola test for Texas deputy comes back negative, state health officials say. http://t.co/hucKJBgORA\",\"dateTime\":\"2014-10-09T20:42:33.000Z\",\"source\":\"twitter\",\"author\":\"CNN\",\"deserializationClass\":\"com.w2olabs.analytics.files.content.SimpleContentRecord\"}},\"place\":null,\"retweeted_status\":{\"contributors\":null,\"text\":\"Ebola test for Texas deputy comes back negative, state health officials say. http://t.co/hucKJBgORA\",\"geo\":null,\"retweeted\":false,\"in_reply_to_screen_name\":null,\"possibly_sensitive\":false,\"truncated\":false,\"lang\":\"en\",\"entities\":{\"symbols\":[],\"urls\":[{\"expanded_url\":\"http://cnn.it/1vUBvoa\",\"indices\":[77,99],\"display_url\":\"cnn.it/1vUBvoa\",\"url\":\"http://t.co/hucKJBgORA\"}],\"hashtags\":[],\"user_mentions\":[]},\"in_reply_to_status_id_str\":null,\"id\":520312974192672769,\"source\":\"<a href=\\\"http://twitter.com\\\" rel=\\\"nofollow\\\">Twitter Web Client</a>\",\"in_reply_to_user_id_str\":null,\"favorited\":false,\"in_reply_to_status_id\":null,\"retweet_count\":404,\"created_at\":\"Thu Oct 09 20:40:54 +0000 2014\",\"in_reply_to_user_id\":null,\"favorite_count\":399,\"id_str\":\"520312974192672769\",\"place\":null,\"user\":{\"location\":\"Everywhere\",\"default_profile\":false,\"profile_background_tile\":false,\"statuses_count\":35392,\"lang\":\"en\",\"profile_link_color\":\"004287\",\"id\":428333,\"following\":false,\"protected\":false,\"favourites_count\":13,\"profile_text_color\":\"000000\",\"description\":\"Breaking News from CNN, via the http://t.co/2TcIcA9MAX homepage team. Now 19M strong. Check @cnn for all things CNN, breaking and more.\",\"verified\":true,\"contributors_enabled\":false,\"profile_sidebar_border_color\":\"DADADA\",\"name\":\"CNN Breaking News\",\"profile_background_color\":\"323232\",\"created_at\":\"Tue Jan 02 01:48:14 +0000 2007\",\"is_translation_enabled\":true,\"default_profile_image\":false,\"followers_count\":19619559,\"profile_image_url_https\":\"https://pbs.twimg.com/profile_images/1762504301/128x128_cnnbrk_avatar_normal.gif\",\"geo_enabled\":false,\"profile_background_image_url\":\"http://pbs.twimg.com/profile_background_images/17349501/CNN_Twitter_Background_05.2009.GIF\",\"profile_background_image_url_https\":\"https://pbs.twimg.com/profile_background_images/17349501/CNN_Twitter_Background_05.2009.GIF\",\"follow_request_sent\":false,\"entities\":{\"description\":{\"urls\":[{\"expanded_url\":\"http://CNN.com\",\"indices\":[32,54],\"display_url\":\"CNN.com\",\"url\":\"http://t.co/2TcIcA9MAX\"}]},\"url\":{\"urls\":[{\"expanded_url\":\"http://cnn.com/\",\"indices\":[0,22],\"display_url\":\"cnn.com\",\"url\":\"http://t.co/HjKR4rob8d\"}]}},\"url\":\"http://t.co/HjKR4rob8d\",\"utc_offset\":-14400,\"time_zone\":\"Eastern Time (US & Canada)\",\"notifications\":false,\"profile_use_background_image\":true,\"friends_count\":112,\"profile_sidebar_fill_color\":\"EEEEEE\",\"screen_name\":\"cnnbrk\",\"id_str\":\"428333\",\"profile_image_url\":\"http://pbs.twimg.com/profile_images/1762504301/128x128_cnnbrk_avatar_normal.gif\",\"listed_count\":158418,\"is_translator\":false},\"coordinates\":null},\"geo\":null}";

        Tweet tweet = StreamsJacksonMapper.getInstance().readValue(json, Tweet.class);

        Assert.assertNotNull(tweet);
        /*Assert.assertNotNull(tweet.getRetweetedStatus());
        Assert.assertNotNull(tweet.getRetweetedStatus().getUser());

        Assert.assertEquals("cnnbrk",tweet.getRetweetedStatus().getUser().getScreenName());*/

    }
}
