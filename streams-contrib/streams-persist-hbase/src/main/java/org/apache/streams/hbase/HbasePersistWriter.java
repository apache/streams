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

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.util.GuidUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.client.HConnection;
import org.apache.hadoop.hbase.client.HConnectionManager;
import org.apache.hadoop.hbase.client.HTable;
import org.apache.hadoop.hbase.client.HTableInterface;
import org.apache.hadoop.hbase.client.HTablePool;
import org.apache.hadoop.hbase.client.Put;
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

  protected HConnection connection;
  protected HTablePool pool;
  protected HTableInterface table;
  protected HTableDescriptor descriptor;

  protected volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper = new ObjectMapper();

  private HbaseConfiguration config;

  /**
   * HbasePersistWriter constructor - resolve HbaseConfiguration from JVM 'hbase'.
   */
  public HbasePersistWriter() {
    this.config = new ComponentConfigurator<>(HbaseConfiguration.class)
        .detectConfiguration(StreamsConfigurator.getConfig().getConfig("hbase"));
    this.persistQueue  = new ConcurrentLinkedQueue<>();
  }

  /**
   * HbasePersistWriter constructor - use supplied persistQueue.
   * @param persistQueue persistQueue
   */
  // TODO: refactor this to use HbaseConfiguration
  public HbasePersistWriter(Queue<StreamsDatum> persistQueue) {
    this.config = new ComponentConfigurator<>(HbaseConfiguration.class)
        .detectConfiguration(StreamsConfigurator.getConfig().getConfig("hbase"));
    this.persistQueue = persistQueue;
  }

  private synchronized void connectToHbase() {

    // TODO: refactor this to resolve this stuff from typesafe
    Configuration configuration = new Configuration();
    configuration.set("hbase.rootdir", config.getRootdir());
    configuration.set("zookeeper.znode.parent", config.getParent());
    configuration.set("zookeeper.znode.rootserver", config.getRootserver());
    //configuration.set("hbase.master", config.getRootserver());
    //configuration.set("hbase.cluster.distributed", "false");
    configuration.set("hbase.zookeeper.quorum", config.getQuorum());
    configuration.set("hbase.zookeeper.property.clientPort", Long.toString(config.getClientPort()));
    configuration.setInt("zookeeper.session.timeout", 1000);

    configuration.setInt("timeout", 1000);

    //pool = new HTablePool(configuration, 10);
    try {
      connection = HConnectionManager.createConnection(configuration);
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }

    try {
      //    table = new HTable(configuration, config.getTable());
      //    table = (HTable) pool.getTable(config.getTable());
      table = new HTable(configuration, config.getTable().getBytes());
      table.setAutoFlush(true);
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }
    //

    try {
      descriptor = table.getTableDescriptor();
    } catch (Exception ex) {
      ex.printStackTrace();
      return;
    }

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
    Put put = new Put();
    if ( streamsDatum.getDocument() instanceof String ) {
      try {
        node = mapper.readValue((String)streamsDatum.getDocument(), ObjectNode.class);
      } catch (IOException ex) {
        ex.printStackTrace();
        LOGGER.warn("Invalid json: {}", streamsDatum.getDocument().toString());
        return;
      }
      put.setId(GuidUtils.generateGuid(node.toString()));
      try {
        byte[] value = node.binaryValue();
        put.add(config.getFamily().getBytes(), config.getQualifier().getBytes(), value);
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
      try {
        byte[] value = node.binaryValue();
        put.add(config.getFamily().getBytes(), config.getQualifier().getBytes(), value);
      } catch (IOException ex) {
        ex.printStackTrace();
        LOGGER.warn("Failure preparing put: {}", streamsDatum.getDocument().toString());
        return;
      }
    }
    try {
      table.put(put);
    } catch (IOException ex) {
      ex.printStackTrace();
      LOGGER.warn("Failure executin put: {}", streamsDatum.getDocument().toString());
    }

  }

  public void flush() throws IOException {
    table.flushCommits();
  }

  public synchronized void close() throws IOException {
    table.close();
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
