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

package org.apache.streams.pojo.extensions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
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

    private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public static Map<String, Object> getExtensions(Activity activity) {
        Map<String,Object> extensions = ensureExtensions(activity);
        return extensions;
    }

    public static Object getExtension(Activity activity, String key) {
        Map<String,Object> extensions = ensureExtensions(activity);
        return extensions.get(key);
    }

    public static void setExtensions(Activity activity, Map<String, Object> extensions) {
        activity.setAdditionalProperty(EXTENSION_PROPERTY, extensions);
    };

    public static void addExtension(Activity activity, String key, Object extension) {
        Map<String,Object> extensions = ensureExtensions(activity);
        extensions.put(key, extension);
    };

    public static void addExtensions(Activity activity, Map<String, Object> extensions) {
        for( Map.Entry<String, Object> item : extensions.entrySet())
            addExtension(activity, item.getKey(), item.getValue());
    };

    public static void removeExtension(Activity activity, String key) {
        Map<String,Object> extensions = ensureExtensions(activity);
        extensions.remove(key);
    };

    public static void promoteExtensions(Activity activity) {
        Map<String,Object> extensions = ensureExtensions(activity);
        for( String key : extensions.keySet() ) {
            activity.getAdditionalProperties().put(key, extensions.get(key));
            removeExtension(activity, key);
        }
        activity.getAdditionalProperties().remove("extensions");
    };

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

    public static void promoteExtensions(ActivityObject object) {
        Map<String,Object> extensions = ensureExtensions(object);
        for( String key : extensions.keySet() ) {
            object.getAdditionalProperties().put(key, extensions.get(key));
            removeExtension(object, key);
        }
        object.getAdditionalProperties().remove("extensions");
    };

    /**
     * Creates a standard extension property
     * @param activity activity to create the property in
     * @return the Map representing the extensions property
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> ensureExtensions(Activity activity) {
        Map<String,Object> extensions = (Map<String,Object>) activity.getAdditionalProperties().get(EXTENSION_PROPERTY);
        if(extensions == null) {
            extensions = Maps.newHashMap();
            setExtensions(activity, extensions);
        }
        return extensions;
    }

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
