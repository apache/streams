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

package org.apache.streams.amazon.kinesis;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.Protocol;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.kinesis.AmazonKinesisClient;
import com.amazonaws.services.kinesis.model.DescribeStreamResult;
import com.amazonaws.services.kinesis.model.Shard;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * KinesisPersistReader reads documents from kinesis.
 */
public class KinesisPersistReader implements StreamsPersistReader, Serializable {

  public static final String STREAMS_ID = "KinesisPersistReader";

  private static final Logger LOGGER = LoggerFactory.getLogger(KinesisPersistReader.class);

  protected volatile Queue<StreamsDatum> persistQueue;

  private ObjectMapper mapper = new ObjectMapper();

  private KinesisReaderConfiguration config;

  protected Long pollInterval = StreamsConfigurator.detectConfiguration().getBatchFrequencyMs();

  private List<String> streamNames;

  private ExecutorService executor;

  protected AmazonKinesisClient client;

  /**
   * KinesisPersistReader constructor - resolves KinesisReaderConfiguration from JVM 'kinesis'.
   */
  public KinesisPersistReader() {
    Config config = StreamsConfigurator.getConfig().getConfig("kinesis");
    this.config = new ComponentConfigurator<>(KinesisReaderConfiguration.class).detectConfiguration(config);
    this.persistQueue  = new ConcurrentLinkedQueue<>();
  }

  /**
   * KinesisPersistReader constructor - uses provided KinesisReaderConfiguration.
   */
  public KinesisPersistReader(KinesisReaderConfiguration config) {
    this.config = config;
    this.persistQueue  = new ConcurrentLinkedQueue<>();
  }

  public void setConfig(KinesisReaderConfiguration config) {
    this.config = config;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void startStream() {

    this.streamNames = this.config.getStreams();

    for (final String stream : streamNames) {

      DescribeStreamResult describeStreamResult = client.describeStream(stream);

      if( "ACTIVE".equals(describeStreamResult.getStreamDescription().getStreamStatus())) {

        List<Shard> shardList = describeStreamResult.getStreamDescription().getShards();

        for( Shard shard : shardList ) {
          executor.submit(new KinesisPersistReaderTask(this, stream, shard.getShardId()));
        }
      }

    }

  }

  @Override
  public StreamsResultSet readAll() {
    return readCurrent();
  }

  public StreamsResultSet readCurrent() {

    StreamsResultSet current;
    synchronized( KinesisPersistReader.class ) {
      current = new StreamsResultSet(new ConcurrentLinkedQueue<>(persistQueue));
      persistQueue.clear();
    }
    return current;
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

  @Override
  public void prepare(Object configurationObject) {
    // Connect to Kinesis
    synchronized (this) {
      // Create the credentials Object
      AWSCredentials credentials = new BasicAWSCredentials(config.getKey(), config.getSecretKey());

      ClientConfiguration clientConfig = new ClientConfiguration();
      clientConfig.setProtocol(Protocol.valueOf(config.getProtocol().toString()));

      this.client = new AmazonKinesisClient(credentials, clientConfig);
      if (StringUtils.isNotEmpty(config.getRegion()))
        this.client.setRegion(Region.getRegion(Regions.fromName(config.getRegion())));
    }
    streamNames = this.config.getStreams();
    executor = Executors.newFixedThreadPool(streamNames.size());
  }

  @Override
  public void cleanUp() {

    while( !executor.isTerminated()) {
      try {
        executor.awaitTermination(5, TimeUnit.SECONDS);
      } catch (InterruptedException ignored) {}
    }
  }
}
