package org.apache.streams.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by sblackmon on 11/13/14.
 */
public class TypeConverterUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(TypeConverterUtil.class);

    public static Object convert(Object object, Class outClass) {
        return TypeConverterUtil.convert(object, outClass, StreamsJacksonMapper.getInstance());
    }

    public static Object convert(Object object, Class outClass, ObjectMapper mapper) {
        ObjectNode node = null;
        Object outDoc = null;
        if( object instanceof String ) {
            try {
                node = mapper.readValue((String)object, ObjectNode.class);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            node = mapper.convertValue(object, ObjectNode.class);
        }

        if(node != null) {
            try {
                if( outClass == String.class )
                    outDoc = mapper.writeValueAsString(node);
                else
                    outDoc = mapper.convertValue(node, outClass);

            } catch (Throwable e) {
                LOGGER.warn(e.getMessage());
                LOGGER.warn(node.toString());
            }
        }

        return outDoc;
    }
}
