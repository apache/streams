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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.ElasticsearchClientManager;
import org.apache.streams.elasticsearch.ElasticsearchPersistUpdater;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
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
import org.elasticsearch.index.query.QueryBuilders;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

/**
 * Created by sblackmon on 10/20/14.
 */
public class ElasticsearchPersistUpdaterIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPersistUpdaterIT.class);

    private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    protected ElasticsearchWriterConfiguration testConfiguration;
    protected Client testClient;

    @Before
    public void prepareTest() throws Exception {

        Config reference  = ConfigFactory.load();
        File conf_file = new File("target/test-classes/ElasticsearchPersistUpdaterIT.conf");
        assert(conf_file.exists());
        Config testResourceConfig  = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));
        Properties es_properties  = new Properties();
        InputStream es_stream  = new FileInputStream("elasticsearch.properties");
        es_properties.load(es_stream);
        Config esProps  = ConfigFactory.parseProperties(es_properties);
        Config typesafe  = testResourceConfig.withFallback(esProps).withFallback(reference).resolve();
        StreamsConfiguration streams  = StreamsConfigurator.detectConfiguration(typesafe);
        testConfiguration = new ComponentConfigurator<>(ElasticsearchWriterConfiguration.class).detectConfiguration(typesafe, "elasticsearch");
        testClient = new ElasticsearchClientManager(testConfiguration).getClient();

        ClusterHealthRequest clusterHealthRequest = Requests.clusterHealthRequest();
        ClusterHealthResponse clusterHealthResponse = testClient.admin().cluster().health(clusterHealthRequest).actionGet();
        assertNotEquals(clusterHealthResponse.getStatus(), ClusterHealthStatus.RED);

        IndicesExistsRequest indicesExistsRequest = Requests.indicesExistsRequest(testConfiguration.getIndex());
        IndicesExistsResponse indicesExistsResponse = testClient.admin().indices().exists(indicesExistsRequest).actionGet();
        assertTrue(indicesExistsResponse.isExists());

    }

    @Test
    public void testPersistUpdater() throws Exception {

        IndicesExistsRequest indicesExistsRequest = Requests.indicesExistsRequest(testConfiguration.getIndex());
        IndicesExistsResponse indicesExistsResponse = testClient.admin().indices().exists(indicesExistsRequest).actionGet();
        assertTrue(indicesExistsResponse.isExists());

        SearchRequestBuilder countRequest = testClient
                .prepareSearch(testConfiguration.getIndex())
                .setTypes(testConfiguration.getType());
        SearchResponse countResponse = countRequest.execute().actionGet();

        long count = countResponse.getHits().getTotalHits();

        ElasticsearchPersistUpdater testPersistUpdater = new ElasticsearchPersistUpdater(testConfiguration);
        testPersistUpdater.prepare(null);

        InputStream testActivityFolderStream = ElasticsearchPersistUpdaterIT.class.getClassLoader()
                .getResourceAsStream("activities");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        for( String file : files) {
            LOGGER.info("File: " + file );
            InputStream testActivityFileStream = ElasticsearchPersistUpdaterIT.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
            Activity update = new Activity();
            update.setAdditionalProperty("updated", Boolean.TRUE);
            update.setAdditionalProperty("str", "str");
            update.setAdditionalProperty("long", 10l);
            update.setActor(
                    (Actor) new Actor()
                    .withAdditionalProperty("updated", Boolean.TRUE)
                    .withAdditionalProperty("double", 10d)
                    .withAdditionalProperty("map",
                            MAPPER.createObjectNode().set("field", MAPPER.createArrayNode().add("item"))));

            StreamsDatum datum = new StreamsDatum(update, activity.getVerb());
            testPersistUpdater.write( datum );
            LOGGER.info("Updated: " + activity.getVerb() );
        }

        testPersistUpdater.cleanUp();

        SearchRequestBuilder updatedCountRequest = testClient
                .prepareSearch(testConfiguration.getIndex())
                .setTypes(testConfiguration.getType())
                .setQuery(QueryBuilders.existsQuery("updated"));
        SearchResponse updatedCount = updatedCountRequest.execute().actionGet();

        LOGGER.info("updated: {}", updatedCount.getHits().getTotalHits());

        assertEquals(count, updatedCount.getHits().getTotalHits());

        SearchRequestBuilder actorUpdatedCountRequest = testClient
                .prepareSearch(testConfiguration.getIndex())
                .setTypes(testConfiguration.getType())
                .setQuery(QueryBuilders.termQuery("actor.updated", true));
        SearchResponse actorUpdatedCount = actorUpdatedCountRequest.execute().actionGet();

        LOGGER.info("actor.updated: {}", actorUpdatedCount.getHits().getTotalHits());

        assertEquals(count, actorUpdatedCount.getHits().getTotalHits());

        SearchRequestBuilder strUpdatedCountRequest = testClient
                .prepareSearch(testConfiguration.getIndex())
                .setTypes(testConfiguration.getType())
                .setQuery(QueryBuilders.termQuery("str", "str"));
        SearchResponse strUpdatedCount = strUpdatedCountRequest.execute().actionGet();

        LOGGER.info("strupdated: {}", strUpdatedCount.getHits().getTotalHits());

        assertEquals(count, strUpdatedCount.getHits().getTotalHits());

        SearchRequestBuilder longUpdatedCountRequest = testClient
                .prepareSearch(testConfiguration.getIndex())
                .setTypes(testConfiguration.getType())
                .setQuery(QueryBuilders.rangeQuery("long").from(9).to(11));
        SearchResponse longUpdatedCount = longUpdatedCountRequest.execute().actionGet();

        LOGGER.info("longupdated: {}", longUpdatedCount.getHits().getTotalHits());

        assertEquals(count, longUpdatedCount.getHits().getTotalHits());

        SearchRequestBuilder doubleUpdatedCountRequest = testClient
                .prepareSearch(testConfiguration.getIndex())
                .setTypes(testConfiguration.getType())
                .setQuery(QueryBuilders.rangeQuery("long").from(9).to(11));
        SearchResponse doubleUpdatedCount = doubleUpdatedCountRequest.execute().actionGet();

        LOGGER.info("doubleupdated: {}", doubleUpdatedCount.getHits().getTotalHits());

        assertEquals(count, doubleUpdatedCount.getHits().getTotalHits());

        SearchRequestBuilder mapUpdatedCountRequest = testClient
                .prepareSearch(testConfiguration.getIndex())
                .setTypes(testConfiguration.getType())
                .setQuery(QueryBuilders.termQuery("actor.map.field", "item"));
        SearchResponse mapUpdatedCount = mapUpdatedCountRequest.execute().actionGet();

        LOGGER.info("mapfieldupdated: {}", mapUpdatedCount.getHits().getTotalHits());

        assertEquals(count, mapUpdatedCount.getHits().getTotalHits());

    }
}
