package org.apache.streams.data.data.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

/**
 * Created by sblackmon on 3/31/14.
 */
public class DateTimeSerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(DateTimeSerDeTest.class);
    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    @Test
    public void testActivityStringSer() {
        String input = "2013-09-18T20:52:47Z";
        try {
            DateTime output = mapper.readValue(input, DateTime.class);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testJodaJsonDeser() {
        String input = "{\"year\":2012,\"era\":1,\"dayOfMonth\":17,\"dayOfWeek\":2,\"dayOfYear\":17,\"weekOfWeekyear\":3,\"weekyear\":2012,\"monthOfYear\":1,\"yearOfEra\":2012,\"yearOfCentury\":12,\"centuryOfEra\":20,\"millisOfSecond\":0,\"millisOfDay\":69706000,\"secondOfMinute\":46,\"secondOfDay\":69706,\"minuteOfHour\":21,\"minuteOfDay\":1161,\"hourOfDay\":19,\"zone\":{\"fixed\":false,\"uncachedZone\":{\"cachable\":true,\"fixed\":false,\"id\":\"America/Los_Angeles\"},\"id\":\"America/Los_Angeles\"},\"millis\":1326856906000,\"chronology\":{\"zone\":{\"fixed\":false,\"uncachedZone\":{\"cachable\":true,\"fixed\":false,\"id\":\"America/Los_Angeles\"},\"id\":\"America/Los_Angeles\"}},\"afterNow\":false,\"beforeNow\":true,\"equalNow\":false}";
        try {
            DateTime output = mapper.readValue(input, DateTime.class);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testMillisDeser() {
        Long input = 1326856906000l;
        try {
            DateTime output = mapper.readValue(input.toString(), DateTime.class);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testActivityStringDeser() {
        String output = "2013-09-18T20:52:47Z";
        DateTime input = StreamsJacksonMapper.ACTIVITY_FORMAT.parseDateTime(output);
        try {
            String result = mapper.writeValueAsString(input);
            assertEquals(result, output);
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}
