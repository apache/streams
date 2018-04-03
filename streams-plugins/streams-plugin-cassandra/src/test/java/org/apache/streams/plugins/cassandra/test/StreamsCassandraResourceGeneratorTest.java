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

package org.apache.streams.plugins.cassandra.test;

import org.apache.streams.plugins.cassandra.StreamsCassandraGenerationConfig;
import org.apache.streams.plugins.cassandra.StreamsCassandraResourceGenerator;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Test that cassandra resources are generated.
 */
public class StreamsCassandraResourceGeneratorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsCassandraResourceGeneratorTest.class);

  public static final String[] cqlFilter = new String[]{"cql"};

  /**
   * Test that cassandra resources are generated.
   *
   * @throws Exception Exception
   */
  @Test
  public void testStreamsCassandraResourceGenerator() throws Exception {

    StreamsCassandraGenerationConfig config = new StreamsCassandraGenerationConfig();

    String sourceDirectory = "target/dependency/activitystreams-schemas";

    config.setSourceDirectory(sourceDirectory);

    config.setTargetDirectory("target/generated-resources/cassandra");

    config.setExclusions(Stream.of("attachments").collect(Collectors.toSet()));

    config.setMaxDepth(2);

    StreamsCassandraResourceGenerator streamsCassandraResourceGenerator = new StreamsCassandraResourceGenerator(config);
    streamsCassandraResourceGenerator.run();

    File testOutput = config.getTargetDirectory();

    Assert.assertNotNull(testOutput);
    Assert.assertTrue(testOutput.exists());
    Assert.assertTrue(testOutput.isDirectory());

    Collection<File> outputCollection = FileUtils.listFiles(testOutput, cqlFilter, true);
    Assert.assertEquals(outputCollection.size(), 1);

    Path path = Paths.get(testOutput.getAbsolutePath()).resolve("types.cql");

    Assert.assertTrue(path.toFile().exists());

    String typesCqlBytes = new String(java.nio.file.Files.readAllBytes(path));

    Assert.assertEquals(StringUtils.countMatches(typesCqlBytes, "CREATE TYPE"), 133);

  }
}