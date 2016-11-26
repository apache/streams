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

import org.apache.streams.plugins.hbase.StreamsHbaseResourceGenerator;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static org.apache.streams.plugins.test.StreamsHbaseResourceGeneratorTest.txtFilter;

/**
 * Test that StreamsHbaseResourceGeneratorCLI generates resources.
 */
public class StreamsHbaseResourceGeneratorCLITest {

  @Test
  public void testStreamsHbaseResourceGeneratorCLI() throws Exception {

    String sourceDirectory = "target/test-classes/activitystreams-schemas";
    String targetDirectory = "target/generated-resources/hbase-cli";

    List<String> argsList = Lists.newArrayList(sourceDirectory, targetDirectory);
    StreamsHbaseResourceGenerator.main(argsList.toArray(new String[0]));

    File testOutput = new File(targetDirectory);

    Assert.assertNotNull(testOutput);
    Assert.assertTrue(testOutput.exists());
    Assert.assertTrue(testOutput.isDirectory());

    Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
        .filter(txtFilter);
    Collection<File> outputCollection = Lists.newArrayList(outputIterator);
    assert ( outputCollection.size() == 133 );
  }
}