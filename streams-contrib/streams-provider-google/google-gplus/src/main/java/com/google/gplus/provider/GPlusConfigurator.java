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

import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.google.gplus.GPlusConfiguration;
import org.apache.streams.google.gplus.GPlusOAuthConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class GPlusConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(GPlusConfigurator.class);

    public static GPlusConfiguration detectConfiguration(Config config) {
        Config oauth = StreamsConfigurator.config.getConfig("gplus.oauth");

        GPlusConfiguration gplusConfiguration = new GPlusConfiguration();

        gplusConfiguration.setProtocol(config.getString("protocol"));
        gplusConfiguration.setHost(config.getString("host"));
        gplusConfiguration.setPort(config.getLong("port"));
        gplusConfiguration.setVersion(config.getString("version"));
        GPlusOAuthConfiguration gPlusOAuthConfiguration = new GPlusOAuthConfiguration();
        gPlusOAuthConfiguration.setConsumerKey(oauth.getString("consumerKey"));
        gPlusOAuthConfiguration.setConsumerSecret(oauth.getString("consumerSecret"));
        gPlusOAuthConfiguration.setAccessToken(oauth.getString("accessToken"));
        gPlusOAuthConfiguration.setAccessTokenSecret(oauth.getString("accessTokenSecret"));
        gplusConfiguration.setOauth(gPlusOAuthConfiguration);

        return gplusConfiguration;
    }

}
