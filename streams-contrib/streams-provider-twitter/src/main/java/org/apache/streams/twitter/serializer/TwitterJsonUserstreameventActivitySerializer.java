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
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Favorite;
import org.apache.streams.pojo.json.Follow;
import org.apache.streams.pojo.json.Like;
import org.apache.streams.pojo.json.StopFollowing;
import org.apache.streams.pojo.json.Unlike;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.UserstreamEvent;
import org.apache.streams.twitter.serializer.util.TwitterActivityUtil;

import java.io.IOException;
import java.util.List;

import static org.apache.streams.twitter.serializer.util.TwitterActivityUtil.*;


/**
* Created with IntelliJ IDEA.
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public class TwitterJsonUserstreameventActivitySerializer implements ActivitySerializer<String> {

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public String serialize(Activity deserialized) throws ActivitySerializerException {
        throw new NotImplementedException();
    }

    @Override
    public Activity deserialize(String serialized) throws ActivitySerializerException {

        ObjectMapper mapper = StreamsTwitterMapper.getInstance();
        ObjectNode objectNode = null;
        try {
            objectNode = mapper.readValue(serialized, ObjectNode.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return convert(objectNode);
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
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
        activity.setId(ActivityUtil.getActivityId("twitter", activity.getVerb()));
        if(Strings.isNullOrEmpty(activity.getId()))
            throw new ActivitySerializerException("Unable to determine activity id");
        activity.setProvider(getProvider());
        return activity;
    }

    public Actor buildActor(UserstreamEvent event) {
        Actor actor = new Actor();
        actor.setId(ActivityUtil.getObjectId("twitter", "page", event.getSource()));
        actor.setObjectType("page");
        return actor;
    }

    public ActivityObject buildActivityObject(UserstreamEvent event) {
        ActivityObject actObj = new ActivityObject();
        actObj.setId(ActivityUtil.getObjectId("twitter", "page", event.getTarget()));
        actObj.setObjectType("page");
        return actObj;
    }

    public String detectVerb(UserstreamEvent event) {
        if( event.getEventType().equals(UserstreamEvent.EventType.FOLLOW))
            return (new Follow()).getVerb();
        if( event.getEventType().equals(UserstreamEvent.EventType.UNFOLLOW))
            return (new StopFollowing()).getVerb();
        if( event.getEventType().equals(UserstreamEvent.EventType.FAVORITE))
            return (new Like()).getVerb();
        if( event.getEventType().equals(UserstreamEvent.EventType.UNFAVORITE))
            return (new Unlike()).getVerb();
        else return null;
    }

    public ActivityObject buildTarget(UserstreamEvent event) {
        return null;
    }

}
