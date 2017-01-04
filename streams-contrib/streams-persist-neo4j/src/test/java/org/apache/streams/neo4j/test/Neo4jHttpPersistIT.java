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

package org.apache.streams.neo4j.test;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.neo4j.Neo4jConfiguration;
import org.apache.streams.neo4j.Neo4jReaderConfiguration;
import org.apache.streams.neo4j.http.Neo4jHttpClient;
import org.apache.streams.neo4j.http.Neo4jHttpPersistReader;
import org.apache.streams.neo4j.http.Neo4jHttpPersistWriter;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * Integration test for Neo4jHttpPersist.
 *
 * Test that graph db responses can be converted to streams data.
 */
public class Neo4jHttpPersistIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jHttpPersistIT.class);

  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  private Neo4jHttpClient testClient;

  private Neo4jConfiguration testConfiguration;

  @BeforeClass
  public void prepareTest() throws IOException {

    Config reference  = ConfigFactory.load();
    File conf_file = new File("target/test-classes/Neo4jHttpPersistIT.conf");
    assertTrue(conf_file.exists());
    Config testResourceConfig  = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));
    Config typesafe  = testResourceConfig.withFallback(reference).resolve();
    testConfiguration = new ComponentConfigurator<>(Neo4jConfiguration.class).detectConfiguration(typesafe, "neo4j");

  }

  @Test
  public void testNeo4jHttpPersist() throws Exception {

    Neo4jHttpPersistWriter testPersistWriter = new Neo4jHttpPersistWriter(testConfiguration);
    testPersistWriter.prepare(null);

    InputStream testActivityFolderStream = Neo4jHttpPersistIT.class.getClassLoader()
        .getResourceAsStream("activities");
    List<String> files = IOUtils.readLines(testActivityFolderStream, StandardCharsets.UTF_8);

    // write data

    int count = 0;
    for( String file : files) {
      LOGGER.info("File: " + file );
      InputStream testActivityFileStream = Neo4jHttpPersistIT.class.getClassLoader()
          .getResourceAsStream("activities/" + file);
      Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
      if( activity.getActor() != null && activity.getActor().getId() == null && activity.getActor().getObjectType() != null) {
        activity.getActor().setId(activity.getActor().getObjectType());
      }
      if( activity.getObject() != null && activity.getObject().getId() == null && activity.getObject().getObjectType() != null) {
        activity.getObject().setId(activity.getObject().getObjectType());
      }
      if( activity.getTarget() != null && activity.getTarget().getId() == null && activity.getTarget().getObjectType() != null) {
        activity.getTarget().setId(activity.getTarget().getObjectType());
      }
      if( activity.getId() == null && activity.getVerb() != null) {
        activity.setId(activity.getVerb());
      }
      StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
      testPersistWriter.write( datum );
      LOGGER.info("Wrote: " + activity.getVerb() );
      count++;
    }

    testPersistWriter.cleanUp();

    LOGGER.info("Total Written: {}", count );
    Assert.assertEquals(count, 89);

    Neo4jReaderConfiguration vertexReaderConfiguration= MAPPER.convertValue(testConfiguration, Neo4jReaderConfiguration.class);
    vertexReaderConfiguration.setQuery("MATCH (v) return v");
    Neo4jHttpPersistReader vertexReader = new Neo4jHttpPersistReader(vertexReaderConfiguration);
    vertexReader.prepare(null);
    StreamsResultSet vertexResultSet = vertexReader.readAll();
    LOGGER.info("Total Read: {}", vertexResultSet.size() );
    Assert.assertEquals(vertexResultSet.size(), 24);

    Neo4jReaderConfiguration edgeReaderConfiguration= MAPPER.convertValue(testConfiguration, Neo4jReaderConfiguration.class);
    edgeReaderConfiguration.setQuery("MATCH (s)-[r]->(d) return r");
    Neo4jHttpPersistReader edgeReader = new Neo4jHttpPersistReader(edgeReaderConfiguration);
    edgeReader.prepare(null);
    StreamsResultSet edgeResultSet = edgeReader.readAll();
    LOGGER.info("Total Read: {}", edgeResultSet.size() );
    Assert.assertEquals(edgeResultSet.size(), 100);

  }

}
