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

package org.apache.streams.plugins.test;

import org.apache.streams.plugins.hbase.StreamsHbaseGenerationConfig;
import org.apache.streams.plugins.hbase.StreamsHbaseResourceGenerator;

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
 * Test that Activity beans are compatible with the example activities in the spec.
 */
public class StreamsHbaseResourceGeneratorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsHbaseResourceGeneratorTest.class);

  public static final String[] txtFilter = new String[]{"txt"};

  /**
   * Tests that all example activities can be loaded into Activity beans.
   *
   * @throws Exception Exception
   */
  @Test
  public void testStreamsHbaseResourceGenerator() throws Exception {

    StreamsHbaseGenerationConfig config = new StreamsHbaseGenerationConfig();

    String sourceDirectory = "target/dependency/jsonschemaorg-schemas";

    config.setSourceDirectory(sourceDirectory);

    config.setTargetDirectory("target/generated-resources/hbase");

    config.setExclusions(Stream.of("attachments").collect(Collectors.toSet()));

    config.setColumnFamily("cf");
    config.setMaxDepth(2);

    StreamsHbaseResourceGenerator streamsHbaseResourceGenerator = new StreamsHbaseResourceGenerator(config);
    streamsHbaseResourceGenerator.run();

    File testOutput = config.getTargetDirectory();

    Assert.assertNotNull(testOutput);
    Assert.assertTrue(testOutput.exists());
    Assert.assertTrue(testOutput.isDirectory());

    Collection<File> outputCollection = FileUtils.listFiles(testOutput, txtFilter, true);
    Assert.assertEquals(4, outputCollection.size());
  }
}