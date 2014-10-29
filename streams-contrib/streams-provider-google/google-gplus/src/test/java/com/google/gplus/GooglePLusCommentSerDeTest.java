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

package com.google.gplus;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.api.client.util.Lists;
import com.google.api.services.plus.model.Comment;
import com.google.gplus.serializer.util.GPlusCommentDeserializer;
import com.google.gplus.serializer.util.GooglePlusActivityUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class GooglePlusCommentSerDeTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GooglePlusCommentSerDeTest.class);
    private ObjectMapper objectMapper;
    private GooglePlusActivityUtil googlePlusActivityUtil;

    @Before
    public void setup() {
        objectMapper = new StreamsJacksonMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Comment.class, new GPlusCommentDeserializer());
        objectMapper.registerModule(simpleModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        googlePlusActivityUtil = new GooglePlusActivityUtil();
    }

    @org.junit.Test
    public void testCommentObjects() {
        InputStream is = GooglePlusPersonSerDeTest.class.getResourceAsStream("/google_plus_comments_jsons.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        Activity activity = new Activity();
        List<Comment> comments = Lists.newArrayList();

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

            googlePlusActivityUtil.updateActivity(comments, activity);
            assertNotNull(activity);
            assertNotNull(activity.getObject());
            assertEquals(activity.getObject().getAttachments().size(), 3);
        } catch (Exception e) {
            LOGGER.error("Exception while testing serializability: {}", e);
        }
    }

    @org.junit.Test
    public void testEmptyComments() {
        Activity activity = new Activity();

        googlePlusActivityUtil.updateActivity(new ArrayList<Comment>(), activity);

        assertNull(activity.getObject());
    }
}
