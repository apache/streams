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

package org.apache.streams.twitter.provider;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.core.util.ExecutorUtils;
import org.apache.streams.core.util.QueueUtils;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.StreamsJacksonMapperConfiguration;
import org.apache.streams.twitter.api.SevenDaySearchRequest;
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.config.SevenDaySearchProviderConfiguration;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.Tweet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import org.apache.commons.collections4.iterators.IteratorChain;
import org.apache.commons.lang3.NotImplementedException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Retrieve recent posts from a list of user ids or names.
 */
public class SevenDaySearchProvider implements Callable<Iterator<Tweet>>, StreamsProvider, Serializable {

  private static final String STREAMS_ID = "SevenDaySearchProvider";

  private static final Logger LOGGER = LoggerFactory.getLogger(SevenDaySearchProvider.class);

  public static final int MAX_NUMBER_WAITING = 10000;

  private SevenDaySearchProviderConfiguration config;

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  public SevenDaySearchProviderConfiguration getConfig() {
    return config;
  }

  protected volatile Queue<StreamsDatum> providerQueue;

  protected SevenDaySearchRequest request;

  protected Twitter client;

  protected ExecutorService executor;

  StreamsConfiguration streamsConfiguration;

  private List<Callable<Iterator<Tweet>>> tasks = new ArrayList<>();
  private List<Future<Iterator<Tweet>>> futures = new ArrayList<>();
  private CompletionService<Iterator<Tweet>> completionService;

  protected final AtomicBoolean running = new AtomicBoolean();

  /**
   * To use from command line:
   *
   * <p></p>
   * Supply (at least) the following required configuration in application.conf:
   *
   * <p></p>
   * twitter.oauth.consumerKey
   * twitter.oauth.consumerSecret
   * twitter.oauth.accessToken
   * twitter.oauth.accessTokenSecret
   * twitter.info
   *
   * <p></p>
   * Launch using:
   *
   * <p></p>
   * mvn exec:java -Dexec.mainClass=org.apache.streams.twitter.provider.TwitterTimelineProvider -Dexec.args="application.conf tweets.json"
   *
   * @param args args
   * @throws Exception Exception
   */
  public static void main(String[] args) throws Exception {

    Preconditions.checkArgument(args.length >= 2);

    String configfile = args[0];
    String outfile = args[1];

    File file = new File(configfile);
    assert (file.exists());

    Config testResourceConfig = ConfigFactory.parseFileAnySyntax(file, ConfigParseOptions.defaults().setAllowMissing(false));
    StreamsConfigurator.addConfig(testResourceConfig);

    StreamsConfiguration streamsConfiguration = StreamsConfigurator.detectConfiguration();
    SevenDaySearchProviderConfiguration config = new ComponentConfigurator<>(SevenDaySearchProviderConfiguration.class).detectConfiguration();
    SevenDaySearchProvider provider = new SevenDaySearchProvider(config);

    PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
    ObjectMapper mapper = StreamsJacksonMapper.getInstance(new StreamsJacksonMapperConfiguration().withDateFormats(Stream.of(TwitterDateTimeFormat.TWITTER_FORMAT).collect(Collectors.toList())));

    Iterator<Tweet> results = provider.call();

    results.forEachRemaining(d -> {
      try {
        outStream.println(mapper.writeValueAsString(d));
      } catch( Exception e ) {
        LOGGER.warn("Exception", e);
      }
    });

    outStream.flush();

  }

  public SevenDaySearchProvider() {
    this.config = new ComponentConfigurator<>(SevenDaySearchProviderConfiguration.class).detectConfiguration();
  }

  public SevenDaySearchProvider(SevenDaySearchProviderConfiguration config) {
    this.config = config;
  }

