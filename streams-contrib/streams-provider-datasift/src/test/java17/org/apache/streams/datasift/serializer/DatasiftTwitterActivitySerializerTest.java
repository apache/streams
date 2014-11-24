package org.apache.streams.datasift.serializer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.util.StreamsDatasiftMapper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.junit.Before;
import org.junit.Test;

import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DatasiftTwitterActivitySerializerTest extends DatasiftActivitySerializerTest {

    @Before
    @Override
    public void initSerializer() {
        SERIALIZER = new DatasiftTwitterActivitySerializer();
    }

    @Test
    @Override
    public void testConversion() throws Exception {
        Scanner scanner = new Scanner(DatasiftTwitterActivitySerializerTest.class.getResourceAsStream("/twitter_datasift_json.txt"));
        String line = null;
        while(scanner.hasNextLine()) {
            line = scanner.nextLine();
            Datasift item = MAPPER.readValue(line, Datasift.class);
            testConversion(item);
            String json = MAPPER.writeValueAsString(item);
            testDeserNoNull(json);
            testDeserNoAddProps(json);
        }
    }

}
