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

package org.apache.streams.facebook.provider;

import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.facebook.FacebookConfiguration;
import org.apache.streams.facebook.IdConfig;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.SerializationUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.typesafe.config.ConfigRenderOptions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract {@link org.apache.streams.core.StreamsProvider} for facebook.
 */
public abstract class FacebookProvider implements StreamsProvider {

  private static final String STREAMS_ID = "FacebookProvider";

  private static final Logger LOGGER = LoggerFactory.getLogger(FacebookProvider.class);
  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();
  private static final int MAX_BATCH_SIZE = 2000;

  protected FacebookConfiguration configuration;
  protected BlockingQueue<StreamsDatum> datums;

  private AtomicBoolean isComplete;
  private ListeningExecutorService executor;
  List<ListenableFuture<Object>> futures = new ArrayList<>();

  private FacebookDataCollector dataCollector;

  /**
   * FacebookProvider constructor - resolves FacebookConfiguration from JVM 'facebook'.
   */
  public FacebookProvider() {
    try {
      this.configuration = MAPPER.readValue(StreamsConfigurator.config.getConfig("facebook").root().render(ConfigRenderOptions.concise()), FacebookConfiguration.class);
    } catch (IOException ioe) {
      LOGGER.error("Exception trying to read default config : {}", ioe);
    }
  }

  /**
   * FacebookProvider constructor - uses supplied FacebookConfiguration.
   */
  public FacebookProvider(FacebookConfiguration configuration) {
    this.configuration = SerializationUtil.cloneBySerialization(configuration);
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void startStream() {
    ListenableFuture future = executor.submit(getDataCollector());
    futures.add(future);
    executor.shutdown();
  }

  protected abstract FacebookDataCollector getDataCollector();

  @Override
  public StreamsResultSet readCurrent() {
    int batchSize = 0;
    BlockingQueue<StreamsDatum> batch = new LinkedBlockingQueue<>();
    while (!this.datums.isEmpty() && batchSize < MAX_BATCH_SIZE) {
      ComponentUtils.offerUntilSuccess(ComponentUtils.pollWhileNotEmpty(this.datums), batch);
      ++batchSize;
    }
    return new StreamsResultSet(batch);
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
  public void prepare(Object configurationObject) {
    this.datums = new LinkedBlockingQueue<>();
    this.isComplete = new AtomicBoolean(false);
    this.executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
  }

  @Override
  public void cleanUp() {
    ComponentUtils.shutdownExecutor(executor, 5, 5);
    executor = null;
  }

  /**
   * Overrides the ids and addedAfter time in the configuration.
   * @param idsToAfterDate idsToAfterDate
   */
  public void overrideIds(Map<String, DateTime> idsToAfterDate) {
    Set<IdConfig> ids = new HashSet<>();
    for (String id : idsToAfterDate.keySet()) {
      IdConfig idConfig = new IdConfig();
      idConfig.setId(id);
      idConfig.setAfterDate(idsToAfterDate.get(id));
      ids.add(idConfig);
    }
    this.configuration.setIds(ids);
  }

  @Override
  public boolean isRunning() {
    if (datums.isEmpty() && executor.isTerminated() && Futures.allAsList(futures).isDone()) {
      LOGGER.info("Completed");
      isComplete.set(true);
      LOGGER.info("Exiting");
    }
    return !isComplete.get();
  }
}