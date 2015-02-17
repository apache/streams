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
package com.youtube.serializer;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.api.services.youtube.model.Video;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Provider;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class YoutubeVideoSerDeTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(YoutubeVideoSerDeTest.class);
    private final String testVideo = "{\"etag\":\"\\\"4FSIjSQU83ZJMYAO0IqRYMvZX98/V0q3OIauZ3ZAkszLUDbHL45yEGM\\\"\",\"id\":\"sUOepRctwVE\",\"kind\":\"youtube#video\",\"snippet\":{\"channelId\":\"UCNENOn2nmwguQYkejKhJGPQ\",\"channelTitle\":\"Carilion Clinic\",\"description\":\"Join Carilion Clinic's Heart Failure experts for a LIVE Google+ Hangout on Feb. 23, 12:30-1 p.m. to learn more about heart failure, treatment options, and lifestyle changes. Learn more: https://plus.google.com/u/0/events/cj074q9r6csgv6i2kqhi2isc6k0\",\"publishedAt\":{\"value\":1422977409000,\"dateOnly\":false,\"timeZoneShift\":-360},\"thumbnails\":{\"default\":{\"height\":480,\"url\":\"https://i.ytimg.com/vi/sUOepRctwVE/sddefault.jpg\",\"width\":640}},\"title\":\"Be Heart Smart: Congestive Heart Failure LIVE Event\"},\"statistics\":{\"commentCount\":1,\"dislikeCount\":0,\"favoriteCount\":0,\"likeCount\":0,\"viewCount\":9}}";
    private ObjectMapper objectMapper;
    private YoutubeActivityUtil youtubeActivityUtil;

    @Before
    public void setup() {
        objectMapper = StreamsJacksonMapper.getInstance();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Video.class, new YoutubeVideoDeserializer());
        objectMapper.registerModule(simpleModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        youtubeActivityUtil = new YoutubeActivityUtil();
    }

    @Test
    public void testVideoObject() {
        LOGGER.info("raw: {}", testVideo);

        try {
            Activity activity = new Activity();

            Video video = objectMapper.readValue(testVideo, Video.class);

            youtubeActivityUtil.updateActivity(video, activity, "testChannelId");
            LOGGER.info("activity: {}", activity);

            assertNotNull(activity);
            assert (activity.getId().contains("id:youtube:post"));
            assertEquals(activity.getVerb(), "post");

            Provider provider = activity.getProvider();
            assertEquals(provider.getId(), "id:providers:youtube");
            assertEquals(provider.getDisplayName(), "YouTube");

            Actor actor = activity.getActor();
            assert (actor.getId().contains("id:youtube:"));
            assertNotNull(actor.getDisplayName());
            assertNotNull(actor.getSummary());

            assertNotNull(activity.getTitle());
            assertNotNull(activity.getUrl());
            assertNotNull(activity.getContent());

            assertEquals(activity.getPublished().getClass(), DateTime.class);

            assertNotNull(activity.getAdditionalProperties().get("extensions"));

            Map<String, Object> extensions = (Map<String, Object>)activity.getAdditionalProperties().get("extensions");

            assertNotNull(extensions.get("youtube"));
            assertNotNull(extensions.get("likes"));

            assertTrue(testActivityObject(activity));
        } catch (Exception e) {
            LOGGER.error("Exception while testing the Ser/De functionality of the Video deserializer: {}", e);
        }
    }

    private boolean testActivityObject(Activity activity) {
        boolean valid = false;

        ActivityObject obj = activity.getObject();

        if(obj.getObjectType().equals("video") && !obj.getImage().equals(null) &&
                !obj.getUrl().equals("null") && obj.getUrl().contains("https://www.youtube.com/watch?v=")) {
            valid = true;
        }

        return valid;
    }
}
