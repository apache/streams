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

package org.apache.streams.facebook.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.facebook.serializer.FacebookActivityUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.*;
import org.apache.streams.facebook.Page;

import java.util.List;

/**
 * Serializes activity posts
 *   sblackmon: This class needs a rewrite
 */
public class FacebookPageActivitySerializer implements ActivitySerializer<Page> {

    public static ObjectMapper mapper;
    static {
        mapper = StreamsJacksonMapper.getInstance();
    }

    @Override
    public String serializationFormat() {
        return "facebook_post_json_v1";
    }

    @Override
    public Page serialize(Activity deserialized) throws ActivitySerializerException {
        throw new NotImplementedException("Not currently supported by this deserializer");
    }

    @Override
    public Activity deserialize(Page page) throws ActivitySerializerException {
        Activity activity = new Activity();

        FacebookActivityUtil.updateActivity(page, activity);

        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<Page> serializedList) {
        throw new NotImplementedException("Not currently supported by this deserializer");
    }
}