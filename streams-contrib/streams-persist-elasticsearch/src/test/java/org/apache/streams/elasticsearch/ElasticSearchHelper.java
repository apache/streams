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
package org.apache.streams.elasticsearch;

import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ElasticSearchHelper {

    private ElasticSearchHelper () { /* Static Constructor */ }

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchHelper.class);
    private static final long DEFAULT_PORT = 9300l;

    private static final Map<String, Client> ALL_ES_SERVERS = new HashMap<String, Client>();

    private static final String DEFAULT_KEY = "default";
    private static final Map<String, ElasticsearchClientManager> ALL_ESCMS = new HashMap<String, ElasticsearchClientManager>();

    public static Client getEsClient() {
        return getEsClient(DEFAULT_KEY);
    }

    public static synchronized Client getEsClient(String key) {
        if (!ALL_ES_SERVERS.containsKey(key)) {
            LOGGER.info("Setting up test ElasticSearchEngine");

            Settings settings = ImmutableSettings.settingsBuilder()
                    .put("node.name", "node-test-" + System.currentTimeMillis())
                    .put("node.data", true)
                    .put("index.store.type", "memory")
                    .put("index.store.fs.memory.enabled", "true")
                    .put("gateway.type", "none")
                    .put("path.data", "./target/elasticsearch-test/data")
                    .put("path.work", "./target/elasticsearch-test/work")
                    .put("path.logs", "./target/elasticsearch-test/logs")
                    .put("index.number_of_shards", "1")
                    .put("index.number_of_replicas", "0")
                    .put("cluster.routing.schedule", "50ms")
                    .put("node.local", true)
                    .put("cluster.name", key + Long.toString(new Date().getTime())).build();

            Node node = NodeBuilder.nodeBuilder().local(true).settings(settings).node();

            ALL_ES_SERVERS.put(key, node.client());
        }
        return ALL_ES_SERVERS.get(key);
    }

    public static ElasticsearchClientManager getElasticSearchClientManager() {
        return getElasticSearchClientManager(DEFAULT_KEY);
    }

    public static synchronized ElasticsearchClientManager getElasticSearchClientManager(String key) {
        if (!ALL_ESCMS.containsKey(key)) {
            ElasticsearchClientManager escm = mock(ElasticsearchClientManager.class);

            Client c = getEsClient(key);
            when(escm.getClient())
                    .thenReturn(c);

            ALL_ESCMS.put(key, escm);
        }

        return ALL_ESCMS.get(key);
    }

    public static ElasticsearchReaderConfiguration createReadConfiguration(String clusterName, List<String> indicies, List<String> types) {
        ElasticsearchReaderConfiguration config = new ElasticsearchReaderConfiguration();
        config.setClusterName(clusterName);
        config.setIndexes(indicies);
        config.setTypes(types);
        config.setHosts(new ArrayList<String>() {{
            add("");
        }});
        config.setPort(DEFAULT_PORT);
        return config;
    }


    public static ElasticsearchWriterConfiguration createWriterConfiguration(String clusterName, String index, String type, long batchRecords, long batchBytes) {
        return createWriterConfiguration(clusterName, index, type, batchRecords, batchBytes, 1000*60*60*24); // default 1 day
    }

    public static ElasticsearchWriterConfiguration createWriterConfiguration(String clusterName, String index, String type, long batchRecords, long batchBytes, long batchTimeFlush) {
        ElasticsearchWriterConfiguration config = new ElasticsearchWriterConfiguration();
        config.setClusterName(clusterName);
        config.setHosts(new ArrayList<String>() {{
            add("");
        }});
        config.setPort(DEFAULT_PORT);
        config.setIndex(index);
        config.setType(type);
        config.setBatchSize(batchRecords);
        config.setBatchBytes(batchBytes);
        config.setMaxTimeBetweenFlushMs(batchTimeFlush);
        return config;
    }

    public static void destroyElasticSearchClientManager(ElasticsearchClientManager escm) {
        escm.getClient().admin().indices().prepareDelete("*").execute().actionGet();
        escm.getClient().close();
    }

    public static long countRecordsInIndex(ElasticsearchClientManager escm, String index) throws ExecutionException, InterruptedException {
        return escm.getClient().prepareCount(index).execute().get().getCount();
    }

    public static long countRecordsInIndex(ElasticsearchClientManager escm, String index, String type) throws ExecutionException, InterruptedException {
        return escm.getClient().prepareCount(index).setTypes(type).execute().get().getCount();
    }

    public static void createIndexWithMapping(ElasticsearchClientManager escm, String index, String type, String mappingAsJson) {
        assertTrue("Able to create mapping", escm.getClient().admin().indices().prepareCreate(index).addMapping(type, mappingAsJson).execute().actionGet().isAcknowledged());
    }
}