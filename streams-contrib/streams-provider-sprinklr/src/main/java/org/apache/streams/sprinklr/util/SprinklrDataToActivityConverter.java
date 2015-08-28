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

package org.apache.streams.sprinklr.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.*;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.List;

import static org.apache.streams.data.util.ActivityUtil.*;

/**
 * Converts SprinklrData to an Activity Object
 */
public class SprinklrDataToActivityConverter {
    private static final Logger LOGGER = LoggerFactory.getLogger(SprinklrDataToActivityConverter.class);
    public static final String LANGUAGE_KEY = "LANGUAGE";

    /**
     * convert the given Sprinklr data in JsonNode format to an Activity
     * @param item JsonNode
     * @return an Activity
     */
    public Activity convert(JsonNode item) {
        Activity activity = new Activity();

        try {
            activity.setVerb("post");
            activity.setId(formatId(activity.getVerb(), item.get("universalMessageId").asText()));
            activity.setContent(item.has("message") ? item.get("message").asText() : "");
            activity.setTitle(item.has("title") ? item.get("title").asText() : "");
            activity.setPublished(item.has("snCreatedTime") ? new DateTime(item.get("snCreatedTime").asLong()) : new DateTime());
            activity.setUrl(item.has("permalink") ? item.get("permalink").asText() : "");
            activity.setActor(buildActor(item));

            // set media data
            activity.setObject(buildActivityObject(item));
            activity.setProvider(getProvider(item));

            Map<String, Object> extensions = ExtensionUtil.ensureExtensions(activity);
            extensions.put("sprinklr", item);

            // set location, set language
            setActivityExtensions(item, extensions);
        } catch (Exception e) {
            LOGGER.error("Failed to convert JsonNode to Activity; errorMessage={}", e.getMessage());
        }

        return activity;
    }

    /**
     * Set Activity Extension properties for location and language
     * @param item JsonNode
     * @param extensions a Map of String to Object
     */
    @SuppressWarnings("unchecked")
    private void setActivityExtensions(JsonNode item, Map<String, Object> extensions) {
        Map<String, Object> locationMap;
        // set location
        if (item.has("location")) {
            JsonNode location = item.get("location");
            String country = location.has("text") ? location.get("text").asText() : "";
            if(StringUtils.isNotBlank(country)) {
                if (extensions.containsKey(LOCATION_EXTENSION)) {
                    locationMap = (Map<String, Object>) extensions.get(LOCATION_EXTENSION);
                } else {
                    locationMap = Maps.newHashMap();
                }
                locationMap.put(LOCATION_EXTENSION_COUNTRY, country);
                extensions.put(LOCATION_EXTENSION, locationMap);
            }
            if (location.has("lat") && location.has("lon")) {
                StringBuilder builder = new StringBuilder();
                builder.append("[")
                        .append(location.get("lon").asText())
                        .append(",")
                        .append(location.get("lat").asText())
                        .append("]");
                extensions.put(LOCATION_EXTENSION_COORDINATES, builder.toString());
            }
        }
        // set language
        if (item.has("language")) {
            if(extensions.containsKey(LANGUAGE_KEY)) {
                extensions.put(LANGUAGE_EXTENSION, item.get("language"));
            }
        }
        // set likes etc
        // sprinklr returns different data objects for each social media network.
        // add to extensions if the key exists in the data object
        if (item.has("parentMsgType")) extensions.put("parentMsgType", item.get("parentMsgType").asInt());
        if (item.has("deleted")) extensions.put("deleted", item.get("deleted").asBoolean());
        if (item.has("archived")) extensions.put("archived", item.get("archived").asBoolean());
        if (item.has("brandPost")) extensions.put("brandPost", item.get("brandPost").asBoolean());
        if (item.has("parentBrandPost")) extensions.put("parentBrandPost", item.get("parentBrandPost").asBoolean());
        if (item.has("hasBrandComment")) extensions.put("hasBrandComment", item.get("hasBrandComment").asBoolean());
        if (item.has("hasScheduledComment")) extensions.put("hasScheduledComment", item.get("hasScheduledComment").asBoolean());
        if (item.has("hasParentPost")) extensions.put("hasParentPost", item.get("hasParentPost").asBoolean());
        if (item.has("hasApplicationConversation")) extensions.put("hasApplicationConversation", item.get("hasApplicationConversation").asBoolean());
        if (item.has("hasConversation")) extensions.put("hasConversation", item.get("hasConversation").asBoolean());
        if (item.has("apiStatus")) extensions.put("apiStatus", item.get("apiStatus").asText());
        if (item.has("likeFlag")) extensions.put("likeFlag", item.get("likeFlag").asBoolean());
        if (item.has("hidden")) extensions.put("hidden", item.get("hidden").asBoolean());
        if (item.has("like")) extensions.put("like", item.get("like").asBoolean());
        if (item.has("isUserLikes")) extensions.put("isUserLikes", item.get("isUserLikes").asBoolean());
        if (item.has("numberOfLikes")) extensions.put("numberOfLikes", item.get("numberOfLikes").asInt());
        if (item.has("numberOfShares")) extensions.put("numberOfShares", item.get("numberOfShares").asInt());
        if (item.has("numberOfViews")) extensions.put("numberOfViews", item.get("numberOfViews").asInt());
        if (item.has("numOfComments")) extensions.put("numOfComments", item.get("numOfComments").asInt());
        if (item.has("numOfPlusOned")) extensions.put("numOfPlusOned", item.get("numOfPlusOned").asInt());
        if (item.has("numOfShares")) extensions.put("numOfShares", item.get("numOfShares").asInt());
        if (item.has("actualNumOfComments")) extensions.put("actualNumOfComments", item.get("actualNumOfComments").asInt());
        if (item.has("category")) extensions.put("category", item.get("category").asText());

    }

