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

package com.google.gplus.serializer.util;

import com.google.api.services.plus.model.Person;
import org.apache.streams.pojo.json.*;
import org.apache.streams.pojo.json.Activity;
import org.joda.time.DateTime;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

public class GooglePlusActivityUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(GooglePlusActivityUtil.class);

    /**
     * Given a {@link com.google.api.services.plus.model.Person} object and an
     * {@link org.apache.streams.pojo.json.Activity} object, fill out the appropriate details
     *
     * @param item
     * @param activity
     * @throws ActivitySerializerException
     */
    public static void updateActivity(Person item, Activity activity) throws ActivitySerializerException {
        activity.setActor(buildActor(item));
        activity.setVerb("update");

        activity.setId(formatId(activity.getVerb(),
                Optional.fromNullable(
                        item.getId())
                        .orNull()));

        activity.setProvider(getProvider());
    }

    /**
     * Given a Google Plus {@link com.google.api.services.plus.model.Activity},
     * convert that into an Activity streams formatted {@link org.apache.streams.pojo.json.Activity}
     *
     * @param gPlusActivity
     * @param activity
     */
    public static void updateActivity(com.google.api.services.plus.model.Activity gPlusActivity, Activity activity) {
        activity.setActor(buildActor(gPlusActivity.getActor()));
        activity.setVerb("post");
        activity.setTitle(gPlusActivity.getTitle());
        activity.setUrl(gPlusActivity.getUrl());
        activity.setProvider(getProvider());

        if(gPlusActivity.getObject() != null) {
            activity.setContent(gPlusActivity.getObject().getContent());
        }

        activity.setId(formatId(activity.getVerb(),
                Optional.fromNullable(
                        gPlusActivity.getId())
                        .orNull()));

        DateTime published = new DateTime(String.valueOf(gPlusActivity.getPublished()));
        activity.setPublished(published);

        setObject(activity, gPlusActivity.getObject());
        addGPlusExtensions(activity, gPlusActivity);
    }

    /**
     * Add in necessary extensions from the passed in {@link com.google.api.services.plus.model.Activity} to the
     * {@link org.apache.streams.pojo.json.Activity} object
     *
     * @param activity
     * @param gPlusActivity
     */
    private static void addGPlusExtensions(Activity activity, com.google.api.services.plus.model.Activity gPlusActivity) {
        Map<String, Object> extensions = ensureExtensions(activity);

        com.google.api.services.plus.model.Activity.PlusObject object = gPlusActivity.getObject();
        extensions.put("googlePlus", gPlusActivity);

        if(object != null) {
            com.google.api.services.plus.model.Activity.PlusObject.Plusoners plusoners = object.getPlusoners();
            if(plusoners != null) {
                Map<String, Object> likes = new HashMap<>();
                likes.put("count", plusoners.getTotalItems());
                extensions.put("likes", likes);
            }

            com.google.api.services.plus.model.Activity.PlusObject.Resharers resharers = object.getResharers();
            if(resharers != null) {
                Map<String, Object> rebroadcasts = new HashMap<>();
                rebroadcasts.put("count", resharers.getTotalItems());
                extensions.put("rebroadcasts", rebroadcasts);
            }

            extensions.put("keywords", object.getContent());
        }
    }

    /**
     * Set the {@link org.apache.streams.pojo.json.ActivityObject} field given the passed in
     * {@link com.google.api.services.plus.model.Activity.PlusObject}
     *
     * @param activity
     * @param object
     */
    private static void setObject(Activity activity, com.google.api.services.plus.model.Activity.PlusObject object) {
        if(object != null) {
            ActivityObject activityObject = new ActivityObject();

            activityObject.setContent(object.getContent());
            activityObject.setObjectType(object.getObjectType());

            java.util.List<ActivityObject> attachmentsList = Lists.newArrayList();
            for (com.google.api.services.plus.model.Activity.PlusObject.Attachments attachments : object.getAttachments()) {
                ActivityObject attach = new ActivityObject();

                attach.setContent(attachments.getContent());
                attach.setDisplayName(attachments.getDisplayName());
                attach.setObjectType(attachments.getObjectType());
                attach.setUrl(attachments.getUrl());

                Image image = new Image();
                com.google.api.services.plus.model.Activity.PlusObject.Attachments.Image image1 = attachments.getImage();

                if (image1 != null) {
                    image.setUrl(image1.getUrl());
                    attach.setImage(image);
                }

                attachmentsList.add(attach);
            }

            activityObject.setAttachments(attachmentsList);

            activity.setObject(activityObject);
        }
    }

    /**
     * Given a {@link com.google.api.services.plus.model.Activity.Actor} object, return a fully fleshed
     * out {@link org.apache.streams.pojo.json.Actor} object
     *
     * @param gPlusActor
     * @return
     */
    private static Actor buildActor(com.google.api.services.plus.model.Activity.Actor gPlusActor) {
        Actor actor = new Actor();

        actor.setDisplayName(gPlusActor.getDisplayName());
        actor.setId(formatId(String.valueOf(gPlusActor.getId())));
        actor.setUrl(gPlusActor.getUrl());

        Image image = new Image();
        com.google.api.services.plus.model.Activity.Actor.Image googlePlusImage = gPlusActor.getImage();

        if(googlePlusImage != null) {
            image.setUrl(googlePlusImage.getUrl());
        }
        actor.setImage(image);

        return actor;
    }
    /**
     * Extract the relevant details from the passed in {@link com.google.api.services.plus.model.Person} object and build
     * an actor with them
     *
     * @param person
     * @return Actor constructed with relevant Person details
     */
    private static Actor buildActor(Person person) {
        Actor actor = new Actor();

        actor.setUrl(person.getUrl());
        actor.setDisplayName(person.getDisplayName());
        actor.setId(formatId(String.valueOf(person.getId())));

        if(person.getAboutMe() != null) {
            actor.setSummary(person.getAboutMe());
        } else if(person.getTagline() != null) {
            actor.setSummary(person.getTagline());
        }

        Image image = new Image();
        Person.Image googlePlusImage = person.getImage();

        if(googlePlusImage != null) {
            image.setUrl(googlePlusImage.getUrl());
        }
        actor.setImage(image);

        Map<String, Object> extensions = new HashMap<String, Object>();

        extensions.put("followers", person.getCircledByCount());
        extensions.put("googleplus", person);
        actor.setAdditionalProperty("extensions", extensions);

        return actor;
    }

    /**
     * Gets the common googleplus {@link org.apache.streams.pojo.json.Provider} object
     * @return a provider object representing GooglePlus
     */
    public static Provider getProvider() {
        Provider provider = new Provider();
        provider.setId("id:providers:googleplus");
        provider.setDisplayName("GooglePlus");
        return provider;
    }

    /**
     * Formats the ID to conform with the Apache Streams activity ID convention
     * @param idparts the parts of the ID to join
     * @return a valid Activity ID in format "id:googleplus:part1:part2:...partN"
     */
    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:googleplus", idparts));
    }
}