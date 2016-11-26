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

import org.apache.streams.converter.HoconConverterProcessor;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link HoconConverterProcessor}
 */
public class HoconConverterProcessorTest {

    /**
     * Tests in-place simple conversion from String to String
     */
    @Test
    public void testHoconConverter1() {

        final String TEST_JSON_1 = "{\"race\":\"klingon\",\"gender\":\"male\"}";

        StreamsProcessor processor = new HoconConverterProcessor(String.class, "test1.conf", null, null);
        processor.prepare(null);
        StreamsDatum datum = new StreamsDatum(TEST_JSON_1, "1");
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(1, result.size());
        StreamsDatum resultDatum = result.get(0);
        assertTrue(resultDatum.getDocument() instanceof String);
        String result1 = (String) resultDatum.getDocument();
        assertTrue(result1.contains("race"));
        assertTrue(result1.contains("18"));
        assertTrue(result1.contains("female"));
    }

    /**
     * Tests derived object substitution conversion from String to ObjectNode
     */
    @Test
    public void testHoconConverter2() {

        final String TEST_ID_2 = "2";
        final String TEST_JSON_2 = "{\"race\":\"klingon\",\"gender\":\"male\",\"age\":18}";

        StreamsProcessor processor = new HoconConverterProcessor(ObjectNode.class, "test2.conf", null, "demographics");
        processor.prepare(null);
        StreamsDatum datum = new StreamsDatum(TEST_JSON_2, TEST_ID_2);
        List<StreamsDatum> result2 = processor.process(datum);
        assertNotNull(result2);
        assertEquals(1, result2.size());
        StreamsDatum resultDatum = result2.get(0);
        assertTrue(resultDatum.getDocument() instanceof ObjectNode);
        assertTrue(resultDatum.getId().equals(TEST_ID_2));
        ObjectNode resultDoc = (ObjectNode) resultDatum.getDocument();
        assertNotNull(resultDoc);
        assertTrue(resultDoc.get("race") != null);
        assertTrue(resultDoc.get("age").asDouble() == 18);
        assertTrue(resultDoc.get("gender").asText().equals("female"));
    }

    /**
     * Tests derived object import conversion from String to Activity
     */
    @Test
    public void testHoconConverter3() {

        final String TEST_JSON_3 = "{\"id\":\"123\",\"text\":\"buncha stuff\",\"user\":{\"name\":\"guy\"}}";

        StreamsProcessor processor = new HoconConverterProcessor(Activity.class, "test3a.conf", null, "activity");
        processor.prepare(null);
        StreamsDatum datum = new StreamsDatum(TEST_JSON_3, "3");
        List<StreamsDatum> result = processor.process(datum);
        assertNotNull(result);
        assertEquals(1, result.size());
        StreamsDatum resultDatum = result.get(0);
        assertTrue(resultDatum.getDocument() instanceof Activity);
        Activity result3 = (Activity) resultDatum.getDocument();
        assertNotNull(result3);
        assertTrue(result3.getProvider() != null);
        assertTrue(result3.getId().equals("id:123"));
        assertTrue(result3.getContent().endsWith("stuff"));
        assertTrue(result3.getActor().getDisplayName().equals("Jorge"));

    }

}
