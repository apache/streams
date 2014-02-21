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

package org.apache.streams.pojo.json.test;

import com.google.common.base.Joiner;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.apache.streams.pojo.json.ActivityExtended;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Extensions;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: sblackmon
 * Date: 8/20/13
 * Time: 5:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class ActivityExtendedSerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ActivityExtendedSerDeTest.class);

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    public void TestActivity()
    {
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

        InputStream is = ActivityExtendedSerDeTest.class.getResourceAsStream("/gnip_twitter_extended.json");
        Joiner joiner = Joiner.on(" ").skipNulls();
        is = new BoundedInputStream(is, 10000);
        String json;
        try {
            json = joiner.join(IOUtils.readLines(is));
            LOGGER.debug(json);

            Activity ser = mapper.readValue(json, Activity.class);

            String des = mapper.writeValueAsString(ser);
            LOGGER.debug(des);

        } catch( Exception e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Ignore
    @Test
    public void TestActivityExtended()
    {
        mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        mapper.configure(DeserializationConfig.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        mapper.configure(DeserializationConfig.Feature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);

        InputStream is = ActivityExtendedSerDeTest.class.getResourceAsStream("/gnip_twitter_extended.json");
        Joiner joiner = Joiner.on(" ").skipNulls();
        is = new BoundedInputStream(is, 10000);
        String json;
        try {
            json = joiner.join(IOUtils.readLines(is));
            LOGGER.debug(json);

            ActivityExtended ser = mapper.readValue(json, ActivityExtended.class);

            Extensions extensions = ser.getExtensions();

            String des = mapper.writeValueAsString(extensions);

            Assert.assertTrue(extensions.getAdditionalProperties().size() > 0);
            LOGGER.debug(des);

        } catch( Exception e ) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
