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

import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Queues;
import org.elasticsearch.search.SearchHit;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * ElasticsearchPersistReader reads documents from elasticsearch.
 */
public class ElasticsearchPersistReader implements StreamsPersistReader, Serializable {

  public static final String STREAMS_ID = "ElasticsearchPersistReader";

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPersistReader.class);

  protected volatile Queue<StreamsDatum> persistQueue;

  private ElasticsearchQuery elasticsearchQuery;
  private ElasticsearchReaderConfiguration config;
  private int threadPoolSize = 10;
  private ExecutorService executor;
  private ReadWriteLock lock = new ReentrantReadWriteLock();
  private Future<?> readerTask;

  public ElasticsearchPersistReader() {
  }

  public ElasticsearchPersistReader(ElasticsearchReaderConfiguration config) {
    this.config = config;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  //PersistReader methods
  @Override
  public void startStream() {
    LOGGER.debug("startStream");
    executor = Executors.newSingleThreadExecutor();
    readerTask = executor.submit(new ElasticsearchPersistReaderTask(this, elasticsearchQuery));
  }

  @Override
  public void prepare(Object configuration) {
    elasticsearchQuery = this.config == null ? new ElasticsearchQuery() : new ElasticsearchQuery(config);
    elasticsearchQuery.execute(configuration);
    persistQueue = constructQueue();
  }

  @Override
  public StreamsResultSet readAll() {
    return readCurrent();
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

  //TODO - This just reads current records and does not adjust any queries
  @Override
  public StreamsResultSet readNew(BigInteger sequence) {
    return readCurrent();
  }

  //TODO - This just reads current records and does not adjust any queries
  @Override
  public StreamsResultSet readRange(DateTime start, DateTime end) {
    return readCurrent();
  }

  //If we still have data in the queue, we are still running
  @Override
  public boolean isRunning() {
    return persistQueue.size() > 0 || (!readerTask.isDone() && !readerTask.isCancelled());
  }

  @Override
  public void cleanUp() {
    this.shutdownAndAwaitTermination(executor);
    LOGGER.info("PersistReader done");
    if ( elasticsearchQuery != null ) {
      elasticsearchQuery.cleanUp();
    }
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

  protected void shutdownAndAwaitTermination(ExecutorService pool) {
    pool.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait a while for existing tasks to terminate
      if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
        pool.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
          LOGGER.error("Pool did not terminate");
        }
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      pool.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }

  private Queue<StreamsDatum> constructQueue() {
    return Queues.synchronizedQueue(new LinkedBlockingQueue<StreamsDatum>(10000));
  }

  public static class ElasticsearchPersistReaderTask implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPersistReaderTask.class);

    private ElasticsearchPersistReader reader;
    private ElasticsearchQuery query;
    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public ElasticsearchPersistReaderTask(ElasticsearchPersistReader reader, ElasticsearchQuery query) {
      this.reader = reader;
      this.query = query;
    }

    @Override
    public void run() {

      StreamsDatum item;
      while (query.hasNext()) {
        SearchHit hit = query.next();
        ObjectNode jsonObject;
        try {
          jsonObject = mapper.readValue(hit.getSourceAsString(), ObjectNode.class);
          item = new StreamsDatum(jsonObject, hit.getId());
          item.getMetadata().put("id", hit.getId());
          item.getMetadata().put("index", hit.getIndex());
          item.getMetadata().put("type", hit.getType());
          if ( hit.fields().containsKey("_timestamp")) {
            DateTime timestamp = new DateTime(((Long) hit.field("_timestamp").getValue()).longValue());
            item.setTimestamp(timestamp);
          }
          if ( hit.fields().containsKey("_parent")) {
            item.getMetadata().put("parent", hit.fields().get("_parent").value());
          }
          reader.write(item);
        } catch (IOException ex) {
          LOGGER.warn("Unable to process json source: ", hit.getSourceAsString());
        }

      }
      try {
        Thread.sleep(new Random().nextInt(100));
      } catch (InterruptedException ex) {
        LOGGER.warn("Thread interrupted", ex);
      }

    }
  }
}


