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

package org.apache.streams.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * ComponentConfigurator supplies serializable configuration beans derived from a specified typesafe path or object.
 *
 * Typically a component will select a 'default' typesafe path to be used if no other path or object is provided.
 *
 * For example, streams-persist-elasticsearch will use 'elasticsearch' by default, but an implementation
 *   such as github.com/w2ogroup/elasticsearch-reindex can resolve a reader from elasticsearch.source
 *   and a writer from elasticsearch.destination
 *
 */
public class ComponentConfigurator<T extends Serializable> {

    private Class<T> configClass;
    public ComponentConfigurator(Class<T> configClass) {
        this.configClass = configClass;
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(ComponentConfigurator.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    public T detectConfiguration(Config typesafeConfig) {

        T pojoConfig = null;

        try {
            pojoConfig = mapper.readValue(typesafeConfig.root().render(ConfigRenderOptions.concise()), configClass);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse:", typesafeConfig);
        }

        return pojoConfig;
    }

    public T detectConfiguration(String subConfig) {
        Config streamsConfig = StreamsConfigurator.getConfig();
        return detectConfiguration( streamsConfig.getConfig(subConfig));
    }

    public T detectConfiguration(Config typesafeConfig, String subConfig) {
        return detectConfiguration( typesafeConfig.getConfig(subConfig));
    }
}
