package org.apache.streams.elasticsearch;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.elasticsearch.Version;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthRequestBuilder;
import org.elasticsearch.action.admin.cluster.health.ClusterHealthResponse;
import org.elasticsearch.action.admin.cluster.state.ClusterStateRequestBuilder;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.refresh.RefreshResponse;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by sblackmon on 2/10/14.
 */
public class ElasticsearchClientManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchClientManager.class);
    private static Map<String, ElasticsearchClient> ALL_CLIENTS = new HashMap<String, ElasticsearchClient>();

    private ElasticsearchConfiguration elasticsearchConfiguration;

    public ElasticsearchClientManager(ElasticsearchConfiguration elasticsearchConfiguration) {
        this.elasticsearchConfiguration = elasticsearchConfiguration;
    }

    public ElasticsearchConfiguration getElasticsearchConfiguration() {
        return elasticsearchConfiguration;
    }

    /**
     * ***********************************************************************************
     * Get the Client for this return, it is actually a transport client, but it is much
     * easier to work with the generic object as this interface likely won't change from
     * elasticsearch. This method is synchronized to block threads from creating
     * too many of these at any given time.
     *
     * @return Client for elasticsearch
     * ***********************************************************************************
     */
    public Client getClient() {
        checkAndLoadClient(null);

        return ALL_CLIENTS.get(this.elasticsearchConfiguration.getClusterName()).getClient();
    }

    public Client getClient(String clusterName) {
        checkAndLoadClient(clusterName);

        return ALL_CLIENTS.get(this.elasticsearchConfiguration.getClusterName()).getClient();
    }

    public boolean isOnOrAfterVersion(Version version) {
        return ALL_CLIENTS.get(this.elasticsearchConfiguration.toString()).getVersion().onOrAfter(version);
    }

    public void start() throws Exception {
        /***********************************************************************
         * Note:
         * Everything in these classes is being switched to lazy loading. Within
         * Heroku you only have 60 seconds to connect, and bind to the service,
         * and you are only allowed to run in 1Gb of memory. Switching all
         * of this to lazy loading is how we are fixing some of the issues
         * if you are having issues with these classes, please, refactor
         * and create a UNIT TEST CASE!!!!!! To ensure that everything is
         * working before you check it back in.
         *
         * Author: Smashew @ 2013-08-26
         **********************************************************************/
    }

    public boolean refresh(String index) {
        return refresh(new String[]{index});
    }

    public boolean refresh(String[] indexes) {
        RefreshResponse refreshResponse = this.getClient().admin().indices().prepareRefresh(indexes).execute().actionGet();
        return refreshResponse.getFailedShards() == 0;
    }

    public synchronized void stop() {
        // Terminate the elasticsearch cluster
        // Check to see if we have a client.
        if (ALL_CLIENTS.containsKey(this.elasticsearchConfiguration.toString())) {
            // Close the client
            ALL_CLIENTS.get(this.elasticsearchConfiguration.toString()).getClient().close();

            // Remove it so that it isn't in memory any more.
            ALL_CLIENTS.remove(this.elasticsearchConfiguration.toString());
        }
    }

    public ClusterHealthResponse getStatus() throws ExecutionException, InterruptedException {
        return new ClusterHealthRequestBuilder(this.getClient().admin().cluster())
                .execute()
                .get();
    }

    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    public boolean equals(Object o) {
        return EqualsBuilder.reflectionEquals(this, o, Arrays.asList(this.elasticsearchConfiguration.toString()));
    }

    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this, Arrays.asList(this.elasticsearchConfiguration.toString()));
    }

    private synchronized void checkAndLoadClient(String clusterName) {

        if (clusterName == null)
            clusterName = this.elasticsearchConfiguration.getClusterName();

        // If it is there, exit early
        if (ALL_CLIENTS.containsKey(clusterName))
            return;

        try {
            // We are currently using lazy loading to start the elasticsearch cluster, however.
            LOGGER.info("Creating a new TransportClient: {}", this.elasticsearchConfiguration.getHosts());

            Settings settings = ImmutableSettings.settingsBuilder()
                    .put("cluster.name", this.elasticsearchConfiguration.getClusterName())
                    .put("client.transport.ping_timeout", "90s")
                    .put("client.transport.nodes_sampler_interval", "60s")
                    .build();


            // Create the client
            TransportClient client = new TransportClient(settings);
            for (String h : this.getElasticsearchConfiguration().getHosts()) {
                LOGGER.info("Adding Host: {}", h);
                client.addTransportAddress(new InetSocketTransportAddress(h, this.getElasticsearchConfiguration().getPort().intValue()));
            }

            // Add the client and figure out the version.
            ElasticsearchClient elasticsearchClient = new ElasticsearchClient(client, getVersion(client));

            // Add it to our static map
            ALL_CLIENTS.put(clusterName, elasticsearchClient);

        } catch (Exception e) {
            LOGGER.error("Could not Create elasticsearch Transport Client: {}", e);
        }

    }

    private Version getVersion(Client client) {
        try {
            ClusterStateRequestBuilder clusterStateRequestBuilder = new ClusterStateRequestBuilder(client.admin().cluster());
            ClusterStateResponse clusterStateResponse = clusterStateRequestBuilder.execute().actionGet();

            return clusterStateResponse.getState().getNodes().getMasterNode().getVersion();
        } catch (Exception e) {
            return null;
        }
    }
}
