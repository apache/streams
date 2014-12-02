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
package org.apache.streams.facebook.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.facebook.*;
import org.apache.streams.facebook.Place;
import org.apache.streams.facebook.Post;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.*;

public class FacebookActivityUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(FacebookActivityUtil.class);

    /**
     * Updates the given Activity object with the values from the Page
     * @param page the object to use as the source
     * @param activity the target of the updates.  Will receive all values from the Page.
     * @throws org.apache.streams.exceptions.ActivitySerializerException
     */
    public static void updateActivity(Page page, Activity activity) throws ActivitySerializerException {
        activity.setActor(buildActor(page));
        activity.setId(null);
        activity.setProvider(getProvider());
    }

    /**
     * Updates the given Activity object with the values from the Post
     * @param post
     * @param activity
     * @throws ActivitySerializerException
     */
    public static void updateActivity(Post post, Activity activity) throws ActivitySerializerException {
        activity.setActor(buildActor(post));
        activity.setId(formatId(post.getId()));
        activity.setProvider(getProvider());
        activity.setUpdated(post.getUpdatedTime());
        activity.setPublished(post.getCreatedTime());

        if(post.getLink() != null && post.getLink().length() > 0) {
            List<String> links = Lists.newArrayList();
            links.add(post.getLink());
            activity.setLinks(links);
        }

        activity.setContent(post.getMessage());

        activity.setVerb("post");
        activity.setObject(buildObject(post));
        buildExtensions(activity, post);
    }

    /**
     * Builds out the {@link org.apache.streams.pojo.json.ActivityObject} from the given {@link org.apache.streams.pojo.json.Post}
     * @param post
     * @return {@link org.apache.streams.pojo.json.ActivityObject}
     */
    public static ActivityObject buildObject(Post post) {
        ActivityObject activityObject = new ActivityObject();

        try {
            activityObject.setContent(post.getMessage());
            activityObject.setPublished(post.getCreatedTime());
            activityObject.setUpdated(post.getUpdatedTime());
            activityObject.setDisplayName(post.getFrom().getName());
            activityObject.setId(formatId(post.getId()));
            activityObject.setObjectType(post.getType());
            activityObject.setUrl(post.getLink());

            if(activityObject.getObjectType().equals("photo")) {
                Image image = new Image();
                image.setUrl(activityObject.getUrl());
                activityObject.setImage(image);
            }
        } catch (Exception e) {
            LOGGER.error("Exception while trying to build Activity object for post: {}, exception: {}", post, e);
        }

        return activityObject;
    }

    /**
     * Gets the common facebook {@link org.apache.streams.pojo.json.Provider} object
     * @return a provider object representing Facebook
     */
    public static Provider getProvider() {
        Provider provider = new Provider();
        provider.setId("id:providers:facebook");
        provider.setDisplayName("Facebook");

        return provider;
    }

    /**
     * Builds the activity {@link org.apache.streams.pojo.json.Actor} object from the Page
     * @param page the object to use as the source
     * @return a valid Actor populated from the Page
     */
    public static Actor buildActor(Page page) {
        Actor actor = new Actor();
        actor.setId(formatId(
                Optional.fromNullable(
                        page.getId())
                        .or(Optional.of(page.getId().toString()))
                        .orNull()
        ));

        actor.setDisplayName(page.getName());
        actor.setAdditionalProperty("handle", page.getUsername());
        actor.setSummary(page.getAbout());

        if (page.getLink()!=null){
            actor.setUrl(page.getLink());
        }

        Image profileImage = new Image();
        Cover cover = page.getCover();

        if(cover != null)
            profileImage.setUrl(cover.getSource());
        actor.setImage(profileImage);

        buildExtensions(actor, page);

        return actor;
    }

    /**
     * Builds an {@link org.apache.streams.pojo.json.Actor} object from the {@link org.apache.streams.pojo.json.Post}
     * @param post
     * @return {@link org.apache.streams.pojo.json.Actor}
     */
    public static Actor buildActor(Post post) {
        Actor actor = new Actor();

        try {
            actor.setId(formatId(
                    Optional.fromNullable(
                            post.getFrom().getId())
                            .or(Optional.of(post.getFrom().getId().toString()))
                            .orNull()
            ));

            actor.setDisplayName(post.getFrom().getName());
            actor.setAdditionalProperty("handle", post.getFrom().getName());
        } catch (Exception e) {
            LOGGER.error("Exception trying to build actor for Post: {}, {}", post, e);
        }

        return actor;
    }

    /**
     * Builds the actor extensions given the page object
     * @param actor
     * @param page
     */
    public static void buildExtensions(Actor actor, Page page) {
        Map<String, Object> extensions = new HashMap<String, Object>();
        Location location = page.getLocation();

        if(location != null)
            extensions.put("location", page.getLocation().toString());

        extensions.put("favorites", page.getLikes());
        extensions.put("followers", page.getTalkingAboutCount());

        extensions.put("screenName", page.getUsername());

        actor.setAdditionalProperty("extensions", extensions);
    }

    /**
     * Fills out the extensions attribute of the passed in {@link org.apache.streams.pojo.json.Activity}
     * @param activity
     * @param post
     */
    public static void buildExtensions(Activity activity, Post post) {
        ObjectMapper mapper = StreamsJacksonMapper.getInstance();
        Map<String, Object> extensions = ExtensionUtil.ensureExtensions(activity);

        if(post.getLikes() != null && post.getLikes().size() > 0) {
            Map<String, Object> likes = Maps.newHashMap();
            org.apache.streams.facebook.Like like = post.getLikes().get(0);

            if(like.getAdditionalProperties().containsKey("data")) {
                extensions.put("likes", likes);
            }
        }

        if(post.getShares() != null) {
            Map<String, Object> shares = Maps.newHashMap();
            shares.put("count", ((Map<String, Object>)post.getShares()).get("count"));
            extensions.put("rebroadcasts", shares);
        }

        if(post.getTo() != null) {
            To to = post.getTo();
            List<Datum> data = to.getData();
            extensions.put("user_mentions", Lists.newArrayList());

            for(Datum d : data) {
                Map<String, String> mention = Maps.newHashMap();

                mention.put("id", d.getId());
                mention.put("displayName", d.getName());
                mention.put("handle", d.getName());

                ((List<Map<String,String>>)extensions.get("user_mentions")).add(mention);
            }
        }

        if(post.getPlace() != null) {
            Place place = post.getPlace();
            if(place.getAdditionalProperties().containsKey("location")) {
                extensions.put(LOCATION_EXTENSION, place.getAdditionalProperties().get("location"));
            }
        }

        extensions.put("facebook", mapper.convertValue(post, ObjectNode.class));
    }

    /**
     * Formats the ID to conform with the Apache Streams activity ID convention
     * @param idparts the parts of the ID to join
     * @return a valid Activity ID in format "id:facebook:part1:part2:...partN"
     */
    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:facebook", idparts));
    }
}