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

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.ElasticsearchClientManager;
import org.apache.streams.elasticsearch.ElasticsearchReaderConfiguration;
import org.apache.streams.elasticsearch.processor.DatumFromMetadataProcessor;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.apache.commons.lang.SerializationUtils;
import org.elasticsearch.client.Client;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.assertTrue;

/**
 * Integration Test for
 * @see org.apache.streams.elasticsearch.processor.DatumFromMetadataProcessor
 */
@Test
public class DatumFromMetadataProcessorIT {

  private ElasticsearchReaderConfiguration testConfiguration;
  protected Client testClient;

  @Test
  public void testSerializability() {
    DatumFromMetadataProcessor processor = new DatumFromMetadataProcessor(testConfiguration);

    DatumFromMetadataProcessor clone = (DatumFromMetadataProcessor) SerializationUtils.clone(processor);
  }

  @BeforeClass
  public void prepareTestDatumFromMetadataProcessor() throws Exception {

    testConfiguration = new ComponentConfigurator<>(ElasticsearchReaderConfiguration.class).detectConfiguration( "DatumFromMetadataProcessorIT");
    testClient = ElasticsearchClientManager.getInstance(testConfiguration).client();

  }

  @Test()
  public void testDatumFromMetadataProcessor() {

    Map<String, Object> metadata = new HashMap<>();

    metadata.put("index", testConfiguration.getIndexes().get(0));
    metadata.put("type", testConfiguration.getTypes().get(0));
    metadata.put("id", "post");

    DatumFromMetadataProcessor processor = new DatumFromMetadataProcessor(testConfiguration);

    StreamsDatum testInput = new StreamsDatum(null);

    testInput.setMetadata(metadata);

    Assert.assertNull(testInput.document);

    processor.prepare(null);

    StreamsDatum testOutput = processor.process(testInput).get(0);

    processor.cleanUp();

    Assert.assertNotNull(testOutput.document);

  }
}
