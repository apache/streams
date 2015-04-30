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

package org.apache.streams.graph.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.IOUtils;
import org.apache.streams.graph.GraphHttpConfiguration;
import org.apache.streams.graph.GraphReaderConfiguration;
import org.apache.streams.graph.GraphVertexReader;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Unit test for
 * @see {@link org.apache.streams.graph.GraphVertexReader}
 *
 * Test that graph db responses can be converted to streams data
 */
public class TestNeo4jVertexReader {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestNeo4jVertexReader.class);

    private final static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    private JsonNode sampleReaderResult;

    private GraphReaderConfiguration testConfiguration;

    private GraphVertexReader graphPersistReader;

    @Before
    public void prepareTest() throws IOException {

        testConfiguration = new GraphReaderConfiguration();
        testConfiguration.setType(GraphHttpConfiguration.Type.NEO_4_J);

        graphPersistReader = new GraphVertexReader(testConfiguration);
        InputStream testActivityFileStream = TestNeo4jVertexReader.class.getClassLoader()
                .getResourceAsStream("sampleReaderResult.json");
        String sampleText = IOUtils.toString(testActivityFileStream, "utf-8");
        sampleReaderResult = mapper.readValue(sampleText, JsonNode.class);

    }

    @Test
    public void testParseNeoResult() throws IOException {

        List<ObjectNode> result = graphPersistReader.parse(sampleReaderResult);

        assert( result.size() == 10);

        for( int i = 0 ; i < 10; i++ )
            assert( result.get(i).get("extensions").size() == 5);

    }
}
