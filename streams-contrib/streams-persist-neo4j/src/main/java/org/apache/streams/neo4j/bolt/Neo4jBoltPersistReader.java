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

package org.apache.streams.neo4j.bolt;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.neo4j.Neo4jReaderConfiguration;
import org.apache.streams.util.PropertyUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Queues;

import org.joda.time.DateTime;
import org.neo4j.driver.internal.value.NodeValue;
import org.neo4j.driver.internal.value.RelationshipValue;
import org.neo4j.driver.internal.value.StringValue;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.types.Node;
import org.neo4j.driver.v1.types.Relationship;
import org.neo4j.driver.v1.util.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.annotation.Nullable;


/**
 * Neo4jBoltPersistReader reads documents from neo4j.
 */
public class Neo4jBoltPersistReader implements StreamsPersistReader {

  public static final String STREAMS_ID = "CassandraPersistReader";

  public static final Logger LOGGER = LoggerFactory.getLogger(Neo4jBoltPersistReader.class);

  protected volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  private ExecutorService executor;
  private CompletableFuture<Boolean> readerTaskFuture = new CompletableFuture<>();

  private Neo4jReaderConfiguration config;

  protected Neo4jBoltClient client;

//  protected Cluster cluster;
//  protected Session session;
//
//  protected String keyspace;
//  protected String table;
  protected StatementResult statementResult;

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * Neo4jBoltPersistReader constructor - resolves Neo4jReaderConfiguration from JVM 'neo4j'.
   */
  public Neo4jBoltPersistReader() {
    this.config = new ComponentConfigurator<>(Neo4jReaderConfiguration.class)
      .detectConfiguration(StreamsConfigurator.getConfig().getConfig("neo4j"));
  }

  /**
   * Neo4jBoltPersistReader constructor - uses supplied Neo4jReaderConfiguration.
   * @param config config
   */
  public Neo4jBoltPersistReader(Neo4jReaderConfiguration config) {
    this.config = config;
  }

  /**
   * Neo4jBoltPersistReader constructor - uses supplied persistQueue.
   * @param persistQueue persistQueue
   */
  public Neo4jBoltPersistReader(Queue<StreamsDatum> persistQueue) {
    this();
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
    if( configurationObject instanceof Neo4jReaderConfiguration ) {
      this.config = (Neo4jReaderConfiguration) configurationObject;
    }
    this.client = Neo4jBoltClient.getInstance(this.config);

    persistQueue = constructQueue();

    executor = Executors.newSingleThreadExecutor();
  }

  @Override
  public void cleanUp() {
    stop();
  }

  protected Optional<StreamsDatum> buildDatum(Record record) {
    ObjectNode objectNode;

    if( record != null ) {
      ObjectNode valueJson = null;
      Map<String, ObjectNode> valueJsons = record.asMap(neo4jObjectNodeFunction);
      if( valueJsons.size() == 1) {
        valueJson = valueJsons.get(valueJsons.keySet().iterator().next());
      }
      objectNode = PropertyUtil.getInstance(mapper).unflattenObjectNode(valueJson);
      return Optional.of(new StreamsDatum(objectNode));
    }

    return Optional.empty();
  }

  @Override
  public StreamsResultSet readAll() {

    Session session = null;

    String query = config.getQuery();
    Map<String, Object> params = mapper.convertValue(config.getParams(), Map.class);

    try {
      session = client.client().session();
      Transaction transaction = session.beginTransaction();

      this.statementResult = client.client().session().beginTransaction().run(query, params);

      while( statementResult.hasNext()) {
        Record record = statementResult.next();
        Optional<StreamsDatum> datum = buildDatum(record);
        if( datum.isPresent()) {
          write(datum.get());
        }
      }

    } catch(Exception ex) {
      LOGGER.warn("Exception", ex);
    } finally {
      if( session != null ) {
        session.close();
      }
    }
    return readCurrent();

  }

  @Override
  public void startStream() {
    LOGGER.debug("startStream");
    Neo4jBoltPersistReaderTask readerTask = new Neo4jBoltPersistReaderTask(this);

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

  private static String readAllStatement() {
    return "MATCH (v:streams)";
  }

  public class Neo4jBoltPersistReaderTask implements Runnable {

    private Neo4jBoltPersistReader reader;

    public Neo4jBoltPersistReaderTask(Neo4jBoltPersistReader reader) {
      this.reader = reader;
    }

    @Override
    public void run() {
      try {
        while (reader.statementResult.hasNext()) {
          Record record = statementResult.next();
          Optional<StreamsDatum> datum = reader.buildDatum(record);
          if( datum.isPresent() ) {
            reader.write(datum.get());
          }
        }
      } finally {
        readerTaskFuture.complete(true);
      }
    }
  }

  Function<Value, ObjectNode> neo4jObjectNodeFunction = new Function<Value, ObjectNode>() {

    @Nullable
    @Override
    public ObjectNode apply(@Nullable Value value) {
      ObjectNode resultNode = null;
      if (value instanceof StringValue) {
        StringValue stringValue = (StringValue) value;
        String string = stringValue.asLiteralString();
        try {
          resultNode = mapper.readValue(string, ObjectNode.class);
        } catch (IOException ex) {
          LOGGER.error("IOException", ex);
        }
      } else if ( value instanceof NodeValue) {
        NodeValue nodeValue = (NodeValue) value;
        Node node = nodeValue.asNode();
        Map<String, Object> nodeMap = node.asMap();
        resultNode = PropertyUtil.getInstance(mapper).unflattenMap(nodeMap);
      } else if (value instanceof RelationshipValue) {
        RelationshipValue relationshipValue = (RelationshipValue) value;
        Relationship relationship = relationshipValue.asRelationship();
        Map<String, Object> relationshipMap = relationship.asMap();
        resultNode = PropertyUtil.getInstance(mapper).unflattenMap(relationshipMap);
      }
      return resultNode;
    }
  };
}