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
package com.youtube.processor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.api.services.youtube.model.Video;
import com.youtube.serializer.YoutubeActivityUtil;
import com.youtube.serializer.YoutubeVideoDeserializer;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class YoutubeTypeConverterTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(YoutubeTypeConverterTest.class);
    private final String testVideo = "{\"etag\":\"\\\"4FSIjSQU83ZJMYAO0IqRYMvZX98/V0q3OIauZ3ZAkszLUDbHL45yEGM\\\"\",\"id\":\"sUOepRctwVE\",\"kind\":\"youtube#video\",\"snippet\":{\"channelId\":\"UCNENOn2nmwguQYkejKhJGPQ\",\"channelTitle\":\"Carilion Clinic\",\"description\":\"Join Carilion Clinic's Heart Failure experts for a LIVE Google+ Hangout on Feb. 23, 12:30-1 p.m. to learn more about heart failure, treatment options, and lifestyle changes. Learn more: https://plus.google.com/u/0/events/cj074q9r6csgv6i2kqhi2isc6k0\",\"publishedAt\":{\"value\":1422977409000,\"dateOnly\":false,\"timeZoneShift\":-360},\"thumbnails\":{\"default\":{\"height\":480,\"url\":\"https://i.ytimg.com/vi/sUOepRctwVE/sddefault.jpg\",\"width\":640}},\"title\":\"Be Heart Smart: Congestive Heart Failure LIVE Event\"},\"statistics\":{\"commentCount\":1,\"dislikeCount\":0,\"favoriteCount\":0,\"likeCount\":0,\"viewCount\":9}}";

    private YoutubeTypeConverter youtubeTypeConverter;
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        objectMapper = StreamsJacksonMapper.getInstance();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Video.class, new YoutubeVideoDeserializer());
        objectMapper.registerModule(simpleModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        youtubeTypeConverter = new YoutubeTypeConverter();
        youtubeTypeConverter.prepare(null);
    }

    @Test
    public void testVideoConversion() {
        try {
            LOGGER.info("raw: {}", testVideo);
            Activity activity = new Activity();

            Video video = objectMapper.readValue(testVideo, Video.class);
            StreamsDatum streamsDatum = new StreamsDatum(video);

            assertNotNull(streamsDatum.getDocument());

            List<StreamsDatum> retList = youtubeTypeConverter.process(streamsDatum);
            YoutubeActivityUtil.updateActivity(video, activity, "testChannelId");

            assertEquals(retList.size(), 1);
            assert (retList.get(0).getDocument() instanceof Activity);
            assertEquals(activity, retList.get(0).getDocument());
        } catch (Exception e) {
            LOGGER.error("Exception while trying to convert video to activity: {}", e);
        }
    }

    @Test
    public void testStringVideoConversion() {
        try {
            LOGGER.info("raw: {}", testVideo);
            Activity activity = new Activity();

            Video video = objectMapper.readValue(testVideo, Video.class);
            StreamsDatum streamsDatum = new StreamsDatum(testVideo);

            assertNotNull(streamsDatum.getDocument());

            List<StreamsDatum> retList = youtubeTypeConverter.process(streamsDatum);
            YoutubeActivityUtil.updateActivity(video, activity, "testChannelId");

            assertEquals(retList.size(), 1);
            assert (retList.get(0).getDocument() instanceof Activity);
            assertEquals(activity, retList.get(0).getDocument());
        } catch (Exception e) {
            LOGGER.error("Exception while trying to convert video to activity: {}", e);
        }
    }

}
