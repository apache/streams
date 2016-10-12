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

import org.apache.streams.plugins.StreamsScalaSourceGenerator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;

/**
 * Test that Activity beans are compatible with the example activities in the spec.
 */
public class StreamsScalaSourceGeneratorTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsScalaSourceGeneratorTest.class);

    /**
     * Tests that all example activities can be loaded into Activity beans
     *
     * @throws Exception
     */
    @Test
    public void testDetectPojoScala() throws Exception {
        StreamsScalaSourceGenerator streamsScalaSourceGenerator = new StreamsScalaSourceGenerator();
        streamsScalaSourceGenerator.main(new String[0]);

        File testOutput = new File( "./target/generated-sources/scala/org/apache/streams/scala");
        FileFilter scalaFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if( pathname.getName().endsWith(".scala") )
                    return true;
                return false;
            }
        };

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );
        assert( testOutput.listFiles(scalaFilter).length == 11 );
        assert( new File(testOutput + "/traits").exists() == true );
        assert( new File(testOutput + "/traits").isDirectory() == true );
        assert( new File(testOutput + "/traits").listFiles(scalaFilter) != null );
        assert( new File(testOutput + "/traits").listFiles(scalaFilter).length == 4 );
        assert( new File(testOutput + "/objectTypes").exists() == true );
        assert( new File(testOutput + "/objectTypes").isDirectory() == true );
        assert( new File(testOutput + "/objectTypes").listFiles(scalaFilter) != null );
        assert( new File(testOutput + "/objectTypes").listFiles(scalaFilter).length == 43 );
        assert( new File(testOutput + "/verbs").exists() == true );
        assert( new File(testOutput + "/verbs").isDirectory() == true );
        assert( new File(testOutput + "/verbs").listFiles(scalaFilter) != null );
        assert( new File(testOutput + "/verbs").listFiles(scalaFilter).length == 89 );
    }
}