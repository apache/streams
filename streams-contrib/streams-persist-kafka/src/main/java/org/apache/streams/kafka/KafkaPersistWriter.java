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

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.util.GuidUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Properties;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * KafkaPersistWriter writes documents to kafka.
 */
public class KafkaPersistWriter implements StreamsPersistWriter, Serializable, Runnable {

  public static final String STREAMS_ID = "KafkaPersistWriter";

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaPersistWriter.class);

  protected volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper = new ObjectMapper();

  private KafkaWriterConfiguration config;

  protected Producer<String, String> producer;

  /**
   * KafkaPersistWriter constructor
   */
  public KafkaPersistWriter() {
    this.config = new ComponentConfigurator<>(KafkaWriterConfiguration.class).detectConfiguration();
    this.persistQueue  = new ConcurrentLinkedQueue<>();
  }

  /**
   * KafkaPersistWriter constructor - uses supplied persistQueue.
   */
  public KafkaPersistWriter(Queue<StreamsDatum> persistQueue) {
    this.config = new ComponentConfigurator<>(KafkaWriterConfiguration.class).detectConfiguration();
    this.persistQueue = persistQueue;
  }

  public void setConfig(KafkaWriterConfiguration config) {
    this.config = config;
  }

  /**
   * run persist writer thread
   */
  public void start() {
    Properties props = new Properties();

    props.put("metadata.broker.list", config.getBrokerlist());
    props.put("serializer.class", "kafka.serializer.StringEncoder");
    props.put("partitioner.class", "org.apache.streams.kafka.StreamsPartitioner");
    props.put("request.required.acks", "1");

    producer = new KafkaProducer<>(props);

    new Thread(new KafkaPersistWriterTask(this)).start();
  }

  public void stop() {
    producer.close();
  }

  public void setPersistQueue(Queue<StreamsDatum> persistQueue) {
    this.persistQueue = persistQueue;
  }

  public Queue<StreamsDatum> getPersistQueue() {
    return this.persistQueue;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void write(StreamsDatum entry) {

    try {

      String text = mapper.writeValueAsString(entry);

      String hash = GuidUtils.generateGuid(text);

      ProducerRecord<String, String> data = new ProducerRecord<>(config.getTopic(), hash, text);

      producer.send(data);

    } catch (JsonProcessingException ex) {
      LOGGER.warn("save: {}", ex);
    }
  }

  @Override
  public void run() {
    start();
  }

  @Override
  public void prepare(Object configurationObject) {
    start();
  }

  @Override
  public void cleanUp() {
    stop();
  }
}
