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

package org.apache.streams.elasticsearch.test;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.processor.DocumentToMetadataProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestDocumentToMetadataProcessor {

  private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  @Before
  public void prepareTest() {

  }

  @Test
  public void testSerializability() {
    DocumentToMetadataProcessor processor = new DocumentToMetadataProcessor();

    DocumentToMetadataProcessor clone = (DocumentToMetadataProcessor) SerializationUtils.clone(processor);
  }

  @Test
  public void testDocumentToMetadataProcessor() {

    ObjectNode document = MAPPER.createObjectNode()
        .put("a", "a")
        .put("b", "b")
        .put("c", 6);

    DocumentToMetadataProcessor processor = new DocumentToMetadataProcessor();

    StreamsDatum testInput = new StreamsDatum(document);

    Assert.assertNotNull(testInput.document);
    Assert.assertNotNull(testInput.metadata);
    Assert.assertEquals(testInput.metadata.size(), 0);

    processor.prepare(null);

    StreamsDatum testOutput = processor.process(testInput).get(0);

    processor.cleanUp();

    Assert.assertNotNull(testOutput.metadata);
    Assert.assertEquals(testInput.metadata.size(), 3);

  }
}
