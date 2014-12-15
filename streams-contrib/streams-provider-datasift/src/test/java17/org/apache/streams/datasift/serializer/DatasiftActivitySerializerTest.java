package org.apache.streams.datasift.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.util.StreamsDatasiftMapper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.util.files.StreamsScannerUtil;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.util.Scanner;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DatasiftActivitySerializerTest {

    protected ActivitySerializer SERIALIZER;

    protected static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance(Lists.newArrayList(StreamsDatasiftMapper.DATASIFT_FORMAT));

    @Before
    public void initSerializer() {
        SERIALIZER = new DatasiftActivitySerializer();
    }

    @Test
    public void testConversion() throws Exception {

        Scanner scanner = StreamsScannerUtil.getInstance("/rand_sample_datasift_json.txt");

        String line = null;
        while(scanner.hasNextLine()) {
            try {
                line = scanner.nextLine();
                Datasift item = MAPPER.readValue(line, Datasift.class);
                testConversion(item);
                String json = MAPPER.writeValueAsString(item);
                testDeserNoNull(json);
                testDeserNoAddProps(json);
            } catch (Exception e) {
                System.err.println(line);
                throw e;
            }
        }
    }

    /**
     * Test that the minimum number of things that an activity has
     * @param item
     */
    protected void testConversion(Datasift item) throws Exception {
        Activity activity = SERIALIZER.deserialize(item);
        assertNotNull("activity.id", activity.getId());
        assertNotNull("activity.published", activity.getPublished());
        assertNotNull("activity.provider", activity.getProvider());
        assertNotNull("activity.urls", activity.getUrl());
        assertNotNull("activity.verb", activity.getVerb());
        Actor actor = activity.getActor();
        assertNotNull("activity.actor", actor);
    }

    /**
     * Test that null fields are not present
     * @param json
     */
    protected void testDeserNoNull(String json) throws Exception {
        int nulls = StringUtils.countMatches(json, ":null");
        assertEquals(0l, (long)nulls);

    }

    /**
     * Test that null fields are not present
     * @param json
     */
    protected void testDeserNoAddProps(String json) throws Exception {
        int nulls = StringUtils.countMatches(json, "additionalProperties:{");
        assertEquals(0l, (long)nulls);

    }

}
