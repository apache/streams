package org.apache.streams.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.exceptions.ActivityConversionException;
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

    public static Class requiredClass = String.class;

    private ObjectMapper mapper = new StreamsJacksonMapper();

    @Override
    public Class requiredClass() {
        return requiredClass;
    }

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public String fromActivity(Activity deserialized) throws ActivityConversionException {
        try {
            return mapper.writeValueAsString(deserialized);
        } catch (JsonProcessingException e) {
            throw new ActivityConversionException();
        }
    }

    @Override
    public List<Activity> toActivityList(String serialized) throws ActivityConversionException {
        List<Activity> activityList = Lists.newArrayList();
        try {
            activityList.add(mapper.readValue(serialized, Activity.class));
        } catch (Exception e) {
            throw new ActivityConversionException();
        } finally {
            return activityList;
        }
    }

    @Override
    public List<String> fromActivityList(List<Activity> list) {
        throw new NotImplementedException();
    }

    @Override
    public List<Activity> toActivityList(List<String> list) {
        List<Activity> result = Lists.newArrayList();
        for( String item : list ) {
            try {
                result.addAll(toActivityList(item));
            } catch (ActivityConversionException e) {}
        }
        return result;
    }
}
