/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/
package org.apache.streams.datasift.serializer;


import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.streams.data.util.RFC3339Utils;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.interaction.Interaction;
import org.apache.streams.datasift.interaction.User;
import org.apache.streams.datasift.twitter.Retweet;
import org.apache.streams.datasift.twitter.Twitter;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Image;
import org.apache.streams.twitter.serializer.util.TwitterActivityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
 *
 */
public class DatasiftTweetActivitySerializer extends DatasiftDefaultActivitySerializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatasiftTweetActivitySerializer.class);

    @Override
    public Activity convert(Datasift event) {
        Activity activity = new Activity();
        Twitter twitter = event.getTwitter();
        boolean retweet = twitter.getRetweet() != null;

        activity.setActor(buildActor(event, twitter)); //TODO
        if(retweet) {
            activity.setVerb("share");
        } else {
            activity.setVerb("post");
        }
        activity.setObject(buildActivityObject(event.getInteraction()));
        activity.setId(formatId(activity.getVerb(), event.getInteraction().getId()));
        activity.setTarget(buildTarget(event.getInteraction()));
        activity.setPublished(event.getInteraction().getCreatedAt());
        activity.setGenerator(buildGenerator(event.getInteraction()));
        activity.setIcon(getIcon(event.getInteraction()));
        activity.setProvider(TwitterActivityUtil.getProvider());
        activity.setTitle(event.getInteraction().getTitle());
        activity.setContent(event.getInteraction().getContent());
        activity.setUrl(event.getInteraction().getLink());
        activity.setLinks(getLinks(event));
        addDatasiftExtension(activity, event);
        if( twitter.getGeo() != null) {
            addLocationExtension(activity, twitter);
        }
        addTwitterExtensions(activity, twitter, event.getInteraction());
        return activity;
    }

    public Actor buildActor(Datasift event, Twitter twitter) {
        User user = twitter.getUser();
        Actor actor = super.buildActor(event.getInteraction());
        if(user == null) {
            return retweetBuildActor(actor, twitter.getRetweet().getUser());
        }

        actor.setDisplayName(user.getName());
        actor.setId(formatId(Optional.fromNullable(
                user.getIdStr())
                .or(Optional.of(user.getId().toString()))
                .orNull()));
        actor.setSummary(user.getDescription());
        try {
            actor.setPublished(RFC3339Utils.parseToUTC(user.getCreatedAt()));
        } catch (Exception e) {
            LOGGER.warn("Exception trying to parse date : {}", e);
        }

        if(user.getUrl() != null) {
            actor.setUrl(user.getUrl());
        }

        Map<String, Object> extensions = new HashMap<String,Object>();
        extensions.put("location", user.getLocation());
        extensions.put("posts", user.getStatusesCount());
        extensions.put("followers", user.getFollowersCount());
        extensions.put("screenName", user.getScreenName());
        if(user.getAdditionalProperties() != null) {
            extensions.put("favorites", user.getAdditionalProperties().get("favourites_count"));
        }

        Image profileImage = new Image();
        String profileUrl = null;
        profileUrl = event.getInteraction().getAuthor().getAvatar();
        if(profileUrl == null && user.getAdditionalProperties() != null) {
            Object url = user.getAdditionalProperties().get("profile_image_url_https");
            if(url instanceof String)
                profileUrl = (String) url;
        }
        if(profileUrl == null) {
            profileUrl = user.getProfileImageUrl();
        }
        profileImage.setUrl(profileUrl);
        actor.setImage(profileImage);

        actor.setAdditionalProperty("extensions", extensions);
        return actor;
    }

    //Need to make retweet user and tweet user the same object.
    public Actor retweetBuildActor(Actor actor, org.apache.streams.datasift.twitter.User user) {

        actor.setDisplayName(user.getName());
        actor.setId(formatId(Optional.fromNullable(
                user.getIdStr())
                .or(Optional.of(user.getId().toString()))
                .orNull()));
        actor.setSummary(user.getDescription());
        try {
            actor.setPublished(RFC3339Utils.parseToUTC(user.getCreatedAt()));
        } catch (Exception e) {
            LOGGER.warn("Exception trying to parse date : {}", e);
        }

        if(user.getUrl() != null) {
            actor.setUrl(user.getUrl());
        }

        Map<String, Object> extensions = new HashMap<String,Object>();
        extensions.put("location", user.getLocation());
        extensions.put("posts", user.getStatusesCount());
        extensions.put("followers", user.getFollowersCount());
        extensions.put("screenName", user.getScreenName());
        if(user.getAdditionalProperties() != null) {
            extensions.put("favorites", user.getAdditionalProperties().get("favourites_count"));
        }

        Image profileImage = new Image();
        String profileUrl = null;
        if(actor.getImage() == null && user.getAdditionalProperties() != null) {
            Object url = user.getAdditionalProperties().get("profile_image_url_https");
            if(url instanceof String)
                profileUrl = (String) url;
        }
        if(actor.getImage() == null && profileUrl == null) {
            profileUrl = user.getProfileImageUrl();
        }
        profileImage.setUrl(profileUrl);
        actor.setImage(profileImage);

        actor.setAdditionalProperty("extensions", extensions);
        return actor;
    }

    public void addLocationExtension(Activity activity, Twitter twitter) {
        Map<String, Object> extensions = ensureExtensions(activity);
        Map<String, Object> location = Maps.newHashMap();
        double[] coordiantes = new double[] { twitter.getGeo().getLongitude(), twitter.getGeo().getLatitude() };
        location.put("coordinates", coordiantes);
        extensions.put("location", location);
    }

    public void addTwitterExtensions(Activity activity, Twitter twitter, Interaction interaction) {
        Retweet retweet = twitter.getRetweet();
        Map<String, Object> extensions = ensureExtensions(activity);
        List<String> hashTags = Lists.newLinkedList();
        List<Object> hts = Lists.newLinkedList();
        if(twitter.getHashtags() != null) {
            hts = twitter.getHashtags();
        } else if (retweet != null) {
            hts = retweet.getHashtags();
        }
        if(hts != null) {
            for(Object ht : twitter.getHashtags()) {
                if(ht instanceof String) {
                    hashTags.add((String) ht);
                } else {
                    LOGGER.warn("Hashtag was not instance of String : {}", ht.getClass().getName());
                }
            }
        }
        extensions.put("hashtags", hashTags);


        if(retweet != null) {
            Map<String, Object> rebroadcasts = Maps.newHashMap();
            rebroadcasts.put("perspectival", true);
            rebroadcasts.put("count", retweet.getCount());
            extensions.put("rebroadcasts", rebroadcasts);
        }

        if(interaction.getAdditionalProperties() != null) {
            Object mentionsObject = interaction.getAdditionalProperties().get("mentions");
            if(mentionsObject != null ) {
                if(mentionsObject instanceof List) {
                    List mentions = (List) mentionsObject;
                    List<Map<String, Object>> userMentions = Lists.newLinkedList();
                    for(Object mention : mentions) {
                        Map<String, Object> actor = Maps.newHashMap();
                        actor.put("displayName", mention);
                        userMentions.add(actor);
                    }
                    extensions.put("user_mentions", userMentions);
                }
            }
        }
        extensions.put("keywords", interaction.getContent());
    }
}
