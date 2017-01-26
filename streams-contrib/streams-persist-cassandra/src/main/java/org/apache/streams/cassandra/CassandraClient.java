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

package org.apache.streams.cassandra;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Host;
import com.datastax.driver.core.JdkSSLOptions;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.SSLOptions;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.SocketOptions;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.security.KeyStore;
import java.util.Collection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import static com.datastax.driver.core.SocketOptions.DEFAULT_CONNECT_TIMEOUT_MILLIS;
import static com.datastax.driver.core.SocketOptions.DEFAULT_READ_TIMEOUT_MILLIS;

public class CassandraClient {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(CassandraClient.class);

  private Cluster cluster;
  private Session session;

  public CassandraConfiguration config;

  public CassandraClient(CassandraConfiguration config) throws Exception {
    this.config = config;
    org.apache.cassandra.config.Config.setClientMode(true);
  }

  public void start() throws Exception {

    Preconditions.checkNotNull(config);

    LOGGER.info("CassandraClient.start {}", config);

    Cluster.Builder builder = Cluster.builder()
        .withPort(config.getPort().intValue())
        .withoutJMXReporting()
        .withoutMetrics()
        .withSocketOptions(
            new SocketOptions()
                .setConnectTimeoutMillis(DEFAULT_CONNECT_TIMEOUT_MILLIS*10)
                .setReadTimeoutMillis(DEFAULT_READ_TIMEOUT_MILLIS*10)
        );

    if( config.getSsl() != null && config.getSsl().getEnabled() == true) {

      Ssl ssl = config.getSsl();

      KeyStore ks = KeyStore.getInstance("JKS");

      InputStream trustStore = new FileInputStream(ssl.getTrustStore());
      ks.load(trustStore, ssl.getTrustStorePassword().toCharArray());
      InputStream keyStore = new FileInputStream(ssl.getKeyStore());
      ks.load(keyStore, ssl.getKeyStorePassword().toCharArray());

      TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
      tmf.init(ks);

      KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
      kmf.init(ks, ssl.getKeyStorePassword().toCharArray());

      SSLContext sslContext = SSLContext.getInstance("SSLv3");
      sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

      SSLOptions sslOptions = JdkSSLOptions.builder()
          .withSSLContext(sslContext)
          .build();

      builder = builder.withSSL(sslOptions);
    }

    Collection<InetSocketAddress> addresses = Lists.newArrayList();
    for (String h : config.getHosts()) {
      LOGGER.info("Adding Host: {}", h);
      InetSocketAddress socketAddress = new InetSocketAddress(h, config.getPort().intValue());
      addresses.add(socketAddress);
    }
    builder.addContactPointsWithPorts(addresses);

    if( !Strings.isNullOrEmpty(config.getUser()) &&
        !Strings.isNullOrEmpty(config.getPassword())) {
      builder.withCredentials(config.getUser(), config.getPassword());
    }
    cluster = builder.build();

    Preconditions.checkNotNull(cluster);

    try {
      Metadata metadata = cluster.getMetadata();
      LOGGER.info("Connected to cluster: {}\n",
          metadata.getClusterName());
      for ( Host host : metadata.getAllHosts() ) {
        LOGGER.info("Datacenter: {}; Host: {}; Rack: {}\n",
            host.getDatacenter(), host.getAddress(), host.getRack());
      }
    } catch( Exception e ) {
      LOGGER.error("Exception: {}", e);
      throw e;
    }

    try {
      session = cluster.connect();
    } catch( Exception e ) {
      LOGGER.error("Exception: {}", e);
      throw e;
    }

    Preconditions.checkNotNull(session);

  }

  public void stop() throws Exception {
    session.close();
    cluster.close();
  }

  public CassandraConfiguration config() {
    return config;
  }

  public Session client() {
    return session;
  }

  public Cluster cluster() {
    return cluster;
  }
}
