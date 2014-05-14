package org.apache.streams.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.apache.streams.data.util.RFC3339Utils;
import org.joda.time.Period;

import java.io.IOException;
import java.io.Serializable;

public class StreamsPeriodDeserializer extends StdDeserializer<Period> implements Serializable
{

    protected StreamsPeriodDeserializer(Class<Period> dateTimeClass) {
        super(dateTimeClass);
    }

    public Period deserialize(JsonParser jpar, DeserializationContext context) throws IOException {
        return Period.millis(jpar.getIntValue());
    }
}
