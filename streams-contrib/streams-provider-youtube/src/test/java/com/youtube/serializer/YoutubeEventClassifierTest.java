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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.api.services.youtube.model.Video;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class YoutubeEventClassifierTest {

  private final String testVideo = "{\"etag\":\"\\\"4FSIjSQU83ZJMYAO0IqRYMvZX98/V0q3OIauZ3ZAkszLUDbHL45yEGM\\\"\",\"id\":\"sUOepRctwVE\",\"kind\":\"youtube#video\",\"snippet\":{\"channelId\":\"UCNENOn2nmwguQYkejKhJGPQ\",\"channelTitle\":\"Carilion Clinic\",\"description\":\"Join Carilion Clinic's Heart Failure experts for a LIVE Google+ Hangout on Feb. 23, 12:30-1 p.m. to learn more about heart failure, treatment options, and lifestyle changes. Learn more: https://plus.google.com/u/0/events/cj074q9r6csgv6i2kqhi2isc6k0\",\"publishedAt\":{\"value\":1422977409000,\"dateOnly\":false,\"timeZoneShift\":-360},\"thumbnails\":{\"default\":{\"height\":480,\"url\":\"https://i.ytimg.com/vi/sUOepRctwVE/sddefault.jpg\",\"width\":640}},\"title\":\"Be Heart Smart: Congestive Heart Failure LIVE Event\"},\"statistics\":{\"commentCount\":1,\"dislikeCount\":0,\"favoriteCount\":0,\"likeCount\":0,\"viewCount\":9}}";
  private final String testObjectNode = "{\"etag\":\"\\\"4FSIjSQU83ZJMYAO0IqRYMvZX98/V0q3OIauZ3ZAkszLUDbHL45yEGM\\\"\",\"id\":\"sUOepRctwVE\",\"kind\":\"youtube#somethingElse\",\"snippet\":{\"channelId\":\"UCNENOn2nmwguQYkejKhJGPQ\",\"channelTitle\":\"Carilion Clinic\",\"description\":\"Join Carilion Clinic's Heart Failure experts for a LIVE Google+ Hangout on Feb. 23, 12:30-1 p.m. to learn more about heart failure, treatment options, and lifestyle changes. Learn more: https://plus.google.com/u/0/events/cj074q9r6csgv6i2kqhi2isc6k0\",\"publishedAt\":{\"value\":1422977409000,\"dateOnly\":false,\"timeZoneShift\":-360},\"thumbnails\":{\"default\":{\"height\":480,\"url\":\"https://i.ytimg.com/vi/sUOepRctwVE/sddefault.jpg\",\"width\":640}},\"title\":\"Be Heart Smart: Congestive Heart Failure LIVE Event\"},\"statistics\":{\"commentCount\":1,\"dislikeCount\":0,\"favoriteCount\":0,\"likeCount\":0,\"viewCount\":9}}";

  @Test
  public void testVideoClassification() {
    Class klass = YoutubeEventClassifier.detectClass(testVideo);

    assertEquals(klass, Video.class);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testExceptionClassification() {
    YoutubeEventClassifier.detectClass("");
  }

  @Test
  public void testObjectNodeClassification() {
    Class klass = YoutubeEventClassifier.detectClass(testObjectNode);

    assertEquals(klass, ObjectNode.class);
  }
}
