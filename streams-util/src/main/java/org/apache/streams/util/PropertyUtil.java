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
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
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

  private static final ObjectMapper mapper = new ObjectMapper();

  public static Map<String, Object> flattenToMap(ObjectNode object) {
    Map<String, Object> flatObject = new HashMap<>();
    addKeys(new String(), object, flatObject, '.');
    return flatObject;
  }

  public static ObjectNode flattenToObjectNode(ObjectNode object) {
    Map<String, Object> flatObject = flattenToMap(object, '.');
    addKeys(new String(), object, flatObject, '.');
    return mapper.convertValue(flatObject, ObjectNode.class);
  }

  public static Map<String, Object> flattenToMap(ObjectNode object, char seperator) {
    Map<String, Object> flatObject = new HashMap<>();
    addKeys(new String(), object, flatObject, seperator);
    return flatObject;
  }

  public static ObjectNode flattenToObjectNode(ObjectNode object, char seperator) {
    Map<String, Object> flatObject = flattenToMap(object, seperator);
    addKeys(new String(), object, flatObject, seperator);
    return mapper.convertValue(flatObject, ObjectNode.class);
  }

  private static void addKeys(String currentPath, JsonNode jsonNode, Map<String, Object> map, char seperator) {
    if (jsonNode.isObject()) {
      ObjectNode objectNode = (ObjectNode) jsonNode;
      Iterator<Map.Entry<String, JsonNode>> iter = objectNode.fields();
      String pathPrefix = currentPath.isEmpty() ? "" : currentPath + seperator;

      while (iter.hasNext()) {
        Map.Entry<String, JsonNode> entry = iter.next();
        addKeys(pathPrefix + entry.getKey(), entry.getValue(), map, seperator);
      }
    } else if (jsonNode.isArray()) {
      ArrayNode arrayNode = (ArrayNode) jsonNode;
      if( arrayNode.isTextual()) {
        List<String> list = mapper.convertValue(arrayNode, List.class);
        map.put(currentPath, list);
      }
      if( arrayNode.isNumber()) {
        List<String> list = mapper.convertValue(arrayNode, List.class);
        map.put(currentPath, list);
      }
    } else if (jsonNode.isValueNode()) {
      ValueNode valueNode = (ValueNode) jsonNode;
      if( valueNode.isTextual() )
        map.put(currentPath, valueNode.asText());
      else if ( valueNode.isNumber() )
        map.put(currentPath, valueNode);
    }
  }

  public static ObjectNode unflattenMap(Map<String, Object> object, char seperator) {
    return unflattenObjectNode(mapper.convertValue(object, ObjectNode.class), seperator);
  }

  public static ObjectNode unflattenObjectNode(ObjectNode flatObject, char seperator) {
    ObjectNode root = mapper.createObjectNode();
    Iterator<Map.Entry<String, JsonNode>> iter = flatObject.fields();
    while (iter.hasNext()) {
      Map.Entry<String, JsonNode> item = iter.next();
      String fullKey = item.getKey();
      if( !fullKey.contains(Character.valueOf(seperator).toString())) {
        root.put(item.getKey(), item.getValue());
      } else {
        ObjectNode currentNode = root;
        List<String> keyParts = new ArrayList<>(Arrays.asList(StringUtils.split(item.getKey(), seperator)));
        keyParts.remove(keyParts.size()-1);
        Iterator<String> keyPartIterator = keyParts.iterator();
        while( keyPartIterator.hasNext()) {
          String part = keyPartIterator.next();
          if( currentNode.has(part) && currentNode.get(part).isObject() ) {
            currentNode = (ObjectNode) currentNode.get(part);
          } else {
            ObjectNode newNode = mapper.createObjectNode();
            currentNode.put(part, newNode);
            currentNode = newNode;
          }
        };
        currentNode.put(keyParts.get(keyParts.size()-1), item.getValue());

      }
    }
    return root;
  }


}
