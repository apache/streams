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

import com.google.common.net.InetAddresses;

import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * Wrapper class for multiple
 * @see org.apache.streams.elasticsearch.ElasticsearchClient
 */
public class ElasticsearchClientManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchClientManager.class);

  private ElasticsearchConfiguration config;

  private org.elasticsearch.client.transport.TransportClient client;

  private ElasticsearchClientManager(ElasticsearchConfiguration config) {
    this.config = config;
    try {
      this.start();
    } catch (Exception e) {
      e.printStackTrace();
      this.client = null;
    }
  }

  private static Map<ElasticsearchConfiguration, ElasticsearchClientManager> INSTANCE_MAP = new HashMap<>();

  public static ElasticsearchClientManager getInstance(ElasticsearchConfiguration configuration) {
    if (INSTANCE_MAP != null &&
        INSTANCE_MAP.size() > 0 &&
        INSTANCE_MAP.containsKey(configuration)
        )                  {
      return INSTANCE_MAP.get(configuration);
    } else {
      ElasticsearchClientManager instance = new ElasticsearchClientManager(configuration);
      if (instance != null) {
        INSTANCE_MAP.put(configuration, instance);
        return instance;
      } else {
        return null;
      }
    }
  }

  private synchronized void start() {

    try {
      // We are currently using lazy loading to start the elasticsearch cluster, however.
      LOGGER.info("Creating a new TransportClient: {}", this.config.getHosts());

      Settings settings = Settings.settingsBuilder()
          .put("cluster.name", this.config.getClusterName())
          .put("client.transport.ping_timeout", "90s")
          .put("client.transport.nodes_sampler_interval", "60s")
          .build();

      // Create the client
      client = TransportClient.builder().settings(settings).build();
      for (String h : config.getHosts()) {
        LOGGER.info("Adding Host: {}", h);
        InetAddress address;

        if ( InetAddresses.isInetAddress(h)) {
          LOGGER.info("{} is an IP address", h);
          address = InetAddresses.forString(h);
        } else {
          LOGGER.info("{} is a hostname", h);
          address = InetAddress.getByName(h);
        }
        client.addTransportAddress(
            new InetSocketTransportAddress(
                address,
                config.getPort().intValue()));
      }
    } catch (Exception ex) {
      LOGGER.error("Could not Create elasticsearch Transport Client: {}", ex);
    }

  }

  public ElasticsearchConfiguration config() {
    return config;
  }

  public org.elasticsearch.client.transport.TransportClient client() {
    return client;
  }
}
