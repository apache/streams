package org.apache.streams.datasift.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import java.util.List;

/**
 * Created by rebanks on 5/30/14.
 */
public class DatasiftActivitySerializer implements ActivitySerializer<Datasift> {

    private static final DatasiftTweetActivitySerializer TWITTER_SERIALIZER = new DatasiftTweetActivitySerializer();
    private static final DatasiftDefaultActivitySerializer DEFAULT_SERIALIZER = new DatasiftDefaultActivitySerializer();
    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public Datasift serialize(Activity deserialized) throws ActivitySerializerException {
        return null;
    }

    @Override
    public Activity deserialize(Datasift serialized) throws ActivitySerializerException {
        if(serialized.getTwitter() != null) {
            return TWITTER_SERIALIZER.deserialize(serialized);
        } else {
            return DEFAULT_SERIALIZER.deserialize(serialized);
        }
    }

    public Activity deserialize(String json) throws ActivitySerializerException {
        try {
            return deserialize(MAPPER.readValue(json, Datasift.class));
        } catch (Exception e) {
            throw new ActivitySerializerException(e);
        }
    }

    @Override
    public List<Activity> deserializeAll(List<Datasift> serializedList) {
        return null;
    }
}
