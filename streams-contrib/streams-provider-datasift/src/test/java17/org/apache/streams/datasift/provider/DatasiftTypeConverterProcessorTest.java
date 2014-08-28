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

package org.apache.streams.datasift.provider;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.datasift.processor.DatasiftTypeConverterProcessor;
import org.apache.streams.pojo.json.Activity;
import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.*;

/**
 *
 */
public class DatasiftTypeConverterProcessorTest {

    private static final String DATASIFT_JSON = "{\"demographic\":{\"gender\":\"female\"},\"interaction\":{\"schema\":{\"version\":3},\"source\":\"Twitter for Android\",\"author\":{\"username\":\"ViiOLeee\",\"name\":\"Violeta Anguita\",\"id\":70931384,\"avatar\":\"http://pbs.twimg.com/profile_images/378800000851401229/bbf480cde2e9923a1d20acd393da0212_normal.jpeg\",\"link\":\"http://twitter.com/ViiOLeee\",\"language\":\"en\"},\"type\":\"twitter\",\"created_at\":\"Tue, 27 May 2014 22:38:15 +0000\",\"received_at\":1.401230295658E9,\"content\":\"RT @AliiAnguita: \\\"@Pharrell: Loved working with @edsheeran on Sing. He's a genius. https://t.co/wB2qKyJMRw\\\" @ViiOLeee  look at this!\",\"id\":\"1e3e5ef97532a580e0741841f5746728\",\"link\":\"http://twitter.com/ViiOLeee/status/471420141989666817\",\"mentions\":[\"Pharrell\",\"edsheeran\",\"ViiOLeee\",\"AliiAnguita\"],\"mention_ids\":[338084918,85452649,70931384]},\"klout\":{\"score\":34},\"language\":{\"tag\":\"en\",\"tag_extended\":\"en\",\"confidence\":98},\"links\":{\"code\":[200],\"created_at\":[\"Tue, 27 May 2014 14:28:06 +0000\"],\"meta\":{\"charset\":[\"UTF-8\"],\"content_type\":[\"text/html\"],\"description\":[\"Official Video for Ed Sheeran&#39;s track SING Get this track on iTunes: http://smarturl.it/EdSing Pre-order &#39;x&#39; on iTunes and get &#39;One&#39; instantly: http://smartu...\"],\"keywords\":[[\"ed sheeran\",\"ed sheeran sing\",\"ed sheeran new album\",\"Ed Sheeran (Musical Artist)\",\"ed sheeran one\",\"ed sheeran fault in our stars\",\"ed sheeran all of the stars\",\"s...\"]],\"lang\":[\"en\"],\"opengraph\":[{\"site_name\":\"YouTube\",\"url\":\"http://www.youtube.com/watch?v=tlYcUqEPN58\",\"title\":\"Ed Sheeran - SING [Official Video]\",\"image\":\"https://i1.ytimg.com/vi/tlYcUqEPN58/maxresdefault.jpg\",\"description\":\"Official Video for Ed Sheeran&#39;s track SING Get this track on iTunes: http://smarturl.it/EdSing Pre-order &#39;x&#39; on iTunes and get &#39;One&#39; instantly: http://smartu...\",\"type\":\"video\"}],\"twitter\":[{\"card\":\"player\",\"site\":\"@youtube\",\"url\":\"http://www.youtube.com/watch?v=tlYcUqEPN58\",\"title\":\"Ed Sheeran - SING [Official Video]\",\"description\":\"Official Video for Ed Sheeran&#39;s track SING Get this track on iTunes: http://smarturl.it/EdSing Pre-order &#39;x&#39; on iTunes and get &#39;One&#39; instantly: http://smartu...\",\"image\":\"https://i1.ytimg.com/vi/tlYcUqEPN58/maxresdefault.jpg\",\"app\":{\"iphone\":{\"name\":\"YouTube\",\"id\":\"544007664\",\"url\":\"vnd.youtube://watch/tlYcUqEPN58\"},\"ipad\":{\"name\":\"YouTube\",\"id\":\"544007664\",\"url\":\"vnd.youtube://watch/tlYcUqEPN58\"},\"googleplay\":{\"name\":\"YouTube\",\"id\":\"com.google.android.youtube\",\"url\":\"http://www.youtube.com/watch?v=tlYcUqEPN58\"}},\"player\":\"https://www.youtube.com/embed/tlYcUqEPN58\",\"player_width\":\"1280\",\"player_height\":\"720\"}]},\"normalized_url\":[\"https://youtube.com/watch?v=tlYcUqEPN58\"],\"retweet_count\":[0],\"title\":[\"Ed Sheeran - SING [Official Video] - YouTube\"],\"url\":[\"https://www.youtube.com/watch?v=tlYcUqEPN58\"]},\"twitter\":{\"id\":\"471420141989666817\",\"retweet\":{\"text\":\"\\\"@Pharrell: Loved working with @edsheeran on Sing. He's a genius. https://t.co/wB2qKyJMRw\\\" @ViiOLeee  look at this!\",\"id\":\"471420141989666817\",\"user\":{\"name\":\"Violeta Anguita\",\"description\":\"La vida no seria la fiesta que todos esperamos, pero mientras estemos aqui debemos BAILAR!!! #ErasmusOnceErasmusForever\",\"location\":\"Espanhaa..Olaa!\",\"statuses_count\":5882,\"followers_count\":249,\"friends_count\":1090,\"screen_name\":\"ViiOLeee\",\"profile_image_url\":\"http://pbs.twimg.com/profile_images/378800000851401229/bbf480cde2e9923a1d20acd393da0212_normal.jpeg\",\"profile_image_url_https\":\"https://pbs.twimg.com/profile_images/378800000851401229/bbf480cde2e9923a1d20acd393da0212_normal.jpeg\",\"lang\":\"en\",\"time_zone\":\"Madrid\",\"utc_offset\":7200,\"listed_count\":1,\"id\":70931384,\"id_str\":\"70931384\",\"geo_enabled\":false,\"verified\":false,\"favourites_count\":275,\"created_at\":\"Wed, 02 Sep 2009 10:19:59 +0000\"},\"source\":\"<a href=\\\"http://twitter.com/download/android\\\" rel=\\\"nofollow\\\">Twitter for Android</a>\",\"count\":1,\"created_at\":\"Tue, 27 May 2014 22:38:15 +0000\",\"mentions\":[\"Pharrell\",\"edsheeran\",\"ViiOLeee\",\"AliiAnguita\"],\"mention_ids\":[338084918,85452649,70931384],\"links\":[\"https://www.youtube.com/watch?v=tlYcUqEPN58\"],\"display_urls\":[\"youtube.com/watch?v=tlYcUq���\"],\"domains\":[\"www.youtube.com\"],\"lang\":\"en\"},\"retweeted\":{\"id\":\"471419867078209536\",\"user\":{\"name\":\"Alicia Anguita \",\"description\":\"Estudiante de Ingenieria de la Edificaci��n en Granada.\",\"statuses_count\":371,\"followers_count\":185,\"friends_count\":404,\"screen_name\":\"AliiAnguita\",\"profile_image_url\":\"http://pbs.twimg.com/profile_images/424248659677442048/qCPZL8c9_normal.jpeg\",\"profile_image_url_https\":\"https://pbs.twimg.com/profile_images/424248659677442048/qCPZL8c9_normal.jpeg\",\"lang\":\"es\",\"listed_count\":0,\"id\":561201891,\"id_str\":\"561201891\",\"geo_enabled\":false,\"verified\":false,\"favourites_count\":17,\"created_at\":\"Mon, 23 Apr 2012 13:11:44 +0000\"},\"source\":\"<a href=\\\"http://twitter.com/download/android\\\" rel=\\\"nofollow\\\">Twitter for Android</a>\",\"created_at\":\"Tue, 27 May 2014 22:37:09 +0000\"}}}";

    @Test
    public void testTypeConverterToString() {
        final String ID = "1";
        StreamsProcessor processor = new DatasiftTypeConverterProcessor(String.class);
        processor.prepare(null);
        StreamsDatum datum = new StreamsDatum(DATASIFT_JSON, ID);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(1, result.size());
        StreamsDatum resultDatum = result.get(0);
        assertNotNull(resultDatum);
        assertNotNull(resultDatum.getDocument());
        assertTrue(resultDatum.getDocument() instanceof String);
        assertEquals(ID, resultDatum.getId());
    }

    @Test
    public void testTypeConverterToActivity() {
        final String ID = "1";
        StreamsProcessor processor = new DatasiftTypeConverterProcessor(Activity.class);
        processor.prepare(null);
        StreamsDatum datum = new StreamsDatum(DATASIFT_JSON, ID);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(1, result.size());
        StreamsDatum resultDatum = result.get(0);
        assertNotNull(resultDatum);
        assertNotNull(resultDatum.getDocument());
        assertTrue(resultDatum.getDocument() instanceof Activity);
        assertEquals(ID, resultDatum.getId());
    }



}
