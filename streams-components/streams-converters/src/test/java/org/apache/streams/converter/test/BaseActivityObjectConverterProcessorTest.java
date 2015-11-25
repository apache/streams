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

package org.apache.streams.converter.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.converter.ActivityObjectConverterProcessor;
import org.apache.streams.converter.ActivityObjectConverterProcessorConfiguration;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Test for
 * @see {@link ActivityObjectConverterProcessor}
 *
 * Test that default String & ObjectNode conversion works.
 */
public class BaseActivityObjectConverterProcessorTest {

    private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    private static final String ACTIVITYOBJECT_JSON = "{\"id\":\"id\",\"objectType\":\"person\"}";

    ActivityObjectConverterProcessor processor;

    @Before
    public void setup() {
        processor = new ActivityObjectConverterProcessor(new ActivityObjectConverterProcessorConfiguration());
        processor.prepare(new ActivityObjectConverterProcessorConfiguration());
    }

    @Test
    public void testBaseActivityObjectSerializerProcessorInvalid() {
        String INVALID_DOCUMENT = " 38Xs}";
        StreamsDatum datum = new StreamsDatum(INVALID_DOCUMENT);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testActivityObjectConverterProcessorString() {
        StreamsDatum datum = new StreamsDatum(ACTIVITYOBJECT_JSON);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(1, result.size());
        StreamsDatum resultDatum = result.get(0);
        assertNotNull(resultDatum);
        assertNotNull(resultDatum.getDocument());
        assertTrue(resultDatum.getDocument() instanceof ActivityObject);
        assertTrue(((ActivityObject)resultDatum.getDocument()).getObjectType().equals("person"));
    }

    @Test
    public void testBaseActivityObjectSerializerProcessorObject() throws IOException {
        ObjectNode OBJECT_DOCUMENT = mapper.readValue(ACTIVITYOBJECT_JSON, ObjectNode.class);
        StreamsDatum datum = new StreamsDatum(OBJECT_DOCUMENT);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(1, result.size());
        StreamsDatum resultDatum = result.get(0);
        assertNotNull(resultDatum);
        assertNotNull(resultDatum.getDocument());
        assertTrue(resultDatum.getDocument() instanceof ActivityObject);
        assertTrue(((ActivityObject)resultDatum.getDocument()).getObjectType().equals("person"));
    }

}
