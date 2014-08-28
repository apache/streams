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

package org.apache.streams.data.data.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

/**
 * Created by sblackmon on 3/31/14.
 */
public class DateTimeSerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(DateTimeSerDeTest.class);
    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    @Ignore
    @Test
    // this really needs to be able to pass...
    public void testActivityStringSer() {
        String input = "2012-01-17T21:21:46.000Z";
        try {
            DateTime output = mapper.readValue(input, DateTime.class);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testMillisDeser() {
        Long input = 1326856906000l;
        try {
            DateTime output = mapper.readValue(input.toString(), DateTime.class);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testActivityStringDeser() {
        String output = "2012-01-17T21:21:46.000Z";
        long inputMillis = 1326835306000L;
        DateTime input;
        try {
            input = new DateTime(inputMillis);
            //Writes out value as a String including quotes
            String result = mapper.writeValueAsString(input);
            assertEquals(result.replace("\"", ""), output);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

}
