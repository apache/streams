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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.util.JSON;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MongoPersistWriter implements StreamsPersistWriter, Runnable {

    public final static String STREAMS_ID = "MongoPersistWriter";

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoPersistWriter.class);

    private final static long MAX_WRITE_LATENCY = 1000;

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();
    private volatile AtomicLong lastWrite = new AtomicLong(System.currentTimeMillis());
    private ScheduledExecutorService backgroundFlushTask = Executors.newSingleThreadScheduledExecutor();

    private MongoConfiguration config;

    protected MongoClient client;
    protected DB db;
    protected DBCollection collection;

    protected List<DBObject> insertBatch = Lists.newArrayList();

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public MongoPersistWriter() {
        this(MongoConfigurator.detectConfiguration(StreamsConfigurator.config.getConfig("mongo")));
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

    public void flush() throws IOException {
        try {
            LOGGER.debug("Attempting to flush {} items to mongo", insertBatch.size());
            lock.writeLock().lock();
            collection.insert(insertBatch);
            lastWrite.set(System.currentTimeMillis());
            insertBatch = Lists.newArrayList();
        } finally {
            lock.writeLock().unlock();
        }

    }

    public synchronized void close() throws IOException {
//        client.cleanCursors(true);
//        backgroundFlushTask.shutdownNow();
    }

    public void start() {
        connectToMongo();
        backgroundFlushTask.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                flushIfNecessary();
            }
        }, 0, MAX_WRITE_LATENCY * 2, TimeUnit.MILLISECONDS);
    }

    public void stop() {

        try {
            flush();
        } catch (IOException e) {
            LOGGER.error("Error flushing", e);
        }
        try {
            close();
        } catch (IOException e) {
            LOGGER.error("Error closing", e);
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try {
                Thread.sleep(new Random().nextInt(1));
            } catch (InterruptedException e) {
            }
        }

    }

    @Override
    public void prepare(Object configurationObject) {
        this.persistQueue = new ConcurrentLinkedQueue<StreamsDatum>();
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
            } catch (IOException e) {
                LOGGER.error("Error writing to Mongo", e);
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
            } catch (Exception e) {
                LOGGER.error("Unsupported type: " + streamsDatum.getDocument().getClass(), e);
            }
        }
        return dbObject;
    }

    private synchronized void connectToMongo() {

        ServerAddress serverAddress = new ServerAddress(config.getHost(), config.getPort().intValue());

        if (!Strings.isNullOrEmpty(config.getUser()) && !Strings.isNullOrEmpty(config.getPassword())) {
            MongoCredential credential =
                    MongoCredential.createCredential(config.getUser(), config.getDb(), config.getPassword().toCharArray());
            client = new MongoClient(serverAddress, Lists.<MongoCredential>newArrayList(credential));
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
