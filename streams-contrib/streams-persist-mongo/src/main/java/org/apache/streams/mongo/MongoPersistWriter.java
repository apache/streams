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
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MongoPersistWriter implements StreamsPersistWriter, Runnable, Flushable, Closeable {

  public static final String STREAMS_ID = "MongoPersistWriter";

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoPersistWriter.class);

  private static final long MAX_WRITE_LATENCY = 1000;

  protected volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper = StreamsJacksonMapper.getInstance();
  private volatile AtomicLong lastWrite = new AtomicLong(System.currentTimeMillis());
  private ScheduledExecutorService backgroundFlushTask = Executors.newSingleThreadScheduledExecutor();

  private MongoConfiguration config;

  protected MongoClient client;
  protected DB db;
  protected DBCollection collection;

  protected List<DBObject> insertBatch = new ArrayList<>();

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  public MongoPersistWriter() {
    this(new ComponentConfigurator<>(MongoConfiguration.class)
        .detectConfiguration(StreamsConfigurator.getConfig().getConfig("mongo")));
  }

  public MongoPersistWriter(MongoConfiguration config) {
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

    DBObject dbObject = prepareObject(streamsDatum);
    if (dbObject != null) {
      addToBatch(dbObject);
      flushIfNecessary();
    }
  }

  @Override
  public void flush() throws IOException {
    try {
      LOGGER.debug("Attempting to flush {} items to mongo", insertBatch.size());
      lock.writeLock().lock();
      collection.insert(insertBatch);
      lastWrite.set(System.currentTimeMillis());
      insertBatch = new ArrayList<>();
    } finally {
      lock.writeLock().unlock();
    }

  }

  public synchronized void close() throws IOException {
    client.close();
    backgroundFlushTask.shutdownNow();
  }

  /**
   * start write thread.
   */
  public void start() {
    connectToMongo();
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
          ex.printStackTrace();
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
        LOGGER.error("Error writing to Mongo", ex);
      }
    }
  }

  protected void addToBatch(DBObject dbObject) {
    try {
      lock.readLock().lock();
      insertBatch.add(dbObject);
    } finally {
      lock.readLock().unlock();
    }
  }

  protected DBObject prepareObject(StreamsDatum streamsDatum) {
    DBObject dbObject = null;
    if (streamsDatum.getDocument() instanceof String) {
      dbObject = (DBObject) JSON.parse((String) streamsDatum.getDocument());
    } else {
      try {
        ObjectNode node = mapper.valueToTree(streamsDatum.getDocument());
        dbObject = (DBObject) JSON.parse(node.toString());
      } catch (Exception ex) {
        LOGGER.error("Unsupported type: " + streamsDatum.getDocument().getClass(), ex);
      }
    }
    return dbObject;
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


}
