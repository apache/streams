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

package org.apache.streams.facebook.api;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.facebook.Post;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.*;

/**
 * Serializes activity posts
 *   sblackmon: This class needs a rewrite
 */
public class FacebookPostActivitySerializer implements ActivitySerializer<org.apache.streams.facebook.Post> {

    public static final DateTimeFormatter FACEBOOK_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    public static final DateTimeFormatter ACTIVITY_FORMAT = ISODateTimeFormat.basicDateTime();

    public static final String PROVIDER_NAME = "Facebook";

    public static ObjectMapper mapper;
    static {
        mapper = StreamsJacksonMapper.getInstance();
    }

    @Override
    public String serializationFormat() {
        return "facebook_post_json_v1";
    }

    @Override
    public Post serialize(Activity deserialized) throws ActivitySerializerException {
        throw new NotImplementedException("Not currently supported by this deserializer");
    }

    @Override
    public Activity deserialize(Post post) throws ActivitySerializerException {

        Activity activity = new Activity();
        activity.setPublished(post.getCreatedTime());
        activity.setUpdated(post.getUpdatedTime());
        addActor(activity, mapper.convertValue(post.getFrom(), ObjectNode.class));
        setProvider(activity);
        setObjectType(post.getType(), activity);
        parseObject(activity, mapper.convertValue(post, ObjectNode.class));
        fixContentFromSummary(activity);
        activity.setVerb("post");
        List<String> links = Lists.newLinkedList();
        links.add(post.getLink());
        activity.setLinks(links);
        ensureExtensions(activity).put("facebook", post);
        if(post.getLikes() != null) {
            Map<String, Object> likes = Maps.newHashMap();
            likes.put("count", post.getLikes().size());
            ensureExtensions(activity).put("likes", likes);
        }
        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<Post> serializedList) {
        throw new NotImplementedException("Not currently supported by this deserializer");
    }

    private void fixContentFromSummary(Activity activity) {
        //we MUST have a content field set, so choose the best option
        if(activity.getContent() == null) {
            activity.setContent(activity.getAdditionalProperties().containsKey("summary") ?
                    (String) activity.getAdditionalProperties().get("summary") :
                    activity.getObject().getSummary());
        }
    }

    private void fixObjectId(Activity activity) {
        //An artifact of schema generation, the default value is {link}
        if(activity.getObject().getId().equals("{link}")) {
            activity.getObject().setId(null);
        }
    }

    private void setObjectType(String type, Activity activity) {
        ActivityObject object = new ActivityObject();
        activity.setObject(object);
        object.setObjectType(type);
    }

    private void setProvider(Activity activity) {
        Provider provider = new Provider();
        provider.setId("id:provider:"+PROVIDER_NAME);
        provider.setDisplayName(PROVIDER_NAME);
        activity.setProvider(provider);
    }

