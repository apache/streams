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

import java.io.InputStream;
import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DatasiftInteractionActivitySerializerTest extends DatasiftActivitySerializerTest {

    @Before
    @Override
    public void initSerializer() {
        SERIALIZER = new DatasiftInteractionActivitySerializer();
    }

    @Test
    @Override
    public void testConversion() throws Exception {

        InputStream testFileStream = DatasiftInteractionActivitySerializerTest.class.getResourceAsStream("/rand_sample_datasift_json.txt");
        Scanner scanner = new Scanner(testFileStream, "UTF-8").useDelimiter(newLinePattern);

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

}
