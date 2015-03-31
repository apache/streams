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

package org.apache.streams.twitter.serializer.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Image;
import org.apache.streams.pojo.json.Provider;
import org.apache.streams.twitter.Url;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Entities;
import org.apache.streams.twitter.pojo.Hashtag;
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.pojo.UserMentions;
import org.apache.streams.twitter.serializer.StreamsTwitterMapper;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
 * Provides utilities for working with Activity objects within the context of Twitter
 */
public class TwitterActivityUtil {

    /**
     * Updates the given Activity object with the values from the Tweet
     * @param tweet the object to use as the source
     * @param activity the target of the updates.  Will receive all values from the tweet.
     * @throws ActivitySerializerException
     */
    public static void updateActivity(Tweet tweet, Activity activity) throws ActivitySerializerException {
        ObjectMapper mapper = StreamsTwitterMapper.getInstance();
        activity.setActor(buildActor(tweet));
        activity.setId(formatId(activity.getVerb(),
                Optional.fromNullable(
                        tweet.getIdStr())
                        .or(Optional.of(tweet.getId().toString()))
                        .orNull()));

        if(tweet instanceof Retweet) {
            updateActivityContent(activity,  ((Retweet) tweet).getRetweetedStatus(), "share");
        } else {
            updateActivityContent(activity, tweet, "post");
        }

        if(Strings.isNullOrEmpty(activity.getId()))
            throw new ActivitySerializerException("Unable to determine activity id");
        try {
            activity.setPublished(tweet.getCreatedAt());
        } catch( Exception e ) {
            throw new ActivitySerializerException("Unable to determine publishedDate", e);
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
     * @throws ActivitySerializerException
     */
    public static void updateActivity(User user, Activity activity) throws ActivitySerializerException {
        ObjectMapper mapper = StreamsTwitterMapper.getInstance();

        activity.setActor(buildActor(user));
        activity.setId(null);
        activity.setPublished(new DateTime());
        activity.setProvider(getProvider());
        addTwitterExtension(activity, mapper.convertValue(user, ObjectNode.class));
    }

    /**
     * Updates the activity for a delete event
     * @param delete the delete event
     * @param activity the Activity object to update
     * @throws ActivitySerializerException
     */
    public static void updateActivity(Delete delete, Activity activity) throws ActivitySerializerException {
        activity.setActor(buildActor(delete));
        activity.setVerb("delete");
        activity.setObject(buildActivityObject(delete));
        activity.setId(formatId(activity.getVerb(), delete.getDelete().getStatus().getIdStr()));
        if(Strings.isNullOrEmpty(activity.getId()))
            throw new ActivitySerializerException("Unable to determine activity id");
        activity.setProvider(getProvider());
        addTwitterExtension(activity, StreamsTwitterMapper.getInstance().convertValue(delete, ObjectNode.class));
    }

    /**
     * Builds the actor for a delete event
     * @param delete the delete event
     * @return a valid Actor
     */
    public static Actor buildActor(Delete delete) {
        Actor actor = new Actor();
        actor.setId(formatId(delete.getDelete().getStatus().getUserIdStr()));
        return actor;
    }

    /**
     * Builds the ActivityObject for the delete event
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
        if( tweet != null ) {
            activity.setObject(buildActivityObject(tweet));
            activity.setLinks(getLinks(tweet));
            activity.setContent(tweet.getText());
            addLocationExtension(activity, tweet);
            addTwitterExtensions(activity, tweet);
        }
    }

    /**
     * Creates an {@link org.apache.streams.pojo.json.ActivityObject} for the tweet
     * @param tweet the object to use as the source
     * @return a valid ActivityObject
     */
    public static ActivityObject buildActivityObject(Tweet tweet) {
        ActivityObject actObj = new ActivityObject();
        String id =  Optional.fromNullable(
                tweet.getIdStr())
                .or(Optional.of(tweet.getId().toString()))
                .orNull();
        if( id != null )
            actObj.setId(id);
        actObj.setObjectType("tweet");
        actObj.setContent(tweet.getText());
        return actObj;
    }

    /**
     * Builds the activity {@link org.apache.streams.pojo.json.Actor} object from the tweet
     * @param tweet the object to use as the source
     * @return a valid Actor populated from the Tweet
     */
    public static Actor buildActor(Tweet tweet) {
        Actor actor = new Actor();
        User user = tweet.getUser();

        return buildActor(user);
    }

    /**
     * Builds the activity {@link org.apache.streams.pojo.json.Actor} object from the User
     * @param user the object to use as the source
     * @return a valid Actor populated from the Tweet
     */
    public static Actor buildActor(User user) {
        Actor actor = new Actor();
        actor.setId(formatId(
                Optional.fromNullable(
                        user.getIdStr())
                        .or(Optional.of(user.getId().toString()))
                        .orNull()
        ));

        actor.setDisplayName(user.getName());
        actor.setAdditionalProperty("handle", user.getScreenName());
        actor.setSummary(user.getDescription());

        if (user.getUrl()!=null){
            actor.setUrl(user.getUrl());
        }

        Map<String, Object> extensions = new HashMap<String, Object>();
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
     * Gets the links from the Twitter event
     * @param tweet the object to use as the source
     * @return a list of links corresponding to the expanded URL (no t.co)
     */
    public static List<String> getLinks(Tweet tweet) {
        List<String> links = Lists.newArrayList();
        if( tweet.getEntities().getUrls() != null ) {
            for (Url url : tweet.getEntities().getUrls()) {
                links.add(url.getUrl());
            }
        }
        else
            System.out.println("  0 links");
        return links;
    }

    /**
     * Builds the {@link org.apache.streams.twitter.pojo.TargetObject} from the tweet
     * @param tweet the object to use as the source
     * @return currently returns null for all activities
     */
    public static ActivityObject buildTarget(Tweet tweet) {
        return null;
    }

    /**
     * Adds the location extension and populates with teh twitter data
     * @param activity the Activity object to update
     * @param tweet the object to use as the source
     */
    public static void addLocationExtension(Activity activity, Tweet tweet) {
        Map<String, Object> extensions = ExtensionUtil.ensureExtensions(activity);
        Map<String, Object> location = new HashMap<String, Object>();
        location.put("id", formatId(
                Optional.fromNullable(
                        tweet.getIdStr())
                        .or(Optional.of(tweet.getId().toString()))
                        .orNull()
        ));
        location.put("coordinates", tweet.getCoordinates());
        extensions.put("location", location);
    }

    /**
     * Gets the common twitter {@link org.apache.streams.pojo.json.Provider} object
     * @return a provider object representing Twitter
     */
    public static Provider getProvider() {
        Provider provider = new Provider();
        provider.setId("id:providers:twitter");
        provider.setDisplayName("Twitter");
        return provider;
    }
    /**
     * Adds the given Twitter event to the activity as an extension
     * @param activity the Activity object to update
     * @param event the Twitter event to add as the extension
     */
    public static void addTwitterExtension(Activity activity, ObjectNode event) {
        Map<String, Object> extensions = ExtensionUtil.ensureExtensions(activity);
        extensions.put("twitter", event);
    }
    /**
     * Formats the ID to conform with the Apache Streams activity ID convention
     * @param idparts the parts of the ID to join
     * @return a valid Activity ID in format "id:twitter:part1:part2:...partN"
     */
    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:twitter", idparts));
    }

    /**
     * Takes various parameters from the twitter object that are currently not part of teh
     * activity schema and stores them in a generic extensions attribute
     * @param activity
     * @param tweet
     */
    public static void addTwitterExtensions(Activity activity, Tweet tweet) {
        Map<String, Object> extensions = ExtensionUtil.ensureExtensions(activity);

        List<String> hashtags = new ArrayList<String>();
        for(Hashtag hashtag : tweet.getEntities().getHashtags()) {
            hashtags.add(hashtag.getText());
        }
        extensions.put("hashtags", hashtags);

        Map<String, Object> likes = new HashMap<String, Object>();
        likes.put("perspectival", tweet.getFavorited());
        likes.put("count", tweet.getAdditionalProperties().get("favorite_count"));

        extensions.put("likes", likes);

        Map<String, Object> rebroadcasts = new HashMap<String, Object>();
        rebroadcasts.put("perspectival", tweet.getRetweeted());
        rebroadcasts.put("count", tweet.getRetweetCount());

        extensions.put("rebroadcasts", rebroadcasts);

        List<Map<String, Object>> userMentions = new ArrayList<Map<String, Object>>();
        Entities entities = tweet.getEntities();

        for(UserMentions user : entities.getUserMentions()) {
            //Map the twitter user object into an actor
            Map<String, Object> actor = new HashMap<String, Object>();
            actor.put("id", "id:twitter:" + user.getIdStr());
            actor.put("displayName", user.getName());
            actor.put("handle", user.getScreenName());

            userMentions.add(actor);
        }

        extensions.put("user_mentions", userMentions);

        extensions.put("keywords", tweet.getText());
    }
}
