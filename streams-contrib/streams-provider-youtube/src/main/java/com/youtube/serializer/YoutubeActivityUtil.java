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

package com.youtube.serializer;

import com.google.api.client.util.Maps;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.Thumbnail;
import com.google.api.services.youtube.model.ThumbnailDetails;
import com.google.api.services.youtube.model.Video;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Image;
import org.apache.streams.pojo.json.Provider;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class YoutubeActivityUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeActivityUtil.class);

    /**
     * Given a {@link com.google.api.services.youtube.YouTube.Videos} object and an
     * {@link org.apache.streams.pojo.json.Activity} object, fill out the appropriate details
     *
     * @param video
     * @param activity
     * @throws org.apache.streams.exceptions.ActivitySerializerException
     */
    public static void updateActivity(Video video, Activity activity, String channelId) throws ActivitySerializerException {
        activity.setActor(buildActor(video, video.getSnippet().getChannelId()));
        activity.setVerb("post");

        activity.setId(formatId(activity.getVerb(),
                Optional.fromNullable(
                        video.getId())
                        .orNull()));

        activity.setPublished(new DateTime(video.getSnippet().getPublishedAt().getValue()));
        activity.setTitle(video.getSnippet().getTitle());
        activity.setContent(video.getSnippet().getDescription());
        activity.setUrl("https://www.youtube.com/watch?v=" + video.getId());

        activity.setProvider(getProvider());

        activity.setObject(buildActivityObject(video));

        addYoutubeExtensions(activity, video);
    }


    /**
     * Given a {@link com.google.api.services.youtube.model.Channel} object and an
     * {@link org.apache.streams.pojo.json.Activity} object, fill out the appropriate details
     *
     * @param channel
     * @param activity
     * @throws org.apache.streams.exceptions.ActivitySerializerException
     */
    public static void updateActivity(Channel channel, Activity activity, String channelId) throws ActivitySerializerException {
        try {
            activity.setProvider(getProvider());
            activity.setVerb("post");
            activity.setActor(createActorForChannel(channel));
            Map<String, Object> extensions = Maps.newHashMap();
            extensions.put("youtube", channel);
            activity.setAdditionalProperty("extensions", extensions);
        } catch (Throwable t) {
            throw new ActivitySerializerException(t);
        }
    }

    public static Actor createActorForChannel(Channel channel) {
        Actor actor = new Actor();
        actor.setId("id:youtube:"+channel.getId());
        actor.setSummary(channel.getSnippet().getDescription());
        actor.setDisplayName(channel.getSnippet().getTitle());
        Image image = new Image();
        image.setUrl(channel.getSnippet().getThumbnails().getHigh().getUrl());
        actor.setImage(image);
        actor.setUrl("https://youtube.com/user/" + channel.getId());
        Map<String, Object> actorExtensions = Maps.newHashMap();
        actorExtensions.put("followers", channel.getStatistics().getSubscriberCount());
        actorExtensions.put("posts", channel.getStatistics().getVideoCount());
        actor.setAdditionalProperty("extensions", actorExtensions);
        return actor;
    }

    /**
     * Given a video object, create the appropriate activity object with a valid image
     * (thumbnail) and video URL
     * @param video
     * @return Activity Object with Video URL and a thumbnail image
     */
    private static ActivityObject buildActivityObject(Video video) {
        ActivityObject activityObject = new ActivityObject();

        ThumbnailDetails thumbnailDetails = video.getSnippet().getThumbnails();
        Thumbnail thumbnail = thumbnailDetails.getDefault();

        if(thumbnail != null) {
            Image image = new Image();
            image.setUrl(thumbnail.getUrl());
            image.setHeight(thumbnail.getHeight().doubleValue());
            image.setWidth(thumbnail.getWidth().doubleValue());

            activityObject.setImage(image);
        }

        activityObject.setUrl("https://www.youtube.com/watch?v=" + video.getId());
        activityObject.setObjectType("video");

        return activityObject;
    }

    /**
     * Add the Youtube extensions to the Activity object that we're building
     * @param activity
     * @param video
     */
    private static void addYoutubeExtensions(Activity activity, Video video) {
        Map<String, Object> extensions = ExtensionUtil.ensureExtensions(activity);

        extensions.put("youtube", video);

        if(video.getStatistics() != null) {
            Map<String, Object> likes = new HashMap<>();
            likes.put("count", video.getStatistics().getCommentCount());
            extensions.put("likes", likes);
        }
    }

    /**
     * Build an {@link org.apache.streams.pojo.json.Actor} object given the video object
     * @param video
     * @param id
     * @return Actor object
     */
    private static Actor buildActor(Video video, String id) {
        Actor actor = new Actor();

        actor.setId("id:youtube:" + id);
        actor.setDisplayName(video.getSnippet().getChannelTitle());
        actor.setSummary(video.getSnippet().getDescription());
        actor.setAdditionalProperty("handle", video.getSnippet().getChannelTitle());

        return actor;
    }

    /**
     * Gets the common youtube {@link org.apache.streams.pojo.json.Provider} object
     * @return a provider object representing YouTube
     */
    public static Provider getProvider() {
        Provider provider = new Provider();
        provider.setId("id:providers:youtube");
        provider.setDisplayName("YouTube");
        return provider;
    }

    /**
     * Formats the ID to conform with the Apache Streams activity ID convention
     * @param idparts the parts of the ID to join
     * @return a valid Activity ID in format "id:youtube:part1:part2:...partN"
     */
    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:youtube", idparts));
    }
}