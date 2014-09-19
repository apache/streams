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

/**
 * Converts a {@link com.typesafe.config.Config} element into an instance of ElasticSearchConfiguration
 */
public class ElasticsearchConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchConfigurator.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    public static ElasticsearchConfiguration detectConfiguration(Config elasticsearch) {

        ElasticsearchConfiguration elasticsearchConfiguration = null;

        try {
            elasticsearchConfiguration = mapper.readValue(elasticsearch.root().render(ConfigRenderOptions.concise()), ElasticsearchConfiguration.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse elasticsearchconfiguration");
        }

        return elasticsearchConfiguration;
    }

    public static ElasticsearchReaderConfiguration detectReaderConfiguration(Config elasticsearch) {

        ElasticsearchReaderConfiguration elasticsearchReaderConfiguration = null;

        try {
            elasticsearchReaderConfiguration = mapper.readValue(elasticsearch.root().render(ConfigRenderOptions.concise()), ElasticsearchReaderConfiguration.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse elasticsearchconfiguration");
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
