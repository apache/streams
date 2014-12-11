package org.apache.streams.datasift.serializer;

import org.apache.streams.datasift.Datasift;
import org.apache.streams.util.files.StreamsScannerUtil;
import org.junit.Before;
import org.junit.Test;

import java.util.Scanner;

public class DatasiftTwitterActivitySerializerTest extends DatasiftActivitySerializerTest {

    @Before
    @Override
    public void initSerializer() {
        SERIALIZER = new DatasiftTwitterActivitySerializer();
    }

    @Test
    @Override
    public void testConversion() throws Exception {

        Scanner scanner = StreamsScannerUtil.getInstance("/twitter_datasift_json.txt");

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
