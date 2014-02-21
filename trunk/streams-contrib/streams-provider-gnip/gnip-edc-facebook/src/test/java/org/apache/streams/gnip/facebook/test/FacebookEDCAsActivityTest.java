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

package org.apache.streams.gnip.facebook.test;

//import org.codehaus.jackson.map.ObjectMapper;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.gnip.powertrack.GnipActivityFixer;
import org.apache.streams.pojo.json.Activity;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

//import com.fasterxml.jackson.xml.XmlMapper;
//import com.gnip.xmlpojo.generated.FacebookEDC;

/**
 * Created with IntelliJ IDEA.
 * User: rebanks
 * Date: 8/21/13
 * Time: 11:53 AM
 * To change this template use File | Settings | File Templates.
 */
public class FacebookEDCAsActivityTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(FacebookEDCAsActivityTest.class);

    private ObjectMapper jsonMapper = new ObjectMapper();
    XmlMapper xmlMapper = new XmlMapper();

    @Ignore
    @Test
    public void Tests()   throws Exception
    {
        InputStream is = FacebookEDCAsActivityTest.class.getResourceAsStream("/FacebookEDC.xml");
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
                if(!StringUtils.isEmpty(line))
                {
                    LOGGER.debug(line);
                    Object activityObject = xmlMapper.readValue(line, Object.class);

                    String jsonString = jsonMapper.writeValueAsString(activityObject);

                    JSONObject jsonObject = new JSONObject(jsonString);

                    JSONObject fixedObject = GnipActivityFixer.fix(jsonObject);

                    Activity activity = null;
                    try {
                        activity = jsonMapper.readValue(fixedObject.toString(), Activity.class);
                    } catch( Exception e ) {
                        LOGGER.error(jsonObject.toString());
                        LOGGER.error(fixedObject.toString());
                        e.printStackTrace();
                        Assert.fail();
                    }
                    //LOGGER.info(activity);
                }
            }
        } catch( Exception e ) {
            LOGGER.error(e.getMessage());
            Assert.fail();
        }
    }
}
