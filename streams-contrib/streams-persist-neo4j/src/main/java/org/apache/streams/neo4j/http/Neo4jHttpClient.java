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

package org.apache.streams.neo4j.http;

import org.apache.streams.neo4j.Neo4jConfiguration;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;

public class Neo4jHttpClient {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(Neo4jHttpClient.class);

    public Neo4jConfiguration config;

    private HttpClient client;

    private Neo4jHttpClient(Neo4jConfiguration neo4jConfiguration) {
        this.config = neo4jConfiguration;
        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
            this.client = null;
        }
    }

    private static Map<Neo4jConfiguration, Neo4jHttpClient> INSTANCE_MAP = new ConcurrentHashMap<Neo4jConfiguration, Neo4jHttpClient>();

    public static Neo4jHttpClient getInstance(Neo4jConfiguration neo4jConfiguration) {
        if ( INSTANCE_MAP != null &&
             INSTANCE_MAP.size() > 0 &&
             INSTANCE_MAP.containsKey(neo4jConfiguration)) {
            return INSTANCE_MAP.get(neo4jConfiguration);
        } else {
            Neo4jHttpClient instance = new Neo4jHttpClient(neo4jConfiguration);
            if( instance != null && instance.client != null ) {
                INSTANCE_MAP.put(neo4jConfiguration, instance);
                return instance;
            } else {
                return null;
            }
        }
    }

    public void start() throws Exception {

        Objects.nonNull(config);
        assertThat("config.getScheme().startsWith(\"http\")", config.getScheme().startsWith("http"));

        LOGGER.info("Neo4jConfiguration.start {}", config);

        Objects.nonNull(client);

    }

    public void stop() throws Exception {
        this.client = null;
    }

    public Neo4jConfiguration config() {
        return config;
    }

    public HttpClient client() {
        return client;
    }
}
