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

package com.google.gplus.provider;

import com.google.gplus.serializer.util.GooglePlusActivityUtil;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class GPlusActivitySerializer implements ActivitySerializer<com.google.api.services.plus.model.Activity> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GPlusActivitySerializer.class);

    GPlusProvider provider;

    public GPlusActivitySerializer(GPlusProvider provider) {

        this.provider = provider;
    }

    public GPlusActivitySerializer() {
    }

    @Override
    public String serializationFormat() {
        return "gplus.v1";
    }

    @Override
    public com.google.api.services.plus.model.Activity serialize(Activity deserialized) {
        throw new NotImplementedException("Not currently implemented");
    }

    @Override
    public Activity deserialize(com.google.api.services.plus.model.Activity gplusActivity) {
        Activity activity = new Activity();

        GooglePlusActivityUtil.updateActivity(gplusActivity, activity);
        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<com.google.api.services.plus.model.Activity> serializedList) {
        throw new NotImplementedException("Not currently implemented");
    }
}
