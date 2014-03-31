package org.apache.streams.jackson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.apache.streams.exceptions.ActivityDeserializerException;
import org.apache.streams.exceptions.ActivitySerializerException;
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
    public DateTime deserialize(JsonParser jpar, DeserializationContext context) throws IOException {
        DateTime result = null;

        try {
            result = ACTIVITY_FORMAT.parseDateTime(jpar.getText());
            return result;
        } catch( Exception e ) {}

        try {
            result = ACTIVITY_FORMAT.parseDateTime(jpar.getValueAsString());
            return result;
        } catch( Exception e ) {}


        try {
            result = jpar.readValueAs(DateTime.class);
            return result;
        } catch( Exception e ) {}

        if( result == null )
            throw new IOException(" could not deserialize " + jpar.toString());

        return result;
    }
}
