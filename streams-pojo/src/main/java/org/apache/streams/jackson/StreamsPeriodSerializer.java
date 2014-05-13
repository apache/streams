package org.apache.streams.jackson;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.streams.data.util.RFC3339Utils;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.io.IOException;
import java.io.Serializable;

public class StreamsPeriodSerializer extends StdSerializer<Period> implements Serializable
{
    protected StreamsPeriodSerializer(Class<Period> dateTimeClass) {
        super(dateTimeClass);
    }

    @Override
    public void serialize(Period value, JsonGenerator jgen, SerializerProvider provider) throws IOException
    {
        jgen.writeString(Integer.toString(value.getMillis()));
    }
}