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
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;

import java.util.Map;

/**
 *  Class makes it easier to manage extensions added to activities, actors, objects, etc...
 */
public class ExtensionUtil {

    public static final String DEFAULT_EXTENSION_PROPERTY = "extensions";

    private static final ExtensionUtil INSTANCE = new ExtensionUtil(DEFAULT_EXTENSION_PROPERTY);

    private String extensionProperty;

    public static ExtensionUtil getInstance(){
        return INSTANCE;
    }

    public static ExtensionUtil getInstance(String property){
        return new ExtensionUtil(property);
    }

    private ExtensionUtil(String extensionProperty) {
        if( !Strings.isNullOrEmpty(extensionProperty) )
            this.extensionProperty = extensionProperty;
        else
            this.extensionProperty = DEFAULT_EXTENSION_PROPERTY;
    }

    /**
     * Property on the activity object to use for extensions
     */

    private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public Map<String, Object> getExtensions(Activity activity) {
        Map<String,Object> extensions = ensureExtensions(activity);
        return extensions;
    }

    public Object getExtension(Activity activity, String key) {
        Map<String,Object> extensions = ensureExtensions(activity);
        return extensions.get(key);
    }

    public void setExtensions(Activity activity, Map<String, Object> extensions) {
        activity.setAdditionalProperty(extensionProperty, extensions);
    };

    public void addExtension(Activity activity, String key, Object extension) {
        Map<String,Object> extensions = ensureExtensions(activity);
        extensions.put(key, extension);
    };

    public void addExtensions(Activity activity, Map<String, Object> extensions) {
        for( Map.Entry<String, Object> item : extensions.entrySet())
            addExtension(activity, item.getKey(), item.getValue());
    };

    public void removeExtension(Activity activity, String key) {
        Map<String,Object> extensions = ensureExtensions(activity);
        extensions.remove(key);
    };

    public void promoteExtensions(Activity activity) {
        Map<String,Object> extensions = ensureExtensions(activity);
        for( String key : extensions.keySet() ) {
            activity.getAdditionalProperties().put(key, extensions.get(key));
            removeExtension(activity, key);
        }
        activity.getAdditionalProperties().remove("extensions");
    };

    public Map<String, Object> getExtensions(ActivityObject object) {
        ActivityObject activityObject = mapper.convertValue(object, ActivityObject.class);
        Map<String,Object> extensions = ensureExtensions(object);
        return extensions;
    }

    public Object getExtension(ActivityObject object, String key) {
        Map<String,Object> extensions = ensureExtensions(object);
        return extensions.get(key);
    }

    public void setExtensions(ActivityObject object, Map<String, Object> extensions) {
        object.setAdditionalProperty(extensionProperty, extensions);
    };

    public void addExtension(ActivityObject object, String key, Object extension) {
        Map<String,Object> extensions = ensureExtensions(object);
        extensions.put(key, extension);
    };

    public void addExtensions(ActivityObject object, Map<String, Object> extensions) {
        for( Map.Entry<String, Object> item : extensions.entrySet())
            addExtension(object, item.getKey(), item.getValue());
    };

    public void removeExtension(ActivityObject object, String key) {
        Map<String,Object> extensions = ensureExtensions(object);
        extensions.remove(key);
    };

    public void promoteExtensions(ActivityObject object) {
        Map<String,Object> extensions = ensureExtensions(object);
        for( String key : extensions.keySet() ) {
            object.getAdditionalProperties().put(key, extensions.get(key));
            removeExtension(object, key);
        }
        object.getAdditionalProperties().remove(extensionProperty);
    };

    /**
     * Creates a standard extension property
     * @param activity activity to create the property in
     * @return the Map representing the extensions property
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> ensureExtensions(Activity activity) {
        Map<String,Object> extensions = (Map<String,Object>) activity.getAdditionalProperties().get(extensionProperty);
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
    public Map<String, Object> ensureExtensions(ActivityObject object) {
        Map<String,Object> extensions = (Map<String,Object>) object.getAdditionalProperties().get(extensionProperty);
        if(extensions == null) {
            extensions = Maps.newHashMap();
            setExtensions(object, extensions);
        }
        return extensions;
    }

}
