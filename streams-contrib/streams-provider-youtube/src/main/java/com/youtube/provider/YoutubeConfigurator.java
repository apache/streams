/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package com.youtube.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.youtube.pojo.YoutubeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class YoutubeConfigurator {
    private final static Logger LOGGER = LoggerFactory.getLogger(YoutubeConfigurator.class);
    private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    public static YoutubeConfiguration detectConfiguration(Config config) {
        YoutubeConfiguration configuration = null;

        try {
            configuration = MAPPER.readValue(config.root().render(ConfigRenderOptions.concise()), YoutubeConfiguration.class);
        } catch (IOException e) {
            LOGGER.error("Exception while trying to use YoutubeConfigurator: {}", e);
        }

        Preconditions.checkNotNull(configuration);

        return configuration;
    }
}
