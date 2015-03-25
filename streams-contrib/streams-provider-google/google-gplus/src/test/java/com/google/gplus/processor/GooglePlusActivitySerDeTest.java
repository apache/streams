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

package com.google.gplus.processor;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.gplus.serializer.util.GPlusActivityDeserializer;
import com.google.gplus.serializer.util.GooglePlusActivityUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Provider;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GooglePlusActivitySerDeTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GooglePlusActivitySerDeTest.class);
    private ObjectMapper objectMapper;
    private GooglePlusActivityUtil googlePlusActivityUtil;

    @Before
    public void setup() {
        objectMapper = new StreamsJacksonMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(com.google.api.services.plus.model.Activity.class, new GPlusActivityDeserializer());
        objectMapper.registerModule(simpleModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        googlePlusActivityUtil = new GooglePlusActivityUtil();
    }

    @Test
    public void TestActivityObjects() {
        InputStream is = GooglePlusActivitySerDeTest.class.getResourceAsStream("/google_plus_activity_jsons.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        try {
            while (br.ready()) {
                String line = br.readLine();
                if (!StringUtils.isEmpty(line)) {
                    LOGGER.info("raw: {}", line);
                    Activity activity = new Activity();

                    com.google.api.services.plus.model.Activity gPlusActivity = objectMapper.readValue(line, com.google.api.services.plus.model.Activity.class);

                    googlePlusActivityUtil.updateActivity(gPlusActivity, activity);
                    LOGGER.info("activity: {}", activity);

                    assertNotNull(activity);
                    assert(activity.getId().contains("id:googleplus:post"));
                    assertEquals(activity.getVerb(), "post");

                    Provider provider = activity.getProvider();
                    assertEquals(provider.getId(), "id:providers:googleplus");
                    assertEquals(provider.getDisplayName(), "GooglePlus");

                    Actor actor = activity.getActor();
                    assertNotNull(actor.getImage());
                    assert(actor.getId().contains("id:googleplus:"));
                    assertNotNull(actor.getUrl());

                    assertNotNull(activity.getPublished());
                    assertNotNull(activity.getTitle());
                    assertNotNull(activity.getUrl());

                    Map<String, Object> extensions = (Map<String, Object>)activity.getAdditionalProperties().get("extensions");
                    assertNotNull(extensions);
                    assertNotNull(extensions.get("googlePlus"));

                    if(activity.getContent() != null) {
                        assertNotNull(extensions.get("rebroadcasts"));
                        assertNotNull(extensions.get("keywords"));
                        assertNotNull(extensions.get("likes"));
                        assert (((Map<String, Object>) extensions.get("rebroadcasts")).containsKey("count"));
                        assert (((Map<String, Object>) extensions.get("likes")).containsKey("count"));
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("Exception while testing serializability: {}", e);
        }
    }
}