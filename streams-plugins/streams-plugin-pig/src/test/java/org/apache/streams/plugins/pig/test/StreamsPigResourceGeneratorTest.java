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

package org.apache.streams.plugins.pig.test;

import org.apache.streams.plugins.pig.StreamsPigGenerationConfig;
import org.apache.streams.plugins.pig.StreamsPigResourceGenerator;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import java.io.File;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test that Activity beans are compatible with the example activities in the spec.
 */
public class StreamsPigResourceGeneratorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsPigResourceGeneratorTest.class);

  public static final String[] pigFilter = new String[]{"pig"};

  /**
   * Tests that StreamsPigResourceGenerator via SDK generates pig resources.
   *
   * @throws Exception Exception
   */
  @Test
  public void testStreamsPigResourceGenerator() throws Exception {

    StreamsPigGenerationConfig config = new StreamsPigGenerationConfig();

    String sourceDirectory = "target/dependency/activitystreams-schemas";

    config.setSourceDirectory(sourceDirectory);

    config.setTargetDirectory("target/generated-resources/pig");

    config.setExclusions(Stream.of("attachments").collect(Collectors.toSet()));

    config.setMaxDepth(2);

    StreamsPigResourceGenerator streamsPigResourceGenerator = new StreamsPigResourceGenerator(config);
    streamsPigResourceGenerator.run();

    File testOutput = config.getTargetDirectory();

    Assert.assertNotNull(testOutput);
    Assert.assertTrue(testOutput.exists());
    Assert.assertTrue(testOutput.isDirectory());

    Collection<File> outputCollection = FileUtils.listFiles(testOutput, pigFilter, true);
    Assert.assertEquals(outputCollection.size(), 133);
  }
}