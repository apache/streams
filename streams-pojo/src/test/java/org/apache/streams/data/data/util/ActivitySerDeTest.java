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

package org.apache.streams.data.data.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Test that Activity beans are compatible with the example activities in the spec.
 */
public class ActivitySerDeTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ActivitySerDeTest.class);

    private final static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    /**
     * Tests that all example activities can be loaded into Activity beans
     * @throws Exception
     */
    @Test
    public void testActivitySerDe() throws Exception {

        InputStream testActivityFolderStream = ActivitySerDeTest.class.getClassLoader()
                .getResourceAsStream("activities");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        for( String file : files) {
            LOGGER.info("File: " + file );
            LOGGER.info("Serializing: activities/" + file );
            InputStream testActivityFileStream = ActivitySerDeTest.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
            String activityString = MAPPER.writeValueAsString(activity);
            LOGGER.info("Deserialized: " + activityString );
        }
    }

    /**
     * Tests that defined activity verbs have an example which can be loaded into
     * Activity beans and into verb-specific beans
     * @throws Exception
     */
    @Test
    public void testVerbSerDe() throws Exception {

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("org.apache.streams.pojo.json"))
                .setScanners(new SubTypesScanner()));
        Set<Class<? extends Activity>> verbs = reflections.getSubTypesOf(Activity.class);

        for( Class verbClass : verbs) {
            LOGGER.info("Verb: " + verbClass.getSimpleName() );
            Activity activity = (Activity) verbClass.newInstance();
            String verbName = activity.getVerb();
            String testfile = verbName.toLowerCase() + ".json";
            LOGGER.info("Serializing: activities/" + testfile );
            assert(ActivitySerDeTest.class.getClassLoader().getResource("activities/" + testfile) != null);
            InputStream testActivityFileStream = ActivitySerDeTest.class.getClassLoader()
                    .getResourceAsStream("activities/" + testfile);
            assert(testActivityFileStream != null);
            activity = MAPPER.convertValue(MAPPER.readValue(testActivityFileStream, verbClass), Activity.class);
            String activityString = MAPPER.writeValueAsString(activity);
            LOGGER.info("Deserialized: " + activityString );
        }
    }
}
