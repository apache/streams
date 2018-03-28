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

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class HbasePersistReader implements StreamsPersistReader, Serializable {

    public final static String STREAMS_ID = "HbasePersistReader";

    private static final Logger LOGGER = LoggerFactory.getLogger(HbasePersistReader.class);

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    protected final AtomicBoolean running = new AtomicBoolean();

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = new ObjectMapper();

    protected HbaseConfiguration config;

    protected Properties props = new Properties();

    private ExecutorService executor = Executors.newSingleThreadExecutor();

    public HbasePersistReader() {
        this.config = new ComponentConfigurator<>(HbaseConfiguration.class).detectConfiguration();
    }

    private HbasePersistReaderTask task;

    public HbasePersistReader(HbaseConfiguration config) {
        this.config = config;
    }

    public void setConfig(HbaseConfiguration config) {
        this.config = config;
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    @Override
    public void startStream() {

        LOGGER.debug("{} startStream", STREAMS_ID);

        executor.submit(task);

        running.set(true);

    }

    @Override
    public StreamsResultSet readAll() {
        return readCurrent();
    }

    @Override
    public StreamsResultSet readCurrent() {

        StreamsResultSet result;

        LOGGER.debug("Providing {} docs", persistQueue.size());

        try {
            lock.readLock().lock();
            result = new StreamsResultSet(persistQueue);
            persistQueue = constructQueue();
        } finally {
            lock.readLock().unlock();
        }

        return result;
    }

    protected Queue<StreamsDatum> constructQueue() {
        return new LinkedBlockingQueue<StreamsDatum>();
    }

    @Override
    public StreamsResultSet readNew(BigInteger bigInteger) {
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime dateTime, DateTime dateTime2) {
        return null;
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    @Override
    public void prepare(Object configurationObject) {

//        props.put("bootstrap.servers", config.getBrokerlist());
//        props.put("group.id", config.getGroupId());
//        props.put("auto.commit.interval.ms", "1000");
//        props.put("auto.offset.reset", "earliest");
//        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
//        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        persistQueue = constructQueue();

        task = new HbasePersistReaderTask(this);

    }

    @Override
    public void cleanUp() {
        running.set(false);
        executor.shutdown();
//
//        try {
//            executor.awaitTermination(5, TimeUnit.SECONDS);
//        } catch (InterruptedException ignored) {}

    }

    public void write(StreamsDatum streamsDatum) {
        try {
            lock.writeLock().lock();
            persistQueue.add(streamsDatum);
        } finally {
            lock.writeLock().unlock();
        }

    }
}
