package org.apache.streams.gnip.facebook.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.gnip.api.Entry;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: rebanks
 * Date: 8/21/13
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class FacebookEDCSerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(FacebookEDCSerDeTest.class);

    XmlMapper xmlMapper = new XmlMapper();

    @Test
    public void Tests()   throws Exception
    {
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        xmlMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

        InputStream is = FacebookEDCSerDeTest.class.getResourceAsStream("/FacebookEDC.xml");
        if(is == null) System.out.println("null");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        xmlMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

        try {
            while (br.ready()) {
                String line = br.readLine();
                LOGGER.debug(line);

                Entry xmlObject = xmlMapper.readValue(line, Entry.class);

                String xml = xmlMapper.writeValueAsString(xmlObject);

            }
        } catch( Exception e ) {
            LOGGER.error(e.getMessage());
            Assert.fail();
        }
    }
}
