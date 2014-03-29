package org.apache.streams.jackson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

/**
 * Created by sblackmon on 3/27/14.
 */
public class StreamsDateTimeDeserializer extends StdDeserializer<DateTime> {

    public static final DateTimeFormatter ACTIVITY_FORMAT = ISODateTimeFormat.basicDateTime();

    protected StreamsDateTimeDeserializer(Class<DateTime> dateTimeClass) {
        super(dateTimeClass);
    }

    @Override
    public DateTime deserialize(JsonParser jpar, DeserializationContext context) throws IOException, JsonProcessingException {
        return ACTIVITY_FORMAT.parseDateTime(jpar.getValueAsString());
    }
}
