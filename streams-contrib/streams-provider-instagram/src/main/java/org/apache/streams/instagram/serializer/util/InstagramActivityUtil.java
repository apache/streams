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

import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.instagram.pojo.Comment;
import org.apache.streams.instagram.pojo.Comments;
import org.apache.streams.instagram.pojo.Images;
import org.apache.streams.instagram.pojo.Media;
import org.apache.streams.instagram.pojo.MediaItem;
import org.apache.streams.instagram.pojo.UserInfo;
import org.apache.streams.instagram.pojo.UserInfoCounts;
import org.apache.streams.instagram.pojo.Videos;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Image;
import org.apache.streams.pojo.json.Provider;

import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Provides utilities for working with Activity objects within the context of Instagram.
 */
public class InstagramActivityUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramActivityUtil.class);
  /**
   * Updates the given Activity object with the values from the item
   * @param item the object to use as the source
   * @param activity the target of the updates.  Will receive all values from the tweet.
   * @throws ActivityConversionException ActivityConversionException
   */

  public static void updateActivity(Media item, Activity activity) throws ActivityConversionException {
    activity.setActor(buildActor(item));
    activity.setVerb("post");

    if (item.getCreatedTime() != null) {
      activity.setPublished(new DateTime(Long.parseLong(item.getCreatedTime()) * 1000));
    }

    activity.setId(formatId(activity.getVerb(),
        Optional.ofNullable(item.getId()).orElse(null)));

    activity.setProvider(getProvider());
    activity.setUrl(item.getLink());
    activity.setObject(buildActivityObject(item));

    if (item.getCaption() != null) {
      activity.setContent(item.getCaption().getText());
    }

    addInstagramExtensions(activity, item);
  }

  /**
   * Updates the given Activity object with the values from the item
   * @param item the object to use as the source
   * @param activity the target of the updates.  Will receive all values from the tweet.
   * @throws ActivitySerializerException ActivitySerializerException
   */
  public static void updateActivity(UserInfo item, Activity activity) throws ActivitySerializerException {
    activity.setActor(buildActor(item));
    activity.setId(null);
    activity.setProvider(getProvider());
  }

  /**
   * Builds an Actor object given a UserInfo object.
   * @param item UserInfo item
   * @return Actor object
   */
  public static ActivityObject buildActor(UserInfo item) {
    ActivityObject actor = new ActivityObject();

    try {
      Image image = new Image();
      image.setUrl(item.getProfilePicture());

      UserInfoCounts counts = item.getCounts();

      Map<String, Object> extensions = new HashMap<>();

      extensions.put("followers", counts.getFollowedBy());
      extensions.put("follows", counts.getFollows());
      extensions.put("screenName", item.getUsername());
      extensions.put("posts", counts.getMedia());

      actor.setId(formatId(String.valueOf(item.getId())));
      actor.setImage(image);
      actor.setDisplayName(item.getFullName());
      actor.setSummary(item.getBio());
      actor.setUrl(item.getWebsite());

      actor.setAdditionalProperty("handle", item.getUsername());
      actor.setAdditionalProperty("extensions", extensions);
    } catch (Exception ex) {
      LOGGER.error("Exception trying to build actor object: {}", ex.getMessage());
    }

    return actor;
  }

  /**
   * Builds the actor.
   * @param item Media item
   * @return a valid ActivityObject
   */
  public static ActivityObject buildActor(Media item) {
    ActivityObject actor = new ActivityObject();

    try {
      Image image = new Image();
      image.setUrl(item.getUser().getProfilePicture());

      Map<String, Object> extensions = new HashMap<>();
      extensions.put("screenName", item.getUser().getUsername());

      actor.setDisplayName(item.getUser().getFullName());
      actor.setSummary(item.getUser().getBio());
      actor.setUrl(item.getUser().getWebsite());

      actor.setId(formatId(String.valueOf(item.getUser().getId())));
      actor.setImage(image);
      actor.setAdditionalProperty("extensions", extensions);
      actor.setAdditionalProperty("handle", item.getUser().getUsername());
    } catch (Exception ex) {
      LOGGER.error("Exception trying to build actor object: {}", ex.getMessage());
    }

    return actor;
  }

  /**
   * Builds the object.
   * @param item the item
   * @return a valid Activity Object
   */
  public static ActivityObject buildActivityObject(Media item) {
    ActivityObject actObj = new ActivityObject();

    actObj.setObjectType(item.getType().toString());
    actObj.setAttachments(buildActivityObjectAttachments(item));

    Image standardResolution = new Image();
    if (item.getType().equals("image") && item.getImages() != null) {
      MediaItem standardResolutionData = item.getImages().getStandardResolution();
      standardResolution.setHeight(standardResolutionData.getHeight().longValue());
      standardResolution.setWidth(standardResolutionData.getWidth().longValue());
      standardResolution.setUrl(standardResolutionData.getUrl());
    } else if (item.getType().equals("video") && item.getVideos() != null) {
      MediaItem standardResolutionData = item.getVideos().getStandardResolution();
      standardResolution.setHeight(standardResolutionData.getHeight().longValue());
      standardResolution.setWidth(standardResolutionData.getWidth().longValue());
      standardResolution.setUrl(standardResolutionData.getUrl());
    }

    actObj.setImage(standardResolution);

    return actObj;
  }

  /**
   * Builds all of the attachments associated with a Media object.
   *
   * @param item item
   * @return result
   */
  public static List<ActivityObject> buildActivityObjectAttachments(Media item) {
    List<ActivityObject> attachments = new ArrayList<>();

    addImageObjects(attachments, item);
    addVideoObjects(attachments, item);

    return attachments;
  }

  /**
   * Adds any image objects to the attachment field.
   * @param attachments attachments
   * @param item item
   */
  public static void addImageObjects(List<ActivityObject> attachments, Media item) {
    Images images = item.getImages();

    if (images != null) {
      try {
        MediaItem thumbnail = images.getThumbnail();
        MediaItem lowResolution = images.getLowResolution();

        ActivityObject thumbnailObject = new ActivityObject();
        Image thumbnailImage = new Image();
        thumbnailImage.setUrl(thumbnail.getUrl());
        thumbnailImage.setHeight(thumbnail.getHeight().longValue());
        thumbnailImage.setWidth(thumbnail.getWidth().longValue());
        thumbnailObject.setImage(thumbnailImage);
        thumbnailObject.setObjectType("image");

        ActivityObject lowResolutionObject = new ActivityObject();
        Image lowResolutionImage = new Image();
        lowResolutionImage.setUrl(lowResolution.getUrl());
        lowResolutionImage.setHeight(lowResolution.getHeight().longValue());
        lowResolutionImage.setWidth(lowResolution.getWidth().longValue());
        lowResolutionObject.setImage(lowResolutionImage);
        lowResolutionObject.setObjectType("image");

        attachments.add(thumbnailObject);
        attachments.add(lowResolutionObject);
      } catch (Exception ex) {
        LOGGER.error("Failed to add image objects: {}", ex.getMessage());
      }
    }
  }

  /**
   * Adds any video objects to the attachment field.
   * @param attachments attachments
   * @param item item
   */
  public static void addVideoObjects(List<ActivityObject> attachments, Media item) {
    Videos videos = item.getVideos();

    if (videos != null) {
      try {
        MediaItem lowResolutionVideo = videos.getLowResolution();

        ActivityObject lowResolutionVideoObject = new ActivityObject();
        Image lowResolutionVideoImage = new Image();
        lowResolutionVideoImage.setUrl(lowResolutionVideo.getUrl());
        lowResolutionVideoImage.setHeight(lowResolutionVideo.getHeight().longValue());
        lowResolutionVideoImage.setWidth(lowResolutionVideo.getWidth().longValue());
        lowResolutionVideoObject.setImage(lowResolutionVideoImage);
        lowResolutionVideoObject.setObjectType("video");

        attachments.add(lowResolutionVideoObject);
      } catch (Exception ex) {
        LOGGER.error("Failed to add video objects: {}", ex.getMessage());
      }
    }
  }

  /**
   * Gets the links from the Instagram event.
   * @param item the object to use as the source
   * @return a list of links corresponding to the expanded URL
   */
  public static List<String> getLinks(Media item) {
    return new ArrayList<>();
  }

  /**
   * Adds the location extension and populates with the instagram data.
   * @param activity the Activity object to update
   * @param item the object to use as the source
   */
  public static void addLocationExtension(Activity activity, Media item) {
    Map<String, Object> extensions = ExtensionUtil.getInstance().ensureExtensions(activity);

    if (item.getLocation() != null) {
      Map<String, Object> coordinates = new HashMap<>();
      coordinates.put("type", "Point");
      coordinates.put("coordinates", "[" + item.getLocation().getLongitude() + "," + item.getLocation().getLatitude() + "]");

      extensions.put("coordinates", coordinates);
    }
  }

  /**
   * Gets the common instagram {@link Provider} object.
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
    return String.join(":",
        Stream.concat(Arrays.stream(new String[]{"id:instagram"}), Arrays.stream(idparts)).collect(Collectors.toList()));
  }

  /**
   * Takes various parameters from the instagram object that are currently not part of the
   * activity schema and stores them in a generic extensions attribute.
   * @param activity Activity activity
   * @param item Media item
   */
  public static void addInstagramExtensions(Activity activity, Media item) {
    Map<String, Object> extensions = ExtensionUtil.getInstance().ensureExtensions(activity);

    addLocationExtension(activity, item);

    if (item.getLikes() != null) {
      Map<String, Object> likes = new HashMap<>();
      likes.put("count", item.getLikes().getCount());
      extensions.put("likes", likes);
    }

    if (item.getComments() != null) {
      Map<String, Object> comments = new HashMap<>();
      comments.put("count", item.getComments().getCount());
      extensions.put("comments", comments);
    }

    extensions.put("hashtags", item.getTags());

    Comments comments = item.getComments();
    String commentsConcat = "";

    if (comments != null) {
      for (Comment comment : comments.getData()) {
        commentsConcat += " " + comment.getText();
      }
    }
    if (item.getCaption() != null) {
      commentsConcat += " " + item.getCaption().getText();
    }

    extensions.put("keywords", commentsConcat);
  }
}