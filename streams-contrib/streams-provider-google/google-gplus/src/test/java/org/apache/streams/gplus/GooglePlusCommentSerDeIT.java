/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.gplus;

import org.apache.streams.gplus.serializer.util.GPlusCommentDeserializer;
import org.apache.streams.gplus.serializer.util.GooglePlusActivityUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.api.services.plus.model.Comment;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;

/**
 * Tests serialization of google plus comments.
 *
 * Disabled until a companion provider/test to generate the data exists.
 */
public class GooglePlusCommentSerDeIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(GooglePlusCommentSerDeIT.class);

  private static Config application = ConfigFactory.parseResources("GooglePlusCommentSerDeIT.conf").withFallback(ConfigFactory.load());

  private ObjectMapper objectMapper;

  /**
   * setup.
   */
  @BeforeClass
  public void setupTestCommentObjects() {
    objectMapper = StreamsJacksonMapper.getInstance();
    SimpleModule simpleModule = new SimpleModule();
    simpleModule.addDeserializer(Comment.class, new GPlusCommentDeserializer());
    objectMapper.registerModule(simpleModule);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }


  @Test(enabled = false, groups = "GooglePlusCommentSerDeIT", dependsOnGroups = "GPlusUserActivityProviderIT")
  public void testCommentObjects() {

    String inputResourcePath = application.getString("inputResourcePath");

    InputStream is = GooglePlusCommentSerDeIT.class.getResourceAsStream(inputResourcePath);
    InputStreamReader isr = new InputStreamReader(is);
    BufferedReader br = new BufferedReader(isr);

    Activity activity = new Activity();
    List<Comment> comments = new ArrayList<>();

    try {
      while (br.ready()) {
        String line = br.readLine();
        if (!StringUtils.isEmpty(line)) {
          LOGGER.info("raw: {}", line);
          Comment comment = objectMapper.readValue(line, Comment.class);

          LOGGER.info("comment: {}", comment);

          assertNotNull(comment);
          assertNotNull(comment.getEtag());
          assertNotNull(comment.getId());
          assertNotNull(comment.getInReplyTo());
          assertNotNull(comment.getObject());
          assertNotNull(comment.getPlusoners());
          assertNotNull(comment.getPublished());
          assertNotNull(comment.getUpdated());
          assertNotNull(comment.getSelfLink());
          assertEquals(comment.getVerb(), "post");

          comments.add(comment);
        }
      }

      assertEquals(comments.size(), 3);

      GooglePlusActivityUtil.updateActivity(comments, activity);
      assertNotNull(activity);
      assertNotNull(activity.getObject());
      assertEquals(activity.getObject().getAttachments().size(), 3);
    } catch (Exception ex) {
      LOGGER.error("Exception while testing serializability: {}", ex);
    }
  }

  @Test
  public void testEmptyComments() {
    Activity activity = new Activity();
    GooglePlusActivityUtil.updateActivity(new ArrayList<>(), activity);
    assertNull(activity.getObject());
  }
}
