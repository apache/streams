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

package org.apache.streams.graph;

import org.apache.streams.components.http.HttpPersistWriterConfiguration;
import org.apache.streams.components.http.persist.SimpleHTTPPostPersistWriter;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.graph.neo4j.CypherQueryGraphHelper;
import org.apache.streams.graph.neo4j.Neo4jHttpGraphHelper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Adds activityobjects as vertices and activities as edges to a graph database with
 * an http rest endpoint (such as neo4j).
 */
public class GraphHttpPersistWriter extends SimpleHTTPPostPersistWriter {

  public static final String STREAMS_ID = GraphHttpPersistWriter.class.getCanonicalName();

  private static final Logger LOGGER = LoggerFactory.getLogger(GraphHttpPersistWriter.class);
  private static final long MAX_WRITE_LATENCY = 1000;

  private GraphHttpConfiguration configuration;

  private QueryGraphHelper queryGraphHelper;
  private HttpGraphHelper httpGraphHelper;

  private static ObjectMapper mapper;

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * GraphHttpPersistWriter constructor - resolve GraphHttpConfiguration from JVM 'graph'.
   */
  public GraphHttpPersistWriter() {
    this(new ComponentConfigurator<>(GraphHttpConfiguration.class).detectConfiguration(StreamsConfigurator.config.getConfig("graph")));
  }

  /**
   * GraphHttpPersistWriter constructor - use supplied GraphHttpConfiguration.
   * @param configuration GraphHttpConfiguration
   */
  public GraphHttpPersistWriter(GraphHttpConfiguration configuration) {
    super(StreamsJacksonMapper.getInstance().convertValue(configuration, HttpPersistWriterConfiguration.class));
    if ( configuration.getType().equals(GraphHttpConfiguration.Type.NEO_4_J)) {
      super.configuration.setResourcePath("/db/" + configuration.getGraph() + "/transaction/commit/");
    } else if ( configuration.getType().equals(GraphHttpConfiguration.Type.REXSTER)) {
      super.configuration.setResourcePath("/graphs/" + configuration.getGraph());
    }
    this.configuration = configuration;
  }

  @Override
  protected ObjectNode preparePayload(StreamsDatum entry) throws Exception {

    Activity activity = null;
    ActivityObject activityObject;
    Object document = entry.getDocument();

    if (document instanceof Activity) {
      activity = (Activity) document;
      activityObject = activity.getObject();
    } else if (document instanceof ActivityObject) {
      activityObject = (ActivityObject) document;
    } else {
      ObjectNode objectNode;
      if (document instanceof ObjectNode) {
        objectNode = (ObjectNode) document;
      } else if ( document instanceof String) {
        try {
          objectNode = mapper.readValue((String) document, ObjectNode.class);
        } catch (IOException ex) {
          LOGGER.error("Can't handle input: ", entry);
          throw ex;
        }
      } else {
        LOGGER.error("Can't handle input: ", entry);
        throw new Exception("Can't create payload from datum.");
      }

      if ( objectNode.get("verb") != null ) {
        try {
          activity = mapper.convertValue(objectNode, Activity.class);
          activityObject = activity.getObject();
        } catch (Exception ex) {
          activityObject = mapper.convertValue(objectNode, ActivityObject.class);
        }
      } else {
        activityObject = mapper.convertValue(objectNode, ActivityObject.class);
      }
    }

    Preconditions.checkArgument(activity != null || activityObject != null);

    ObjectNode request = mapper.createObjectNode();
    ArrayNode statements = mapper.createArrayNode();

    // always add vertices first

    List<String> labels = Collections.singletonList("streams");

    if ( activityObject != null ) {
      if ( activityObject.getObjectType() != null ) {
        labels.add(activityObject.getObjectType());
      }
      statements.add(httpGraphHelper.createHttpRequest(queryGraphHelper.mergeVertexRequest(activityObject)));
    }

    if ( activity != null ) {

      ActivityObject actor = activity.getActor();
      Provider provider = activity.getProvider();

      if (provider != null && StringUtils.isNotBlank(provider.getId())) {
        labels.add(provider.getId());
      }
      if (actor != null && StringUtils.isNotBlank(actor.getId())) {
        if (actor.getObjectType() != null) {
          labels.add(actor.getObjectType());
        }
        statements.add(httpGraphHelper.createHttpRequest(queryGraphHelper.mergeVertexRequest(actor)));
      }

      if (activityObject != null && StringUtils.isNotBlank(activityObject.getId())) {
        if (activityObject.getObjectType() != null) {
          labels.add(activityObject.getObjectType());
        }
        statements.add(httpGraphHelper.createHttpRequest(queryGraphHelper.mergeVertexRequest(activityObject)));
      }

      // then add edge

      if (StringUtils.isNotBlank(activity.getVerb())) {
        statements.add(httpGraphHelper.createHttpRequest(queryGraphHelper.createEdgeRequest(activity)));
      }
    }

    request.put("statements", statements);
    return request;

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

    super.prepare(configuration);
    mapper = StreamsJacksonMapper.getInstance();

    if ( configuration.getType().equals(GraphHttpConfiguration.Type.NEO_4_J)) {
      queryGraphHelper = new CypherQueryGraphHelper();
      httpGraphHelper = new Neo4jHttpGraphHelper();
    }

    Objects.requireNonNull(queryGraphHelper);
    Objects.requireNonNull(httpGraphHelper);
  }

  @Override
  public void cleanUp() {

    LOGGER.info("exiting");

  }

}
