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

package org.apache.streams.elasticsearch.test;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.ElasticsearchClientManager;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsResponse;
import org.elasticsearch.action.admin.indices.template.put.PutIndexTemplateRequestBuilder;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.Requests;
import org.elasticsearch.cluster.health.ClusterHealthStatus;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;
import static org.testng.Assert.assertTrue;

/**
 * Integration Test for
 * @see org.apache.streams.elasticsearch.ElasticsearchPersistWriter
 * using parent/child associated documents.
 */
@Test
public class ElasticsearchParentChildWriterIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchParentChildWriterIT.class);

  private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  protected ElasticsearchWriterConfiguration testConfiguration;
  protected Client testClient;

  Set<Class<? extends ActivityObject>> objectTypes;

  List<String> files;

  @BeforeClass
  public void prepareTestParentChildPersistWriter() throws Exception {

    testConfiguration = new ComponentConfigurator<>(ElasticsearchWriterConfiguration.class).detectConfiguration( "ElasticsearchParentChildWriterIT");
    testClient = ElasticsearchClientManager.getInstance(testConfiguration).client();

    ClusterHealthRequest clusterHealthRequest = Requests.clusterHealthRequest();
    ClusterHealthResponse clusterHealthResponse = testClient.admin().cluster().health(clusterHealthRequest).actionGet();
    assertNotEquals(clusterHealthResponse.getStatus(), ClusterHealthStatus.RED);

    IndicesExistsRequest indicesExistsRequest = Requests.indicesExistsRequest(testConfiguration.getIndex());
    IndicesExistsResponse indicesExistsResponse = testClient.admin().indices().exists(indicesExistsRequest).actionGet();
    if(indicesExistsResponse.isExists()) {
      DeleteIndexRequest deleteIndexRequest = Requests.deleteIndexRequest(testConfiguration.getIndex());
      DeleteIndexResponse deleteIndexResponse = testClient.admin().indices().delete(deleteIndexRequest).actionGet();
      assertTrue(deleteIndexResponse.isAcknowledged());
    }

    PutIndexTemplateRequestBuilder putTemplateRequestBuilder = testClient.admin().indices().preparePutTemplate("mappings");
    URL templateURL = ElasticsearchParentChildWriterIT.class.getResource("/ActivityChildObjectParent.json");
    ObjectNode template = MAPPER.readValue(templateURL, ObjectNode.class);
    String templateSource = MAPPER.writeValueAsString(template);
    putTemplateRequestBuilder.setSource(templateSource);

    testClient.admin().indices().putTemplate(putTemplateRequestBuilder.request()).actionGet();

    Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage("org.apache.streams.pojo.json"))
        .setScanners(new SubTypesScanner()));
    objectTypes = reflections.getSubTypesOf(ActivityObject.class);

    InputStream testActivityFolderStream = ElasticsearchParentChildWriterIT.class.getClassLoader()
        .getResourceAsStream("activities");
    files = IOUtils.readLines(testActivityFolderStream, StandardCharsets.UTF_8);

  }

  @Test
  public void testParentChildPersistWriter() throws Exception {

    IndicesExistsRequest indicesExistsRequest = Requests.indicesExistsRequest(testConfiguration.getIndex());
    IndicesExistsResponse indicesExistsResponse = testClient.admin().indices().exists(indicesExistsRequest).actionGet();
    if(indicesExistsResponse.isExists()) {
      DeleteIndexRequest deleteIndexRequest = Requests.deleteIndexRequest(testConfiguration.getIndex());
      DeleteIndexResponse deleteIndexResponse = testClient.admin().indices().delete(deleteIndexRequest).actionGet();
    }

    ElasticsearchPersistWriter testPersistWriter = new ElasticsearchPersistWriter(testConfiguration);
    testPersistWriter.prepare(null);

    for( Class objectType : objectTypes ) {
      Object object = objectType.newInstance();
      ActivityObject activityObject = MAPPER.convertValue(object, ActivityObject.class);
      StreamsDatum datum = new StreamsDatum(activityObject, activityObject.getObjectType());
      datum.getMetadata().put("type", "object");
      testPersistWriter.write( datum );
    }

    for( String file : files) {
      LOGGER.info("File: " + file );
      InputStream testActivityFileStream = ElasticsearchParentChildWriterIT.class.getClassLoader()
          .getResourceAsStream("activities/" + file);
      Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
      StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
      if( !StringUtils.isEmpty(activity.getObject().getObjectType())) {
        datum.getMetadata().put("parent", activity.getObject().getObjectType());
        datum.getMetadata().put("type", "activity");
        testPersistWriter.write(datum);
        LOGGER.info("Wrote: " + activity.getVerb());
      }
    }

    testPersistWriter.cleanUp();

    SearchRequestBuilder countParentRequest = testClient
        .prepareSearch(testConfiguration.getIndex())
        .setTypes("object");
    SearchResponse countParentResponse = countParentRequest.execute().actionGet();

    assertEquals(41, countParentResponse.getHits().getTotalHits());

    SearchRequestBuilder countChildRequest = testClient
        .prepareSearch(testConfiguration.getIndex())
        .setTypes("activity");
    SearchResponse countChildResponse = countChildRequest.execute().actionGet();

    assertEquals(84, countChildResponse.getHits().getTotalHits());

  }

}
