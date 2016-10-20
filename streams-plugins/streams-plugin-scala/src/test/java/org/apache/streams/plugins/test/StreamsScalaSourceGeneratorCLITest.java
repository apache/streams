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
import org.apache.streams.plugins.StreamsScalaSourceGenerator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static org.apache.streams.plugins.test.StreamsScalaSourceGeneratorTest.scalaFilter;

/**
 * Created by sblackmon on 5/5/16.
 */
public class StreamsScalaSourceGeneratorCLITest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsScalaSourceGeneratorCLITest.class);

    @Test
    public void testStreamsScalaSourceGeneratorCLI() throws Exception {

        String sourcePackages = "org.apache.streams.pojo.json";
        String targetPackage = "org.apache.streams.scala";
        String targetDirectory = "./target/generated-sources/scala-cli";

        List<String> argsList = Lists.newArrayList(sourcePackages, targetDirectory, targetPackage);
        StreamsScalaSourceGenerator.main(argsList.toArray(new String[argsList.size()]));

        File testOutput = new File(targetDirectory);

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(scalaFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() > 133 );
    }
}
