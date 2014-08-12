package org.apache.streams.datasift.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.datasift.util.StreamsDatasiftMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.junit.Test;

import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DatasiftActivitySerializerTest {

    private static final DatasiftActivitySerializer SERIALIZER = new DatasiftActivitySerializer();
    private static final ObjectMapper MAPPER = StreamsDatasiftMapper.getInstance();

    @Test
    public void testGeneralConversion() throws Exception {
        Scanner scanner = new Scanner(DatasiftActivitySerializerTest.class.getResourceAsStream("/rand_sample_datasift_json.txt"));
        String line = null;
        while(scanner.hasNextLine()) {
            try {
                line = scanner.nextLine();
                testGeneralConversion(line);
            } catch (Exception e) {
                System.err.println(line);
                throw e;
            }
        }
    }

    @Test
    public void testTwitterConversion() throws Exception {
        Scanner scanner = new Scanner(DatasiftActivitySerializerTest.class.getResourceAsStream("/twitter_datasift_json.txt"));
        String line = null;
        while(scanner.hasNextLine()) {
            line = scanner.nextLine();
            testGeneralConversion(line);
            testDeserNoNull(line);
            testDeserNoAddProps(line);
            System.out.println("ORIGINAL -> "+line);
            System.out.println("ACTIVITY -> "+MAPPER.writeValueAsString(SERIALIZER.deserialize(line)));
            System.out.println("NODE     -> "+MAPPER.convertValue(SERIALIZER.deserialize(line), JsonNode.class));
        }
    }

    @Test
    public void testInstagramConversion() throws Exception {
        Scanner scanner = new Scanner(DatasiftActivitySerializerTest.class.getResourceAsStream("/instagram_datasift_json.txt"));
        String line = null;
        while(scanner.hasNextLine()) {
            line = scanner.nextLine();
            testGeneralConversion(line);
            System.out.println("ORIGINAL -> "+line);
            System.out.println("ACTIVITY -> "+MAPPER.writeValueAsString(SERIALIZER.deserialize(line)));
            System.out.println("NODE     -> "+MAPPER.convertValue(SERIALIZER.deserialize(line), JsonNode.class));
        }
    }

    /**
     * Test that the minimum number of things that an activity has
     * @param json
     */
    private void testGeneralConversion(String json) throws Exception {
        Activity activity = SERIALIZER.deserialize(json);
        assertNotNull(json, activity.getId());
        assertNotNull(json, activity.getPublished());
        assertNotNull(json, activity.getProvider());
        assertNotNull(json, activity.getUrl());
        assertNotNull(json, activity.getVerb());
        Actor actor = activity.getActor();
        assertNotNull(json, actor);

    }

    /**
     * Test that null fields are not present
     * @param json
     */
    private void testDeserNoNull(String json) throws Exception {
        Activity ser = SERIALIZER.deserialize(json);
        String deser = MAPPER.writeValueAsString(ser);
        int nulls = StringUtils.countMatches(deser, ":null");
        assertEquals(0l, (long)nulls);

    }

    /**
     * Test that null fields are not present
     * @param json
     */
    private void testDeserNoAddProps(String json) throws Exception {
        Activity ser = SERIALIZER.deserialize(json);
        String deser = MAPPER.writeValueAsString(ser);
        int nulls = StringUtils.countMatches(deser, "additionalProperties:{");
        assertEquals(0l, (long)nulls);

    }

}
