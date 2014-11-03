/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package com.google.gplus.serializer.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Lists;
import com.google.api.services.plus.model.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Custom deserializer for GooglePlus' Person model
 */
public class GPlusActivityDeserializer extends JsonDeserializer<Activity> {
    private final static Logger LOGGER = LoggerFactory.getLogger(GPlusActivityDeserializer.class);

    /**
     * Because the GooglePlus Activity object {@link com.google.api.services.plus.model.Activity} contains complex objects
     * within its hierarchy, we have to use a custom deserializer
     *
     * @param jsonParser
     * @param deserializationContext
     * @return The deserialized {@link com.google.api.services.plus.model.Activity} object
     * @throws IOException
     * @throws JsonProcessingException
     */
    @Override
    public Activity deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Activity activity = new Activity();

        try {
            activity.setUrl(node.get("url").asText());
            activity.setEtag(node.get("etag").asText());
            activity.setTitle(node.get("title").asText());
            activity.setPublished(DateTime.parseRfc3339(node.get("published").asText()));
            activity.setUpdated(DateTime.parseRfc3339(node.get("updated").asText()));
            activity.setId(node.get("id").asText());
            activity.setVerb(node.get("verb").asText());

            activity.setActor(buildActor(node));

            activity.setObject(buildPlusObject(node));
        } catch (Exception e) {
            LOGGER.error("Exception while trying to deserialize activity object: {}", e);
        }

        return activity;
    }

    /**
     * Given a raw JsonNode, build out the G+ {@link com.google.api.services.plus.model.Activity.Actor} object
     *
     * @param node
     * @return {@link com.google.api.services.plus.model.Activity.Actor} object
     */
    private Activity.Actor buildActor(JsonNode node) {
        Activity.Actor actor = new Activity.Actor();
        JsonNode actorNode = node.get("actor");

        actor.setId(actorNode.get("id").asText());
        actor.setDisplayName(actorNode.get("displayName").asText());
        actor.setUrl(actorNode.get("url").asText());

        Activity.Actor.Image image = new Activity.Actor.Image();
        JsonNode imageNode = actorNode.get("image");
        image.setUrl(imageNode.get("url").asText());

        actor.setImage(image);

        return actor;
    }

    /**
     * Given a JsonNode, build out all aspects of the {@link com.google.api.services.plus.model.Activity.PlusObject} object
     *
     * @param node
     * @return {@link com.google.api.services.plus.model.Activity.PlusObject} object
     */
    private Activity.PlusObject buildPlusObject(JsonNode node) {
        Activity.PlusObject object = new Activity.PlusObject();
        JsonNode objectNode = node.get("object");
        object.setObjectType(objectNode.get("objectType").asText());
        object.setContent(objectNode.get("content").asText());
        object.setUrl(objectNode.get("url").asText());

        Activity.PlusObject.Replies replies = new Activity.PlusObject.Replies();
        JsonNode repliesNode = objectNode.get("replies");
        replies.setTotalItems(repliesNode.get("totalItems").asLong());
        replies.setSelfLink(repliesNode.get("selfLink").asText());
        object.setReplies(replies);

        Activity.PlusObject.Plusoners plusoners = new Activity.PlusObject.Plusoners();
        JsonNode plusonersNode = objectNode.get("plusoners");
        plusoners.setTotalItems(plusonersNode.get("totalItems").asLong());
        plusoners.setSelfLink(plusonersNode.get("selfLink").asText());
        object.setPlusoners(plusoners);

        Activity.PlusObject.Resharers resharers = new Activity.PlusObject.Resharers();
        JsonNode resharersNode = objectNode.get("resharers");
        resharers.setTotalItems(resharersNode.get("totalItems").asLong());
        resharers.setSelfLink(resharersNode.get("selfLink").asText());
        object.setResharers(resharers);

        object.setAttachments(buildAttachments(objectNode));//attachments);

        return object;
    }

    /**
     * Given a raw JsonNode representation of an Activity's attachments, build out that
     * list of {@link com.google.api.services.plus.model.Activity.PlusObject.Attachments} objects
     *
     * @param objectNode
     * @return list of {@link com.google.api.services.plus.model.Activity.PlusObject.Attachments} objects
     */
    private List<Activity.PlusObject.Attachments> buildAttachments(JsonNode objectNode) {
        List<Activity.PlusObject.Attachments> attachments = Lists.newArrayList();
        for (JsonNode attachmentNode : objectNode.get("attachments")) {
            Activity.PlusObject.Attachments attachments1 = new Activity.PlusObject.Attachments();
            attachments1.setObjectType(attachmentNode.get("objectType").asText());
            attachments1.setDisplayName(attachmentNode.get("displayName").asText());
            attachments1.setContent(attachmentNode.get("content").asText());
            attachments1.setUrl(attachmentNode.get("url").asText());

            Activity.PlusObject.Attachments.Image image1 = new Activity.PlusObject.Attachments.Image();
            JsonNode imageNode1 = attachmentNode.get("image");
            image1.setUrl(imageNode1.get("url").asText());
            attachments1.setImage(image1);

            attachments.add(attachments1);
        }

        return attachments;
    }
}