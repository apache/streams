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

package org.apache.streams.jackson.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.converter.ActivityConverterProcessor;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.*;

/**
 *
 */
public class ActivityConverterProcessorTest {

    private static final ObjectMapper mapper = new StreamsJacksonMapper();

    @Test
    public void testBaseActivitySerializerProcessorInvalid() {
        String INVALID_DOCUMENT = " 38Xs}";
        StreamsProcessor processor = new ActivityConverterProcessor();
        processor.prepare(null);
        StreamsDatum datum = new StreamsDatum(INVALID_DOCUMENT);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    public void testBaseActivitySerializerProcessorString() {
        String STRING_DOCUMENT = "{\"verb\":\"post\"}";
        StreamsProcessor processor = new ActivityConverterProcessor();
        processor.prepare(null);
        StreamsDatum datum = new StreamsDatum(STRING_DOCUMENT);
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
        ObjectNode OBJECT_DOCUMENT = mapper.readValue("{\"verb\":\"share\"}", ObjectNode.class);
        StreamsProcessor processor = new ActivityConverterProcessor();
        processor.prepare(null);
        StreamsDatum datum = new StreamsDatum(OBJECT_DOCUMENT);
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(1, result.size());
        StreamsDatum resultDatum = result.get(0);
        assertNotNull(resultDatum);
        assertNotNull(resultDatum.getDocument());
        assertTrue(resultDatum.getDocument() instanceof Activity);
        assertTrue(((Activity)resultDatum.getDocument()).getVerb().equals("share"));
    }

//    @Test
//    public void testTypeConverterObjectNodeToString() throws IOException {
//        final String ID = "1";
//        StreamsProcessor processor = new ActivitySerializerProcessor(ObjectNode.class, String.class, Lists.newArrayList(DATASIFT_FORMAT));
//        processor.prepare(null);
//        ObjectMapper mapper = StreamsJacksonMapper.getInstance(Lists.newArrayList(DATASIFT_FORMAT));
//        ObjectNode node = mapper.readValue(DATASIFT_JSON, ObjectNode.class);
//        StreamsDatum datum = new StreamsDatum(node, ID);
//        List<StreamsDatum> result = processor.process(datum);
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        StreamsDatum resultDatum = result.get(0);
//        assertNotNull(resultDatum);
//        assertNotNull(resultDatum.getDocument());
//        assertTrue(resultDatum.getDocument() instanceof String);
//        assertEquals(ID, resultDatum.getId());
//    }
}
