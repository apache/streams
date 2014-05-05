package org.apache.streams.datasift.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.streams.data.util.RFC3339Utils;
import org.apache.streams.jackson.StreamsDateTimeDeserializer;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

/**
 * Created by sblackmon on 3/27/14.
 */
public class StreamsDatasiftMapper extends StreamsJacksonMapper {

    public static final DateTimeFormatter DATASIFT_FORMAT = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss Z");

    private static final StreamsDatasiftMapper INSTANCE = new StreamsDatasiftMapper();

    public static StreamsDatasiftMapper getInstance(){
        return INSTANCE;
    }

    public StreamsDatasiftMapper() {
        super();
        registerModule(new SimpleModule()
        {
            {
                addDeserializer(DateTime.class, new StdDeserializer<DateTime>(DateTime.class) {
                    @Override
                    public DateTime deserialize(JsonParser jpar, DeserializationContext context) throws IOException {
                        DateTime dateTime;
                        try {
                            dateTime = RFC3339Utils.getInstance().parseUTC(jpar.getValueAsString());
                            return dateTime;
                        } catch( Exception e ) {};
                        return DATASIFT_FORMAT.parseDateTime(jpar.getValueAsString());
                    }
                });
            }
        });

    }

}
