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

package org.apache.streams.twitter.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.streams.twitter.TwitterConfiguration;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.apache.streams.twitter.TwitterUserInformationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * This class resolves TwitterConfiguration from typesafe config
 *
 * Deprecated: use ComponentConfigurator.detectConfig() instead
 */
@Deprecated
public class TwitterConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterConfigurator.class);
    private final static ObjectMapper mapper = new ObjectMapper();

    public static TwitterConfiguration detectTwitterConfiguration(Config config) {
        try {
            return mapper.readValue(config.root().render(ConfigRenderOptions.concise()), TwitterConfiguration.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static TwitterStreamConfiguration detectTwitterStreamConfiguration(Config config) {

        try {
            return mapper.readValue(config.root().render(ConfigRenderOptions.concise()), TwitterStreamConfiguration.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static TwitterUserInformationConfiguration detectTwitterUserInformationConfiguration(Config config) {
        try {
            return mapper.readValue(config.root().render(ConfigRenderOptions.concise()), TwitterUserInformationConfiguration.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

}
