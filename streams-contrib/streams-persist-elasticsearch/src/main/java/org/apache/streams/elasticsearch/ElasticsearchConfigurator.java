package org.apache.streams.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Converts a {@link com.typesafe.config.Config} element into an instance of ElasticSearchConfiguration
 */
public class ElasticsearchConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchConfigurator.class);

    private final static ObjectMapper mapper = new ObjectMapper();

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

    public static ElasticsearchReaderConfiguration detectReaderConfiguration(Config elasticsearch) {

        ElasticsearchConfiguration elasticsearchConfiguration = detectConfiguration(elasticsearch);
        ElasticsearchReaderConfiguration elasticsearchReaderConfiguration = mapper.convertValue(elasticsearchConfiguration, ElasticsearchReaderConfiguration.class);

        List<String> indexes = elasticsearch.getStringList("indexes");
        List<String> types = elasticsearch.getStringList("types");

        elasticsearchReaderConfiguration.setIndexes(indexes);
        elasticsearchReaderConfiguration.setTypes(types);

        return elasticsearchReaderConfiguration;
    }

    public static ElasticsearchWriterConfiguration detectWriterConfiguration(Config elasticsearch) {

        ElasticsearchConfiguration elasticsearchConfiguration = detectConfiguration(elasticsearch);
        ElasticsearchWriterConfiguration elasticsearchWriterConfiguration = mapper.convertValue(elasticsearchConfiguration, ElasticsearchWriterConfiguration.class);

        String index = elasticsearch.getString("index");
        String type = elasticsearch.getString("type");
        Long maxMsBeforeFlush = elasticsearch.hasPath("MaxTimeBetweenFlushMs") ? elasticsearch.getLong("MaxTimeBetweenFlushMs") : null;

        if( elasticsearch.hasPath("bulk"))
            elasticsearchWriterConfiguration.setBulk(elasticsearch.getBoolean("bulk"));

        if( elasticsearch.hasPath("batchSize"))
            elasticsearchWriterConfiguration.setBatchSize(elasticsearch.getLong("batchSize"));

        elasticsearchWriterConfiguration.setIndex(index);
        elasticsearchWriterConfiguration.setType(type);
        elasticsearchWriterConfiguration.setMaxTimeBetweenFlushMs(maxMsBeforeFlush);


        return elasticsearchWriterConfiguration;
    }

}
