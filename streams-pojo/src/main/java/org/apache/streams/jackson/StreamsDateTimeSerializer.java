package org.apache.streams.jackson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

/**
 * Created by sblackmon on 3/27/14.
 */
public class StreamsDateTimeSerializer extends StdSerializer<DateTime> {

    public static final DateTimeFormatter ACTIVITY_FORMAT = ISODateTimeFormat.basicDateTime();

    protected StreamsDateTimeSerializer(Class<DateTime> dateTimeClass) {
        super(dateTimeClass);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonGenerationException {
        jgen.writeString(ACTIVITY_FORMAT.print(value));
    }
}
