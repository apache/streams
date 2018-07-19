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

package org.apache.streams.plugins.elasticsearch.test;

import org.apache.streams.plugins.elasticsearch.StreamsElasticsearchGenerationConfig;
import org.apache.streams.plugins.elasticsearch.StreamsElasticsearchResourceGenerator;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test that Elasticsearch resources are generated.
 */
public class StreamsElasticsearchResourceGeneratorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsElasticsearchResourceGeneratorTest.class);

  public static final String[] jsonFilter = new String[]{"json"};

  /**
   * Test that Elasticsearch resources are generated.
   *
   * @throws Exception Exception
   */
  @Test
  public void StreamsElasticsearchResourceGenerator() throws Exception {

    StreamsElasticsearchGenerationConfig config = new StreamsElasticsearchGenerationConfig();

    String sourceDirectory = "target/dependency/jsonschemaorg-schemas";

    config.setSourceDirectory(sourceDirectory);

    config.setTargetDirectory("target/generated-resources/elasticsearch");

    config.setExclusions(Stream.of("attachments").collect(Collectors.toSet()));

    config.setMaxDepth(2);

    StreamsElasticsearchResourceGenerator streamsElasticsearchResourceGenerator = new StreamsElasticsearchResourceGenerator(config);
    streamsElasticsearchResourceGenerator.run();

    File testOutput = config.getTargetDirectory();

    Assert.assertNotNull(testOutput);
    Assert.assertTrue(testOutput.exists());
    Assert.assertTrue(testOutput.isDirectory());

    Collection<File> outputCollection = FileUtils.listFiles(testOutput, jsonFilter, true);
    Assert.assertEquals(4, outputCollection.size());
  }
}