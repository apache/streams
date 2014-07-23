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

package org.apache.streams.instagram.serializer.util;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.*;
import org.jinstagram.entity.comments.CommentData;
import org.jinstagram.entity.common.*;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
 * Provides utilities for working with Activity objects within the context of Instagram
 */
public class InstagramActivityUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramActivityUtil.class);
    /**
     * Updates the given Activity object with the values from the item
     * @param item the object to use as the source
     * @param activity the target of the updates.  Will receive all values from the tweet.
     * @throws ActivitySerializerException
     */
    public static void updateActivity(MediaFeedData item, Activity activity) throws ActivitySerializerException {
        activity.setActor(buildActor(item));
        activity.setVerb("post");

        if(item.getCreatedTime() != null)
            activity.setPublished(new DateTime(Long.parseLong(item.getCreatedTime()) * 1000));

        activity.setId(formatId(activity.getVerb(),
            Optional.fromNullable(
                    item.getId())
                        .orNull()));

        activity.setProvider(getProvider());
        activity.setUrl(item.getLink());
        activity.setObject(buildActivityObject(item));

        if(item.getCaption() != null)
            activity.setContent(item.getCaption().getText());

        addInstagramExtensions(activity, item);
    }

    /**
     * Builds the actor
     * @param item the item
     * @return a valid Actor
     */
    public static  Actor buildActor(MediaFeedData item) {
        Actor actor = new Actor();

        try {
            Image image = new Image();
            image.setUrl(item.getUser().getProfilePictureUrl());

            Map<String, Object> extensions = new HashMap<String, Object>();
            extensions.put("screenName", item.getUser().getUserName());

            actor.setId(formatId(String.valueOf(item.getUser().getId())));
            actor.setImage(image);
            actor.setAdditionalProperty("extensions", extensions);
            actor.setAdditionalProperty("handle", item.getUser().getUserName());
        } catch (Exception e) {
            LOGGER.error("Exception trying to build actor object: {}", e.getMessage());
        }

        return actor;
    }

    /**
     * Builds the ActivityObject
     * @param item the item
     * @return a valid Activity Object
     */
    public static ActivityObject buildActivityObject(MediaFeedData item) {
        ActivityObject actObj = new ActivityObject();

        actObj.setObjectType(item.getType());
        actObj.setAttachments(buildActivityObjectAttachments(item));

        Image standardResolution = new Image();
        if(item.getType() == "image" && item.getImages() != null) {
            ImageData standardResolutionData = item.getImages().getStandardResolution();
            standardResolution.setHeight((double)standardResolutionData.getImageHeight());
            standardResolution.setWidth((double)standardResolutionData.getImageWidth());
            standardResolution.setUrl(standardResolutionData.getImageUrl());
        } else if(item.getType() == "video" && item.getVideos() != null) {
            VideoData standardResolutionData = item.getVideos().getStandardResolution();
            standardResolution.setHeight((double)standardResolutionData.getHeight());
            standardResolution.setWidth((double)standardResolutionData.getWidth());
            standardResolution.setUrl(standardResolutionData.getUrl());
        }

        actObj.setImage(standardResolution);

        return actObj;
    }

    /**
     * Builds all of the attachments associated with a MediaFeedData object
     *
     * @param item
     * @return
     */
    public static List<ActivityObject> buildActivityObjectAttachments(MediaFeedData item) {
        List<ActivityObject> attachments = new ArrayList<ActivityObject>();

        addImageObjects(attachments, item);
        addVideoObjects(attachments, item);

        return attachments;
    }

    /**
     * Adds any image objects to the attachment field
     * @param attachments
     * @param item
     */
    public static void addImageObjects(List<ActivityObject> attachments, MediaFeedData item) {
        Images images = item.getImages();

        if(images != null) {
            try {
                ImageData thumbnail = images.getThumbnail();
                ImageData lowResolution = images.getLowResolution();

                ActivityObject thumbnailObject = new ActivityObject();
                Image thumbnailImage = new Image();
                thumbnailImage.setUrl(thumbnail.getImageUrl());
                thumbnailImage.setHeight((double) thumbnail.getImageHeight());
                thumbnailImage.setWidth((double) thumbnail.getImageWidth());
                thumbnailObject.setImage(thumbnailImage);
                thumbnailObject.setObjectType("image");

                ActivityObject lowResolutionObject = new ActivityObject();
                Image lowResolutionImage = new Image();
                lowResolutionImage.setUrl(lowResolution.getImageUrl());
                lowResolutionImage.setHeight((double) lowResolution.getImageHeight());
                lowResolutionImage.setWidth((double) lowResolution.getImageWidth());
                lowResolutionObject.setImage(lowResolutionImage);
                lowResolutionObject.setObjectType("image");

                attachments.add(thumbnailObject);
                attachments.add(lowResolutionObject);
            } catch (Exception e) {
                LOGGER.error("Failed to add image objects: {}", e.getMessage());
            }
        }
    }

    /**
     * Adds any video objects to the attachment field
     * @param attachments
     * @param item
     */
    public static void addVideoObjects(List<ActivityObject> attachments, MediaFeedData item) {
        Videos videos = item.getVideos();

        if(videos != null) {
            try {
                VideoData lowResolutionVideo = videos.getLowResolution();

                ActivityObject lowResolutionVideoObject = new ActivityObject();
                Image lowResolutionVideoImage = new Image();
                lowResolutionVideoImage.setUrl(lowResolutionVideo.getUrl());
                lowResolutionVideoImage.setHeight((double) lowResolutionVideo.getHeight());
                lowResolutionVideoImage.setWidth((double) lowResolutionVideo.getWidth());
                lowResolutionVideoObject.setImage(lowResolutionVideoImage);
                lowResolutionVideoObject.setObjectType("video");

                attachments.add(lowResolutionVideoObject);
            } catch (Exception e) {
                LOGGER.error("Failed to add video objects: {}", e.getMessage());
            }
        }
    }

    /**
     * Gets the links from the Instagram event
     * @param item the object to use as the source
     * @return a list of links corresponding to the expanded URL
     */
    public static List<String> getLinks(MediaFeedData item) {
        List<String> links = Lists.newArrayList();
        return links;
    }

    /**
     * Adds the location extension and populates with teh instagram data
     * @param activity the Activity object to update
     * @param item the object to use as the source
     */
    public static void addLocationExtension(Activity activity, MediaFeedData item) {
        Map<String, Object> extensions = ensureExtensions(activity);

        if(item.getLocation() != null) {
            Map<String, Object> coordinates = new HashMap<String, Object>();
            coordinates.put("type", "Point");
            coordinates.put("coordinates", "[" + item.getLocation().getLongitude() + "," + item.getLocation().getLatitude() + "]");

            extensions.put("coordinates", coordinates);
        }
    }

    /**
     * Gets the common instagram {@link org.apache.streams.pojo.json.Provider} object
     * @return a provider object representing Instagram
     */
    public static Provider getProvider() {
        Provider provider = new Provider();
        provider.setId("id:providers:instagram");
        provider.setDisplayName("Instagram");
        return provider;
    }

    /**
     * Formats the ID to conform with the Apache Streams activity ID convention
     * @param idparts the parts of the ID to join
     * @return a valid Activity ID in format "id:instagram:part1:part2:...partN"
     */
    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:instagram", idparts));
    }

    /**
     * Takes various parameters from the instagram object that are currently not part of teh
     * activity schema and stores them in a generic extensions attribute
     * @param activity
     * @param item
     */
    public static void addInstagramExtensions(Activity activity, MediaFeedData item) {
        Map<String, Object> extensions = ensureExtensions(activity);

        addLocationExtension(activity, item);

        if(item.getLikes() != null) {
            Map<String, Object> likes = new HashMap<String, Object>();
            likes.put("count", item.getLikes().getCount());
            extensions.put("likes", likes);
        }

        extensions.put("hashtags", item.getTags());

        Comments comments = item.getComments();
        String commentsConcat = "";
        for(CommentData commentData : comments.getComments()) {
            commentsConcat += " " + commentData.getText();
        }
        if(item.getCaption() != null) {
            commentsConcat += " " + item.getCaption().getText();
        }

        extensions.put("keywords", commentsConcat);
    }
}