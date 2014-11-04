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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.util.DateTime;
import com.google.api.client.util.Lists;
import com.google.api.services.plus.model.Comment;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class GPlusCommentDeserializer  extends JsonDeserializer<Comment> {
    private final static Logger LOGGER = LoggerFactory.getLogger(GPlusActivityDeserializer.class);

    /**
     * Because the GooglePlus Comment object {@link com.google.api.services.plus.model.Comment} contains complex objects
     * within its hierarchy, we have to use a custom deserializer
     *
     * @param jsonParser
     * @param deserializationContext
     * @return The deserialized {@link com.google.api.services.plus.model.Comment} object
     * @throws java.io.IOException
     * @throws com.fasterxml.jackson.core.JsonProcessingException
     */
    @Override
    public Comment deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {

        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        ObjectMapper objectMapper = new StreamsJacksonMapper();
        Comment comment = new Comment();

        try {
            comment.setEtag(node.get("etag").asText());
            comment.setVerb(node.get("verb").asText());
            comment.setId(node.get("id").asText());
            comment.setPublished(DateTime.parseRfc3339(node.get("published").asText()));
            comment.setUpdated(DateTime.parseRfc3339(node.get("updated").asText()));

            Comment.Actor actor = new Comment.Actor();
            JsonNode actorNode = node.get("actor");
            actor.setDisplayName(actorNode.get("displayName").asText());
            actor.setUrl(actorNode.get("url").asText());

            Comment.Actor.Image image = new Comment.Actor.Image();
            JsonNode imageNode = actorNode.get("image");
            image.setUrl(imageNode.get("url").asText());

            actor.setImage(image);

            comment.setObject(objectMapper.readValue(objectMapper.writeValueAsString(node.get("object")), Comment.PlusObject.class));

            comment.setSelfLink(node.get("selfLink").asText());

            List<Comment.InReplyTo> replies = Lists.newArrayList();
            for(JsonNode reply : node.get("inReplyTo")) {
                Comment.InReplyTo r = objectMapper.readValue(objectMapper.writeValueAsString(reply), Comment.InReplyTo.class);
                replies.add(r);
            }

            comment.setInReplyTo(replies);

            Comment.Plusoners plusoners = new Comment.Plusoners();
            JsonNode plusonersNode = node.get("plusoners");
            plusoners.setTotalItems(plusonersNode.get("totalItems").asLong());
            comment.setPlusoners(plusoners);
        } catch (Exception e) {
            LOGGER.error("Exception while trying to deserialize activity object: {}", e);
        }

        return comment;
    }
}
