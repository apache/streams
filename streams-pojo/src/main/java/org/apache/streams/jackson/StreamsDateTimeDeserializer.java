package org.apache.streams.jackson;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.apache.streams.exceptions.ActivityDeserializerException;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;

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

        ObjectMapper basicMapper = new ObjectMapper();
        basicMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);

        System.out.println(jpar.getCurrentToken());

        if( jpar.getCurrentToken().isStructStart() ) {

            System.out.println(jpar.getCurrentToken());

            try {
                JsonNode node = jpar.readValueAsTree();
                // now what?
                return result;
            } catch( Exception e ) {
                e.printStackTrace();
            }
        } else if( jpar.getCurrentToken().isScalarValue() ) {
            try {
                result = StreamsJacksonMapper.ACTIVITY_FORMAT.parseDateTime(jpar.getText());
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if( jpar.getCurrentToken().isNumeric() ) {
            try {
                result = new DateTime(jpar.getLongValue());
                return result;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

//        try {
//            result = ACTIVITY_FORMAT.parseDateTime(jpar.getValueAsString());
//            return result;
//        } catch( Exception e ) {
//            e.printStackTrace();
//        }

//

        if( result == null )
            throw new IOException(" could not deserialize " + jpar.toString());

        return result;
    }
}
