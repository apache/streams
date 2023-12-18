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

package org.apache.streams.hbase;

import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.util.GuidUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * HbasePersistWriter writes to hbase.
 */
public class HbasePersistWriter implements StreamsPersistWriter, Flushable, Closeable {

  public static final String STREAMS_ID = "HbasePersistWriter";

  private static final Logger LOGGER = LoggerFactory.getLogger(HbasePersistWriter.class);

  protected ConnectionFactory connectionFactory;
  protected Connection connection;
  protected HTableDescriptor descriptor;

  protected BufferedMutator bufferedMutator;

  protected volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper = new ObjectMapper();

  private HbaseConfiguration config;

  /**
   * HbasePersistWriter constructor - resolve HbaseConfiguration from JVM 'hbase'.
   */
  public HbasePersistWriter() {
    this.config = new ComponentConfigurator<>(HbaseConfiguration.class).detectConfiguration();
    this.persistQueue = new ConcurrentLinkedQueue<>();
  }

  /**
   * HbasePersistWriter constructor - use supplied persistQueue.
   * @param hbaseConfiguration HbaseConfiguration
   */
  // TODO: refactor this to use HbaseConfiguration
  public HbasePersistWriter(HbaseConfiguration hbaseConfiguration) {
    this.config = hbaseConfiguration;
    this.persistQueue = new ConcurrentLinkedQueue<>();
  }

  private synchronized void connectToHbase() {

    // TODO: refactor this to resolve this stuff from typesafe
    Configuration configuration = new Configuration();
    configuration.set("hbase.rootdir", config.getRootdir());
    configuration.set("zookeeper.znode.parent", config.getParent());
    configuration.set("zookeeper.znode.rootserver", config.getRootserver());
    configuration.set("hbase.cluster.distributed", "false");
    configuration.set("hbase.zookeeper.quorum", config.getQuorum());
    configuration.set("hbase.zookeeper.property.clientPort", Long.toString(config.getClientPort()));
    configuration.setInt("zookeeper.session.timeout", 1000);

    configuration.setInt("timeout", 1000);

    //pool = new HTablePool(configuration, 10);
    try {
      connection = ConnectionFactory.createConnection(configuration);
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }

    try {
      bufferedMutator = connection.getBufferedMutator(TableName.valueOf(config.getTable()));
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
    //

    try {
      LOGGER.info("Table : {}", descriptor);
    } catch (Exception ex) {
      LOGGER.error("There was an error connecting to HBase, please check your settings and try again");
      ex.printStackTrace();
    }
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void write(StreamsDatum streamsDatum) {

    ObjectNode node;
    byte[] row;
    if (StringUtils.isNotBlank(streamsDatum.getId())) {
      row = streamsDatum.getId().getBytes();
    } else {
      row = GuidUtils.generateGuid(streamsDatum.toString()).getBytes();
    }
    Put put = new Put(row);
    if ( streamsDatum.getDocument() instanceof String ) {
      try {
        node = mapper.readValue((String)streamsDatum.getDocument(), ObjectNode.class);
      } catch (IOException ex) {
        ex.printStackTrace();
        LOGGER.warn("Invalid json: {}", streamsDatum.getDocument().toString());
        return;
      }
      try {
        byte[] value = node.binaryValue();
        bufferedMutator.mutate(put);
      } catch (IOException ex) {
        ex.printStackTrace();
        LOGGER.warn("Failure adding object: {}", streamsDatum.getDocument().toString());
        return;
      }
    } else {
      try {
        node = mapper.valueToTree(streamsDatum.getDocument());
      } catch (Exception ex) {
        ex.printStackTrace();
        LOGGER.warn("Invalid json: {}", streamsDatum.getDocument().toString());
        return;
      }
      put.setId(GuidUtils.generateGuid(node.toString()));

    }

    try {
      bufferedMutator.mutate(put);
    } catch (IOException ex) {
      ex.printStackTrace();
      LOGGER.warn("Failure preparing put: {}", streamsDatum.getDocument().toString());
      return;
    }

  }

  public void flush() throws IOException {
    bufferedMutator.flush();
  }

  public synchronized void close() throws IOException {
    bufferedMutator.close();
  }

  @Override
  public void prepare(Object configurationObject) {

    connectToHbase();

    Thread task = new Thread(new HbasePersistWriterTask(this));
    task.start();

    try {
      task.join();
    } catch (InterruptedException ex) {
      ex.printStackTrace();
    }

  }

  @Override
  public void cleanUp() {

    try {
      flush();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
    try {
      close();
    } catch (IOException ex) {
      ex.printStackTrace();
    }

  }
}
