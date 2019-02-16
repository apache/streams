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

package org.apache.streams.twitter.converter.util;

import org.apache.streams.exceptions.ActivityConversionException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Image;
import org.apache.streams.pojo.json.Provider;
import org.apache.streams.twitter.Url;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Entities;
import org.apache.streams.twitter.pojo.Hashtag;
import org.apache.streams.twitter.pojo.Place;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.TargetObject;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.pojo.UserMentions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringUtils;
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

import static com.google.common.math.DoubleMath.mean;

/**
 * Provides utilities for working with Activity objects within the context of Twitter.
 */
public class TwitterActivityUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterActivityUtil.class);

  private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  /**
   * Updates the given Activity object with the values from the Tweet.
   * @param tweet the object to use as the source
   * @param activity the target of the updates.  Will receive all values from the tweet.
   * @throws ActivityConversionException ActivityConversionException
   */
  public static void updateActivity(Tweet tweet, Activity activity) throws ActivityConversionException {
    activity.setActor(buildActor(tweet));
    activity.setId(formatId(activity.getVerb(),
        Optional.ofNullable(Optional.ofNullable(tweet.getIdStr())
            .orElseGet(Optional.of(tweet.getId().toString())::get)).orElse(null)));

    if (tweet instanceof Retweet) {
      updateActivityContent(activity,  (tweet).getRetweetedStatus(), "share");
    } else {
      updateActivityContent(activity, tweet, "post");
    }

    if (StringUtils.isBlank(activity.getId())) {
      throw new ActivityConversionException("Unable to determine activity id");
    }
    try {
      activity.setPublished(tweet.getCreatedAt());
    } catch ( Exception ex ) {
      throw new ActivityConversionException("Unable to determine publishedDate", ex);
    }
    activity.setTarget(buildTarget(tweet));
    activity.setProvider(getProvider());
    activity.setUrl(String.format("http://twitter.com/%s/%s/%s", tweet.getUser().getScreenName(),"/status/",tweet.getIdStr()));

    addTwitterExtension(activity, mapper.convertValue(tweet, ObjectNode.class));
  }

  /**
   * Updates the given Activity object with the values from the User
   * @param user the object to use as the source
   * @param activity the target of the updates.  Will receive all values from the tweet.
   */
  public static void updateActivity(User user, Activity activity) {
    activity.setActor(buildActor(user));
    activity.setId(null);
    activity.setVerb(null);
  }

  /**
   * Updates the activity for a delete event.
   * @param delete the delete event
   * @param activity the Activity object to update
   * @throws ActivityConversionException ActivityConversionException
   */
  public static void updateActivity(Delete delete, Activity activity) throws ActivityConversionException {
    activity.setActor(buildActor(delete));
    activity.setVerb("delete");
    activity.setObject(buildActivityObject(delete));
    activity.setId(formatId(activity.getVerb(), delete.getDelete().getStatus().getIdStr()));
    if (StringUtils.isBlank(activity.getId())) {
      throw new ActivityConversionException("Unable to determine activity id");
    }
    activity.setProvider(getProvider());
    addTwitterExtension(activity, StreamsJacksonMapper.getInstance().convertValue(delete, ObjectNode.class));
  }

  /**
   * Builds the activity {@link ActivityObject} actor from the tweet.
   * @param tweet the object to use as the source
   * @return a valid Actor populated from the Tweet
   */
  public static ActivityObject buildActor(Tweet tweet) {
    ActivityObject actor = new ActivityObject();
    User user = tweet.getUser();

    return buildActor(user);
  }

  /**
   * Builds the activity {@link ActivityObject} actor from the User.
   * @param user the object to use as the source
   * @return a valid Actor populated from the Tweet
   */
  public static ActivityObject buildActor(User user) {
    ActivityObject actor = new ActivityObject();
    actor.setId(formatId(
        Optional.ofNullable(Optional.ofNullable(user.getIdStr())
            .orElseGet(Optional.of(user.getId().toString())::get)).orElse(null)
    ));
    actor.setObjectType("page");
    actor.setDisplayName(user.getName());
    actor.setAdditionalProperty("handle", user.getScreenName());
    actor.setSummary(user.getDescription());

    if (user.getUrl() != null) {
      actor.setUrl(user.getUrl());
    }

    Map<String, Object> extensions = new HashMap<>();
    extensions.put("location", user.getLocation());
    extensions.put("posts", user.getStatusesCount());
    extensions.put("favorites", user.getFavouritesCount());
    extensions.put("followers", user.getFollowersCount());

    Image profileImage = new Image();
    profileImage.setUrl(user.getProfileImageUrlHttps());
    actor.setImage(profileImage);

    extensions.put("screenName", user.getScreenName());

    actor.setAdditionalProperty("extensions", extensions);
    return actor;
  }

  /**
   * Builds the actor for a delete event.
   * @param delete the delete event
   * @return a valid Actor
   */
  public static ActivityObject buildActor(Delete delete) {
    ActivityObject actor = new ActivityObject();
    actor.setId(formatId(delete.getDelete().getStatus().getUserIdStr()));
    actor.setObjectType("page");
    return actor;
  }

  /**
   * Creates an {@link ActivityObject} for the tweet.
   * @param tweet the object to use as the source
   * @return a valid ActivityObject
   */
  public static ActivityObject buildActivityObject(Tweet tweet) {
    ActivityObject actObj = new ActivityObject();
    String id = Optional.ofNullable(Optional.ofNullable(tweet.getIdStr())
        .orElseGet(Optional.of(tweet.getId().toString())::get)).orElse(null);
    if ( id != null ) {
      actObj.setId(id);
    }
    actObj.setObjectType("post");
    actObj.setContent(tweet.getText());
    return actObj;
  }

  /**
   * Builds the ActivityObject for the delete event.
   * @param delete the delete event
   * @return a valid Activity Object
   */
  public static ActivityObject buildActivityObject(Delete delete) {
    ActivityObject actObj = new ActivityObject();
    actObj.setId(formatId(delete.getDelete().getStatus().getIdStr()));
    actObj.setObjectType("tweet");
    return actObj;
  }

  /**
   * Updates the content, and associated fields, with those from the given tweet
   * @param activity the target of the updates.  Will receive all values from the tweet.
   * @param tweet the object to use as the source
   * @param verb the verb for the given activity's type
   */
  public static void updateActivityContent(Activity activity, Tweet tweet, String verb) {
    activity.setVerb(verb);
    activity.setTitle("");
    if ( tweet != null ) {
      activity.setObject(buildActivityObject(tweet));
      activity.setLinks(getLinks(tweet));
      activity.setContent(tweet.getText());
      addLocationExtension(activity, tweet);
      addTwitterExtensions(activity, tweet);
    }
  }

  /**
   * Gets the links from the Twitter event
   * @param tweet the object to use as the source
   * @return a list of links corresponding to the expanded URL (no t.co)
   */
  public static List<String> getLinks(Tweet tweet) {
    List<String> links = new ArrayList<>();
    if ( tweet.getEntities().getUrls() != null ) {
      for (Url url : tweet.getEntities().getUrls()) {
        links.add(url.getExpandedUrl());
      }
    } else {
      LOGGER.debug(" 0 links");
    }
    return links;
  }

  /**
   * Builds the {@link TargetObject} from the tweet.
   * @param tweet the object to use as the source
   * @return currently returns null for all activities
   */
  public static ActivityObject buildTarget(Tweet tweet) {
    return null;
  }

  /**
   * Adds the location extension and populates with teh twitter data.
   * @param activity the Activity object to update
   * @param tweet the object to use as the source
   */
  public static void addLocationExtension(Activity activity, Tweet tweet) {
    Map<String, Object> extensions = ExtensionUtil.getInstance().ensureExtensions(activity);
    Map<String, Object> location = new HashMap<>();
    location.put("id", formatId(
        Optional.ofNullable(Optional.ofNullable(tweet.getIdStr())
            .orElseGet(Optional.of(tweet.getId().toString())::get)).orElse(null)
    ));
    location.put("coordinates", boundingBoxCenter(tweet.getPlace()));
    extensions.put("location", location);
  }

  /**
   * Gets the common twitter {@link Provider} object
   * @return a provider object representing Twitter
   */
  public static Provider getProvider() {
    Provider provider = new Provider();
    provider.setId("id:providers:twitter");
    provider.setObjectType("application");
    provider.setDisplayName("Twitter");
    return provider;
  }

  /**
   * Adds the given Twitter event to the activity as an extension.
   * @param activity the Activity object to update
   * @param event the Twitter event to add as the extension
   */
  public static void addTwitterExtension(Activity activity, ObjectNode event) {
    Map<String, Object> extensions = ExtensionUtil.getInstance().ensureExtensions(activity);
    extensions.put("twitter", event);
  }

  /**
   * Formats the ID to conform with the Apache Streams activity ID convention.
   * @param idparts the parts of the ID to join
   * @return a valid Activity ID in format "id:twitter:part1:part2:...partN"
   */
  public static String formatId(String... idparts) {
    return String.join(":", Stream.concat(Arrays.stream(new String[]{"id:twitter"}), Arrays.stream(idparts)).collect(Collectors.toList()));
  }

  /**
   * Takes various parameters from the twitter object that are currently not part of the
   * activity schema and stores them in a generic extensions attribute.
   * @param activity Activity
   * @param tweet Tweet
   */
  public static void addTwitterExtensions(Activity activity, Tweet tweet) {
    Map<String, Object> extensions = ExtensionUtil.getInstance().ensureExtensions(activity);

    List<String> hashtags = new ArrayList<>();
    for (Hashtag hashtag : tweet.getEntities().getHashtags()) {
      hashtags.add(hashtag.getText());
    }
    extensions.put("hashtags", hashtags);

    Map<String, Object> likes = new HashMap<>();
    likes.put("perspectival", tweet.getFavorited());
    likes.put("count", tweet.getAdditionalProperties().get("favorite_count"));

    extensions.put("likes", likes);

    Map<String, Object> rebroadcasts = new HashMap<>();
    rebroadcasts.put("perspectival", tweet.getRetweeted());
    rebroadcasts.put("count", tweet.getRetweetCount());

    extensions.put("rebroadcasts", rebroadcasts);

    List<Map<String, Object>> userMentions = new ArrayList<>();
    Entities entities = tweet.getEntities();

    for (UserMentions user : entities.getUserMentions()) {
      //Map the twitter user object into an actor
      Map<String, Object> actor = new HashMap<>();
      actor.put("id", "id:twitter:" + user.getIdStr());
      actor.put("displayName", user.getName());
      actor.put("handle", user.getScreenName());

      userMentions.add(actor);
    }

    extensions.put("user_mentions", userMentions);

    extensions.put("keywords", tweet.getText());
  }

  /**
   * Compute central coordinates from bounding box.
   * @param place the bounding box to use as the source
   */
  public static List<Double> boundingBoxCenter(Place place) {
    if ( place == null ) {
      return new ArrayList<>();
    }
    if ( place.getBoundingBox() == null ) {
      return new ArrayList<>();
    }
    if ( place.getBoundingBox().getCoordinates().size() != 1 ) {
      return new ArrayList<>();
    }
    if ( place.getBoundingBox().getCoordinates().get(0).size() != 4 ) {
      return new ArrayList<>();
    }
    List<Double> lats = new ArrayList<>();
    List<Double> lons = new ArrayList<>();
    for ( List<Double> point : place.getBoundingBox().getCoordinates().get(0)) {
      lats.add(point.get(0));
      lons.add(point.get(1));
    }
    List<Double> result = new ArrayList<>();
    result.add(mean(lats));
    result.add(mean(lons));
    return result;
  }

}