    private String getObjectType(JsonNode node) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        ensureMoreFields(fields);
        Map.Entry<String, JsonNode> field = fields.next();
        //ensureNoMoreFields(fields);
        return node.asText();
    }

    private void parseObject(Activity activity, JsonNode jsonNode) throws ActivitySerializerException {
        for(Iterator<Map.Entry<String, JsonNode>> fields = jsonNode.fields(); fields.hasNext();) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();
            mapField(activity, key, value);
        }
    }

    private void mapField(Activity activity, String name, JsonNode value) throws ActivitySerializerException {
        if("application".equals(name)) {
            addGenerator(activity, value);
        } else if ("caption".equals(name)) {
            addSummary(activity, value);
        } else if ("comments".equals(name)) {
            addAttachments(activity, value);
        } else if ("description".equals(name)) {
            addObjectSummary(activity, value);
        } else if ("from".equals(name)) {
            addActor(activity, value);
        } else if ("icon".equals(name)) {
            addIcon(activity, value);
        } else if ("id".equals(name)) {
            addId(activity, value);
        } else if ("is_hidden".equals(name)) {
            addObjectHiddenExtension(activity, value);
        } else if ("like_count".equals(name)) {
            addLikeExtension(activity, value);
        } else if ("link".equals(name)) {
            addObjectLink(activity, value);
        } else if ("message".equals(name)) {
            activity.setContent(value.asText());
        } else if ("name".equals(name)) {
            addObjectName(activity, value);
        } else if ("object_id".equals(name)) {
            addObjectId(activity, value);
        } else if ("picture".equals(name)) {
            addObjectImage(activity, value);
        } else if ("place".equals(name)) {
            addLocationExtension(activity, value);
        } else if ("shares".equals(name)) {
            addRebroadcastExtension(activity, value);
        } else if ("source".equals(name)) {
            addObjectLink(activity, value);
        } else if ("story".equals(name)) {
            addTitle(activity, value);
        }
    }

    private void addSummary(Activity activity, JsonNode value) {
        activity.setAdditionalProperty("summary", value.asText());
    }

    private void addTitle(Activity activity, JsonNode value) {
        activity.setTitle(value.asText());
    }

    private void addLikeExtension(Activity activity, JsonNode value) {
        Map<String, Object> extensions = ensureExtensions(activity);
        Map<String, Object> likes = Maps.newHashMap();
        likes.put("count", value.asLong());
        extensions.put(LIKES_EXTENSION, likes);
    }

    private void addLocationExtension(Activity activity, JsonNode value) {
        Map<String, Object> extensions = ensureExtensions(activity);
        if(value.has("location")) {
            Map<String, Object> location = new HashMap<String, Object>();
            JsonNode fbLocation = value.get("location");
            if(fbLocation.has("country")) {
                location.put(LOCATION_EXTENSION_COUNTRY, fbLocation.get("country"));
            }
            if(fbLocation.has("latitude") && fbLocation.has("longitude")) {
                location.put(LOCATION_EXTENSION_COORDINATES, String.format("%s,%s", fbLocation.get("longitude"), fbLocation.get("latitude")));
            }
            extensions.put(LOCATION_EXTENSION, location);
        }
    }

    private void addObjectImage(Activity activity, JsonNode value) {
        Image image = new Image();
        image.setUrl(value.asText());
        activity.getObject().setImage(image);
    }

    private void addObjectId(Activity activity, JsonNode value) {
        activity.getObject().setId(getObjectId("facebook", activity.getObject().getObjectType(), value.asText()));
    }

    private void addObjectName(Activity activity, JsonNode value) {
        activity.getObject().setDisplayName(value.asText());
    }

    private void addId(Activity activity, JsonNode value) {
        activity.setId("id:"+PROVIDER_NAME+":"+value.asText());
    }

    private void addObjectLink(Activity activity, JsonNode value) {
        activity.getObject().setUrl(value.asText());
    }

    private void addRebroadcastExtension(Activity activity, JsonNode value) {
        Map<String, Object> extensions = ensureExtensions(activity);
        if(value.has("count")) {
            Map<String, Object> rebroadCast = Maps.newHashMap();
            rebroadCast.put("count", value.get("count").asLong());
            rebroadCast.put("perspectival", true);
            extensions.put(REBROADCAST_EXTENSION, rebroadCast);
        }
    }

    private void addObjectHiddenExtension(Activity activity, JsonNode value) {
        Map<String, Object> extensions = ensureExtensions(activity);
        extensions.put("hidden", value.asBoolean());
    }

    private void addIcon(Activity activity, JsonNode value) {
        Icon icon = new Icon();
        //Apparently the Icon didn't map from the schema very well
        icon.setAdditionalProperty("url", value.asText());
        activity.setIcon(icon);
    }

    private void addActor(Activity activity, JsonNode value) {
        Actor actor = new Actor();
        if(value.has("name")) {
            actor.setDisplayName(value.get("name").asText());
        }
        if(value.has("id")) {
            actor.setId("id:"+PROVIDER_NAME+":"+value.get("id").asText());
        }
        activity.setActor(actor);
    }

    private void addObjectSummary(Activity activity, JsonNode value) {
        activity.getObject().setSummary(value.asText());
    }

    private void addGenerator(Activity activity, JsonNode value) {
        Generator generator = new Generator();
        if(value.has("id")) {
            generator.setId(getObjectId(PROVIDER_NAME, "generator", value.get("id").asText()));
        }
        if(value.has("name")) {
            generator.setDisplayName(value.get("name").asText());
        }
        if(value.has("namespace")) {
            generator.setSummary(value.get("namespace").asText());
        }
        activity.setGenerator(generator);
    }

    private void addAttachments(Activity activity, JsonNode value) {
        //No direct mapping at this time
    }

    private static void ensureMoreFields(Iterator<Map.Entry<String, JsonNode>> fields) {
        if(!fields.hasNext()) {
            throw new IllegalStateException("Facebook activity must have one and only one root element");
        }
    }
    private static void ensureNoMoreFields(Iterator<Map.Entry<String, JsonNode>> fields) {
        if(fields.hasNext()) {
            throw new IllegalStateException("Facebook activity must have one and only one root element");
        }
    }

}
