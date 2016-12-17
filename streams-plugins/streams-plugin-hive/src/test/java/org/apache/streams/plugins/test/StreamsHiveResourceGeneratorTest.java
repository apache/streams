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

import org.apache.streams.plugins.hive.StreamsHiveGenerationConfig;
import org.apache.streams.plugins.hive.StreamsHiveResourceGenerator;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Ignore;
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
@Ignore
public class StreamsHiveResourceGeneratorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsHiveResourceGeneratorTest.class);

  public static final String[] hqlFilter = new String[]{"hql"};

  /**
   * Tests that all example activities can be loaded into Activity beans.
   *
   * @throws Exception Exception
   */
  @Test
  public void testStreamsHiveResourceGenerator() throws Exception {

    StreamsHiveGenerationConfig config = new StreamsHiveGenerationConfig();

    String sourceDirectory = "target/test-classes/activitystreams-schemas";

    config.setSourceDirectory(sourceDirectory);

    config.setTargetDirectory("target/generated-resources/test");

    config.setExclusions(Stream.of("attachments").collect(Collectors.toSet()));

    config.setMaxDepth(2);

    StreamsHiveResourceGenerator streamsHiveResourceGenerator = new StreamsHiveResourceGenerator(config);
    streamsHiveResourceGenerator.run();

    File testOutput = config.getTargetDirectory();

    Assert.assertNotNull(testOutput);
    Assert.assertTrue(testOutput.exists());
    Assert.assertTrue(testOutput.isDirectory());

    Collection<File> testOutputFiles = FileUtils.listFiles(testOutput, hqlFilter, true);
    Assert.assertEquals(testOutputFiles.size(), 133);
  }
}