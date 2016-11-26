/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.amazon.kinesis;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.converter.TypeConverterUtil;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.PutRecordRequest;
import com.amazonaws.services.kinesis.model.PutRecordResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Strings;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * KinesisPersistWriter writes documents to kinesis.
 */
public class KinesisPersistWriter implements StreamsPersistWriter {

  public static final String STREAMS_ID = "KinesisPersistWriter";

  private static final Logger LOGGER = LoggerFactory.getLogger(KinesisPersistWriter.class);

  protected volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper = new ObjectMapper();

  private KinesisWriterConfiguration config;

  private List<String> streamName;

  private ExecutorService executor;

  protected AmazonKinesisClient client;

  /**
   * KinesisPersistWriter constructor - resolves KinesisWriterConfiguration from JVM 'kinesis'.
   */
  public KinesisPersistWriter() {
    Config config = StreamsConfigurator.config.getConfig("kinesis");
    this.config = new ComponentConfigurator<>(KinesisWriterConfiguration.class).detectConfiguration(config);
    this.persistQueue  = new ConcurrentLinkedQueue<>();
  }

  /**
   * KinesisPersistWriter constructor - uses provided KinesisWriterConfiguration.
   */
  public KinesisPersistWriter(KinesisWriterConfiguration config) {
    this.config = config;
    this.persistQueue  = new ConcurrentLinkedQueue<>();
  }

  public void setConfig(KinesisWriterConfiguration config) {
    this.config = config;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void write(StreamsDatum entry) {

    String document = (String) TypeConverterUtil.getInstance().convert(entry.getDocument(), String.class);

    PutRecordRequest putRecordRequest = new PutRecordRequest()
        .withStreamName(config.getStream())
        .withPartitionKey(entry.getId())
        .withData(ByteBuffer.wrap(document.getBytes()));

    PutRecordResult putRecordResult = client.putRecord(putRecordRequest);

    entry.setSequenceid(new BigInteger(putRecordResult.getSequenceNumber()));

    LOGGER.debug("Wrote {}", entry);
  }

  @Override
  public void prepare(Object configurationObject) {
    // Connect to Kinesis
    synchronized (this) {
      // Create the credentials Object
      AWSCredentials credentials = new BasicAWSCredentials(config.getKey(), config.getSecretKey());

      ClientConfiguration clientConfig = new ClientConfiguration();
      clientConfig.setProtocol(Protocol.valueOf(config.getProtocol().toString()));

      this.client = new AmazonKinesisClient(credentials, clientConfig);
      if (!Strings.isNullOrEmpty(config.getRegion())) {
        this.client.setRegion(Region.getRegion(Regions.fromName(config.getRegion())));
      }
    }
    executor = Executors.newSingleThreadExecutor();

  }

  @Override
  public void cleanUp() {
    try {
      executor.awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException ex) {
      LOGGER.debug("Interrupted! ", ex);
    }
  }
}
