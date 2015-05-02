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

package org.apache.streams.instagram.serializer;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.instagram.UsersInfo;
import org.apache.streams.instagram.provider.userinfo.InstagramUserInfoProvider;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Image;
import org.apache.streams.pojo.json.Provider;
import org.jinstagram.entity.users.basicinfo.UserInfoData;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.commons.lang.NotImplementedException;

import java.util.List;
import java.util.Map;

/**
 *
 */
public class InstagramUserInfoSerializer implements ActivitySerializer<UserInfoData> {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramUserInfoSerializer.class);

    private static final String STREAMS_ID_PREFIX = "id:instagram:";
    private static final String PROVIDER_ID = "id:provider:instagram";
    private static final String DISPLAY_NAME = "Instagram";

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public UserInfoData serialize(Activity deserialized) throws ActivitySerializerException {
        throw new NotImplementedException();
    }

    @Override
    public Activity deserialize(UserInfoData serialized) throws ActivitySerializerException {
        Activity activity = new Activity();
        Provider provider = new Provider();
        provider.setId(PROVIDER_ID);
        provider.setDisplayName(DISPLAY_NAME);
        activity.setProvider(provider);
        activity.setPublished(DateTime.now().withZone(DateTimeZone.UTC));
        Actor actor = new Actor();
        Image image = new Image();
        image.setUrl(serialized.getProfile_picture());
        actor.setImage(image);
        actor.setId(STREAMS_ID_PREFIX+serialized.getId());
        actor.setSummary(serialized.getBio());
        actor.setAdditionalProperty("handle", serialized.getUsername());
        actor.setDisplayName(serialized.getFullName());
        Map<String, Object> extensions = Maps.newHashMap();
        actor.setAdditionalProperty("extensions", extensions);
        extensions.put("screenName", serialized.getUsername());
        extensions.put("posts", serialized.getCounts().getMedia());
        extensions.put("followers", serialized.getCounts().getFollwed_by());
        extensions.put("website", serialized.getWebsite());
        extensions.put("following", serialized.getCounts().getFollows());
        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<UserInfoData> serializedList) {
        List<Activity> result = Lists.newLinkedList();
        for(UserInfoData data : serializedList) {
            try {
                result.add(deserialize(data));
            } catch (ActivitySerializerException ase) {
                LOGGER.error("Caught ActivitySerializerException, dropping user info data : {}", data.getId());
                LOGGER.error("Exception : {}", ase);
            }
        }
        return result;
    }
}
