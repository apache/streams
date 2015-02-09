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
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.joda.time.DateTime;
import org.junit.Assert;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Testing {@link org.apache.streams.jackson.StreamsJacksonMapper} ability to bind
 * custom DateTime formats.
 */
public class CustomDateTimeFormatTest {

    @Test
    public void testCustomDateTimeFormatExplicit() {
        String format = "EEE MMM dd HH:mm:ss Z yyyy";
        String input = "Tue Jan 17 21:21:46 Z 2012";
        Long outputMillis = 1326835306000L;
        ObjectMapper mapper = StreamsJacksonMapper.getInstance(format);
        DateTime time;
        try {
            String json = "{\"published\":\"" + input + "\"}";
            Activity activity = mapper.readValue(json, Activity.class);

            //Writes out value as a String including quotes
            Long result = activity.getPublished().getMillis();

            assertEquals(result, outputMillis);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void testCustomDateTimeFormatReflection() {
        String input = "Tue Jan 17 21:21:46 Z 2012";
        Long outputMillis = 1326835306000L;
        ObjectMapper mapper = StreamsJacksonMapper.getInstance();
        DateTime time;
        try {
            String json = "{\"published\":\"" + input + "\"}";
            Activity activity = mapper.readValue(json, Activity.class);

            //Writes out value as a String including quotes
            Long result = activity.getPublished().getMillis();

            assertEquals(result, outputMillis);
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }


}
