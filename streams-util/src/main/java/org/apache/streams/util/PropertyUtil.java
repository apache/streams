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

package org.apache.streams.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.unflattener.JsonUnflattener;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *  Class transforms nested properties of activities, actors, objects, etc...
 */
public class PropertyUtil {

  private static final PropertyUtil INSTANCE = new PropertyUtil(new ObjectMapper());

  public static PropertyUtil getInstance() {
    return INSTANCE;
  }

  public static PropertyUtil getInstance(ObjectMapper mapper) {
    return new PropertyUtil(mapper);
  }

  private ObjectMapper mapper;

  public PropertyUtil(ObjectMapper mapper) {
    this.mapper = mapper;
  }

  public Map<String, Object> flattenToMap(ObjectNode object) {
    return flattenToMap(mapper, object);
  }

  public ObjectNode flattenToObjectNode(ObjectNode object) {
    return flattenToObjectNode(mapper, object);
  }

  public ObjectNode unflattenMap(Map<String, Object> object) {
    return unflattenMap(mapper, object);
  }

  public ObjectNode unflattenObjectNode(ObjectNode object) {
    return unflattenObjectNode(mapper, object);
  }

  public static Map<String, Object> flattenToMap(ObjectMapper mapper, ObjectNode object) {
    Map<String, Object> flatObject;
    try {
      flatObject = mapper.readValue(JsonFlattener.flatten(mapper.writeValueAsString(object)), Map.class);
    } catch( Exception ex ) {
      return null;
    }
    return flatObject;
  }

  public static ObjectNode flattenToObjectNode(ObjectMapper mapper, ObjectNode object) {
    ObjectNode flatObjectNode;
    try {
      flatObjectNode = mapper.readValue(JsonFlattener.flatten(mapper.writeValueAsString(object)), ObjectNode.class);
    } catch( Exception ex ) {
      return null;
    }
    return flatObjectNode;
  }

  public static ObjectNode unflattenMap(ObjectMapper mapper, Map<String, Object> object) {
    ObjectNode unflatObjectNode;
    try {
      unflatObjectNode = mapper.readValue(JsonUnflattener.unflatten(mapper.writeValueAsString(object)), ObjectNode.class);
    } catch( Exception ex ) {
      return null;
    }
    return unflatObjectNode;
  }

  public static ObjectNode unflattenObjectNode(ObjectMapper mapper, ObjectNode object) {
    ObjectNode unflatObjectNode;
    try {
      unflatObjectNode = mapper.readValue(JsonUnflattener.unflatten(mapper.writeValueAsString(object)), ObjectNode.class);
    } catch( Exception ex ) {
      return null;
    }
    return unflatObjectNode;
  }

  /**
   * merge parent and child properties maps.
   * @param content ObjectNode
   * @param parent ObjectNode
   * @return merged ObjectNode
   */
  public static ObjectNode mergeProperties(ObjectNode content, ObjectNode parent) {

    ObjectNode merged = parent.deepCopy();
    Iterator<Map.Entry<String, JsonNode>> fields = content.fields();
    for ( ; fields.hasNext(); ) {
      Map.Entry<String, JsonNode> field = fields.next();
      String fieldId = field.getKey();
      if( merged.get(fieldId) != null ) {
        if( merged.get(fieldId).getNodeType().equals(JsonNodeType.OBJECT)) {
          merged.put(fieldId, mergeProperties(field.getValue().deepCopy(), (ObjectNode)merged.get(fieldId)));
        } else if ( merged.get(fieldId).getNodeType().equals(JsonNodeType.ARRAY)) {
          merged.put(fieldId, mergeArrays(((ArrayNode)field.getValue()), ((ArrayNode)merged.get(fieldId))));
        } else {
          merged.put(fieldId, content.get(fieldId));
        }
      } else {
        merged.put(fieldId, content.get(fieldId));
      }
    }
    return merged;
  }

  /**
   * merge two arrays.
   * @param content ArrayNode
   * @param parent ArrayNode
   * @return merged ArrayNode
   */
  private static ArrayNode mergeArrays(ArrayNode content, ArrayNode parent) {
    return parent.deepCopy().addAll(content);
  }

}
