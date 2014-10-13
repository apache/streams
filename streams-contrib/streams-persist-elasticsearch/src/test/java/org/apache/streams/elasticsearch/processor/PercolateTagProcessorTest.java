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

package org.apache.streams.elasticsearch.processor;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PercolateTagProcessorTest {
    private final String id = "test_id";
    private final String query = "test_query";
    private final String defaultPercolateField = "activity.content";

    private final String expectedResults = "{ \n" +
            "\"query\" : {\n" +
            "  \"query_string\" : {\n" +
            "    \"query\" : \"test_query\",\n" +
            "    \"default_field\" : \"activity.content\"\n" +
            "  }\n" +
            "}\n" +
            "}";

    @Test
    public void percolateTagProcessorQueryBuilderTest() {
        PercolateTagProcessor.PercolateQueryBuilder percolateQueryBuilder = new PercolateTagProcessor.PercolateQueryBuilder(id, query, defaultPercolateField);

        assertEquals(id, percolateQueryBuilder.getId());
        assertEquals(expectedResults, percolateQueryBuilder.getSource());
    }
}
