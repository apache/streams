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

import org.apache.streams.components.http.HttpConfiguration;
import org.apache.streams.components.http.HttpProviderConfiguration;
import org.apache.streams.components.http.provider.SimpleHttpProvider;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.graph.HttpGraphHelper;
import org.apache.streams.graph.QueryGraphHelper;
import org.apache.streams.neo4j.CypherQueryGraphHelper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.neo4j.CypherQueryResponse;
import org.apache.streams.neo4j.ItemData;
import org.apache.streams.neo4j.ItemMetadata;
import org.apache.streams.neo4j.Neo4jReaderConfiguration;
import org.apache.streams.util.PropertyUtil;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Reads a stream of activityobjects from vertices in a graph database with
 * an http rest endpoint (such as neo4j).
 */
public class Neo4jHttpPersistReader extends SimpleHttpProvider implements StreamsPersistReader {

  public static final String STREAMS_ID = Neo4jHttpPersistReader.class.getCanonicalName();

  private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jHttpPersistReader.class);

  private Neo4jReaderConfiguration config;

  private QueryGraphHelper queryGraphHelper;
  private HttpGraphHelper httpGraphHelper;

  private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  /**
   * GraphVertexReader constructor - resolve GraphReaderConfiguration from JVM 'graph'.
   */
  public Neo4jHttpPersistReader() {
    this(new ComponentConfigurator<>(Neo4jReaderConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig().getConfig("neo4j")));
  }

  /**
   * GraphVertexReader constructor - use supplied GraphReaderConfiguration.
   * @param configuration GraphReaderConfiguration
   */
  public Neo4jHttpPersistReader(Neo4jReaderConfiguration configuration) {
    super((HttpProviderConfiguration)StreamsJacksonMapper.getInstance().convertValue(configuration, HttpProviderConfiguration.class).withHostname(configuration.getHosts().get(0)));
    super.configuration.setRequestMethod(HttpConfiguration.RequestMethod.POST);
    super.configuration.setResourcePath("/db");
    super.configuration.setResource("data");
    super.configuration.setResourcePostfix("cypher");
    this.config = configuration;
  }

  /**
   * prepareHttpRequest
   * @param uri uri
   * @return result
   */
  public HttpRequestBase prepareHttpRequest(URI uri) {
    HttpRequestBase baseRequest = super.prepareHttpRequest(uri);
    HttpPost post = (HttpPost) baseRequest;
    String query = config.getQuery();
    Map<String, Object> params = mapper.convertValue(config.getParams(), Map.class);
    Pair<String, Map<String, Object>> queryPlusParams = new Pair(query, params);
    ObjectNode queryNode = httpGraphHelper.readData(queryPlusParams);
    try {
      String queryJsonString = mapper.writeValueAsString(queryNode);
      HttpEntity entity = new StringEntity(queryJsonString, ContentType.create("application/json"));
      post.setEntity(entity);
    } catch (JsonProcessingException ex) {
      LOGGER.error("JsonProcessingException", ex);
      return null;
    }
    return post;

  }
  /**
   * Neo API query returns something like this:
   * { "columns": [ "v" ], "data": [ [ { "data": { props }, etc... } ], [ { "data": { props }, etc... } ] ] }
   *
   * @param jsonNode jsonNode
   * @return result
   */
  public List<ObjectNode> parse(JsonNode jsonNode) {
    List<ObjectNode> results = new ArrayList<>();

    ObjectNode root = (ObjectNode) jsonNode;

    CypherQueryResponse cypherQueryResponse = mapper.convertValue(root, CypherQueryResponse.class);

    for ( List<List<ItemMetadata>> dataWrapper : cypherQueryResponse.getData()) {

      for (List<ItemMetadata> itemMetadatas : dataWrapper) {

        for (ItemMetadata itemMetadata : itemMetadatas) {

          ItemData itemData = itemMetadata.getData();

          LOGGER.debug("itemData: " + itemData);

          results.add(PropertyUtil.getInstance(mapper).unflattenMap(itemData.getAdditionalProperties()));
        }

      }

    }
    return results;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void prepare(Object configurationObject) {

    super.prepare(configurationObject);
    mapper = StreamsJacksonMapper.getInstance();

    queryGraphHelper = new CypherQueryGraphHelper();
    httpGraphHelper = new Neo4jHttpGraphHelper();

    Objects.requireNonNull(queryGraphHelper);
    Objects.requireNonNull(httpGraphHelper);
  }

  @Override
  public StreamsResultSet readAll() {
    return readCurrent();
  }
}
