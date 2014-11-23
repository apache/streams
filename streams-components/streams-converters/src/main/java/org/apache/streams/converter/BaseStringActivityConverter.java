package org.apache.streams.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import java.util.List;

/**
 * BaseObjectNodeActivityConverter is included by default in all
 * @see {@link org.apache.streams.converter.ActivityConverterProcessor}
 *
 * Ensures generic String Json representation of an Activity can be converted to Activity
 *
 */
public class BaseStringActivityConverter implements ActivityConverter<String> {

    private ObjectMapper mapper = new StreamsJacksonMapper();

    private static BaseStringActivityConverter instance = new BaseStringActivityConverter();

    public static BaseStringActivityConverter getInstance() {
        return instance;
    }


    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public String serialize(Activity deserialized) throws ActivitySerializerException {
        try {
            return mapper.writeValueAsString(deserialized);
        } catch (JsonProcessingException e) {
            throw new ActivitySerializerException();
        }
    }

    @Override
    public Activity deserialize(String serialized) throws ActivitySerializerException {
        try {
            return mapper.readValue(serialized, Activity.class);
        } catch (Exception e) {
            throw new ActivitySerializerException();
        }
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        List<Activity> result = Lists.newArrayList();
        for( String item : serializedList ) {
            try {
                Activity activity = deserialize(item);
                result.add(activity);
            } catch (ActivitySerializerException e) {}
        }
        return result;
    }
}