    /**
     * Construct the ActivityObject
     * @param item JsonNode
     * @return an ActivityObject
     */
    private ActivityObject buildActivityObject(JsonNode item) {
        ActivityObject activityObject = new ActivityObject();
        try {
            if (item.get("snType").asText().equalsIgnoreCase("TWITTER")) {
                activityObject.setObjectType("tweet");
            } else {
                activityObject.setObjectType("post");
            }
            activityObject.setId(getObjectId(item.get("snType").asText().toLowerCase(), activityObject.getObjectType(), item.get("snMsgId").asText()));
            activityObject.setAttachments(buildActivityObjectAttachments(item));
            activityObject.setUrl(item.has("permalink") ? item.get("permalink").asText() : "");
            activityObject.setContent(item.has("message") ? item.get("message").asText() : "");

        } catch (Exception e) {
            LOGGER.error("Failed to create ActivityObject errorMessage={}", e.getMessage());
        }
        return activityObject;
    }

    /**
     * Helper to build ActivityObject attachments
     * @param item JsonNode
     * @return a List of ActivityObject
     */
    private List<ActivityObject> buildActivityObjectAttachments(JsonNode item) {
        List<ActivityObject> attachments = Lists.newArrayList();
        if (item.has("mediaList")) {
            for(JsonNode mediaItem : item.get("mediaList")) {
                if (mediaItem.has("type")) {
                    String mediaType = mediaItem.get("type").asText();
                    addMediaObject(mediaItem, attachments, mediaType.toLowerCase());
                }
            }
        }
        return attachments;
    }

