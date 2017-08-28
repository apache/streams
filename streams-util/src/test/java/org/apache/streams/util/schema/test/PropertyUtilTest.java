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

package org.apache.streams.util.schema.test;

import org.apache.streams.util.PropertyUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

/**
 * Unit Test for PropertyUtil.
 */
public class PropertyUtilTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  String flatJson = "{\"a.b\": \"ab\", \"c.d\": \"cd\", \"a.e\": \"ae\", \"c.f\": \"cf\"}";

  @Test
  public void testUnflattenObjectNode() throws Exception {
    PropertyUtil propertyUtil = PropertyUtil.getInstance();
    ObjectNode flatNode = mapper.readValue(flatJson, ObjectNode.class);
    ObjectNode unflattenedNode = propertyUtil.unflattenObjectNode(flatNode);
    assert(unflattenedNode.size() == 2);
    assert(unflattenedNode.get("a") != null);
    assert(unflattenedNode.get("b") == null);
    assert(unflattenedNode.get("c") != null);
    assert(unflattenedNode.get("a").size() == 2);
    assert(unflattenedNode.get("a").get("b").asText().equals("ab"));
    assert(unflattenedNode.get("a").get("e").asText().equals("ae"));
    assert(unflattenedNode.get("c").size() == 2);
    assert(unflattenedNode.get("c").get("d").asText().equals("cd"));
    assert(unflattenedNode.get("c").get("f").asText().equals("cf"));
  }
}

