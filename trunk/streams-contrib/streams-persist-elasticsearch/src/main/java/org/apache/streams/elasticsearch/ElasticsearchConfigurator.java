package org.apache.streams.elasticsearch;

import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by sblackmon on 12/10/13.
 */
public class ElasticsearchConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchConfigurator.class);

    public static ElasticsearchConfiguration detectConfiguration(Config elasticsearch) {
        List<String> hosts = elasticsearch.getStringList("hosts");
        Long port = elasticsearch.getLong("port");
        String clusterName = elasticsearch.getString("clusterName");

        ElasticsearchConfiguration elasticsearchConfiguration = new ElasticsearchConfiguration();

        elasticsearchConfiguration.setHosts(hosts);
        elasticsearchConfiguration.setPort(port);
        elasticsearchConfiguration.setClusterName(clusterName);

        return elasticsearchConfiguration;
    }

}
