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

package org.apache.streams.kafka;

import com.google.common.collect.Lists;
import org.apache.commons.collections.ListUtils;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;

import kafka.serializer.StringDecoder;
import kafka.utils.VerifiableProperties;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * KafkaPersistReader reads documents from kafka.
 */
public class KafkaPersistReader implements StreamsPersistReader, Serializable {

  public static final String STREAMS_ID = "KafkaPersistReader";

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPersistReader.class);

  protected volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper = new ObjectMapper();

  private KafkaReaderConfiguration config;

  protected Consumer<String, String> consumer;

  private ExecutorService executor = Executors.newSingleThreadExecutor();

  /**
   * KafkaPersistReader constructor - resolves KafkaConfiguration from JVM 'kafka'.
   */
  public KafkaPersistReader() {
    this.config = new ComponentConfigurator<>(KafkaReaderConfiguration.class).detectConfiguration();
    this.persistQueue  = new ConcurrentLinkedQueue<>();
  }

  /**
   * KafkaPersistReader constructor - uses supplied persistQueue.
   */
  public KafkaPersistReader(Queue<StreamsDatum> persistQueue) {
    this.config = new ComponentConfigurator<>(KafkaReaderConfiguration.class).detectConfiguration();
    this.persistQueue = persistQueue;
  }

  public void setConfig(KafkaReaderConfiguration config) {
    this.config = config;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void startStream() {

    Properties props = new Properties();
    props.setProperty("serializer.encoding", "UTF8");

    consumer = new KafkaConsumer(props);
    List<String> topics = ListUtils.EMPTY_LIST;
    topics.add(config.getTopic());

    consumer.subscribe(topics);
    VerifiableProperties vprops = new VerifiableProperties(props);

    executor.submit(new KafkaPersistReaderTask(this));

  }

  @Override
  public StreamsResultSet readAll() {
    return readCurrent();
  }

  @Override
  public StreamsResultSet readCurrent() {
    return null;
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
    return !executor.isShutdown() && !executor.isTerminated();
  }

  private static ConsumerConfig createConsumerConfig(String zookeeper, String groupId) {
    Properties props = new Properties();
    props.put("zookeeper.connect", zookeeper);
    props.put("group.id", groupId);
    props.put("zookeeper.session.timeout.ms", "400");
    props.put("zookeeper.sync.time.ms", "200");
    props.put("auto.commit.interval.ms", "1000");
    return new ConsumerConfig(props);
  }

  @Override
  public void prepare(Object configurationObject) {

  }

  @Override
  public void cleanUp() {
    consumer.close();
    while ( !executor.isTerminated()) {
      try {
        executor.awaitTermination(5, TimeUnit.SECONDS);
      } catch (InterruptedException interrupt) {
        LOGGER.trace("Interrupt", interrupt);
      }
    }
  }
}
