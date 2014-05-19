package org.apache.streams.elasticsearch;

/*
 * #%L
 * streams-persist-elasticsearch
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
