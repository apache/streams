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

package com.google.gplus.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.google.gplus.GPlusConfiguration;
import org.apache.streams.google.gplus.GPlusOAuthConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by sblackmon on 12/10/13.
 */
public class GPlusConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(GPlusConfigurator.class);
    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    public static GPlusConfiguration detectConfiguration(Config config) {
        GPlusConfiguration configuration = null;
        try {
            configuration = MAPPER.readValue(config.root().render(ConfigRenderOptions.concise()), GPlusConfiguration.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Preconditions.checkNotNull(configuration);

        return configuration;
    }

}
