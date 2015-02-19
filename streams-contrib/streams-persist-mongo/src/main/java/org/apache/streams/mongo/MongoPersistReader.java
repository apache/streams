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
import com.google.common.collect.Queues;
import com.mongodb.*;
import com.mongodb.util.JSON;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.*;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class MongoPersistReader implements StreamsPersistReader {

    public static final String STREAMS_ID = "MongoPersistReader";

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoPersistReader.class);
    private final static long MAX_WRITE_LATENCY = 1000;

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();
    private volatile AtomicLong lastWrite = new AtomicLong(System.currentTimeMillis());

    private ExecutorService executor;

    private MongoConfiguration config;
    private MongoPersistReaderTask readerTask;

    protected DB client;
    protected DBCollection collection;

    protected DBCursor cursor;

    protected List<DBObject> insertBatch = Lists.newArrayList();

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public MongoPersistReader() {
        Config config = StreamsConfigurator.config.getConfig("mongo");
        this.config = MongoConfigurator.detectConfiguration(config);
    }

    public MongoPersistReader(MongoConfiguration config) {
        this.config = config;
    }

    public MongoPersistReader(Queue<StreamsDatum> persistQueue) {
        Config config = StreamsConfigurator.config.getConfig("mongo");
        this.config = MongoConfigurator.detectConfiguration(config);
        this.persistQueue = persistQueue;
    }

    public void setPersistQueue(Queue<StreamsDatum> persistQueue) {
        this.persistQueue = persistQueue;
    }

    public Queue<StreamsDatum> getPersistQueue() {
        return persistQueue;
    }

    public void stop() {

        try {
            client.cleanCursors(true);
            client.requestDone();
        } catch (Exception e) {
        } finally {
            client.requestDone();
        }
    }

    @Override
    public void prepare(Object configurationObject) {

        connectToMongo();

        if( client == null ||
                collection == null )
            throw new RuntimeException("Unable to connect!");

        cursor = collection.find();

        if( cursor == null ||
            cursor.hasNext() == false )
            throw new RuntimeException("Collection not present or empty!");

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
        } catch (IOException e) {
            LOGGER.warn("document isn't valid JSON.");
            return null;
        }
        StreamsDatum datum = new StreamsDatum(objectNode, id);

        return datum;
    }

    private synchronized void connectToMongo() {

        try {
            client = new MongoClient(config.getHost(), config.getPort().intValue()).getDB(config.getDb());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        if (!Strings.isNullOrEmpty(config.getUser()) && !Strings.isNullOrEmpty(config.getPassword()))
            client.authenticate(config.getUser(), config.getPassword().toCharArray());

        if (!client.collectionExists(config.getCollection())) {
            client.createCollection(config.getCollection(), null);
        }
        ;

        collection = client.getCollection(config.getCollection());
    }

    @Override
    public StreamsResultSet readAll() {

        DBCursor cursor = collection.find();
        try {
            while(cursor.hasNext()) {
                DBObject dbObject = cursor.next();
                StreamsDatum datum = prepareDatum(dbObject);
                write(datum);
            }
        } finally {
            cursor.close();
        }

        return readCurrent();
    }

    @Override
    public void startStream() {

        LOGGER.debug("startStream");
        readerTask = new MongoPersistReaderTask(this);
        Thread readerTaskThread = new Thread(readerTask);
        Future future = executor.submit(readerTaskThread);

        while( !future.isDone() && !future.isCancelled()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}
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
//            current.getCounter().add(countersCurrent);
//            countersTotal.add(countersCurrent);
//            countersCurrent = new DatumStatusCounter();
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
                while(reader.cursor.hasNext()) {
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
