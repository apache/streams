package org.apache.streams.twitter.test;

/*
 * #%L
 * streams-provider-twitter
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.provider.TwitterEventClassifier;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by sblackmon on 12/13/13.
 */
public class TwitterEventClassifierTest {

    private String tweet = "{\"created_at\":\"Wed Dec 11 22:27:34 +0000 2013\",\"id\":410898682381615105,\"id_str\":\"410898682381615105\",\"text\":\"Men's Basketball Single-Game Tickets Available - A limited number of tickets remain for Kentucky's upcoming men's ... http:\\/\\/t.co\\/SH5YZGpdRx\",\"source\":\"\\u003ca href=\\\"http:\\/\\/www.hootsuite.com\\\" rel=\\\"nofollow\\\"\\u003eHootSuite\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":91407775,\"id_str\":\"91407775\",\"name\":\"Winchester, KY\",\"screen_name\":\"winchester_ky\",\"location\":\"\",\"url\":null,\"description\":null,\"protected\":false,\"followers_count\":136,\"friends_count\":0,\"listed_count\":1,\"created_at\":\"Fri Nov 20 19:29:02 +0000 2009\",\"favourites_count\":0,\"utc_offset\":null,\"time_zone\":null,\"geo_enabled\":false,\"verified\":false,\"statuses_count\":1793,\"lang\":\"en\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C0DEED\",\"profile_background_image_url\":\"http:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_image_url_https\":\"https:\\/\\/abs.twimg.com\\/images\\/themes\\/theme1\\/bg.png\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/613854495\\/winchester_sociallogo_normal.jpg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/613854495\\/winchester_sociallogo_normal.jpg\",\"profile_link_color\":\"0084B4\",\"profile_sidebar_border_color\":\"C0DEED\",\"profile_sidebar_fill_color\":\"DDEEF6\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":true,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[{\"url\":\"http:\\/\\/t.co\\/SH5YZGpdRx\",\"expanded_url\":\"http:\\/\\/ow.ly\\/2C2XL1\",\"display_url\":\"ow.ly\\/2C2XL1\",\"indices\":[118,140]}],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"possibly_sensitive\":false,\"filter_level\":\"medium\",\"lang\":\"en\"}\n";
    private String retweet = "{\"created_at\":\"Wed Dec 11 22:27:34 +0000 2013\",\"id\":410898682385797121,\"id_str\":\"410898682385797121\",\"text\":\"RT @hemocional: Cuando te acarici\\u00e9 me di cuenta que hab\\u00eda vivido toda mi vida con las manos vac\\u00edas.\\nALEJANDRO JODOROWSKY.\",\"source\":\"web\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":163149656,\"id_str\":\"163149656\",\"name\":\"Carolina\",\"screen_name\":\"_titinaok\",\"location\":\"Montevideo\",\"url\":\"http:\\/\\/www.youtube.com\\/watch?v=N3v5vZ-tU1E\",\"description\":\"Tantas veces me defin\\u00ed ...Soy nada y todo a la vez\",\"protected\":false,\"followers_count\":41,\"friends_count\":75,\"listed_count\":2,\"created_at\":\"Mon Jul 05 17:35:49 +0000 2010\",\"favourites_count\":4697,\"utc_offset\":-10800,\"time_zone\":\"Buenos Aires\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":5257,\"lang\":\"es\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"C4A64B\",\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/profile_background_images\\/378800000096791690\\/f64a07abbaa735b39ad7655fdaa2f416.jpeg\",\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_background_images\\/378800000096791690\\/f64a07abbaa735b39ad7655fdaa2f416.jpeg\",\"profile_background_tile\":true,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/378800000799213504\\/496d008f457390005825d2eb4ca50a63_normal.jpeg\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/378800000799213504\\/496d008f457390005825d2eb4ca50a63_normal.jpeg\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/163149656\\/1379722210\",\"profile_link_color\":\"BF415A\",\"profile_sidebar_border_color\":\"000000\",\"profile_sidebar_fill_color\":\"B17CED\",\"profile_text_color\":\"3D1957\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweeted_status\":{\"created_at\":\"Wed Dec 11 22:25:06 +0000 2013\",\"id\":410898060206960640,\"id_str\":\"410898060206960640\",\"text\":\"Cuando te acarici\\u00e9 me di cuenta que hab\\u00eda vivido toda mi vida con las manos vac\\u00edas.\\nALEJANDRO JODOROWSKY.\",\"source\":\"\\u003ca href=\\\"http:\\/\\/bufferapp.com\\\" rel=\\\"nofollow\\\"\\u003eBuffer\\u003c\\/a\\u003e\",\"truncated\":false,\"in_reply_to_status_id\":null,\"in_reply_to_status_id_str\":null,\"in_reply_to_user_id\":null,\"in_reply_to_user_id_str\":null,\"in_reply_to_screen_name\":null,\"user\":{\"id\":552929456,\"id_str\":\"552929456\",\"name\":\"Habilidad emocional\",\"screen_name\":\"hemocional\",\"location\":\"\",\"url\":\"http:\\/\\/www.habilidademocional.com\",\"description\":\"Pensamientos y reflexiones para ayudar a mirar la vida de una manera m\\u00e1s saludable y a crecer interiormente cada d\\u00eda m\\u00e1s. #InteligenciaEmocional #Psicolog\\u00eda\",\"protected\":false,\"followers_count\":34307,\"friends_count\":325,\"listed_count\":361,\"created_at\":\"Fri Apr 13 19:00:11 +0000 2012\",\"favourites_count\":44956,\"utc_offset\":3600,\"time_zone\":\"Madrid\",\"geo_enabled\":false,\"verified\":false,\"statuses_count\":24011,\"lang\":\"es\",\"contributors_enabled\":false,\"is_translator\":false,\"profile_background_color\":\"000000\",\"profile_background_image_url\":\"http:\\/\\/a0.twimg.com\\/profile_background_images\\/378800000123681920\\/aab7226ae139f0ff93b04a08a8541477.jpeg\",\"profile_background_image_url_https\":\"https:\\/\\/si0.twimg.com\\/profile_background_images\\/378800000123681920\\/aab7226ae139f0ff93b04a08a8541477.jpeg\",\"profile_background_tile\":false,\"profile_image_url\":\"http:\\/\\/pbs.twimg.com\\/profile_images\\/2430091220\\/zdkea46xhe3g4e65nuwl_normal.gif\",\"profile_image_url_https\":\"https:\\/\\/pbs.twimg.com\\/profile_images\\/2430091220\\/zdkea46xhe3g4e65nuwl_normal.gif\",\"profile_banner_url\":\"https:\\/\\/pbs.twimg.com\\/profile_banners\\/552929456\\/1383180255\",\"profile_link_color\":\"FF00E1\",\"profile_sidebar_border_color\":\"FFFFFF\",\"profile_sidebar_fill_color\":\"F3F3F3\",\"profile_text_color\":\"333333\",\"profile_use_background_image\":true,\"default_profile\":false,\"default_profile_image\":false,\"following\":null,\"follow_request_sent\":null,\"notifications\":null},\"geo\":null,\"coordinates\":null,\"place\":null,\"contributors\":null,\"retweet_count\":9,\"favorite_count\":6,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[]},\"favorited\":false,\"retweeted\":false,\"lang\":\"es\"},\"retweet_count\":0,\"favorite_count\":0,\"entities\":{\"hashtags\":[],\"symbols\":[],\"urls\":[],\"user_mentions\":[{\"screen_name\":\"hemocional\",\"name\":\"Habilidad emocional\",\"id\":552929456,\"id_str\":\"552929456\",\"indices\":[3,14]}]},\"favorited\":false,\"retweeted\":false,\"filter_level\":\"medium\",\"lang\":\"es\"}\n";
    private String delete = "{\"delete\":{\"status\":{\"id\":377518972486553600,\"user_id\":1249045572,\"id_str\":\"377518972486553600\",\"user_id_str\":\"1249045572\"}}}\n";

    @Test
    public void testDetectTweet() {
        Class result = TwitterEventClassifier.detectClass(tweet);
        if( !result.equals(Tweet.class) )
            Assert.fail();
    }

    @Test
    public void testDetectRetweet() {
        Class result = TwitterEventClassifier.detectClass(retweet);
        if( !result.equals(Retweet.class) )
            Assert.fail();
    }

    @Test
    public void testDetectDelete() {
        Class result = TwitterEventClassifier.detectClass(delete);
        if( !result.equals(Delete.class) )
            Assert.fail();
    }
}
