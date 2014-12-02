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
import org.apache.streams.converter.ActivityConverterProcessor;
import org.apache.streams.converter.ActivityConverterProcessorConfiguration;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Test for
 * @see {@link org.apache.streams.converter.ActivityConverterProcessor}
 *
 * Test that default String & ObjectNode conversion works.
 */
public class BaseActivityConverterProcessorTest {

    private static final ObjectMapper mapper = new StreamsJacksonMapper();

    private static final String ACTIVITY_JSON = "{\"id\":\"id\",\"published\":\"Tue Jan 17 21:21:46 Z 2012\",\"verb\":\"post\",\"provider\":{\"id\":\"providerid\"}}";

    ActivityConverterProcessor processor;

    @Before
    public void setup() {
        processor = new ActivityConverterProcessor(new ActivityConverterProcessorConfiguration());
        processor.prepare(new ActivityConverterProcessorConfiguration());
    }

    @Test
    public void testBaseActivitySerializerProcessorInvalid() {
        String INVALID_DOCUMENT = " 38Xs}";
        StreamsDatum datum = new StreamsDatum(INVALID_DOCUMENT);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testActivityConverterProcessorString() {
        StreamsDatum datum = new StreamsDatum(ACTIVITY_JSON);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(1, result.size());
        StreamsDatum resultDatum = result.get(0);
        assertNotNull(resultDatum);
        assertNotNull(resultDatum.getDocument());
        assertTrue(resultDatum.getDocument() instanceof Activity);
        assertTrue(((Activity)resultDatum.getDocument()).getVerb().equals("post"));
    }

    @Test
    public void testBaseActivitySerializerProcessorObject() throws IOException {
        ObjectNode OBJECT_DOCUMENT = mapper.readValue(ACTIVITY_JSON, ObjectNode.class);
        StreamsDatum datum = new StreamsDatum(OBJECT_DOCUMENT);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(1, result.size());
        StreamsDatum resultDatum = result.get(0);
        assertNotNull(resultDatum);
        assertNotNull(resultDatum.getDocument());
        assertTrue(resultDatum.getDocument() instanceof Activity);
        assertTrue(((Activity)resultDatum.getDocument()).getVerb().equals("post"));
    }

}
