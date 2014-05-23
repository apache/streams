package org.apache.streams.twitter.serializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.jackson.StreamsJacksonModule;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.IOException;

/**
 * Created by sblackmon on 3/27/14.
 */
public class StreamsTwitterMapper extends StreamsJacksonMapper {

    public static final DateTimeFormatter TWITTER_FORMAT = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss Z yyyy");

    public static final Long getMillis(String dateTime) {

        // this function is for pig which doesn't handle exceptions well
        try {
            Long result = TWITTER_FORMAT.parseMillis(dateTime);
            return result;
        } catch( Exception e ) {
            return null;
        }

    }

    private static final StreamsTwitterMapper INSTANCE = new StreamsTwitterMapper();

    public static StreamsTwitterMapper getInstance(){
        return INSTANCE;
    }

    public StreamsTwitterMapper() {
        super();
        registerModule(new SimpleModule()
        {
            {
                addDeserializer(DateTime.class, new StdDeserializer<DateTime>(DateTime.class) {
                    @Override
                    public DateTime deserialize(JsonParser jpar, DeserializationContext context) throws IOException, JsonProcessingException {
                        return TWITTER_FORMAT.parseDateTime(jpar.getValueAsString());
                    }
                });
            }
        });

    }

}
