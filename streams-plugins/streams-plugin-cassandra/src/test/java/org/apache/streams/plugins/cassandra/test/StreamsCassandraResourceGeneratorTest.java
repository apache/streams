/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.apache.streams.plugins.cassandra.StreamsCassandraGenerationConfig;
import org.apache.streams.plugins.cassandra.StreamsCassandraResourceGenerator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Test that cassandra resources are generated.
 */
public class StreamsCassandraResourceGeneratorTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsCassandraResourceGeneratorTest.class);

    public static final Predicate<File> cqlFilter = new Predicate<File>() {
        @Override
        public boolean apply(@Nullable File file) {
            if( file.getName().endsWith(".cql") )
                return true;
            else return false;
        }
    };

    /**
     * Test that cassandra resources are generated
     *
     * @throws Exception
     */
    @Test
    public void StreamsCassandraResourceGenerator() throws Exception {

        StreamsCassandraGenerationConfig config = new StreamsCassandraGenerationConfig();

        String sourceDirectory = "target/test-classes/streams-schema-activitystreams";

        config.setSourceDirectory(sourceDirectory);

        config.setTargetDirectory("target/generated-resources/cassandra");

        config.setExclusions(Sets.newHashSet("attachments"));

        config.setMaxDepth(2);

        StreamsCassandraResourceGenerator streamsCassandraResourceGenerator = new StreamsCassandraResourceGenerator(config);
        streamsCassandraResourceGenerator.run();

        File testOutput = config.getTargetDirectory();

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(cqlFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() == 1 );

        Path path = Paths.get(testOutput.getAbsolutePath()).resolve("types.cql");

        assert( path.toFile().exists() );

        String typesCqlBytes = new String(
                java.nio.file.Files.readAllBytes(path));

        assert( StringUtils.countMatches(typesCqlBytes, "CREATE TYPE") == 133 );

    }
}