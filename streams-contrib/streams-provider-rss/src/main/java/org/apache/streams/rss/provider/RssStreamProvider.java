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

package org.apache.streams.rss.provider;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.rss.RssStreamConfiguration;
import org.apache.streams.rss.provider.perpetual.RssFeedScheduler;
import org.apache.streams.util.ComponentUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.math.BigInteger;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RSS {@link org.apache.streams.core.StreamsProvider} that provides content from rss feeds in boilerpipe format
 */
public class RssStreamProvider implements StreamsProvider {

  private static final String STREAMS_ID = "RssStreamProvider";

  private static final Logger LOGGER = LoggerFactory.getLogger(RssStreamProvider.class);

  private static final int MAX_SIZE = 1000;

  private RssStreamConfiguration config;
  private boolean perpetual;
  private ExecutorService executor;
  private BlockingQueue<StreamsDatum> dataQueue;
  private AtomicBoolean isComplete;

  @VisibleForTesting
  protected RssFeedScheduler scheduler;

  public RssStreamProvider() {
    this(new ComponentConfigurator<>(RssStreamConfiguration.class)
        .detectConfiguration(), false);
  }

  public RssStreamProvider(boolean perpetual) {
    this(new ComponentConfigurator<>(RssStreamConfiguration.class)
        .detectConfiguration(), perpetual);
  }

  public RssStreamProvider(RssStreamConfiguration config) {
    this(config, false);
  }

  public RssStreamProvider(RssStreamConfiguration config, boolean perpetual) {
    this.perpetual = perpetual;
    this.config = config;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void startStream() {
    LOGGER.trace("Starting Rss Scheduler");
    this.executor.submit(this.scheduler);
  }

  @Override
  public StreamsResultSet readCurrent() {
    Queue<StreamsDatum> batch = new ConcurrentLinkedQueue<>();
    int batchSize = 0;
    while (!this.dataQueue.isEmpty() && batchSize < MAX_SIZE) {
      StreamsDatum datum = ComponentUtils.pollWhileNotEmpty(this.dataQueue);
      if (datum != null) {
        ++batchSize;
        batch.add(datum);
      }
    }
    this.isComplete.set(this.scheduler.isComplete() && batch.isEmpty() && this.dataQueue.isEmpty());
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
  public boolean isRunning() {
    return !this.isComplete.get();
  }

  @Override
  public void prepare(Object configurationObject) {
    this.executor = new ThreadPoolExecutor(1, 4, 15L, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    this.dataQueue = new LinkedBlockingQueue<>();
    this.scheduler = getScheduler(this.dataQueue);
    this.isComplete = new AtomicBoolean(false);
    int consecutiveEmptyReads = 0;
  }

  @VisibleForTesting
  protected RssFeedScheduler getScheduler(BlockingQueue<StreamsDatum> queue) {
    if (this.perpetual) {
      return new RssFeedScheduler(this.executor, this.config.getFeeds(), queue);
    } else {
      return new RssFeedScheduler(this.executor, this.config.getFeeds(), queue, 0);
    }
  }

  @Override
  public void cleanUp() {
    this.scheduler.stop();
    ComponentUtils.shutdownExecutor(this.executor, 10, 10);
  }

  /**
   * To use from command line:
   *
   * <p></p>
   * Supply configuration similar to src/test/resources/rss.conf
   *
   * <p></p>
   * Launch using:
   *
   * <p></p>
   * mvn exec:java -Dexec.mainClass=org.apache.streams.rss.provider.RssStreamProvider -Dexec.args="rss.conf articles.json"
   * @param args args
   * @throws Exception Exception
   */
  public static void main(String[] args) throws Exception {

    Preconditions.checkArgument(args.length >= 2);

    String configfile = args[0];
    String outfile = args[1];

    Config reference = ConfigFactory.load();
    File file = new File(configfile);
    assert (file.exists());
    Config testResourceConfig = ConfigFactory.parseFileAnySyntax(file, ConfigParseOptions.defaults().setAllowMissing(false));

    Config typesafe  = testResourceConfig.withFallback(reference).resolve();

    StreamsConfiguration streamsConfiguration = StreamsConfigurator.detectConfiguration(typesafe);
    RssStreamConfiguration config = new ComponentConfigurator<>(RssStreamConfiguration.class).detectConfiguration(typesafe, "rss");
    RssStreamProvider provider = new RssStreamProvider(config);

    ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
    provider.prepare(config);
    provider.startStream();
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
      for (StreamsDatum datum : provider.readCurrent()) {
        String json;
        try {
          json = mapper.writeValueAsString(datum.getDocument());
          outStream.println(json);
        } catch (JsonProcessingException ex) {
          System.err.println(ex.getMessage());
        }
      }
    }
    while ( provider.isRunning());
    provider.cleanUp();
    outStream.flush();
  }
}
