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

package org.apache.streams.twitter.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.twitter.pojo.UserstreamEvent;

import java.util.List;

import static org.apache.streams.twitter.serializer.util.TwitterActivityUtil.*;


/**
* Created with IntelliJ IDEA.
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public class TwitterJsonUserstreameventActivityConverter implements ActivityConverter<UserstreamEvent> {

    private static TwitterJsonUserstreameventActivityConverter instance = new TwitterJsonUserstreameventActivityConverter();

    public static TwitterJsonUserstreameventActivityConverter getInstance() {
        return instance;
    }

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public UserstreamEvent serialize(Activity deserialized) throws ActivitySerializerException {
        throw new NotImplementedException();
    }

    @Override
    public Activity deserialize(UserstreamEvent serialized) throws ActivitySerializerException {
        return null;
    }

    @Override
    public List<Activity> deserializeAll(List<UserstreamEvent> serializedList) {
        return null;
    }

    public Activity convert(ObjectNode item) throws ActivitySerializerException {

        ObjectMapper mapper = StreamsTwitterMapper.getInstance();
        UserstreamEvent event = null;
        try {
            event = mapper.treeToValue(item, UserstreamEvent.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Activity activity = new Activity();
        activity.setActor(buildActor(event));
        activity.setVerb(detectVerb(event));
        activity.setObject(buildActivityObject(event));
        activity.setId(formatId(activity.getVerb()));
        if(Strings.isNullOrEmpty(activity.getId()))
            throw new ActivitySerializerException("Unable to determine activity id");
        activity.setProvider(getProvider());
        return activity;
    }

    public Actor buildActor(UserstreamEvent event) {
        Actor actor = new Actor();
        //actor.setId(formatId(delete.getDelete().getStatus().getUserIdStr()));
        return actor;
    }

    public ActivityObject buildActivityObject(UserstreamEvent event) {
        ActivityObject actObj = new ActivityObject();
        //actObj.setId(formatId(delete.getDelete().getStatus().getIdStr()));
        //actObj.setObjectType("tweet");
        return actObj;
    }

    public String detectVerb(UserstreamEvent event) {
        return null;
    }

    public ActivityObject buildTarget(UserstreamEvent event) {
        return null;
    }

}
