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

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.GuidUtils;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.BoundStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class CassandraPersistWriter implements StreamsPersistWriter, Runnable, Flushable, Closeable {

  public static final String STREAMS_ID = "CassandraPersistWriter";

  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraPersistWriter.class);

  private static final long MAX_WRITE_LATENCY = 1000;

  protected volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper = StreamsJacksonMapper.getInstance();
  private volatile AtomicLong lastWrite = new AtomicLong(System.currentTimeMillis());
  private ScheduledExecutorService backgroundFlushTask = Executors.newSingleThreadScheduledExecutor();

  private CassandraConfiguration config;
  private CassandraClient client;

  private Session session;

  protected PreparedStatement insertStatement;

  protected List<BoundStatement> insertBatch = new ArrayList<>();

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  public CassandraPersistWriter() {
    this(new ComponentConfigurator<>(CassandraConfiguration.class).detectConfiguration());
  }

  public CassandraPersistWriter(CassandraConfiguration config) {
    this.config = config;
  }

  public void setPersistQueue(Queue<StreamsDatum> persistQueue) {
    this.persistQueue = persistQueue;
  }

  public Queue<StreamsDatum> getPersistQueue() {
    return persistQueue;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void write(StreamsDatum streamsDatum) {

    ObjectNode node;

    if (streamsDatum.getDocument() instanceof String) {
      try {
        node = mapper.readValue((String) streamsDatum.getDocument(), ObjectNode.class);

        byte[] value = node.toString().getBytes();

        String key = GuidUtils.generateGuid(node.toString());
        if(!Objects.isNull(streamsDatum.getMetadata().get("id"))) {
          key = streamsDatum.getMetadata().get("id").toString();
        }

        BoundStatement statement = insertStatement.bind(key, ByteBuffer.wrap(value));
        insertBatch.add(statement);
      } catch (IOException ex) {
        LOGGER.warn("Failure adding object: {}", streamsDatum.getDocument().toString());
        return;
      }
    } else {
      try {
        node = mapper.valueToTree(streamsDatum.getDocument());

        byte[] value = node.toString().getBytes();

        String key = GuidUtils.generateGuid(node.toString());
        if(!Objects.isNull(streamsDatum.getId())) {
          key = streamsDatum.getId();
        }

        BoundStatement statement = insertStatement.bind(key, ByteBuffer.wrap(value));
        insertBatch.add(statement);
      } catch (Exception ex) {
        LOGGER.warn("Failure adding object: {}", streamsDatum.getDocument().toString());
        return;
      }
    }

    flushIfNecessary();
  }

  @Override
  public void flush() throws IOException {
    try {
      LOGGER.debug("Attempting to flush {} items to cassandra", insertBatch.size());
      lock.writeLock().lock();

      BatchStatement batchStatement = new BatchStatement();
      batchStatement.addAll(insertBatch);
      session.execute(batchStatement);

      lastWrite.set(System.currentTimeMillis());
      insertBatch = new ArrayList<>();
    } finally {
      lock.writeLock().unlock();
    }
  }

  @Override
  public synchronized void close() throws IOException {
    session.close();
    client.cluster().close();
    backgroundFlushTask.shutdownNow();
  }

  /**
   * start write thread.
   */
  public void start() {
    try {
      connectToCassandra();
      client.start();
      createKeyspaceAndTable();
      createInsertStatement();
    } catch (Exception e) {
      LOGGER.error("Exception", e);
      return;
    }
    backgroundFlushTask.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        flushIfNecessary();
      }
    }, 0, MAX_WRITE_LATENCY * 2, TimeUnit.MILLISECONDS);
  }

  /**
   * stop.
   */
  public void stop() {
    try {
      flush();
    } catch (IOException ex) {
      LOGGER.error("Error flushing", ex);
    }

    try {
      close();
    } catch (IOException ex) {
      LOGGER.error("Error closing", ex);
    }

    try {
      backgroundFlushTask.shutdown();
      // Wait a while for existing tasks to terminate
      if (!backgroundFlushTask.awaitTermination(15, TimeUnit.SECONDS)) {
        backgroundFlushTask.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!backgroundFlushTask.awaitTermination(15, TimeUnit.SECONDS)) {
          LOGGER.error("Stream did not terminate");
        }
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      backgroundFlushTask.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void run() {
    while (true) {
      if (persistQueue.peek() != null) {
        try {
          StreamsDatum entry = persistQueue.remove();
          write(entry);
        } catch (Exception ex) {
          LOGGER.warn("Failure writing entry from Queue: {}", ex.getMessage());
        }
      }
      try {
        Thread.sleep(new Random().nextInt(1));
      } catch (InterruptedException interrupt) {
        LOGGER.trace("Interrupt", interrupt);
      }
    }
  }

  @Override
  public void prepare(Object configurationObject) {
    this.persistQueue = new ConcurrentLinkedQueue<>();
    start();
  }

  @Override
  public void cleanUp() {
    stop();
  }

  protected void flushIfNecessary() {
    long lastLatency = System.currentTimeMillis() - lastWrite.get();
    //Flush iff the size > 0 AND the size is divisible by 100 or the time between now and the last flush is greater
    //than the maximum desired latency
    if (insertBatch.size() > 0 && (insertBatch.size() % 100 == 0 || lastLatency > MAX_WRITE_LATENCY)) {
      try {
        flush();
      } catch (IOException ex) {
        LOGGER.error("Error writing to Cassandra", ex);
      }
    }
  }

  private synchronized void connectToCassandra() throws Exception {
    client = new CassandraClient(config);
  }

  private void createKeyspaceAndTable() {
    Metadata metadata = client.cluster().getMetadata();
    if (Objects.isNull(metadata.getKeyspace(config.getKeyspace()))) {
      LOGGER.info("Keyspace {} does not exist. Creating Keyspace", config.getKeyspace());
      Map<String, Object> replication = new HashMap<>();
      replication.put("class", "SimpleStrategy");
      replication.put("replication_factor", 1);

      String createKeyspaceStmt = SchemaBuilder.createKeyspace(config.getKeyspace()).with()
          .replication(replication).getQueryString();
      client.cluster().connect().execute(createKeyspaceStmt);
    }

    session = client.cluster().connect(config.getKeyspace());

    KeyspaceMetadata ks = metadata.getKeyspace(config.getKeyspace());
    TableMetadata tableMetadata = ks.getTable(config.getTable());

    if (Objects.isNull(tableMetadata)) {
      LOGGER.info("Table {} does not exist in Keyspace {}. Creating Table", config.getTable(), config.getKeyspace());
      String createTableStmt = SchemaBuilder.createTable(config.getTable())
                                .addPartitionKey(config.getPartitionKeyColumn(), DataType.varchar())
                                .addColumn(config.getColumn(), DataType.blob()).getQueryString();

      session.execute(createTableStmt);
    }
  }

  private void createInsertStatement() {
    Insert insertBuilder = QueryBuilder.insertInto(config.getTable());
    insertBuilder.value(config.getPartitionKeyColumn(), new Object());
    insertBuilder.value(config.getColumn(), new Object());
    insertStatement = session.prepare(insertBuilder.getQueryString());
  }
}