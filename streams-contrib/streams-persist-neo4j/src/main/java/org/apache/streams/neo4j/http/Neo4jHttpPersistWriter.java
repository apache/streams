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

package org.apache.streams.neo4j.http;

import org.apache.streams.components.http.HttpPersistWriterConfiguration;
import org.apache.streams.components.http.persist.SimpleHTTPPostPersistWriter;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.graph.HttpGraphHelper;
import org.apache.streams.graph.QueryGraphHelper;
import org.apache.streams.neo4j.CypherQueryGraphHelper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.neo4j.Neo4jConfiguration;
import org.apache.streams.neo4j.Neo4jPersistUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Adds activityobjects as vertices and activities as edges to a graph database with
 * an http rest endpoint (such as neo4j).
 */
public class Neo4jHttpPersistWriter extends SimpleHTTPPostPersistWriter {

  public static final String STREAMS_ID = Neo4jHttpPersistWriter.class.getCanonicalName();

  private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jHttpPersistWriter.class);
  private static final long MAX_WRITE_LATENCY = 1000;

  private Neo4jConfiguration configuration;

  private QueryGraphHelper queryGraphHelper;
  private HttpGraphHelper httpGraphHelper;

  private static ObjectMapper mapper;

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * GraphHttpPersistWriter constructor - resolve GraphHttpConfiguration from JVM 'graph'.
   */
  public Neo4jHttpPersistWriter() {
    this(new ComponentConfigurator<>(Neo4jConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig().getConfig("neo4j")));
  }

  /**
   * GraphHttpPersistWriter constructor - use supplied GraphHttpConfiguration.
   * @param configuration GraphHttpConfiguration
   */
  public Neo4jHttpPersistWriter(Neo4jConfiguration configuration) {
    super((HttpPersistWriterConfiguration)StreamsJacksonMapper.getInstance().convertValue(configuration, HttpPersistWriterConfiguration.class).withHostname(configuration.getHosts().get(0)));
    super.configuration.setResourcePath("/db/data/transaction/commit/");
    this.configuration = configuration;
  }

  @Override
  protected ObjectNode preparePayload(StreamsDatum entry) throws Exception {

    List<Pair<String, Map<String, Object>>> statements = Neo4jPersistUtil.prepareStatements(entry);

    ObjectNode requestNode = mapper.createObjectNode();
    ArrayNode statementsArray = mapper.createArrayNode();

    for( Pair<String, Map<String, Object>> statement : statements ) {
      statementsArray.add(httpGraphHelper.writeData(statement));
    }

    requestNode.put("statements", statementsArray);
    return requestNode;

  }

  @Override
  protected ObjectNode executePost(HttpPost httpPost) {

    Objects.requireNonNull(httpPost);

    ObjectNode result = null;

    CloseableHttpResponse response = null;

    String entityString = null;
    try {
      response = httpclient.execute(httpPost);
      HttpEntity entity = response.getEntity();
      if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201 && entity != null) {
        entityString = EntityUtils.toString(entity);
        result = mapper.readValue(entityString, ObjectNode.class);
      }
      LOGGER.debug("Writer response:\n{}\n{}\n{}", httpPost.toString(), response.getStatusLine().getStatusCode(), entityString);
      if ( result == null
           || (
              result.get("errors") != null
                  && result.get("errors").isArray()
                  && result.get("errors").iterator().hasNext()
              )
          ) {
        LOGGER.error("Write Error: " + result.get("errors"));
      } else {
        LOGGER.debug("Write Success");
      }
    } catch (IOException ex) {
      LOGGER.error("IO error:\n{}\n{}\n{}", httpPost.toString(), response, ex.getMessage());
    } catch (Exception ex) {
      LOGGER.error("Write Exception:\n{}\n{}\n{}", httpPost.toString(), response, ex.getMessage());
    } finally {
      try {
        if ( response != null) {
          response.close();
        }
      } catch (IOException ignored) {
        LOGGER.trace("ignored IOException", ignored);
      }
    }
    return result;
  }

  @Override
  public void prepare(Object configurationObject) {

    super.prepare(null);
    mapper = StreamsJacksonMapper.getInstance();

    queryGraphHelper = new CypherQueryGraphHelper();
    httpGraphHelper = new Neo4jHttpGraphHelper();

    Objects.requireNonNull(queryGraphHelper);
    Objects.requireNonNull(httpGraphHelper);
  }

  @Override
  public void cleanUp() {

    LOGGER.info("exiting");

  }

}
