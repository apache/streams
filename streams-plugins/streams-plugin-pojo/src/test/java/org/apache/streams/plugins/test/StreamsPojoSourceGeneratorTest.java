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

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.streams.plugins.StreamsPojoGenerationConfig;
import org.apache.streams.plugins.StreamsPojoSourceGenerator;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.List;

/**
 * Test that Activity beans are compatible with the example activities in the spec.
 */
public class StreamsPojoSourceGeneratorTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPojoSourceGeneratorTest.class);

    public static final Predicate<File> javaFilter = new Predicate<File>() {
        @Override
        public boolean apply(@Nullable File file) {
            if( file.getName().endsWith(".java") )
                return true;
            else return false;
        }
    };

    /**
     * Tests that all example activities can be loaded into Activity beans
     *
     * @throws Exception
     */
    @Test
    public void testStreamsPojoSourceGenerator() throws Exception {

        StreamsPojoGenerationConfig config = new StreamsPojoGenerationConfig();

        List<String> sourcePaths = Lists.newArrayList(
            "target/test-classes/activitystreams-schemas/activity.json",
            "target/test-classes/activitystreams-schemas/collection.json",
            "target/test-classes/activitystreams-schemas/media_link.json",
            "target/test-classes/activitystreams-schemas/object.json",
            "target/test-classes/activitystreams-schemas/objectTypes",
            "target/test-classes/activitystreams-schemas/verbs"
        );
        config.setSourcePaths(sourcePaths);

        config.setTargetPackage("org.apache.streams.pojo");
        config.setTargetDirectory("target/generated-sources/pojo");

        StreamsPojoSourceGenerator streamsPojoSourceGenerator = new StreamsPojoSourceGenerator(config);
        streamsPojoSourceGenerator.run();

        assert( config.getTargetDirectory() != null );
        assert( config.getTargetDirectory().exists() == true );
        assert( config.getTargetDirectory().isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(config.getTargetDirectory())
                .filter(javaFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() > 133 );

  }
}