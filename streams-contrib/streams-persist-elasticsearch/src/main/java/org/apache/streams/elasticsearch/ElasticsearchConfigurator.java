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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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

        if( elasticsearch.hasPath("_search") ) {
            LOGGER.info("_search supplied by config");
            Config searchConfig = elasticsearch.getConfig("_search");
            try {
                elasticsearchReaderConfiguration.setSearch(mapper.readValue(searchConfig.root().render(ConfigRenderOptions.concise()), Map.class));
            } catch (IOException e) {
                e.printStackTrace();
                LOGGER.warn("Could not parse _search supplied by config");
            }
        }

        return elasticsearchReaderConfiguration;
    }

    public static ElasticsearchWriterConfiguration detectWriterConfiguration(Config elasticsearch) {

        ElasticsearchWriterConfiguration elasticsearchWriterConfiguration = null;

        try {
            elasticsearchWriterConfiguration = mapper.readValue(elasticsearch.root().render(ConfigRenderOptions.concise()), ElasticsearchWriterConfiguration.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse elasticsearchwriterconfiguration");
        }
        return elasticsearchWriterConfiguration;
    }

}