  public Queue<StreamsDatum> getProviderQueue() {
    return this.providerQueue;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void prepare(Object configurationObject) {

    if( configurationObject instanceof SevenDaySearchProviderConfiguration ) {
      this.config = (SevenDaySearchProviderConfiguration) configurationObject;
    }

    try {
      lock.writeLock().lock();
      providerQueue = QueueUtils.constructQueue();
    } finally {
      lock.writeLock().unlock();
    }

    Objects.requireNonNull(providerQueue);
    Objects.requireNonNull(config.getOauth().getConsumerKey());
    Objects.requireNonNull(config.getOauth().getConsumerSecret());
    Objects.requireNonNull(config.getOauth().getAccessToken());
    Objects.requireNonNull(config.getOauth().getAccessTokenSecret());
    Objects.requireNonNull(config.getQ());
    Objects.requireNonNull(config.getThreadsPerProvider());

    request = new SevenDaySearchRequest();
    request.setQ(config.getQ());
    request.setGeocode(config.getGeocode());
    if( !Objects.isNull(config.getIncludeEntities()) ) {
      request.setIncludeEntities(config.getIncludeEntities().toString());
    }
    request.setLang(config.getLang());
    request.setLocale(config.getLocale());
    if( !Objects.isNull(config.getResultType())) {
      request.setResultType(config.getResultType());
    }
    streamsConfiguration = StreamsConfigurator.detectConfiguration();

    try {
      client = getTwitterClient();
    } catch (InstantiationException e) {
      LOGGER.error("InstantiationException", e);
    }

    Objects.requireNonNull(client);

    executor = MoreExecutors.listeningDecorator(
      ExecutorUtils.newFixedThreadPoolWithQueueSize(
            config.getThreadsPerProvider().intValue(),
            streamsConfiguration.getQueueSize().intValue()
        )
    );

    completionService = new ExecutorCompletionService<>(executor);

    submitSearchThread();

  }

  @Override
  public void startStream() {

    Objects.requireNonNull(executor);

    LOGGER.info("startStream");

    running.set(true);

    LOGGER.info("running: {}", running.get());

    ExecutorUtils.shutdownAndAwaitTermination(executor);

    LOGGER.info("running: {}", running.get());

  }

  protected void submitSearchThread() {

    Callable providerTask = new SevenDaySearchProviderTask(
      this,
      client,
      request
    );
    LOGGER.info("Thread Created: {}", request);
    tasks.add(providerTask);
    Future<Iterator<Tweet>> future = completionService.submit(providerTask);
    futures.add(future);
    LOGGER.info("Thread Submitted: {}", request);

  }

  @Override
  public StreamsResultSet readCurrent() {

    StreamsResultSet result;

    LOGGER.debug("Providing {} docs", providerQueue.size());

    try {
      lock.writeLock().lock();
      result = new StreamsResultSet(providerQueue);
      providerQueue = QueueUtils.constructQueue();
    } finally {
      lock.writeLock().unlock();
    }

    if ( result.size() == 0 && providerQueue.isEmpty() && executor.isTerminated() ) {
      LOGGER.info("Finished.  Cleaning up...");

      running.set(false);

      LOGGER.info("Exiting");
    }

    return result;

  }

  public StreamsResultSet readNew(BigInteger sequence) {
    LOGGER.debug("{} readNew", STREAMS_ID);
    throw new NotImplementedException();
  }

  public StreamsResultSet readRange(DateTime start, DateTime end) {
    LOGGER.debug("{} readRange", STREAMS_ID);
    throw new NotImplementedException();
  }

  /**
   * get Twitter Client from TwitterUserInformationConfiguration.
   * @return result
   */
  public Twitter getTwitterClient() throws InstantiationException {

    return Twitter.getInstance(config);

  }

  @Override
  public void cleanUp() {
    ExecutorUtils.shutdownAndAwaitTermination(executor);
  }

  @Override
  public boolean isRunning() {
    LOGGER.debug("executor.isTerminated: {}", executor.isTerminated());
    LOGGER.debug("tasks.size(): {}", tasks.size());
    LOGGER.debug("futures.size(): {}", futures.size());
    if ( tasks.size() > 0 && tasks.size() == futures.size() && executor.isTerminated() ) {
      running.set(false);
    }
    LOGGER.debug("isRunning: {}", running.get());
    return running.get();
  }

  @Override
  public Iterator<Tweet> call() throws Exception {
    prepare(config);
    startStream();
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
    } while ( isRunning());
    IteratorChain chain = new IteratorChain();
    int received = 0;
    while(received < tasks.size()) {
      Future<Iterator<Tweet>> resultFuture = completionService.take();
      Iterator<Tweet> result = resultFuture.get();
      chain.addIterator(result);
      received ++;
    }
    cleanUp();
    return chain;
  }
}
