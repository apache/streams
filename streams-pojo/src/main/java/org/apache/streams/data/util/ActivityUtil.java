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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for managing activities
 */
public class ActivityUtil {

    private ActivityUtil() {}

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

    /**
     * Creates a standard extension property
     * @param activity activity to create the property in
     * @return the Map representing the extensions property
     */
    @SuppressWarnings("unchecked")
    @Deprecated
    public static Map<String, Object> ensureExtensions(Activity activity) {
        Map<String, Object> extensions = (Map)activity.getAdditionalProperties().get(EXTENSION_PROPERTY);
        if(extensions == null) {
            extensions = new HashMap<String, Object>();
            activity.setAdditionalProperty(EXTENSION_PROPERTY, extensions);
        }
        return extensions;
    }

    /**
     * Gets a formatted ID
     * @param providerName name of the provider
     * @param personId ID of the person within the system
     * @return id:<providerName>:people:<personId>
     */
    public static String getPersonId(String providerName, String personId) {
        return String.format("id:%s:people:%s", providerName, personId);
    }

    /**
     * Gets a formatted provider ID
     * @param providerName name of the provider
     * @return id:providers:<providerName>
     */
    public static String getProviderId(String providerName) {
        return String.format("id:providers:%s", providerName);
    }

    /**
     * Gets a formatted object ID
     * @param provider name of the provider
     * @param objectType type of the object
     * @param objectId the ID of the object
     * @return id:<provider>:<objectType>s:<objectId>
     */
    public static String getObjectId(String provider, String objectType, String objectId) {
        return String.format("id:%s:%ss:%s", provider, objectType, objectId);
    }

    /**
     * Gets a formatted activity ID
     * @param providerName name of the provider
     * @param activityId ID of the provider
     * @return id:<providerName>:activities:<activityId>
     */
    public static String getActivityId(String providerName, String activityId) {
        return String.format("id:%s:activities:%s", providerName, activityId);
    }

}
