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

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import junit.framework.TestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.streams.plugins.test.StreamsScalaSourceGeneratorTest.scalaFilter;

/**
 * Tests that streams-plugin-pojo running via maven can convert activity schemas into pojos
 * which then compile.
 */
public class StreamsScalaSourceGeneratorMojoIT extends TestCase {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsScalaSourceGeneratorMojoIT.class);

    protected void setUp() throws Exception
    {
        // required for mojo lookups to work
        super.setUp();
    }


    @Test
    public void testStreamsScalaSourceGeneratorMojo() throws Exception {

        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/streams-plugin-scala" );

        Verifier verifier;

        verifier = new Verifier( testDir.getAbsolutePath() );

        List cliOptions = new ArrayList();
        cliOptions.add( "-N" );
        verifier.executeGoals( Lists.<String>newArrayList(
                "compile"));

        verifier.verifyErrorFreeLog();

        verifier.resetStreams();

        File testOutput = new File(testDir.getAbsolutePath() + "/target/generated-sources/scala-mojo");

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(scalaFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() > 133 );

    }
}