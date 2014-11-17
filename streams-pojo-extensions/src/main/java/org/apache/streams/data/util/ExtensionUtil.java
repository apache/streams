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

package org.apache.streams.data.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.ActivityObject;

import java.util.Map;

/**
 *  Class makes it easier to manage extensions added to activities, actors, objects, etc...
 */
public class ExtensionUtil {

    /**
     * Property on the activity object to use for extensions
     */
    public static final String EXTENSION_PROPERTY = "extensions";
    /**
     * The number of +1, Like, favorites, etc that the post has received
     */
    public static final String LIKES_EXTENSION = "likes";
    /**
     * The number of retweets, shares, etc that the post has received
     */
    public static final String REBROADCAST_EXTENSION = "rebroadcasts";
    /**
     * The language of the post
     */
    public static final String LANGUAGE_EXTENSION = "language";
    /**
     * Location that the post was made or the actor's residence
     */
    public static final String LOCATION_EXTENSION = "location";
    /**
     * Country that the post was made
     */
    public static final String LOCATION_EXTENSION_COUNTRY = "country";
    /**
     * Specific JSON-geo coordinates (long,lat)
     */
    public static final String LOCATION_EXTENSION_COORDINATES = "coordinates";

    private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public static Map<String, Object> getExtensions(ActivityObject object) {
        ActivityObject activityObject = mapper.convertValue(object, ActivityObject.class);
        Map<String,Object> extensions = ensureExtensions(object);
        return extensions;
    }

    public static Object getExtension(ActivityObject object, String key) {
        Map<String,Object> extensions = ensureExtensions(object);
        return extensions.get(key);
    }

    public static void setExtensions(ActivityObject object, Map<String, Object> extensions) {
        object.setAdditionalProperty(EXTENSION_PROPERTY, extensions);
    };

    public static void addExtension(ActivityObject object, String key, Object extension) {
        Map<String,Object> extensions = ensureExtensions(object);
        extensions.put(key, extension);
    };

    public static void addExtensions(ActivityObject object, Map<String, Object> extensions) {
        for( Map.Entry<String, Object> item : extensions.entrySet())
            addExtension(object, item.getKey(), item.getValue());
    };

    public static void removeExtension(ActivityObject object, String key) {
        Map<String,Object> extensions = ensureExtensions(object);
        extensions.remove(key);
    };

    /**
     * Creates a standard extension property
     * @param object objectnode to create the property in
     * @return the Map representing the extensions property
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> ensureExtensions(ActivityObject object) {
        Map<String,Object> extensions = (Map<String,Object>) object.getAdditionalProperties().get(EXTENSION_PROPERTY);
        if(extensions == null) {
            extensions = Maps.newHashMap();
            setExtensions(object, extensions);
        }
        return extensions;
    }
}
