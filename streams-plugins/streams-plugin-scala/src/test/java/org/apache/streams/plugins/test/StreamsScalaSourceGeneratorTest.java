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

import org.apache.streams.plugins.StreamsScalaGenerationConfig;
import org.apache.streams.plugins.StreamsScalaSourceGenerator;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests that StreamsScalaSourceGenerator via SDK generates scala sources.
 */
public class StreamsScalaSourceGeneratorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsScalaSourceGeneratorTest.class);

  public static final String[] scalaFilter = new String[]{"scala"};

  /**
   * Tests that StreamsScalaSourceGenerator via SDK generates scala sources.
   *
   * @throws Exception Exception
   */
  @Ignore("until find a better way to test it")
  @Test
  public void testStreamsScalaSourceGenerator() throws Exception {

    StreamsScalaGenerationConfig streamsScalaGenerationConfig = new StreamsScalaGenerationConfig();
    streamsScalaGenerationConfig.setSourcePackages(Stream.of("org.apache.streams.pojo.json").collect(Collectors.toList()));
    streamsScalaGenerationConfig.setTargetPackage("org.apache.streams.scala");
    streamsScalaGenerationConfig.setTargetDirectory("target/generated-sources/scala-test");

    StreamsScalaSourceGenerator streamsScalaSourceGenerator = new StreamsScalaSourceGenerator(streamsScalaGenerationConfig);
    streamsScalaSourceGenerator.run();

    File testOutput = new File( "./target/generated-sources/scala-test/org/apache/streams/scala");
    FileFilter scalaFilter = pathname -> pathname.getName().endsWith(".scala");

    assertNotNull( testOutput );
    assertTrue( testOutput.exists() );
    assertTrue( testOutput.isDirectory() );
    assertEquals( 17, testOutput.listFiles(scalaFilter).length );
    assertTrue( new File(testOutput + "/traits").exists() );
    assertTrue( new File(testOutput + "/traits").isDirectory() );
    assertNotNull( new File(testOutput + "/traits").listFiles(scalaFilter) );
    assertEquals( 4, new File(testOutput + "/traits").listFiles(scalaFilter).length );
    assertTrue( new File(testOutput + "/objectTypes").exists() );
    assertTrue( new File(testOutput + "/objectTypes").isDirectory() );
    assertNotNull( new File(testOutput + "/objectTypes").listFiles(scalaFilter) );
    assertEquals( 48, new File(testOutput + "/objectTypes").listFiles(scalaFilter).length);
    assertTrue( new File(testOutput + "/verbs").exists() );
    assertTrue( new File(testOutput + "/verbs").isDirectory() );
    assertNotNull( new File(testOutput + "/verbs").listFiles(scalaFilter) );
    assertEquals( 89, new File(testOutput + "/verbs").listFiles(scalaFilter).length );
  }
}
