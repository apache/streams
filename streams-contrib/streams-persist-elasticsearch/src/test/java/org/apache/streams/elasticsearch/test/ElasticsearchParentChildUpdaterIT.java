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
import org.apache.streams.elasticsearch.ElasticsearchPersistUpdater;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequest;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
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
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration Test for
 * @see org.apache.streams.elasticsearch.ElasticsearchPersistUpdater
 * using parent/child associated documents.
 */
public class ElasticsearchParentChildUpdaterIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchParentChildUpdaterIT.class);

    private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    private ElasticsearchWriterConfiguration testConfiguration;
    private Client testClient;

    private Set<Class<? extends ActivityObject>> objectTypes;

    private List<String> files;

    @Before
    public void prepareTest() throws Exception {

        Config reference  = ConfigFactory.load();
        File conf_file = new File("target/test-classes/ElasticsearchParentChildUpdaterIT.conf");
        assert(conf_file.exists());
        Config testResourceConfig  = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));
        Config typesafe  = testResourceConfig.withFallback(reference).resolve();
        testConfiguration = new ComponentConfigurator<>(ElasticsearchWriterConfiguration.class).detectConfiguration(typesafe, "elasticsearch");
        testClient = new ElasticsearchClientManager(testConfiguration).getClient();

        ClusterHealthRequest clusterHealthRequest = Requests.clusterHealthRequest();
        ClusterHealthResponse clusterHealthResponse = testClient.admin().cluster().health(clusterHealthRequest).actionGet();
        assertNotEquals(clusterHealthResponse.getStatus(), ClusterHealthStatus.RED);

        IndicesExistsRequest indicesExistsRequest = Requests.indicesExistsRequest(testConfiguration.getIndex());
        IndicesExistsResponse indicesExistsResponse = testClient.admin().indices().exists(indicesExistsRequest).actionGet();
        assertTrue(indicesExistsResponse.isExists());

        Reflections reflections = new Reflections(new ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage("org.apache.streams.pojo.json"))
                .setScanners(new SubTypesScanner()));
        objectTypes = reflections.getSubTypesOf(ActivityObject.class);

        InputStream testActivityFolderStream = ElasticsearchParentChildUpdaterIT.class.getClassLoader()
                .getResourceAsStream("activities");
        files = IOUtils.readLines(testActivityFolderStream, StandardCharsets.UTF_8);

    }

    @Test
    public void testPersistUpdater() throws Exception {

        ElasticsearchPersistUpdater testPersistUpdater = new ElasticsearchPersistUpdater(testConfiguration);
        testPersistUpdater.prepare(null);

        for( String file : files) {
            LOGGER.info("File: " + file );
            InputStream testActivityFileStream = ElasticsearchParentChildUpdaterIT.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
            activity.setAdditionalProperty("updated", Boolean.TRUE);
            StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
            if(StringUtils.isNotBlank(activity.getObject().getObjectType())) {
                datum.getMetadata().put("parent", activity.getObject().getObjectType());
                datum.getMetadata().put("type", "activity");
                testPersistUpdater.write(datum);
                LOGGER.info("Updated: " + activity.getVerb() );
            }
        }

        testPersistUpdater.cleanUp();

        SearchRequestBuilder countUpdatedRequest = testClient
                .prepareSearch(testConfiguration.getIndex())
                .setTypes("activity")
                .setQuery(QueryBuilders.queryStringQuery("updated:true"));
        SearchResponse countUpdatedResponse = countUpdatedRequest.execute().actionGet();

        assertEquals(84, countUpdatedResponse.getHits().getTotalHits());

    }
}
