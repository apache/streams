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

package org.apache.streams.example.test;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.example.TwitterFollowNeo4j;
import org.apache.streams.example.TwitterFollowNeo4jConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.neo4j.Neo4jReaderConfiguration;
import org.apache.streams.neo4j.bolt.Neo4jBoltClient;
import org.apache.streams.neo4j.bolt.Neo4jBoltPersistReader;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

import static org.testng.Assert.assertTrue;

/**
 * TwitterFollowNeo4jIT is an integration test for TwitterFollowNeo4j.
 */
public class TwitterFollowNeo4jIT {

  private final static Logger LOGGER = LoggerFactory.getLogger(TwitterFollowNeo4jIT.class);

  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  protected TwitterFollowNeo4jConfiguration testConfiguration;

  private int count = 0;

  private Neo4jBoltClient testClient;

  @BeforeClass
  public void prepareTest() throws IOException {

    Config reference  = ConfigFactory.load();
    File conf = new File("target/test-classes/TwitterFollowNeo4jIT.conf");
    assertTrue(conf.exists());
    Config testResourceConfig  = ConfigFactory.parseFileAnySyntax(conf, ConfigParseOptions.defaults().setAllowMissing(false));
    Config typesafe  = testResourceConfig.withFallback(reference).resolve();
    testConfiguration = new ComponentConfigurator<>(TwitterFollowNeo4jConfiguration.class).detectConfiguration(typesafe);
    testClient = Neo4jBoltClient.getInstance(testConfiguration.getNeo4j());

    Session session = testClient.client().session();
    Transaction transaction = session.beginTransaction();
    transaction.run("MATCH ()-[r]-() DELETE r");
    transaction.run("MATCH (n) DETACH DELETE n");
    transaction.success();
    session.close();
  }

  @Test
  public void testTwitterFollowGraph() throws Exception {

    TwitterFollowNeo4j stream = new TwitterFollowNeo4j(testConfiguration);

    stream.run();

    Neo4jReaderConfiguration vertexReaderConfiguration= MAPPER.convertValue(testConfiguration.getNeo4j(), Neo4jReaderConfiguration.class);
    vertexReaderConfiguration.setQuery("MATCH (v) return v");
    Neo4jBoltPersistReader vertexReader = new Neo4jBoltPersistReader(vertexReaderConfiguration);
    vertexReader.prepare(null);
    StreamsResultSet vertexResultSet = vertexReader.readAll();
    LOGGER.info("Total Read: {}", vertexResultSet.size() );
    assertTrue(vertexResultSet.size() > 100);

    Neo4jReaderConfiguration edgeReaderConfiguration= MAPPER.convertValue(testConfiguration.getNeo4j(), Neo4jReaderConfiguration.class);
    edgeReaderConfiguration.setQuery("MATCH (s)-[r]->(d) return r");
    Neo4jBoltPersistReader edgeReader = new Neo4jBoltPersistReader(edgeReaderConfiguration);
    edgeReader.prepare(null);
    StreamsResultSet edgeResultSet = edgeReader.readAll();
    LOGGER.info("Total Read: {}", edgeResultSet.size() );
    assertTrue(edgeResultSet.size() == vertexResultSet.size()-1);

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
