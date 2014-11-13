package org.apache.streams.converter;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.ActivityConverterResolver;
import org.apache.streams.exceptions.ActivitySerializerException;

/**
 * Created by sblackmon on 11/12/14.
 */
public class BaseActivityConverterResolver implements ActivityConverterResolver {

    private static BaseActivityConverterResolver instance = new BaseActivityConverterResolver();

    public static BaseActivityConverterResolver getInstance() {
        return instance;
    }

    @Override
    public Class bestSerializer(Class documentClass) throws ActivitySerializerException {
        if( documentClass == String.class) {
            return BaseStringActivityConverter.class;
        } else if( documentClass == ObjectNode.class) {
            return BaseObjectNodeActivityConverter.class;
        } else return null;
    }

}
