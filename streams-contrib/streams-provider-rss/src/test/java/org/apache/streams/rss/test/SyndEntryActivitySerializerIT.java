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

package org.apache.streams.rss.test;

import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Author;
import org.apache.streams.pojo.json.Provider;
import org.apache.streams.rss.serializer.SyndEntryActivitySerializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

/**
 * Tests ability to convert SyndEntry ObjectNode form to {@link org.apache.streams.rss.processor.RssTypeConverter} form
 *
 * Disabled until a source of fresh test data is set up today.
 */
public class SyndEntryActivitySerializerIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(SyndEntryActivitySerializerIT.class);

  private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  @Test(enabled = false)
  public void testJsonData() throws Exception {
    Scanner scanner = new Scanner(this.getClass().getResourceAsStream("/TestSyndEntryJson.txt"));
    List<Activity> activities = new LinkedList<>();
    List<ObjectNode> objects = new LinkedList<>();

    SyndEntryActivitySerializer serializer = new SyndEntryActivitySerializer();

    while (scanner.hasNext()) {
      String line = scanner.nextLine();
      LOGGER.debug(line);
      ObjectNode node = (ObjectNode) mapper.readTree(line);

      objects.add(node);
      activities.add(serializer.deserialize(node));
    }

    assertEquals(11, activities.size());

    for (int x = 0; x < activities.size(); x++) {
      ObjectNode objectNode = objects.get(x);
      Activity activity = activities.get(x);

      testActor(objectNode.get("author").asText(), activity.getActor());
      testAuthor(objectNode.get("author").asText(), activity.getObject().getAuthor());
      testProvider("id:providers:rss", "RSS", activity.getProvider());
      validateProviderUrl(activity.getProvider());
      testVerb("post", activity.getVerb());
      testPublished(objectNode.get("publishedDate").asText(), activity.getPublished());
      testUrl(objectNode.get("uri").asText(), objectNode.get("link").asText(), activity);
    }
  }

  public void testVerb(String expected, String verb) {
    assertEquals(expected, verb);
  }

  public void testPublished(String expected, DateTime published) {
    assertEquals(new DateTime(expected, DateTimeZone.UTC), published);
  }

  public void testActor(String expected, ActivityObject actor) {
    assertEquals("id:rss:null" + ":" + expected, actor.getId());
    assertEquals(expected, actor.getDisplayName());
  }

  public void testAuthor(String expected, Author author) {
    assertEquals(expected, author.getDisplayName());
    assertEquals(expected, author.getId());
  }

  public void testProvider(String expectedId, String expectedDisplay, Provider provider) {
    assertEquals(expectedId, provider.getId());
    assertEquals(expectedDisplay, provider.getDisplayName());
  }

  /**
   * validate Provider Url.
   * @param provider Provider
   */
  public void validateProviderUrl(Provider provider) {
    URL url = null;

    try {
      url = new URL(provider.getUrl());
      url.toURI();
    } catch (Exception ex) {
      LOGGER.error("Threw an exception while trying to validate URL: {} - {}", provider.getUrl(), ex);
    }

    assertNotNull(url);
  }

  public void testUrl(String expectedUri, String expectedLink, Activity activity) {
    assertTrue((Objects.equals(expectedUri, activity.getUrl()) || Objects.equals(expectedLink, activity.getUrl())));
    assertTrue((Objects.equals(expectedUri, activity.getObject().getUrl()) ||
      Objects.equals(expectedLink, activity.getObject().getUrl())));
  }
}