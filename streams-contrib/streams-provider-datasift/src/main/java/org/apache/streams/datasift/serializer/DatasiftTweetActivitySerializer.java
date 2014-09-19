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


import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.interaction.Author;
import org.apache.streams.datasift.interaction.Interaction;
import org.apache.streams.datasift.twitter.DatasiftTwitterUser;
import org.apache.streams.datasift.twitter.Retweet;
import org.apache.streams.datasift.twitter.Twitter;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Image;
import org.apache.streams.twitter.serializer.util.TwitterActivityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
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
        if(retweet)
            activity.setLinks(getLinks(twitter.getRetweet()));
        else
            activity.setLinks(getLinks(twitter));
        addDatasiftExtension(activity, event);
        if( twitter.getGeo() != null) {
            addLocationExtension(activity, twitter);
        }
        addTwitterExtensions(activity, twitter, event.getInteraction());
        return activity;
    }

    /**
     * Get the links from this tweet as a list
     * @param twitter
     * @return the links from the tweet
     */
    public List<String> getLinks(Twitter twitter) {
        return getLinks(twitter.getLinks());
    }

    /**
     * Get the links from this tweet as a list
     * @param retweet
     * @return the links from the tweet
     */
    public List<String> getLinks(Retweet retweet) {
        return getLinks(retweet.getLinks());
    }

    /**
     * Converts the list of objects to a list of strings
     * @param links
     * @return
     */
    private List<String> getLinks(List<Object> links) {
        if(links == null)
            return Lists.newArrayList();
        List<String> result = Lists.newLinkedList();
        for(Object obj : links) {
            if(obj instanceof String) {
                result.add((String) obj);
            } else {
                LOGGER.warn("Links is not instance of String : {}", obj.getClass().getName());
            }
        }
        return result;
    }

    public Actor buildActor(Datasift event, Twitter twitter) {
        DatasiftTwitterUser user = twitter.getUser();
        Actor actor = super.buildActor(event.getInteraction());
        if(user == null) {
            user = twitter.getRetweet().getUser();
        }

        actor.setDisplayName(user.getName());
        actor.setId(formatId(Optional.fromNullable(
                user.getIdStr())
                .or(Optional.of(user.getId().toString()))
                .orNull()));
        actor.setSummary(user.getDescription());
        try {
            actor.setPublished(user.getCreatedAt());
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
            extensions.put("favorites", user.getFavouritesCount());
        }

        Image profileImage = new Image();
        String profileUrl = null;
        Author author = event.getInteraction().getAuthor();
        if( author != null )
            profileUrl = author.getAvatar();
        if(profileUrl == null && user.getProfileImageUrlHttps() != null) {
            Object url = user.getProfileImageUrlHttps();
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

    public void addLocationExtension(Activity activity, Twitter twitter) {
        Map<String, Object> extensions = ensureExtensions(activity);
        Map<String, Object> location = Maps.newHashMap();
        double[] coordiantes = new double[] { twitter.getGeo().getLongitude(), twitter.getGeo().getLatitude() };
        Map<String, Object> coords = Maps.newHashMap();
        coords.put("coordinates", coordiantes);
        coords.put("type", "geo_point");
        location.put("coordinates", coords);
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
            ArrayList<Map<String,Object>> userMentions = createUserMentions(interaction);

            if(userMentions.size() > 0)
                extensions.put("user_mentions", userMentions);
        }

        extensions.put("keywords", interaction.getContent());
    }

    /**
     * Returns an ArrayList of all UserMentions in this interaction
     * Note: The ID list and the handle lists do not necessarily correspond 1:1 for this provider
     * If those lists are the same size, then they will be merged into individual UserMention
     * objects. However, if they are not the same size, a new UserMention object will be created
     * for each entry in both lists.
     *
     * @param interaction
     * @return
     */
    private ArrayList<Map<String,Object>> createUserMentions(Interaction interaction) {
        ArrayList<String> mentions = (ArrayList<String>) interaction.getAdditionalProperties().get("mentions");
        ArrayList<Long> mentionIds = (ArrayList<Long>) interaction.getAdditionalProperties().get("mention_ids");
        ArrayList<Map<String,Object>> userMentions = new ArrayList<Map<String,Object>>();

        if(mentions != null && !mentions.isEmpty()) {
            for(int x = 0; x < mentions.size(); x ++) {
                Map<String, Object> actor = new HashMap<String, Object>();
                actor.put("displayName", mentions.get(x));
                actor.put("handle", mentions.get(x));

                userMentions.add(actor);
            }
        }
        if(mentionIds != null && !mentionIds.isEmpty()) {
            for(int x = 0; x < mentionIds.size(); x ++) {
                Map<String, Object> actor = new HashMap<String, Object>();
                actor.put("id", "id:twitter:" + mentionIds.get(x));

                userMentions.add(actor);
            }
        }

        return userMentions;
    }

    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:twitter", idparts));
    }

}