    /**
     * Helper to build Media Objects for the ActivityObject
     * @param mediaItem JsonNode
     * @param attachments List<ActivityObject>
     * @param mediaType a String either 'video' or 'image'
     */
    private void addMediaObject(JsonNode mediaItem, List<ActivityObject> attachments, String mediaType) {
        try {
            ActivityObject mediaObj = new ActivityObject();
            Image image = new Image();
            image.setUrl(mediaItem.has("source") ? mediaItem.get("source").asText() : "");
            image.withAdditionalProperty("name", mediaItem.has("name") ? mediaItem.get("name").asText() : "");
            image.withAdditionalProperty("id", mediaItem.has("id") ? mediaItem.get("id").asText() : "");
            image.withAdditionalProperty("caption", mediaItem.has("caption") ? mediaItem.get("caption").asText() : "");
            image.withAdditionalProperty("description", mediaItem.has("description") ? mediaItem.get("description").asText() : "");
            image.withAdditionalProperty("picture", mediaItem.has("picture") ? mediaItem.get("picture").asText() : "");

            mediaObj.setImage(image);
            mediaObj.setObjectType(mediaType);

            attachments.add(mediaObj);
        } catch (Exception e) {
            LOGGER.error("Failed to add {} object={} errorMessage={}", mediaType, mediaItem.toString(), e.getMessage());
        }
    }

    /**
     * Build the provider for the Activity
     * @param item JsonNode
     * @return Provider
     */
    private Provider getProvider(JsonNode item) {
        Provider provider = new Provider();
        String mediaType = item.get("snType").asText().toLowerCase();
        provider.setId(getProviderId(mediaType));
        provider.setDisplayName(StringUtils.capitalize(mediaType));
        return provider;
    }

    /**
     * Build the actor for the Activity
     * @param item JsonNode
     * @return Actor
     */
    private Actor buildActor(JsonNode item) {
        Actor actor = new Actor();
        if (item.has("senderProfile")) {
            JsonNode senderProfile = item.get("senderProfile");
            Image image = new Image();
            image.setUrl(senderProfile.has("profileImgUrl") ? senderProfile.get("profileImgUrl").asText() : "");

            actor.setDisplayName(senderProfile.has("name") ? senderProfile.get("name").asText() : "");
            actor.setId(formatId(senderProfile.get("snId").asText()));
            actor.setImage(image);
            actor.setUrl(senderProfile.has("permalink") ? senderProfile.get("permalink").asText() : "");

            Map<String, Object> extensions = new HashMap<String, Object>();

            if (senderProfile.has("screenName")) extensions.put("screenName", senderProfile.get("screenName").asText());
            if (senderProfile.has("followers")) extensions.put("followers", senderProfile.get("followers").asInt());
            if (senderProfile.has("follows")) extensions.put("follows", senderProfile.get("follows").asInt());
            if (senderProfile.has("age")) extensions.put("age", senderProfile.get("age").asInt());
            if (senderProfile.has("favCount")) extensions.put("favCount", senderProfile.get("favCount").asInt());
            if (senderProfile.has("statusCount")) extensions.put("posts", senderProfile.get("statusCount").asInt());
            if (senderProfile.has("snId")) extensions.put("snId", senderProfile.get("snId").asText());
            if (senderProfile.has("participationIndex")) extensions.put("participationIndex", senderProfile.get("participationIndex").asInt());
            if (senderProfile.has("influencerIndex")) extensions.put("influencerIndex", senderProfile.get("influencerIndex").asInt());
            if (senderProfile.has("spamIndex")) extensions.put("spamIndex", senderProfile.get("spamIndex").asInt());
            if (senderProfile.has("profileWorkflowProperties")) extensions.put("profileWorkflowProperties", senderProfile.get("profileWorkflowProperties"));
            if (senderProfile.has("universalProfileId")) extensions.put("universalProfileId", senderProfile.get("universalProfileId").asText());
            if (senderProfile.has("accountsBlockingUser")) extensions.put("accountsBlockingUser", senderProfile.get("accountsBlockingUser"));

            extensions.put("senderProfile", senderProfile);

            actor.setAdditionalProperty("extensions", extensions);
            actor.setAdditionalProperty("handle", extensions.get("screenName"));
        }
        return actor;
    }

    /**
     * Formats the ID to conform with the Apache Streams activity ID convention
     * @param idparts the parts of the ID to join
     * @return a valid Activity ID in format "id:sprinklr:part1:part2:...partN"
     */
    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:sprinklr", idparts));
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
}
