package org.apache.streams.jackson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.streams.data.util.RFC3339Utils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.io.Serializable;

/**
 * Created by sblackmon on 3/27/14.
 */
public class StreamsDateTimeSerializer extends StdSerializer<DateTime> implements Serializable {



    protected StreamsDateTimeSerializer(Class<DateTime> dateTimeClass) {
        super(dateTimeClass);
    }

    @Override
    public void serialize(DateTime value, JsonGenerator jgen, SerializerProvider provider) throws IOException {
        jgen.writeString(RFC3339Utils.getInstance().format(value));
    }
}
