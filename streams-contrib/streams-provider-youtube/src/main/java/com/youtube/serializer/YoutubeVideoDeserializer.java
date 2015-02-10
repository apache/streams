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

package com.youtube.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
public class YoutubeVideoDeserializer extends JsonDeserializer<Video> {
    private final static Logger LOGGER = LoggerFactory.getLogger(YoutubeVideoDeserializer.class);

    /**
     * Because the Youtube Video object contains complex objects within its hierarchy, we have to use
     * a custom deserializer
     *
     * @param jsonParser
     * @param deserializationContext
     * @return The deserialized {@link com.google.api.services.youtube.YouTube.Videos} object
     * @throws java.io.IOException
     * @throws com.fasterxml.jackson.core.JsonProcessingException
     */
    @Override
    public Video deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        JsonNode node = jsonParser.getCodec().readTree(jsonParser);
        Video video = new Video();

        try {
            video.setId(node.get("id").asText());
            video.setEtag(node.get("etag").asText());
            video.setKind(node.get("kind").asText());

            video.setSnippet(buildSnippet(node));
            video.setStatistics(buildStatistics(node));
        } catch (Exception e) {
            LOGGER.error("Exception while trying to deserialize a Video object: {}", e);
        }

        return video;
    }

    /**
     * Given the raw JsonNode, construct a video snippet object
     * @param node
     * @return VideoSnippet
     */
    private VideoSnippet buildSnippet(JsonNode node) {
        VideoSnippet snippet = new VideoSnippet();
        JsonNode snippetNode = node.get("snippet");

        snippet.setChannelId(snippetNode.get("channelId").asText());
        snippet.setChannelTitle(snippetNode.get("channelTitle").asText());
        snippet.setDescription(snippetNode.get("description").asText());
        snippet.setTitle(snippetNode.get("title").asText());
        snippet.setPublishedAt(new DateTime(snippetNode.get("publishedAt").get("value").asLong()));

        ThumbnailDetails thumbnailDetails = new ThumbnailDetails();
        for(JsonNode t : snippetNode.get("thumbnails")) {
            Thumbnail thumbnail = new Thumbnail();

            thumbnail.setHeight(t.get("height").asLong());
            thumbnail.setUrl(t.get("url").asText());
            thumbnail.setWidth(t.get("width").asLong());

            thumbnailDetails.setDefault(thumbnail);
        }

        snippet.setThumbnails(thumbnailDetails);

        return snippet;
    }

    /**
     * Given the raw JsonNode, construct a statistics object
     * @param node
     * @return VideoStatistics
     */
    private VideoStatistics buildStatistics(JsonNode node) {
        VideoStatistics statistics = new VideoStatistics();
        JsonNode statisticsNode = node.get("statistics");

        statistics.setCommentCount(statisticsNode.get("commentCount").bigIntegerValue());
        statistics.setDislikeCount(statisticsNode.get("dislikeCount").bigIntegerValue());
        statistics.setFavoriteCount(statisticsNode.get("favoriteCount").bigIntegerValue());
        statistics.setLikeCount(statisticsNode.get("likeCount").bigIntegerValue());
        statistics.setViewCount(statisticsNode.get("viewCount").bigIntegerValue());

        return statistics;
    }
}