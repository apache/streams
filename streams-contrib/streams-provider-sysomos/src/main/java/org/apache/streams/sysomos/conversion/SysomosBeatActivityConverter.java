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

package org.apache.streams.sysomos.conversion;

import com.google.common.collect.Maps;
import com.sysomos.xml.BeatApi;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Provider;
import org.joda.time.DateTime;

import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.*;

/**
 * Converts an instance of a {@link com.sysomos.xml.BeatApi.BeatResponse.Beat} to an {@link org.apache.streams.pojo.json.Activity}
 */
public class SysomosBeatActivityConverter {

    public static final String LANGUAGE_KEY = "LANGUAGE";

    public Activity convert(BeatApi.BeatResponse.Beat beat) {
        Activity converted = new Activity();
        converted.setId(beat.getDocid());
        converted.setVerb("posted");
        converted.setContent(beat.getContent());
        converted.setTitle(beat.getTitle());
        converted.setPublished(new DateTime(beat.getTime()));
        converted.setUrl(beat.getLink());
        converted.setActor(new Actor());
        Map<String, BeatApi.BeatResponse.Beat.Tag> mappedTags = mapTags(beat);
        Map<String, Object> extensions = ensureExtensions(converted);
        extensions.put("keywords", beat.getContent());
        setLocation(beat, extensions);
        setObject(beat, converted);
        setProvider(beat, converted);
        setLanguage(mappedTags, extensions);
        extensions.put("sysomos", beat);

        setChannelSpecificValues(beat, converted, mappedTags);

        return converted;
    }

    protected void setChannelSpecificValues(BeatApi.BeatResponse.Beat beat, Activity converted, Map<String, BeatApi.BeatResponse.Beat.Tag> mappedTags) {
        String mediaType = beat.getMediaType();
        String lowerMediaType = mediaType.toLowerCase();
        Actor actor = converted.getActor();
        ActivityObject object = converted.getObject();
        if ("TWITTER".equals(mediaType)) {
            actor.setId(getPersonId(lowerMediaType, beat.getHost()));
            actor.setDisplayName(beat.getHost());
            actor.setUrl("http://twitter.com/" + beat.getHost());
            object.setObjectType("tweet");
            object.setId(getObjectId(lowerMediaType, "tweet", beat.getTweetid()));
        } else if ("FACEBOOK".equals(mediaType)) {
            actor.setId(getPersonId(lowerMediaType, mappedTags.get("FBID").getValue()));
            actor.setDisplayName(beat.getTitle());
            actor.setUrl(beat.getHost());
            object.setObjectType("post");
            object.setId(getObjectId(lowerMediaType, "post", String.valueOf(converted.getContent().hashCode())));
        } else {
            actor.setId(null);
            actor.setDisplayName(null);
            actor.setUrl(null);
            object.setObjectType("post");
            object.setId(getObjectId(lowerMediaType, "post", String.valueOf(converted.getContent().hashCode())));
        }
    }

    protected void setLanguage(Map<String, BeatApi.BeatResponse.Beat.Tag> mappedTags, Map<String, Object> extensions) {
        if(mappedTags.containsKey(LANGUAGE_KEY)) {
            extensions.put(LANGUAGE_EXTENSION, mappedTags.get(LANGUAGE_KEY).getValue());
        }
    }

    protected void setObject(BeatApi.BeatResponse.Beat beat, Activity converted) {
        ActivityObject object = new ActivityObject();
        converted.setObject(object);
        object.setUrl(beat.getLink());
        object.setContent(beat.getContent());
    }

    @SuppressWarnings("unchecked")
    protected void setLocation(BeatApi.BeatResponse.Beat beat, Map<String, Object> extensions) {
        Map<String, Object> location;
        String country = beat.getLocation().getCountry();
        if(StringUtils.isNotBlank(country)) {
            if (extensions.containsKey(LOCATION_EXTENSION)) {
                location = (Map<String, Object>) extensions.get(LOCATION_EXTENSION);
            } else {
                location = Maps.newHashMap();
                extensions.put(LOCATION_EXTENSION, location);
            }
            location.put(LOCATION_EXTENSION_COUNTRY, country);
        }
    }

    protected void setProvider(BeatApi.BeatResponse.Beat beat, Activity converted) {
        Provider provider = new Provider();
        String mediaType = beat.getMediaType().toLowerCase();
        provider.setId(getProviderId(mediaType));
        provider.setDisplayName(StringUtils.capitalize(mediaType));
        converted.setProvider(provider);
    }

    protected Map<String, BeatApi.BeatResponse.Beat.Tag> mapTags(BeatApi.BeatResponse.Beat beat) {
        Map<String, BeatApi.BeatResponse.Beat.Tag> tags = Maps.newHashMap();
        for(BeatApi.BeatResponse.Beat.Tag tag : beat.getTag()) {
            if(tag.getSystemType() != null) {
                tags.put(tag.getSystemType(), tag);
            }
        }
        return tags;
    }


}
