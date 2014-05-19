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

package com.facebook.api;

/*
 * #%L
 * streams-provider-facebook
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */


import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.*;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

import static org.apache.streams.data.util.ActivityUtil.*;
import static org.apache.streams.data.util.JsonUtil.jsonToJsonNode;

/**
 * Serializes activity posts
 *   sblackmon: This class needs a rewrite
 */
public class FacebookPostActivitySerializer implements ActivitySerializer<String> {

    public static final DateTimeFormatter FACEBOOK_FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ");
    public static final DateTimeFormatter ACTIVITY_FORMAT = ISODateTimeFormat.basicDateTime();

    public static final String PROVIDER_NAME = "facebook";

    public static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper();
        mapper.registerModule(new JodaModule());
        mapper.disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.registerModule(new SimpleModule() {
            {
                addSerializer(DateTime.class, new StdSerializer<DateTime>(DateTime.class) {
                    @Override
                    public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
                        jgen.writeString(ACTIVITY_FORMAT.print(value));
                    }
                });
                addDeserializer(DateTime.class, new StdDeserializer<DateTime>(DateTime.class) {
                    @Override
                    public DateTime deserialize(JsonParser jpar, DeserializationContext context) throws IOException, JsonProcessingException {
                        return FACEBOOK_FORMAT.parseDateTime(jpar.getValueAsString());
                    }
                });
            }
        });
    }

    @Override
    public String serializationFormat() {
        return "facebook_post_json_v1";
    }

    @Override
    public String serialize(Activity deserialized) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Activity deserialize(String serialized) throws ActivitySerializerException {
        //Deserialize the JSON string into a Jackson JsonNode
        JsonNode node = jsonToJsonNode(serialized);
        Map.Entry<String, JsonNode> field = getObjectType(node);
        Activity activity = new Activity();
        setProvider(activity);
        setObjectType(field, activity);
        parseObject(activity, field.getValue());
        fixObjectId(activity);
        fixContentFromSummary(activity);
        return activity;
    }

    @Override
    public java.util.List<Activity> deserializeAll(List<String> serializedList) {
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

    private void setObjectType(Map.Entry<String, JsonNode> field, Activity activity) {
        ActivityObject object = new ActivityObject();
        activity.setObject(object);
        object.setObjectType(field.getKey());
    }

    private void setProvider(Activity activity) {
        Provider provider = new Provider();
        provider.setId(getProviderId(PROVIDER_NAME));
        provider.setDisplayName(PROVIDER_NAME);
        activity.setProvider(provider);
    }

    private Map.Entry<String, JsonNode> getObjectType(JsonNode node) {
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        ensureMoreFields(fields);
        Map.Entry<String, JsonNode> field = fields.next();
        ensureNoMoreFields(fields);
        return field;
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
        if ("created_time".equals(name)) {
            activity.setPublished(parseDate(value));
        } else if("application".equals(name)) {
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
        }  else if ("updated_time".equals(name)) {
            addObjectUpdated(activity, value);
        }
    }

    private void addObjectUpdated(Activity activity, JsonNode value) {
        try {
            activity.getObject().setUpdated(parseDate(value));
        } catch( ActivitySerializerException e ) {
            // losing this field is ok
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
        extensions.put(LIKES_EXTENSION, value.asInt());
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
        activity.setId(getActivityId(PROVIDER_NAME, value.asText()));
    }

    private void addObjectLink(Activity activity, JsonNode value) {
        activity.getObject().setUrl(value.asText());
    }

    private void addRebroadcastExtension(Activity activity, JsonNode value) {
        Map<String, Object> extensions = ensureExtensions(activity);
        if(value.has("count")) {
            extensions.put(REBROADCAST_EXTENSION, value.get("count").asInt());
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
            actor.setId(getPersonId(PROVIDER_NAME, value.get("id").asText()));
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

    private static DateTime parseDate(JsonNode value) throws ActivitySerializerException {
        try {
            return FACEBOOK_FORMAT.parseDateTime(value.asText());
        } catch (Exception e) {
            throw new ActivitySerializerException("Unable to parse date " + value.asText());
        }
    }
}
