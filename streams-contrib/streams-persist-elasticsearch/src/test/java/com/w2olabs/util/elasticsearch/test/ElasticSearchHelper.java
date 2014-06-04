package com.w2olabs.util.elasticsearch.test;

import org.apache.streams.elasticsearch.ElasticsearchClientManager;
import org.apache.streams.elasticsearch.ElasticsearchReaderConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.node.Node;
import org.elasticsearch.node.NodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class ElasticSearchHelper {

    private ElasticSearchHelper () { /* Static Constructor */ }

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticSearchHelper.class);



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
                    .put("index.number_of_shards", "1")
                    .put("index.number_of_replicas", "0")
                    .put("cluster.routing.schedule", "50ms")
                    .put("node.local", true)
                    .put("cluster.name", key + Long.toString(new Date().getTime())).build();

            Node node = NodeBuilder.nodeBuilder().settings(settings).node();

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
        config.setPort(9300l);
        return config;
    }

    public static ElasticsearchWriterConfiguration createWriterConfiguration(String clusterName, String index, String type, long batchRecords, long batchBytes) {
        ElasticsearchWriterConfiguration config = new ElasticsearchWriterConfiguration();
        config.setClusterName(clusterName);
        config.setHosts(new ArrayList<String>() {{
            add("");
        }});
        config.setPort(9300l);
        config.setIndex(index);
        config.setType(type);
        config.setBatchSize(batchRecords);
        config.setBatchBytes(batchBytes);
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

    public static void deleteIndex(ElasticsearchClientManager escm, String index) {
        escm.getClient().admin().indices().prepareDelete(index).execute().actionGet();
    }

}
