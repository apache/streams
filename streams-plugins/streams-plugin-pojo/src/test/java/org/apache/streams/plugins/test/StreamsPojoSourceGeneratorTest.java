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

import org.apache.streams.plugins.StreamsPojoGenerationConfig;
import org.apache.streams.plugins.StreamsPojoSourceGenerator;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Tests that StreamsPojoSourceGenerator via SDK generates java sources.
 *
 */
public class StreamsPojoSourceGeneratorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsPojoSourceGeneratorTest.class);

  public static final String[] javaFilter = new String[]{"java"};

  /**
   * Tests that StreamsPojoSourceGenerator via SDK generates pig resources.
   *
   * @throws Exception Exception
   */
  @Test
  public void testStreamsPojoSourceGenerator() throws Exception {

    StreamsPojoGenerationConfig config = new StreamsPojoGenerationConfig();

    List<String> sourcePaths = Stream.of(
        "target/dependency/jsonschemaorg-schemas"
    ).collect(Collectors.toList());
    config.setSourcePaths(sourcePaths);

    config.setTargetPackage("org.apache.streams.jsonschema.pojo");
    config.setTargetDirectory("target/generated-sources/pojo");

    StreamsPojoSourceGenerator streamsPojoSourceGenerator = new StreamsPojoSourceGenerator(config);
    streamsPojoSourceGenerator.run();

    Assert.assertNotNull(config.getTargetDirectory());
    Assert.assertTrue(config.getTargetDirectory().exists());
    Assert.assertTrue(config.getTargetDirectory().isDirectory());

    Collection<File> targetFiles = FileUtils.listFiles(config.getTargetDirectory(), javaFilter, true);
    Assert.assertTrue(targetFiles.size() == 7);
  }
}