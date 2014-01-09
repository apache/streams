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

import java.io.*;
import java.util.List;

/**
 * JSON utilities
 */
public class JsonUtil {

    private JsonUtil() {}

    public static JsonNode jsonToJsonNode(String json) {
        ObjectMapper mapper = new ObjectMapper();
        JsonFactory factory = mapper.getFactory();

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
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("IO exception while writing JSON", e);
        }
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, clazz);
        } catch (IOException e) {
            throw new RuntimeException("Could not map to object");
        }
    }

    public static <T> T jsonNodeToObject(JsonNode node, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(node, clazz);
    }

    public static <T> JsonNode objectToJsonNode(T obj) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.valueToTree(obj);
    }

    public static <T> List<T> jsoNodeToList(JsonNode node, Class<T> clazz) {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.convertValue(node, new TypeReference<List<T>>() {});
    }

    public static <T> String objectToJson(T object) {
        ObjectMapper mapper = new ObjectMapper();
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
        ObjectMapper mapper = new ObjectMapper();
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
}
