package com.datasift.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streams.datasift.Datasift;
import org.apache.streams.datasift.serializer.StreamsDatasiftMapper;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: sblackmon
 * Date: 8/20/13
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class DatasiftSerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(DatasiftSerDeTest.class);

    private ObjectMapper mapper = StreamsDatasiftMapper.getInstance();

    @Test
    public void Tests()
    {
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.TRUE);

        InputStream is = DatasiftSerDeTest.class.getResourceAsStream("/part-r-00000.json");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        int linenumber = 1;

        try {
            while (br.ready()) {
                String line = br.readLine();
                LOGGER.debug(line);

                Datasift ser = mapper.readValue(line, Datasift.class);

                String de = mapper.writeValueAsString(ser);

                LOGGER.debug(de);

                linenumber++;
            }
        } catch( Exception e ) {
            LOGGER.error("Failed on line: {}", + linenumber);
            e.printStackTrace();
            Assert.fail();
        }
    }
}
