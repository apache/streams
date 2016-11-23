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

package org.apache.streams.dropwizard.test;

import org.apache.streams.dropwizard.GenericWebhookData;
import org.apache.streams.dropwizard.GenericWebhookResource;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.List;

/**
 * Tests {@link: org.apache.streams.dropwizard.GenericWebhookResource}
 */
public class GenericWebhookResourceTest {

  private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  private static final GenericWebhookResource genericWebhookResource = new GenericWebhookResource();

  @ClassRule
  public static final ResourceTestRule resources = ResourceTestRule.builder()
      .addResource(genericWebhookResource)
      .build();

  @Test
  public void testPostJson() {
    Assert.assertEquals(400, genericWebhookResource.json(null, "{").getStatus());
    Assert.assertEquals(400, genericWebhookResource.json(null, "}").getStatus());
    Assert.assertEquals(400, genericWebhookResource.json(null, "srg").getStatus());
    Assert.assertEquals(400, genericWebhookResource.json(null, "123").getStatus());
    Assert.assertEquals(200, genericWebhookResource.json(null, "{}").getStatus());
    Assert.assertEquals(200, genericWebhookResource.json(null, "{\"valid\":\"true\"}").getStatus());
  }

  @Test
  public void testPostJsonNewLine() {
    Assert.assertEquals(200, genericWebhookResource.json_new_line(null, "{}").getStatus());
    Assert.assertEquals(400, genericWebhookResource.json_new_line(null, "notvalid").getStatus());
    Assert.assertEquals(200, genericWebhookResource.json_new_line(null, "{\"valid\":\"true\"}").getStatus());
    Assert.assertEquals(200, genericWebhookResource.json_new_line(null, "{\"valid\":\"true\"}\n{\"valid\":\"true\"}\r{\"valid\":\"true\"}").getStatus());
  }

  @Test
  public void testPostJsonMeta() throws JsonProcessingException {
    Assert.assertEquals(200, genericWebhookResource.json_meta(null, "{}").getStatus());
    Assert.assertEquals(400, genericWebhookResource.json_meta(null, "notvalid").getStatus());
    GenericWebhookData testPostJsonMeta = new GenericWebhookData()
        .withHash("test")
        .withDeliveredAt(DateTime.now())
        .withCount(1)
        .withHashType("type")
        .withId("test");
    List<ObjectNode> testPostJsonData = Lists.newArrayList();
    testPostJsonData.add(mapper.createObjectNode().put("valid", "true"));
    testPostJsonMeta.setData(testPostJsonData);
    String testPostJsonEntity = mapper.writeValueAsString(testPostJsonMeta);
    Assert.assertEquals(200, genericWebhookResource.json_meta(null, testPostJsonEntity).getStatus());

  }

}
