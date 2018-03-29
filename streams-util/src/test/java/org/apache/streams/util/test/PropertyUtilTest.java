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

package org.apache.streams.util.test;

import org.apache.streams.util.PropertyUtil;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.testng.Assert.assertTrue;

/**
 * Unit Test for PropertyUtil.
 */
public class PropertyUtilTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  String flatJson = "{\"a.b\": \"ab\", \"c.d\": \"cd\", \"a.e\": \"ae\", \"c.f\": \"cf\"}";

  @Test
  public void testFlattenToMap() throws Exception {
    PropertyUtil propertyUtil = PropertyUtil.getInstance();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
    node.put("objectType", "post");
    node.putArray("links").add("link1");
    node.putObject("actor").put("id", "leftId").putObject("author").put("id", "authorId");
    node.putObject("extensions");
    Map flattenedMap = propertyUtil.flattenToMap(node);
    assertThat(flattenedMap.size(), equalTo(5));
    assertThat(flattenedMap.get("actor.id"), notNullValue());
    assertThat(flattenedMap.get("actor.author.id"), notNullValue());
    assertThat(flattenedMap.get("objectType"), equalTo("post"));
    assertThat(flattenedMap.get("links"), nullValue());
    assertThat(flattenedMap.get("links[0]"), notNullValue());
    assertThat(flattenedMap.get("extensions"), notNullValue());
  }

  @Test
  public void testUnflattenObjectNode() throws Exception {
    PropertyUtil propertyUtil = PropertyUtil.getInstance();
    ObjectNode flatNode = mapper.readValue(flatJson, ObjectNode.class);
    ObjectNode unflattenedNode = propertyUtil.unflattenObjectNode(flatNode);
    assertThat(unflattenedNode.size(), equalTo(2));
    assertThat(unflattenedNode.get("a"), notNullValue());
    assertThat(unflattenedNode.get("b"), nullValue());
    assertThat(unflattenedNode.get("c"), notNullValue());
    assertThat(unflattenedNode.get("a").size(), equalTo(2));
    assertThat(unflattenedNode.get("a").get("b").asText(), equalTo("ab"));
    assertThat(unflattenedNode.get("a").get("e").asText(), equalTo("ae"));
    assertThat(unflattenedNode.get("c").size(), equalTo(2));
    assertThat(unflattenedNode.get("c").get("d").asText(), equalTo("cd"));
    assertThat(unflattenedNode.get("c").get("f").asText(), equalTo("cf"));
  }

  @Test
  public void testMergeProperties() throws Exception {
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode left = mapper.createObjectNode();
    left.put("objectType", "post");
    left.putArray("links").add("link1");
    left.putObject("actor").put("id", "leftId").putObject("author").put("id", "authorId");
    ObjectNode right = mapper.createObjectNode();
    right.putArray("links").add("link2");
    right.putObject("actor").put("id", "rightId").putObject("image").put("url", "http://url.com");
    JsonNode merged = PropertyUtil.mergeProperties(left, right);
    assertTrue(merged.has("objectType"));
    assertTrue(merged.has("actor"));
    assertTrue(merged.has("links"));
    assertThat((merged.get("links")).size(), equalTo(2));
    assertTrue(merged.get("actor").has("id"));
    assertThat(merged.get("actor").get("id").asText(), equalTo("leftId"));
    assertTrue(merged.get("actor").has("author"));
    assertTrue(merged.get("actor").get("author").has("id"));
    assertTrue(merged.get("actor").has("image"));
    assertTrue(merged.get("actor").get("image").has("url"));

  }

  @Test
  public void testCleanProperties() throws Exception {
    PropertyUtil propertyUtil = PropertyUtil.getInstance();
    ObjectMapper mapper = new ObjectMapper();
    ObjectNode node = mapper.createObjectNode();
    String nullid = null;
    node.put("id", nullid);
    node.put("objectType", "post");
    node.putArray("links").add("link1");
    node.putArray("urls");
    node.putObject("actor").put("id", "leftId").putObject("author").put("id", "authorId");
    node.putObject("object").put("id", "").put("name", "O");
    node.putObject("target").put("id", "");
    node.putObject("extensions");
    ObjectNode cleaned = propertyUtil.cleanProperties(node);
    assertThat(cleaned.has("id"), equalTo(false));
    assertThat(cleaned.has("objectType"), equalTo(true));
    assertThat(cleaned.has("links"), equalTo(true));
    assertThat(cleaned.has("urls"), equalTo(false));
    assertThat(cleaned.has("actor"), equalTo(true));
    assertThat(cleaned.get("actor").size(), equalTo(2));
    assertThat(cleaned.has("object"), equalTo(true));
    assertThat(cleaned.get("object").size(), equalTo(1));
    assertThat(cleaned.has("target"), equalTo(false));
    assertThat(cleaned.has("extensions"), equalTo(false));
  }
}

