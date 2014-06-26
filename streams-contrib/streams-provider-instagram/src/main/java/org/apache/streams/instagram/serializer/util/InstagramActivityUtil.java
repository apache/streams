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

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Provider;
import org.jinstagram.entity.users.feed.MediaFeedData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
 * Provides utilities for working with Activity objects within the context of Instagram
 */
public class InstagramActivityUtil {

    /**
     * Updates the given Activity object with the values from the item
     * @param item the object to use as the source
     * @param activity the target of the updates.  Will receive all values from the tweet.
     * @throws ActivitySerializerException
     */
    public static void updateActivity(MediaFeedData item, Activity activity) throws ActivitySerializerException {

    }

    /**
     * Builds the actor
     * @param item the item
     * @return a valid Actor
     */
    public static  Actor buildActor(MediaFeedData item) {
        Actor actor = new Actor();
        return actor;
    }

    /**
     * Builds the ActivityObject
     * @param item the item
     * @return a valid Activity Object
     */
    public static ActivityObject buildActivityObject(MediaFeedData item) {
        ActivityObject actObj = new ActivityObject();
        return actObj;
    }


    /**
     * Updates the content, and associated fields, with those from the given tweet
     * @param activity the target of the updates.  Will receive all values from the tweet.
     * @param item the object to use as the source
     * @param verb the verb for the given activity's type
     */
    public static void updateActivityContent(Activity activity, MediaFeedData item, String verb) {

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
        Map<String, Object> location = new HashMap<String, Object>();

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
     * Adds the given Instagram event to the activity as an extension
     * @param activity the Activity object to update
     * @param event the Instagram event to add as the extension
     */
    public static void addInstagramExtension(Activity activity, ObjectNode event) {
        Map<String, Object> extensions = org.apache.streams.data.util.ActivityUtil.ensureExtensions(activity);
        extensions.put("instagram", event);
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
    }
}
