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

package org.apache.streams.riak.binary;

import org.apache.streams.riak.pojo.RiakConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * RiakBinaryClient maintains shared connections to riak via binary protocol.
 */
public class RiakBinaryClient {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(RiakBinaryClient.class);

  private com.basho.riak.client.api.RiakClient client;

  public RiakConfiguration config;

  private RiakBinaryClient(RiakConfiguration config) {
    this.config = config;
    try {
      this.start();
    } catch (Exception e) {
      e.printStackTrace();
      this.client = null;
    }
  }

  private static Map<RiakConfiguration, RiakBinaryClient> INSTANCE_MAP = new ConcurrentHashMap<>();

  public static RiakBinaryClient getInstance(RiakConfiguration riakConfiguration) {
    if ( INSTANCE_MAP != null
        && INSTANCE_MAP.size() > 0
        && INSTANCE_MAP.containsKey(riakConfiguration)) {
      return INSTANCE_MAP.get(riakConfiguration);
    } else {
      RiakBinaryClient instance = new RiakBinaryClient(riakConfiguration);
      if( instance != null && instance.client != null ) {
        INSTANCE_MAP.put(riakConfiguration, instance);
        return instance;
      } else {
        return null;
      }
    }
  }

  public void start() throws Exception {

    Objects.nonNull(config);

    LOGGER.info("RiakHttpClient.start {}", config);

    this.client = com.basho.riak.client.api.RiakClient.newClient(config.getPort().intValue(), config.getHosts());

    Objects.nonNull(client);

    Objects.nonNull(client.getRiakCluster());

    assert( client.getRiakCluster().getNodes().size() > 0 );
  }

  public void stop() throws Exception {
    this.client = null;
  }

  public RiakConfiguration config() {
    return config;
  }

  public com.basho.riak.client.api.RiakClient client() {
    return client;
  }

}
