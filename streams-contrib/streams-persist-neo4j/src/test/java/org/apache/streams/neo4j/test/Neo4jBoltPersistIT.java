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
import org.apache.streams.neo4j.bolt.Neo4jBoltClient;
import org.apache.streams.neo4j.bolt.Neo4jBoltPersistReader;
import org.apache.streams.neo4j.bolt.Neo4jBoltPersistWriter;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import org.apache.commons.io.IOUtils;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.testng.Assert.assertTrue;

/**
 * Integration test for Neo4jBoltPersist.
 *
 * Test that graph db responses can be converted to streams data.
 */
public class Neo4jBoltPersistIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jBoltPersistIT.class);

  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  private Neo4jBoltClient testClient;

  private Neo4jConfiguration testConfiguration;

  @BeforeClass
  public void prepareTest() throws IOException {

    testConfiguration = new ComponentConfigurator<>(Neo4jConfiguration.class).detectConfiguration( "Neo4jBoltPersistIT");
    testClient = Neo4jBoltClient.getInstance(testConfiguration);

    Session session = testClient.client().session();
    Transaction transaction = session.beginTransaction();
    transaction.run("MATCH ()-[r]-() DELETE r");
    transaction.run("MATCH (n) DETACH DELETE n");
    transaction.success();
    session.close();
  }

  @Test
  public void testNeo4jBoltPersist() throws Exception {

    Neo4jBoltPersistWriter testPersistWriter = new Neo4jBoltPersistWriter(testConfiguration);
    testPersistWriter.prepare(testConfiguration);

    InputStream testActivityFolderStream = Neo4jBoltPersistIT.class.getClassLoader()
        .getResourceAsStream("activities");
    List<String> files = IOUtils.readLines(testActivityFolderStream, StandardCharsets.UTF_8);

    int count = 0;
    for( String file : files) {
      LOGGER.info("File: " + file );
      InputStream testActivityFileStream = Neo4jBoltPersistIT.class.getClassLoader()
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
      try {
        testPersistWriter.write(datum);
        LOGGER.info("Wrote: " + activity.getVerb());
        count++;
      } catch (Exception e) {
        LOGGER.warn("Exception writing: " + activity.getVerb(), e);
      }
    }

    testPersistWriter.cleanUp();

    LOGGER.info("Total Written: {}", count );
    assertThat(count, equalTo(89));

    Neo4jReaderConfiguration vertexReaderConfiguration= MAPPER.convertValue(testConfiguration, Neo4jReaderConfiguration.class);
    vertexReaderConfiguration.setQuery("MATCH (v) return v");
    Neo4jBoltPersistReader vertexReader = new Neo4jBoltPersistReader(vertexReaderConfiguration);
    vertexReader.prepare(null);
    StreamsResultSet vertexResultSet = vertexReader.readAll();
    LOGGER.info("Total Read: {}", vertexResultSet.size() );
    assertThat(vertexResultSet.size(), greaterThanOrEqualTo(20));

    Neo4jReaderConfiguration edgeReaderConfiguration= MAPPER.convertValue(testConfiguration, Neo4jReaderConfiguration.class);
    edgeReaderConfiguration.setQuery("MATCH (s)-[r]->(d) return r");
    Neo4jBoltPersistReader edgeReader = new Neo4jBoltPersistReader(edgeReaderConfiguration);
    edgeReader.prepare(null);
    StreamsResultSet edgeResultSet = edgeReader.readAll();
    LOGGER.info("Total Read: {}", edgeResultSet.size() );
    assertThat(edgeResultSet.size(), greaterThanOrEqualTo(65));

  }

  @AfterClass
  public void cleanup() throws Exception {
    Session session = testClient.client().session();
    Transaction transaction = session.beginTransaction();
    transaction.run("MATCH ()-[r]-() DELETE r");
    transaction.run("MATCH (n) DETACH DELETE n");
    transaction.success();
    session.close();
  }

}
