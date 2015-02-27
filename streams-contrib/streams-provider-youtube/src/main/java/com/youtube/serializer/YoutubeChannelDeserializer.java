package com.youtube.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.api.client.util.DateTime;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelContentDetails;
import com.google.api.services.youtube.model.ChannelLocalization;
import com.google.api.services.youtube.model.ChannelSnippet;
import com.google.api.services.youtube.model.ChannelStatistics;
import com.google.api.services.youtube.model.ChannelTopicDetails;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 *
 */
public class YoutubeChannelDeserializer extends JsonDeserializer<Channel> {


    @Override
    public Channel deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        JsonNode node = jp.getCodec().readTree(jp);
        try {
            Channel channel = new Channel();
            if(node.findPath("etag") != null)
                channel.setEtag(node.get("etag").asText());
            if(node.findPath("kind") != null)
                channel.setKind(node.get("kind").asText());
            channel.setId(node.get("id").asText());
            channel.setTopicDetails(setTopicDetails(node.findValue("topicDetails")));
            channel.setStatistics(setChannelStatistics(node.findValue("statistics")));
            channel.setContentDetails(setContentDetails(node.findValue("contentDetails")));
            channel.setSnippet(setChannelSnippet(node.findValue("snippet")));
            return channel;
        } catch (Throwable t) {
            throw new IOException(t);
        }
    }

    protected ChannelSnippet setChannelSnippet(JsonNode node) {
        ChannelSnippet snippet = new ChannelSnippet();
        snippet.setTitle(node.get("title").asText());
        snippet.setDescription(node.get("description").asText());
        snippet.setPublishedAt(new DateTime(node.get("publishedAt").get("value").longValue()));
        snippet.setLocalized(setLocalized(node.findValue("localized")));
        snippet.setThumbnails(setThumbnails(node.findValue("thumbnails")));
        return snippet;
    }

    protected ThumbnailDetails setThumbnails(JsonNode node) {
        ThumbnailDetails details = new ThumbnailDetails();
        if(node == null) {
            return details;
        }
        details.setDefault(new Thumbnail().setUrl(node.get("default").get("url").asText()));
        details.setHigh(new Thumbnail().setUrl(node.get("high").get("url").asText()));
        details.setMedium(new Thumbnail().setUrl(node.get("medium").get("url").asText()));
        return details;
    }

    protected ChannelLocalization setLocalized(JsonNode node) {
        if(node == null) {
            return new ChannelLocalization();
        }
        ChannelLocalization localization = new ChannelLocalization();
        localization.setDescription(node.get("description").asText());
        localization.setTitle(node.get("title").asText());
        return localization;
    }

    protected ChannelContentDetails setContentDetails(JsonNode node) {
        ChannelContentDetails contentDetails = new ChannelContentDetails();
        if(node == null) {
            return contentDetails;
        }
        if(node.findValue("googlePlusUserId") != null)
            contentDetails.setGooglePlusUserId(node.get("googlePlusUserId").asText());
        contentDetails.setRelatedPlaylists(setRelatedPlaylists(node.findValue("relatedPlaylists")));
        return contentDetails;
    }

    protected ChannelContentDetails.RelatedPlaylists setRelatedPlaylists(JsonNode node) {
        ChannelContentDetails.RelatedPlaylists playlists = new ChannelContentDetails.RelatedPlaylists();
        if(node == null) {
            return playlists;
        }
        if(node.findValue("favorites") != null)
            playlists.setFavorites(node.get("favorites").asText());
        if(node.findValue("likes") != null)
            playlists.setLikes(node.get("likes").asText());
        if(node.findValue("uploads") != null)
            playlists.setUploads(node.get("uploads").asText());
        return playlists;
    }

    protected ChannelStatistics setChannelStatistics(JsonNode node) {
        ChannelStatistics stats = new ChannelStatistics();
        if(node == null) {
            return stats;
        }
        stats.setCommentCount(node.get("commentCount").bigIntegerValue());
        stats.setHiddenSubscriberCount(node.get("hiddenSubscriberCount").asBoolean());
        stats.setSubscriberCount(node.get("subscriberCount").bigIntegerValue());
        stats.setVideoCount(node.get("videoCount").bigIntegerValue());
        stats.setViewCount(node.get("viewCount").bigIntegerValue());
        return stats;
    }

    protected ChannelTopicDetails setTopicDetails(JsonNode node) {
        ChannelTopicDetails details = new ChannelTopicDetails();
        if(node == null) {
            return details;
        }
        List<String> topicIds = Lists.newLinkedList();
        Iterator<JsonNode> it = node.get("topicIds").iterator();
        while(it.hasNext()) {
            topicIds.add(it.next().asText());
        }
        details.setTopicIds(topicIds);
        return  details;
    }
}
