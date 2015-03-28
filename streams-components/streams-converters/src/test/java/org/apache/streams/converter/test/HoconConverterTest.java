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
import com.google.common.collect.Lists;
import org.apache.streams.converter.HoconConverterUtil;
import org.apache.streams.converter.TypeConverterProcessor;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.*;

/**
 * Tests for
 * @see {@link HoconConverterUtil}
 */
public class HoconConverterTest {

    /**
     * Tests in-place simple conversion from String to String
     */
    @Test
    public void testHoconConverter1() {

        final String TEST_JSON_1 = "{\"race\":\"klingon\",\"gender\":\"male\"}";

        String result1 = (String) HoconConverterUtil.convert(TEST_JSON_1, String.class, "test1.conf");

        assertNotNull(result1);
        assertTrue(result1.contains("race"));
        assertTrue(result1.contains("18"));
        assertTrue(result1.contains("female"));
    }

    /**
     * Tests derived object substitution conversion from String to ObjectNode
     */
    @Test
    public void testHoconConverter2() {

        final String TEST_JSON_2 = "{\"race\":\"klingon\",\"gender\":\"male\",\"age\":18}";

        ObjectNode result2 = (ObjectNode) HoconConverterUtil.convert(TEST_JSON_2, ObjectNode.class, "test2.conf", "demographics");

        assertNotNull(result2);
        assertTrue(result2.get("race") != null);
        assertTrue(result2.get("age").asDouble() == 18);
        assertTrue(result2.get("gender").asText().equals("female"));
    }

    /**
     * Tests derived object import conversion from String to Activity
     */
    @Test
    public void testHoconConverter3() {

        final String TEST_JSON_3 = "{\"id\":\"123\",\"text\":\"buncha stuff\",\"user\":{\"name\":\"guy\"}}";

        Activity result3 = (Activity) HoconConverterUtil.convert(TEST_JSON_3, Activity.class, "test3a.conf", "activity");

        assertNotNull(result3);
        assertTrue(result3.getProvider() != null);
        assertTrue(result3.getId().equals("id:123"));
        assertTrue(result3.getContent().endsWith("stuff"));
        assertTrue(result3.getActor().getDisplayName().equals("Jorge"));

    }

}
