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
import org.testng.annotations.Test;

/**
 * Unit Test for PropertyUtil.
 */
public class PropertyUtilTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  String flatJson = "{\"a.a\": \"aa\", \"a.b\": \"ab\", \"b.a\": \"ba\", \"b.b\": \"bb\"}";

  @Test
  public void testUnflattenObjectNode() throws Exception {
    ObjectNode flatNode = mapper.readValue(flatJson, ObjectNode.class);
    ObjectNode unflattenedNode = PropertyUtil.unflattenObjectNode(flatNode, '.');
    assert(unflattenedNode.size() == 2);
  }
}

