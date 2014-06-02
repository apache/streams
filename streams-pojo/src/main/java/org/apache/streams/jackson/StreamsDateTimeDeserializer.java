package org.apache.streams.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.streams.data.util.RFC3339Utils;
import org.joda.time.DateTime;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by sblackmon on 3/27/14.
 */
public class StreamsDateTimeDeserializer extends StdDeserializer<DateTime> implements Serializable {

    protected StreamsDateTimeDeserializer(Class<DateTime> dateTimeClass) {
        super(dateTimeClass);
    }

    @Override
    public DateTime deserialize(JsonParser jpar, DeserializationContext context) throws IOException {
        return RFC3339Utils.getInstance().parseToUTC(jpar.getValueAsString());
    }
}
