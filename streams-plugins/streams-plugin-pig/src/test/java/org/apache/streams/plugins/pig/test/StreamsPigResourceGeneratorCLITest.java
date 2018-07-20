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

import org.apache.streams.plugins.pig.StreamsPigResourceGenerator;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.testng.Assert;

import java.io.File;
import java.util.Collection;

/**
 * Test whether StreamsPigResourceGeneratorCLI generates resources.
 */
public class StreamsPigResourceGeneratorCLITest {

  @Test
  public void testStreamsPigResourceGeneratorCLI() throws Exception {

    String sourceDirectory = "target/dependency/jsonschemaorg-schemas";
    String targetDirectory = "target/generated-resources/pig-cli";

    StreamsPigResourceGenerator.main(new String[]{sourceDirectory, targetDirectory});

    File testOutput = new File(targetDirectory);

    Assert.assertNotNull(testOutput);
    Assert.assertTrue(testOutput.exists());
    Assert.assertTrue(testOutput.isDirectory());

    Collection<File> outputCollection = FileUtils.listFiles(testOutput, StreamsPigResourceGeneratorTest.pigFilter, true);
    Assert.assertEquals(4, outputCollection.size());
  }
}