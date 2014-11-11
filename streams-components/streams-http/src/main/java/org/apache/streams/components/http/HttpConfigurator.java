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

package org.apache.streams.components.http;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Converts a {@link com.typesafe.config.Config} element into an instance of HttpConfiguration
 */
public class HttpConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(HttpConfigurator.class);

    private final static ObjectMapper mapper = new ObjectMapper();

    public static HttpProviderConfiguration detectProviderConfiguration(Config config) {

        HttpProviderConfiguration httpProviderConfiguration = null;

        try {
            httpProviderConfiguration = mapper.readValue(config.root().render(ConfigRenderOptions.concise()), HttpProviderConfiguration.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse http configuration", e.getMessage());
        }
        return httpProviderConfiguration;
    }

    public static HttpProcessorConfiguration detectProcessorConfiguration(Config config) {

        HttpProcessorConfiguration httpProcessorConfiguration = null;

        try {
            httpProcessorConfiguration = mapper.readValue(config.root().render(ConfigRenderOptions.concise()), HttpProcessorConfiguration.class);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.warn("Could not parse http configuration", e.getMessage());
        }
        return httpProcessorConfiguration;
    }

}
