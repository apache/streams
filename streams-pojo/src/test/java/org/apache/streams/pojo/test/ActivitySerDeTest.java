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

package org.apache.streams.pojo.test;

import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Test that Activity beans are compatible with the example activities in the spec.
 */
public class ActivitySerDeTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(ActivitySerDeTest.class);

  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  /**
   * Tests that all example activities can be loaded into Activity beans.
   * @throws Exception Exception
   */
  @Test
  public void testActivitySerDe() throws Exception {

    Path testdataDir = Paths.get("target/dependency/activitystreams-testdata");
    List<Path> schemaPaths = Files.list(testdataDir).collect(Collectors.toList());
    for( Path schemaPath : schemaPaths ) {
      LOGGER.info("Path: " + schemaPath );
      LOGGER.info("Serializing: activities/" + schemaPath );
      FileInputStream testActivityFileStream = new FileInputStream(schemaPath.toFile());
      Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
      activity.setGenerator(null);
      activity.setLinks(new LinkedList<>());
      String activityString = MAPPER.writeValueAsString(activity);
      LOGGER.info("Deserialized: " + activityString );
      assert ( !activityString.contains("null") );
      assert ( !activityString.contains("[]") );
    }
  }

  /**
   * Tests that defined activity verbs have an example which can be loaded into
   * Activity beans and into verb-specific beans.
   * @throws Exception Exception
   */
  @Test
  public void testVerbSerDe() throws Exception {

    Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage("org.apache.streams.pojo.json"))
        .setScanners(new SubTypesScanner()));
    Set<Class<? extends Activity>> verbs = reflections.getSubTypesOf(Activity.class);

    for ( Class verbClass : verbs) {
      LOGGER.info("Verb: " + verbClass.getSimpleName() );
      Activity activity = (Activity) verbClass.newInstance();
      String verbName = activity.getVerb();
      String testfile = verbName.toLowerCase() + ".json";
      LOGGER.info("Serializing: activities/" + testfile );
      Path testActivityPath = Paths.get("target/dependency/activitystreams-testdata/" + testfile);
      assert (testActivityPath.toFile().exists());
      FileInputStream testActivityFileStream = new FileInputStream(testActivityPath.toFile());
      assert (testActivityFileStream != null);
      activity = MAPPER.convertValue(MAPPER.readValue(testActivityFileStream, verbClass), Activity.class);
      String activityString = MAPPER.writeValueAsString(activity);
      LOGGER.info("Deserialized: " + activityString );
      assert ( !activityString.contains("null") );
      assert ( !activityString.contains("[]") );
    }
  }
}
