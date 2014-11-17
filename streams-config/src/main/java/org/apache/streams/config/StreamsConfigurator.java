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
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * StreamsConfigurator supplies the entire typesafe tree to runtimes and modules.
 *
 * StreamsConfigurator also supplies StreamsConfiguration POJO to runtimes and modules.
 *
 */
public class StreamsConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(ComponentConfigurator.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    /*
        Pull all configuration files from the classpath, system properties, and environment variables
     */
    public static Config config = ConfigFactory.load();

    public static Config getConfig() {
        return config;
    }

    public static StreamsConfiguration detectConfiguration() {
        return detectConfiguration(config);
    }

    public static StreamsConfiguration detectConfiguration(Config typesafeConfig) {

        StreamsConfiguration pojoConfig = null;

        try {
            pojoConfig = mapper.readValue(typesafeConfig.root().render(ConfigRenderOptions.concise()), StreamsConfiguration.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse:", typesafeConfig);
        }

        return pojoConfig;
    }
}
