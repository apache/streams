package org.apache.streams.twitter.serializer;

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

import static org.apache.streams.twitter.serializer.util.TwitterActivityUtil.*;


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
        updateActivity(delete, activity);
        return activity;
    }

    public ActivityObject buildTarget(Tweet tweet) {
        return null;
    }

}
