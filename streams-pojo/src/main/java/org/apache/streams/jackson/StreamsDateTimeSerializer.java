package org.apache.streams.jackson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

/**
 * Created by sblackmon on 3/27/14.
 */
public class StreamsDateTimeSerializer extends StdSerializer<DateTime> {

    protected StreamsDateTimeSerializer(Class<DateTime> dateTimeClass) {
        super(dateTimeClass);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        long timestamp = value.getMillis();
        String result = StreamsJacksonMapper.ACTIVITY_FORMAT.print(timestamp);
        jgen.writeString(result);
    }
}
