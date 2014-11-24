package org.apache.streams.datasift.serializer;

import org.apache.streams.datasift.Datasift;
import org.junit.Before;
import org.junit.Test;

import java.util.Scanner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class DatasiftTwitterActivityConverterTest extends DatasiftActivityConverterTest {

    @Before
    @Override
    public void initSerializer() {
        SERIALIZER = new DatasiftTwitterActivityConverter();
    }

    @Test
    @Override
    public void testConversion() throws Exception {
        Scanner scanner = new Scanner(DatasiftTwitterActivityConverterTest.class.getResourceAsStream("/twitter_datasift_json.txt"));
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
