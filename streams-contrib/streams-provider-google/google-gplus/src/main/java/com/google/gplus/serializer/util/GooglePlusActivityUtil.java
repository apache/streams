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
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Image;
import org.apache.streams.pojo.json.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GooglePlusActivityUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(GooglePlusActivityUtil.class);

    /**
     * Given a Person object and an activity, fill out the appropriate actor details
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
     * Extract the relevant details from the passed in Person object and build
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