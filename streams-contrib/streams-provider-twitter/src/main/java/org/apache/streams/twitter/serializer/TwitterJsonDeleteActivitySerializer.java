package org.apache.streams.twitter.serializer;

/*
 * #%L
 * streams-provider-twitter
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Tweet;

import java.io.Serializable;
import java.util.List;

import static org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer.*;

/**
* Created with IntelliJ IDEA.
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public class TwitterJsonDeleteActivitySerializer implements ActivitySerializer<String>, Serializable {

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
        return null;
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        return null;
    }

    public Activity convert(ObjectNode event) throws ActivitySerializerException {

        ObjectMapper mapper = StreamsTwitterMapper.getInstance();
        Delete delete = null;
        try {
            delete = mapper.treeToValue(event, Delete.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Activity activity = new Activity();
        activity.setActor(buildActor(delete));
        activity.setVerb("delete");
        activity.setObject(buildActivityObject(delete));
        activity.setId(TwitterJsonActivitySerializer.formatId(activity.getVerb(), delete.getDelete().getStatus().getIdStr()));
        if(Strings.isNullOrEmpty(activity.getId()))
            throw new ActivitySerializerException("Unable to determine activity id");
        activity.setProvider(getProvider());
        addTwitterExtension(activity, event);
        return activity;
    }

    public Actor buildActor(Delete delete) {
        Actor actor = new Actor();
        actor.setId(formatId(delete.getDelete().getStatus().getUserIdStr()));
        return actor;
    }

    public ActivityObject buildActivityObject(Delete delete) {
        ActivityObject actObj = new ActivityObject();
        actObj.setId(formatId(delete.getDelete().getStatus().getIdStr()));
        actObj.setObjectType("tweet");
        return actObj;
    }

    public ActivityObject buildTarget(Tweet tweet) {
        return null;
    }

}
