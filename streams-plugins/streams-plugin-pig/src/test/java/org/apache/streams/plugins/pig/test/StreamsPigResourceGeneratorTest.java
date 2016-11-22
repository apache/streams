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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import javax.annotation.Nullable;

/**
 * Test that Activity beans are compatible with the example activities in the spec.
 */
public class StreamsPigResourceGeneratorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(StreamsPigResourceGeneratorTest.class);

  public static final Predicate<File> pigFilter = new Predicate<File>() {
    @Override
    public boolean apply(@Nullable File file) {
      if ( file.getName().endsWith(".pig") ) {
        return true;
      } else {
        return false;
      }
    }
  };

  /**
   * Tests that StreamsPigResourceGenerator via SDK generates pig resources.
   *
   * @throws Exception Exception
   */
  @Test
  public void testStreamsPigResourceGenerator() throws Exception {

    StreamsPigGenerationConfig config = new StreamsPigGenerationConfig();

    String sourceDirectory = "target/test-classes/activitystreams-schemas";

    config.setSourceDirectory(sourceDirectory);

    config.setTargetDirectory("target/generated-resources/pig");

    config.setExclusions(Sets.newHashSet("attachments"));

    config.setMaxDepth(2);

    StreamsPigResourceGenerator streamsPigResourceGenerator = new StreamsPigResourceGenerator(config);
    streamsPigResourceGenerator.run();

    File testOutput = config.getTargetDirectory();

    assert ( testOutput != null );
    assert ( testOutput.exists() == true );
    assert ( testOutput.isDirectory() == true );

    Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
        .filter(pigFilter);
    Collection<File> outputCollection = Lists.newArrayList(outputIterator);
    assert ( outputCollection.size() == 133 );

    // TODO: figure out how to do a match to a test resources that has an apache header.
    //        String expectedDirectory = "target/test-classes/expected";
    //        File testExpected = new File( expectedDirectory );
    //
    //        Iterable<File> expectedIterator = Files.fileTreeTraverser().breadthFirstTraversal(testExpected)
    //                .filter(pigFilter);
    //        Collection<File> expectedCollection = Lists.newArrayList(expectedIterator);
    //
    //        int fails = 0;
    //
    //        Iterator<File> iterator = expectedCollection.iterator();
    //        while( iterator.hasNext() ) {
    //            File objectExpected = iterator.next();
    //            String expectedEnd = dropSourcePathPrefix(objectExpected.getAbsolutePath(),  expectedDirectory);
    //            File objectActual = new File(config.getTargetDirectory() + "/" + expectedEnd);
    //            LOGGER.info("Comparing: {} and {}", objectExpected.getAbsolutePath(), objectActual.getAbsolutePath());
    //            assert( objectActual.exists());
    //            if( FileUtils(objectActual, objectExpected) == true ) {
    //                LOGGER.info("Exact Match!");
    //            } else {
    //                LOGGER.info("No Match!");
    //                fails++;
    //            }
    //        }
    //        if( fails > 0 ) {
    //            LOGGER.info("Fails: {}", fails);
    //            Assert.fail();
    //        }
  }
}