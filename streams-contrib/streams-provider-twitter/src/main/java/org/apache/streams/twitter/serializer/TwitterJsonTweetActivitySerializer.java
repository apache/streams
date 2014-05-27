package org.apache.streams.twitter.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.pojo.Tweet;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

import static org.apache.streams.twitter.serializer.util.TwitterActivityUtil.*;

public class TwitterJsonTweetActivitySerializer implements ActivitySerializer<String>, Serializable {

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
        Tweet tweet = null;
        try {
            tweet = mapper.readValue(serialized, Tweet.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Activity activity = new Activity();

        updateActivity(tweet, activity);

        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        return null;
    }
}
