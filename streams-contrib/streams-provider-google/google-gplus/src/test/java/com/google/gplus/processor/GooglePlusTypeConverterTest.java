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

package com.google.gplus.processor;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.api.services.plus.model.Person;
import com.google.gplus.serializer.util.GPlusActivityDeserializer;
import com.google.gplus.serializer.util.GPlusPersonDeserializer;
import com.google.gplus.serializer.util.GooglePlusActivityUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class GooglePlusTypeConverterTest {
    private final static Logger LOGGER = LoggerFactory.getLogger(GooglePlusTypeConverterTest.class);
    private GooglePlusTypeConverter googlePlusTypeConverter;
    private ObjectMapper objectMapper;

    @Before
    public void setup() {
        objectMapper = new StreamsJacksonMapper();
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addDeserializer(Person.class, new GPlusPersonDeserializer());
        simpleModule.addDeserializer(com.google.api.services.plus.model.Activity.class, new GPlusActivityDeserializer());
        objectMapper.registerModule(simpleModule);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        googlePlusTypeConverter = new GooglePlusTypeConverter();
        googlePlusTypeConverter.prepare(null);
    }

    @Test
    public void testProcessPerson() throws IOException, ActivitySerializerException {
        InputStream is = GooglePlusTypeConverterTest.class.getResourceAsStream("/google_plus_person_jsons.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        while (br.ready()) {
            String line = br.readLine();
            if (!StringUtils.isEmpty(line)) {
                LOGGER.info("raw: {}", line);
                Activity activity = new Activity();

                Person person = objectMapper.readValue(line, Person.class);
                StreamsDatum streamsDatum = new StreamsDatum(person);

                assertNotNull(streamsDatum.getDocument());

                List<StreamsDatum> retList = googlePlusTypeConverter.process(streamsDatum);
                GooglePlusActivityUtil.updateActivity(person, activity);

                assertEquals(retList.size(), 1);
                assert(retList.get(0).getDocument() instanceof Activity);
                assertEquals(activity, retList.get(0).getDocument());
            }
        }
    }

    @Test
    public void testProcessActivity() throws IOException, ActivitySerializerException{
        InputStream is = GooglePlusTypeConverterTest.class.getResourceAsStream("/google_plus_activity_jsons.txt");
        InputStreamReader isr = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(isr);

        while (br.ready()) {
            String line = br.readLine();
            if (!StringUtils.isEmpty(line)) {
                LOGGER.info("raw: {}", line);
                Activity activity = new Activity();

                com.google.api.services.plus.model.Activity gPlusActivity = objectMapper.readValue(line, com.google.api.services.plus.model.Activity.class);
                StreamsDatum streamsDatum = new StreamsDatum(gPlusActivity);

                assertNotNull(streamsDatum.getDocument());

                List<StreamsDatum> retList = googlePlusTypeConverter.process(streamsDatum);
                GooglePlusActivityUtil.updateActivity(gPlusActivity, activity);

                assertEquals(retList.size(), 1);
                assert(retList.get(0).getDocument() instanceof Activity);
                assertEquals(activity, retList.get(0).getDocument());
            }
        }
    }

    @Test
    public void testEmptyProcess() {
        List<StreamsDatum> retList = googlePlusTypeConverter.process(null);

        assertEquals(retList.size(), 0);
    }
}