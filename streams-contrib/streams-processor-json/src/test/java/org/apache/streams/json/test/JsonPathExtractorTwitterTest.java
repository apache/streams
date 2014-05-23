/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.json.test;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.FileUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.json.JsonPathExtractor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test for extracting json fields and
 * objects from datums using JsonPath syntax
 */
public class JsonPathExtractorTwitterTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(JsonPathExtractorTwitterTest.class);

    private String testJson;

    @Before
    public void initialize() {
        try {
            testJson = FileUtils.readFileToString(new File("src/test/resources/tweet.json"));
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void test1()
    {
        JsonPathExtractor extractor = new JsonPathExtractor();
        extractor.prepare("$.user.id_str");
        List<StreamsDatum> result = extractor.process(new StreamsDatum(testJson));
        assertThat(result.size(), is(1));
        assertTrue(result.get(0).getDocument() instanceof String);
        assertThat((String)result.get(0).getDocument(), is("3971941"));
    }

    @Test
    public void test2()
    {
        JsonPathExtractor extractor = new JsonPathExtractor();
        extractor.prepare("$.created_at");
        List<StreamsDatum> result = extractor.process(new StreamsDatum(testJson));
        assertThat(result.size(), is(1));
        assertTrue(result.get(0).getDocument() instanceof String);
        assertThat((String)result.get(0).getDocument(), is("Thu May 08 15:58:59 +0000 2014"));
    }

    @Test
    public void test3()
    {
        JsonPathExtractor extractor = new JsonPathExtractor();
        extractor.prepare("$.user");
        List<StreamsDatum> result = extractor.process(new StreamsDatum(testJson));
        assertThat(result.size(), is(1));
        assertTrue(result.get(0).getDocument() instanceof ObjectNode);
    }

}
