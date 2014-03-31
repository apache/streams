package org.apache.streams.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.joda.time.DateTime;

import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Created by sblackmon on 3/27/14.
 */
public class StreamsDateTimeDeserializer extends StdDeserializer<DateTime> {

    protected StreamsDateTimeDeserializer(Class<DateTime> dateTimeClass) {
        super(dateTimeClass);
    }

    @Override
    public DateTime deserialize(JsonParser jpar, DeserializationContext context) throws IOException {
        DateTime result = null;

        Long numberValue = jpar.getValueAsLong();
        if(numberValue != 0L) {
            result = new DateTime(numberValue);
        } else {
            String nodeValue = jpar.getValueAsString();
            if (nodeValue != null) {
                result = StreamsJacksonMapper.ACTIVITY_FORMAT.parseDateTime(nodeValue);
            }
        }

        if( result == null )
            throw new IOException(" could not deserialize " + jpar.toString());

        return result;
    }
}
