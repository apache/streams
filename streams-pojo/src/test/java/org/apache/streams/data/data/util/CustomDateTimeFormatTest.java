package org.apache.streams.data.data.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by sblackmon on 12/1/14.
 */
public class CustomDateTimeFormatTest {

    @Test
    public void testCustomDateTimeFormatExplicit() {
        String format = "EEE MMM dd HH:mm:ss Z yyyy";
        String input = "Tue Jan 17 21:21:46 Z 2012";
        Long outputMillis = 1326835306000L;
        ObjectMapper mapper = StreamsJacksonMapper.getInstance(format);
        DateTime time;
        try {
            String json = "{\"published\":\"" + input + "\"}";
            Activity activity = mapper.readValue(json, Activity.class);

            //Writes out value as a String including quotes
            Long result = activity.getPublished().getMillis();

            assertEquals(result, outputMillis);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCustomDateTimeFormatReflection() {
        String input = "Tue Jan 17 21:21:46 Z 2012";
        Long outputMillis = 1326835306000L;
        ObjectMapper mapper = StreamsJacksonMapper.getInstance();
        DateTime time;
        try {
            String json = "{\"published\":\"" + input + "\"}";
            Activity activity = mapper.readValue(json, Activity.class);

            //Writes out value as a String including quotes
            Long result = activity.getPublished().getMillis();

            assertEquals(result, outputMillis);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }


}
