package org.apache.streams.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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
 * Ensures generic ObjectNode representation of an Activity can be converted to Activity
 *
 */
public class BaseObjectNodeActivityConverter implements ActivityConverter<ObjectNode> {

    private ObjectMapper mapper = new StreamsJacksonMapper();

    private static BaseObjectNodeActivityConverter instance = new BaseObjectNodeActivityConverter();

    public static BaseObjectNodeActivityConverter getInstance() {
        return instance;
    }

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public ObjectNode serialize(Activity deserialized) throws ActivitySerializerException {
        try {
           return mapper.convertValue(deserialized, ObjectNode.class);
        } catch (Exception e) {
            throw new ActivitySerializerException();
        }
    }

    @Override
    public Activity deserialize(ObjectNode serialized) throws ActivitySerializerException {
        try {
            return mapper.convertValue(serialized, Activity.class);
        } catch (Exception e) {
            throw new ActivitySerializerException();
        }
    }

    @Override
    public List<Activity> deserializeAll(List<ObjectNode> serializedList) {
        List<Activity> result = Lists.newArrayList();
        for( ObjectNode item : serializedList ) {
            try {
                Activity activity = deserialize(item);
                result.add(activity);
            } catch (ActivitySerializerException e) {}
        }
        return result;
    }

}
