package org.apache.streams.data.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Maps;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.ActivityObject;

import java.util.Map;

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

    public static Map<String, Object> getExtensions(ObjectNode object) {
        ActivityObject activityObject = mapper.convertValue(object, ActivityObject.class);
        Map<String,Object> extensions = (Map<String,Object>) activityObject.getAdditionalProperties().get(EXTENSION_PROPERTY);
        return extensions;
    }

    public static Object getExtension(ObjectNode object, String key) {
        ActivityObject activityObject = mapper.convertValue(object, ActivityObject.class);
        Map<String,Object> extensions = (Map<String,Object>) activityObject.getAdditionalProperties().get(EXTENSION_PROPERTY);
        return extensions.get(key);
    }

    public static void setExtensions(ObjectNode object, Map<String, Object> extensions) {
        ActivityObject activityObject = mapper.convertValue(object, ActivityObject.class);
        activityObject.setAdditionalProperty(EXTENSION_PROPERTY, extensions);
    };

    public static void addExtension(ObjectNode object, String key, Object extension) {
        ActivityObject activityObject = mapper.convertValue(object, ActivityObject.class);
        Map<String,Object> extensions = (Map<String,Object>) activityObject.getAdditionalProperties().get(EXTENSION_PROPERTY);
        extensions.put(key, extension);
    };

    public static void addExtensions(ObjectNode object, Map<String, Object> extensions) {
        ActivityObject activityObject = mapper.convertValue(object, ActivityObject.class);
        for( Map.Entry<String, Object> item : extensions.entrySet())
            activityObject.getAdditionalProperties().put(item.getKey(), item.getValue());
    };

    public static void removeExtension(ObjectNode object, String key) {
        ActivityObject activityObject = mapper.convertValue(object, ActivityObject.class);
        Map<String,Object> extensions = (Map<String,Object>) activityObject.getAdditionalProperties().get(EXTENSION_PROPERTY);
        extensions.remove(key);
    };

    /**
     * Creates a standard extension property
     * @param object objectnode to create the property in
     * @return the Map representing the extensions property
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> ensureExtensions(ObjectNode object) {
        ActivityObject activityObject = mapper.convertValue(object, ActivityObject.class);
        Map<String,Object> extensions = (Map<String,Object>) activityObject.getAdditionalProperties().get(EXTENSION_PROPERTY);
        if(extensions == null) {
            extensions = Maps.newHashMap();
            setExtensions(object, extensions);
        }
        return getExtensions(object);
    }
}
