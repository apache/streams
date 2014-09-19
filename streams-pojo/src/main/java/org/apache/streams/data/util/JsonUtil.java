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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.apache.streams.jackson.StreamsJacksonMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * JSON utilities
 */
public class JsonUtil {

    private JsonUtil() {}

    private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();
    private static JsonFactory factory = mapper.getFactory();

    public static JsonNode jsonToJsonNode(String json) {
        JsonNode node;
        try {
            JsonParser jp = factory.createJsonParser(json);
            node = mapper.readTree(jp);
        } catch (IOException e) {
            throw new RuntimeException("IO exception while reading JSON", e);
        }
        return node;
    }

    public static String jsonNodeToJson(JsonNode node) {
        try {
            return mapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("IO exception while writing JSON", e);
        }
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Could not map to object");
        }
    }

    public static <T> T jsonNodeToObject(JsonNode node, Class<T> clazz) {
        return mapper.convertValue(node, clazz);
    }

    public static <T> JsonNode objectToJsonNode(T obj) {
        return mapper.valueToTree(obj);
    }

    public static <T> List<T> jsoNodeToList(JsonNode node, Class<T> clazz) {
        return mapper.convertValue(node, new TypeReference<List<T>>() {});
    }

    public static <T> String objectToJson(T object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (IOException e) {
            throw new RuntimeException("Could not map to object");
        }
    }

    public static <T> T getObjFromFile(String filePath, Class<T> clazz) {
        return jsonNodeToObject(getFromFile(filePath), clazz);
    }

    public static JsonNode getFromFile(String filePath) {
        JsonFactory factory = mapper.getFactory(); // since 2.1 use mapper.getFactory() instead

        JsonNode node = null;
        try {
            InputStream stream = getStreamForLocation(filePath);
            JsonParser jp = factory.createParser(stream);
            node = mapper.readTree(jp);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return node;
    }

    private static InputStream getStreamForLocation(String filePath) throws FileNotFoundException {
        InputStream stream = null;
        if(filePath.startsWith("file:///")) {
            stream = new FileInputStream(filePath.replace("file:///", ""));
        } else if(filePath.startsWith("file:") || filePath.startsWith("/")) {
            stream = new FileInputStream(filePath.replace("file:", ""));
        } else {
            //Assume classpath
            stream = JsonUtil.class.getClassLoader().getResourceAsStream(filePath.replace("classpath:", ""));
        }

        return stream;
    }

    /**
     * Creates an empty array if missing
     * @param node object to create the array within
     * @param field location to create the array
     * @return the Map representing the extensions property
     */
    public static ArrayNode ensureArray(ObjectNode node, String field) {
        String[] path = Lists.newArrayList(Splitter.on('.').split(field)).toArray(new String[0]);
        ObjectNode current = node;
        ArrayNode result = null;
        for( int i = 0; i < path.length; i++) {
            current = ensureObject((ObjectNode) node.get(path[i]), path[i]);
        }
        if (current.get(field) == null)
            current.put(field, mapper.createArrayNode());
        result = (ArrayNode) node.get(field);
        return result;
    }

    /**
     * Creates an empty array if missing
     * @param node objectnode to create the object within
     * @param field location to create the object
     * @return the Map representing the extensions property
     */
    public static ObjectNode ensureObject(ObjectNode node, String field) {
        String[] path = Lists.newArrayList(Splitter.on('.').split(field)).toArray(new String[0]);
        ObjectNode current = node;
        ObjectNode result = null;
        for( int i = 0; i < path.length; i++) {
            if (node.get(field) == null)
                node.put(field, mapper.createObjectNode());
            current = (ObjectNode) node.get(field);
        }
        result = ensureObject((ObjectNode) node.get(path[path.length]), Joiner.on('.').join(Arrays.copyOfRange(path, 1, path.length)));
        return result;
    }

}
