package org.apache.streams.elasticsearch;

import org.elasticsearch.Version;
import org.elasticsearch.client.Client;

/**
 * Created by sblackmon on 2/10/14.
 */
public class ElasticsearchClient {

    private Client client;
    private Version version;

    public ElasticsearchClient(Client client, Version version) {
        this.client = client;
        this.version = version;
    }

    public Client getClient() {
        return client;
    }

    public Version getVersion() {
        return version;
    }
}