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

package org.apache.streams.data.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import com.google.common.collect.Maps;
import org.apache.streams.jackson.StreamsJacksonMapper;

import java.util.Iterator;
import java.util.Map;

/**
 *  Class transforms nested properties of activities, actors, objects, etc...
 */
public class PropertyUtil {

    /**
     * Property on the activity object to use for extensions
     */
    public static final String EXTENSION_PROPERTY = "extensions";

    private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public static Map<String, Object> flattenToMap(ObjectNode object) {
        Map<String, Object> flatObject = Maps.newHashMap();
        addKeys(new String(), object, flatObject, '.');
        return flatObject;
    }

    public static ObjectNode flattenToObjectNode(ObjectNode object) {
        Map<String, Object> flatObject = flattenToMap(object, '.');
        addKeys(new String(), object, flatObject, '.');
        return mapper.convertValue(flatObject, ObjectNode.class);
    }

    public static Map<String, Object> flattenToMap(ObjectNode object, char seperator) {
        Map<String, Object> flatObject = Maps.newHashMap();
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
            map.put(currentPath, arrayNode);
        } else if (jsonNode.isValueNode()) {
            ValueNode valueNode = (ValueNode) jsonNode;
            if( valueNode.isTextual() )
                map.put(currentPath, valueNode.asText());
            else if ( valueNode.isNumber() )
                map.put(currentPath, valueNode);
        }
    }
}
