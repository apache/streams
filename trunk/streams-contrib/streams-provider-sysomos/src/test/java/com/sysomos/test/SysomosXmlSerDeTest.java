package com.sysomos.test;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.sysomos.xml.BeatApi;
import org.junit.Assert;
import org.junit.Before;
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
public class SysomosXmlSerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(SysomosXmlSerDeTest.class);

    private XmlMapper xmlMapper;

    @Before
    public void Before() {

        XmlFactory f = new XmlFactory(new InputFactoryImpl(),
                new OutputFactoryImpl());

        JacksonXmlModule module = new JacksonXmlModule();

        module.setDefaultUseWrapper(false);

        xmlMapper = new XmlMapper(f, module);

        xmlMapper
                .configure(
                        DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY,
                        Boolean.TRUE);
        xmlMapper
                .configure(
                        DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT,
                        Boolean.TRUE);
        xmlMapper
                .configure(
                        DeserializationFeature.USE_JAVA_ARRAY_FOR_JSON_ARRAY,
                        Boolean.TRUE);
        xmlMapper.configure(
                DeserializationFeature.READ_ENUMS_USING_TO_STRING,
                Boolean.TRUE);

    }

    @Test
    public void Test()
    {
        InputStream is = SysomosXmlSerDeTest.class.getResourceAsStream("/sysomos_xmls.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        try {
            while (br.ready()) {
                String line = br.readLine();
                LOGGER.debug(line);

                BeatApi ser = xmlMapper.readValue(line, BeatApi.class);

                String des = xmlMapper.writeValueAsString(ser);
                LOGGER.debug(des);
            }
        } catch( Exception e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
