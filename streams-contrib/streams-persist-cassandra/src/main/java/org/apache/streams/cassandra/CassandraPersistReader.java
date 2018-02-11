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
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.DataType;
import com.datastax.driver.core.KeyspaceMetadata;
import com.datastax.driver.core.Metadata;
import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.TableMetadata;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Queues;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * CassandraPersistReader reads documents from cassandra.
 */
public class CassandraPersistReader implements StreamsPersistReader {

  public static final String STREAMS_ID = "CassandraPersistReader";

  public static final Logger LOGGER = LoggerFactory.getLogger(CassandraPersistReader.class);

  protected volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  private ExecutorService executor;
  private CompletableFuture<Boolean> readerTaskFuture = new CompletableFuture<>();

  private CassandraConfiguration config;
  private CassandraClient client;

  protected Iterator<Row> rowIterator;

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * CassandraPersistReader constructor
   */
  public CassandraPersistReader() {
    this.config = new ComponentConfigurator<>(CassandraConfiguration.class).detectConfiguration();
  }

  /**
   * CassandraPersistReader constructor - uses supplied CassandraConfiguration.
   * @param config config
   */
  public CassandraPersistReader(CassandraConfiguration config) {
    this.config = config;
  }

  /**
   * CassandraPersistReader constructor - uses supplied persistQueue.
   * @param persistQueue persistQueue
   */
  public CassandraPersistReader(Queue<StreamsDatum> persistQueue) {
    this.config = new ComponentConfigurator<>(CassandraConfiguration.class).detectConfiguration();
    this.persistQueue = persistQueue;
  }

  public void setPersistQueue(Queue<StreamsDatum> persistQueue) {
    this.persistQueue = persistQueue;
  }

  public Queue<StreamsDatum> getPersistQueue() {
    return persistQueue;
  }

  public void stop() {
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void prepare(Object configurationObject) {
    try {
      connectToCassandra();
      client.start();
    } catch (Exception e) {
      LOGGER.error("Exception", e);
      return;
    }

    String selectStatement = getSelectStatement();
    ResultSet rs = client.client().execute(selectStatement);
    rowIterator = rs.iterator();

    if (!rowIterator.hasNext()) {
      throw new RuntimeException("Table" + config.getTable() + "is empty!");
    }

    persistQueue = constructQueue();

    executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void cleanUp() {
    stop();
  }

  protected StreamsDatum prepareDatum(Row row) {
    ObjectNode objectNode;

    try {
      byte[] value = row.getBytes(config.getColumn()).array();
      objectNode = mapper.readValue(value, ObjectNode.class);
    } catch (IOException ex) {
      LOGGER.warn("document isn't valid JSON.");
      return null;
    }

    return new StreamsDatum(objectNode);
  }

  private synchronized void connectToCassandra() throws Exception {

    client = new CassandraClient(config);

  }

  @Override
  public StreamsResultSet readAll() {
    ResultSet rs = client.client().execute(getSelectStatement());
    Iterator<Row> rowsIterator = rs.iterator();

    while (rowsIterator.hasNext()) {
      Row row = rowsIterator.next();
      StreamsDatum datum = prepareDatum(row);
      write(datum);
    }

    return readCurrent();
  }

  @Override
  public void startStream() {
    LOGGER.debug("startStream");
    CassandraPersistReaderTask readerTask = new CassandraPersistReaderTask(this);

    CompletableFuture.runAsync(readerTask, executor);

    try {
      if (readerTaskFuture.get()) {
        executor.shutdown();
      }
    } catch (InterruptedException ex) {
      LOGGER.trace("Interrupt", ex);
    } catch (ExecutionException ex) {
      LOGGER.trace("Execution exception", ex);
    }
  }

  @Override
  public StreamsResultSet readCurrent() {

    StreamsResultSet current;

    try {
      lock.writeLock().lock();
      current = new StreamsResultSet(persistQueue);
      current.setCounter(new DatumStatusCounter());
      persistQueue = constructQueue();
    } finally {
      lock.writeLock().unlock();
    }

    return current;
  }

  protected void write(StreamsDatum entry) {
    boolean success;
    do {
      try {
        lock.readLock().lock();
        success = persistQueue.offer(entry);
        Thread.yield();
      } finally {
        lock.readLock().unlock();
      }
    }
    while (!success);
  }

  @Override
  public StreamsResultSet readNew(BigInteger sequence) {
    return null;
  }

  @Override
  public StreamsResultSet readRange(DateTime start, DateTime end) {
    return null;
  }

  @Override
  public boolean isRunning() {
    return !executor.isTerminated() || !executor.isShutdown();
  }

  private Queue<StreamsDatum> constructQueue() {
    return Queues.synchronizedQueue(new LinkedBlockingQueue<StreamsDatum>(10000));
  }

  private String getSelectStatement() {
    return QueryBuilder.select().all()
      .from(config.getKeyspace(), config.getTable())
      .getQueryString();
  }

  public class CassandraPersistReaderTask implements Runnable {

    private CassandraPersistReader reader;

    public CassandraPersistReaderTask(CassandraPersistReader reader) {
      this.reader = reader;
    }

    @Override
    public void run() {
      try {
        while (reader.rowIterator.hasNext()) {
          Row row = reader.rowIterator.next();
          StreamsDatum datum = reader.prepareDatum(row);
          reader.write(datum);
        }
      } finally {
        readerTaskFuture.complete(true);
      }
    }
  }
}