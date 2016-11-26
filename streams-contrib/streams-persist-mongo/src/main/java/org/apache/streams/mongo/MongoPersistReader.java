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

package org.apache.streams.mongo;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * MongoPersistReader reads documents from mongo.
 */
public class MongoPersistReader implements StreamsPersistReader {

  public static final String STREAMS_ID = "MongoPersistReader";

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoPersistReader.class);

  protected volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper = StreamsJacksonMapper.getInstance();
  private volatile AtomicLong lastWrite = new AtomicLong(System.currentTimeMillis());

  private ExecutorService executor;

  private MongoConfiguration config;

  protected MongoClient client;
  protected DB db;
  protected DBCollection collection;

  protected DBCursor cursor;

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * KafkaPersistReader constructor - resolves KafkaConfiguration from JVM 'mongo'.
   */
  public MongoPersistReader() {
    this.config = new ComponentConfigurator<>(MongoConfiguration.class)
        .detectConfiguration(StreamsConfigurator.getConfig().getConfig("mongo"));
  }

  /**
   * KafkaPersistReader constructor - uses supplied MongoConfiguration.
   * @param config config
   */
  public MongoPersistReader(MongoConfiguration config) {
    this.config = config;
  }

  /**
   * KafkaPersistReader constructor - uses supplied persistQueue.
   * @param persistQueue persistQueue
   */
  public MongoPersistReader(Queue<StreamsDatum> persistQueue) {
    this.config = new ComponentConfigurator<>(MongoConfiguration.class)
        .detectConfiguration(StreamsConfigurator.getConfig().getConfig("mongo"));
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

    connectToMongo();

    if ( client == null
        || collection == null ) {
      throw new RuntimeException("Unable to connect!");
    }
    cursor = collection.find();

    if ( cursor == null
        || !cursor.hasNext()) {
      throw new RuntimeException("Collection not present or empty!");
    }

    persistQueue = constructQueue();

    executor = Executors.newSingleThreadExecutor();

  }

  @Override
  public void cleanUp() {
    stop();
  }

  protected StreamsDatum prepareDatum(DBObject dbObject) {

    ObjectNode objectNode;
    String id;

    try {
      objectNode = mapper.readValue(dbObject.toString(), ObjectNode.class);
      id = objectNode.get("_id").get("$oid").asText();
      objectNode.remove("_id");
    } catch (IOException ex) {
      LOGGER.warn("document isn't valid JSON.");
      return null;
    }

    return new StreamsDatum(objectNode, id);
  }

  private synchronized void connectToMongo() {

    ServerAddress serverAddress = new ServerAddress(config.getHost(), config.getPort().intValue());

    if (!Strings.isNullOrEmpty(config.getUser()) && !Strings.isNullOrEmpty(config.getPassword())) {
      MongoCredential credential =
          MongoCredential.createCredential(config.getUser(), config.getDb(), config.getPassword().toCharArray());
      client = new MongoClient(serverAddress, Lists.newArrayList(credential));
    } else {
      client = new MongoClient(serverAddress);
    }

    db = client.getDB(config.getDb());

    if (!db.collectionExists(config.getCollection())) {
      db.createCollection(config.getCollection(), null);
    }

    collection = db.getCollection(config.getCollection());
  }

  @Override
  public StreamsResultSet readAll() {

    try (DBCursor cursor = collection.find()) {
      while (cursor.hasNext()) {
        DBObject dbObject = cursor.next();
        StreamsDatum datum = prepareDatum(dbObject);
        write(datum);
      }
    }

    return readCurrent();
  }

  @Override
  public void startStream() {

    LOGGER.debug("startStream");
    MongoPersistReaderTask readerTask = new MongoPersistReaderTask(this);
    Thread readerTaskThread = new Thread(readerTask);
    Future future = executor.submit(readerTaskThread);

    while ( !future.isDone() && !future.isCancelled()) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException interrupt) {
        LOGGER.trace("Interrupt", interrupt);
      }
    }

    executor.shutdown();

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

  //The locking may appear to be counter intuitive but we really don't care if multiple threads offer to the queue
  //as it is a synchronized queue.  What we do care about is that we don't want to be offering to the current reference
  //if the queue is being replaced with a new instance
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

  public class MongoPersistReaderTask implements Runnable {

    private MongoPersistReader reader;

    public MongoPersistReaderTask(MongoPersistReader reader) {
      this.reader = reader;
    }

    @Override
    public void run() {

      try {
        while (reader.cursor.hasNext()) {
          DBObject dbObject = reader.cursor.next();
          StreamsDatum datum = reader.prepareDatum(dbObject);
          reader.write(datum);
        }
      } finally {
        reader.cursor.close();
      }

    }

  }
}
