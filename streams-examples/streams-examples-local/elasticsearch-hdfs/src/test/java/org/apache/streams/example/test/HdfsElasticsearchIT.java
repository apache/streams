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
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.elasticsearch.ElasticsearchClientManager;
import org.apache.streams.example.ElasticsearchHdfsConfiguration;
import org.apache.streams.example.HdfsElasticsearch;
import org.apache.streams.example.HdfsElasticsearchConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.health.ClusterHealthStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.AssertJUnit.assertTrue;

/**
 * HdfsElasticsearchIT is an integration test for HdfsElasticsearch.
 */
public class HdfsElasticsearchIT {

  private final static Logger LOGGER = LoggerFactory.getLogger(HdfsElasticsearchIT.class);

  ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  protected HdfsElasticsearchConfiguration testConfiguration;
  protected Client testClient;

  @BeforeClass
  public void prepareTest() throws Exception {

    File conf_file = new File("target/test-classes/HdfsElasticsearchIT.conf");
    assert(conf_file.exists());

    Config testResourceConfig  = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));
    StreamsConfigurator.addConfig(testResourceConfig);

    testConfiguration = new StreamsConfigurator<>(HdfsElasticsearchConfiguration.class).detectCustomConfiguration();
    testClient = ElasticsearchClientManager.getInstance(testConfiguration.getDestination()).client();

    ClusterHealthRequest clusterHealthRequest = Requests.clusterHealthRequest();
    ClusterHealthResponse clusterHealthResponse = testClient.admin().cluster().health(clusterHealthRequest).actionGet();
    assertNotEquals(clusterHealthResponse.getStatus(), ClusterHealthStatus.RED);

    IndicesExistsRequest indicesExistsRequest = Requests.indicesExistsRequest(testConfiguration.getDestination().getIndex());
    IndicesExistsResponse indicesExistsResponse = testClient.admin().indices().exists(indicesExistsRequest).actionGet();
    if(indicesExistsResponse.isExists()) {
      DeleteIndexRequest deleteIndexRequest = Requests.deleteIndexRequest(testConfiguration.getDestination().getIndex());
      DeleteIndexResponse deleteIndexResponse = testClient.admin().indices().delete(deleteIndexRequest).actionGet();
      assertTrue(deleteIndexResponse.isAcknowledged());
    };
  }

  @Test
  public void HdfsElasticsearchIT() throws Exception {

    HdfsElasticsearch restore = new HdfsElasticsearch(testConfiguration);

    restore.run();

    IndicesExistsRequest indicesExistsRequest = Requests.indicesExistsRequest(testConfiguration.getDestination().getIndex());
    IndicesExistsResponse indicesExistsResponse = testClient.admin().indices().exists(indicesExistsRequest).actionGet();
    assertTrue(indicesExistsResponse.isExists());

    SearchRequestBuilder countRequest = testClient
        .prepareSearch(testConfiguration.getDestination().getIndex())
        .setTypes(testConfiguration.getDestination().getType());
    SearchResponse countResponse = countRequest.execute().actionGet();

    assertEquals(countResponse.getHits().getTotalHits(), 89);

  }

}
