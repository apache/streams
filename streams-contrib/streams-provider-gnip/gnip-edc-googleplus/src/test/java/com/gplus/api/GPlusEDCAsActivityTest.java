/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.gplus.api;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.streams.pojo.json.Activity;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created with IntelliJ IDEA.
 * User: mdelaet
 * Date: 8/28/13
 * Time: 2:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class GPlusEDCAsActivityTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GPlusEDCAsActivityTest.class);

    private ObjectMapper jsonMapper;
    XmlMapper xmlMapper;
    private GPlusActivityConverter gplusSerializer;

    public GPlusEDCAsActivityTest() {
        gplusSerializer = new GPlusActivityConverter();
        jsonMapper = new ObjectMapper();
        xmlMapper = new XmlMapper();
    }

    @Test
    public void Tests()   throws Exception
    {
        InputStream is = GPlusEDCAsActivityTest.class.getResourceAsStream("/GPlusEDCFixed.json");
        if(is == null) System.out.println("null");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        xmlMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        xmlMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
        jsonMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        jsonMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        jsonMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

        try {
            while (br.ready()) {
                String line = br.readLine();

                Activity activity = null;
                try {
                    activity = gplusSerializer.deserialize(line);
                } catch( Exception e ) {
                    LOGGER.error(line);
                    e.printStackTrace();
                    Assert.fail("Exception on gplus Serializer.deserialize(jsonString) : " + e);
                }

                try {
                    String activityString = gplusSerializer.serialize(activity);
                    LOGGER.debug(activityString);
                } catch (Exception e ){
                    LOGGER.error(activity.toString());
                    e.printStackTrace();
                    Assert.fail("Exception on gplus Serializer.serialize(activity) : " + e);
                }

            }
        } catch( Exception e ) {
            System.out.println("Exception: " + e);
            LOGGER.error(e.getMessage());
            Assert.fail("");
        }
    }

}
