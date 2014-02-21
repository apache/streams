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
package org.apache.streams.facebook.test;

import com.facebook.api.FacebookPostActivitySerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.data.util.JsonUtil;
import org.apache.streams.pojo.json.Activity;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.matches;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class FacebookPostActivitySerializerTest {
    Node fields;
    JsonNode json;
    ActivitySerializer serializer = new FacebookPostActivitySerializer();
    ObjectMapper mapper;

    @Before
    public void setup() throws IOException {
        json = JsonUtil.getFromFile("classpath:org/apache/streams/data/Facebook.json");
        fields = discover(json);

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
    }

    @Test
    public void loadData() throws Exception {
        for (JsonNode item : json) {
            Activity activity = serializer.deserialize(getString(item));
            assertThat(activity, is(not(nullValue())));
            assertThat(activity.getActor(), is(not(nullValue())));
            assertThat(matches("id:facebook:people:[a-zA-Z0-9]*", activity.getActor().getId()), is(true));
            assertThat(activity.getActor().getDisplayName(), is(not(nullValue())));
            assertThat(activity.getObject(), is(not(nullValue())));
            if(activity.getObject().getId() != null) {
                assertThat(matches("id:facebook:[a-z]*s:[a-zA-Z0-9]*", activity.getObject().getId()), is(true));
            }
            assertThat(activity.getObject().getObjectType(), is(not(nullValue())));
            assertThat(activity.getContent(), is(not(nullValue())));
            assertThat(activity.getProvider().getId(), is(equalTo("id:providers:facebook")));
            System.out.println(activity.getPublished());
        }
    }




    public Node discover(JsonNode node) {
        Node root = new Node(null, "root");
        if (node == null || !node.isArray()) {
            throw new RuntimeException("No data");
        } else {
            for (JsonNode item : node) {
                mapNode(root, item);
            }
        }
        //printTree(root, "");
        //printUniqueFields(root);
        return root;
    }


    private String getString(JsonNode jsonNode)  {
        try {
            return new ObjectMapper().writeValueAsString(jsonNode);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private void printUniqueFields(Node root) {
        Map<String, Set<String>> fieldsByType = new HashMap<String, Set<String>>();
        fieldsByType.put("objectType", new HashSet<String>());
        for(Node child : root.getChildren().values()) {
           for(Node grandChild : child.getChildren().values()) {
               fieldsByType.get("objectType").add(grandChild.getName());
               addUniqueValues(grandChild, fieldsByType);
           }
        }
        for(Map.Entry<String, Set<String>> entry : fieldsByType.entrySet()) {
            System.out.println(entry.getKey());
            List<String> value = new ArrayList<String>(entry.getValue());
            Collections.sort(value);
            for(String val : value) {
                System.out.println("      " + val);
            }
            System.out.println();
            System.out.println();
        }
    }

    private void addUniqueValues(Node child, Map<String, Set<String>> fieldsByType) {
        if(!fieldsByType.containsKey(child.getName()) && !isNumber(child.getName())) {
            fieldsByType.put(child.getName(), new HashSet<String>());
        }
        for(Map.Entry<String, Node> gc : child.getChildren().entrySet()) {
            if(!isNumber(gc.getKey()))
                fieldsByType.get(child.getName()).add(gc.getKey());
            addUniqueValues(gc.getValue(), fieldsByType);
        }
    }

    private boolean isNumber(String key) {
        Pattern p = Pattern.compile("[0-9]*");
        return p.matcher(key.trim()).matches();
    }

    private void printTree(Node node, String spacer) {
        System.out.println(String.format("%s %s (%s)", spacer, node.getName(), node.getType()));
        List<Node> children = new ArrayList<Node>(node.getChildren().values());
        Collections.sort(children);
        for(Node child : children) {
            printTree(child, spacer + "      ");
        }
    }

    private void mapNode(Node parent, JsonNode jsonNode) {
        for (Iterator<Map.Entry<String, JsonNode>> iter = jsonNode.fields(); iter.hasNext(); ) {
            Map.Entry<String, JsonNode> property = iter.next();
            Node current;
            String key = property.getKey();
            JsonNode value = property.getValue();
            if (!parent.getChildren().containsKey(key)) {
                current = new Node(null, key);
                current.setType(value.getNodeType().toString());
                parent.getChildren().put(key, current);
            } else {
                current = parent.getChildren().get(key);
            }
            if(!value.isArray() && value.isObject()){
                mapNode(current, value);
            }
        }
    }

    private static class Node implements Comparable<Node>{
        Node parent;
        String name;
        String type;
        Map<String, Node> children = new HashMap<String, Node>();

        private Node(Node parent, String name) {
            this.parent = parent;
            this.name = name;
        }

        private Node getParent() {
            return parent;
        }

        private void setParent(Node parent) {
            this.parent = parent;
        }

        private String getName() {
            return name;
        }

        private void setName(String name) {
            this.name = name;
        }

        private Map<String, Node> getChildren() {
            return children;
        }

        private void setChildren(Map<String, Node> children) {
            this.children = children;
        }

        private String getType() {
            return type;
        }

        private void setType(String type) {
            this.type = type;
        }

        @Override
        public int compareTo(Node node) {
            return this.name.compareTo(node.name);
        }
    }

}
