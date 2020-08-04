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
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.api.UsersLookupRequest;
import org.apache.streams.twitter.config.TwitterFollowingConfiguration;
import org.apache.streams.twitter.config.TwitterUserInformationConfiguration;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;

import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import org.apache.commons.lang.NotImplementedException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.lang.Runnable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Retrieve current profile status from a list of user ids or names.
 */
public class TwitterUserInformationProvider implements Callable<Iterator<User>>, StreamsProvider, Serializable {

  private static final String STREAMS_ID = "TwitterUserInformationProvider";

  protected static ObjectMapper MAPPER = new StreamsJacksonMapper(Collections.singletonList(TwitterDateTimeFormat.TWITTER_FORMAT));

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterUserInformationProvider.class);

  public static final int MAX_NUMBER_WAITING = 1000;

  private TwitterUserInformationConfiguration config;

  protected List<String> names = new ArrayList<>();
  protected List<Long> ids = new ArrayList<>();

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  protected volatile Queue<StreamsDatum> providerQueue;

  StreamsConfiguration streamsConfiguration;

  public TwitterUserInformationConfiguration getConfig() {
    return config;
  }

  public void setConfig(TwitterUserInformationConfiguration config) {
    this.config = config;
  }

  protected Twitter client;

  protected ExecutorService executor;

  protected DateTime start;
  protected DateTime end;

  protected final AtomicBoolean running = new AtomicBoolean();

  private List<Runnable> tasks = new ArrayList<>();
  private List<Future<Object>> futures = new ArrayList<>();

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
   * mvn exec:java -Dexec.mainClass=org.apache.streams.twitter.provider.TwitterUserInformationProvider -Dexec.args="application.conf tweets.json"
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
    TwitterUserInformationConfiguration config = new ComponentConfigurator<>(TwitterUserInformationConfiguration.class).detectConfiguration();
    TwitterUserInformationProvider provider = new TwitterUserInformationProvider(config);

    PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
    provider.prepare(config);
    provider.startStream();
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
      for (StreamsDatum datum : provider.readCurrent()) {
        String json;
        try {
          json = MAPPER.writeValueAsString(datum.getDocument());
          outStream.println(json);
        } catch (JsonProcessingException ex) {
          System.err.println(ex.getMessage());
        }
      }
      outStream.flush();
    }
    while ( provider.isRunning());
    provider.cleanUp();
    outStream.close();
  }

  // TODO: this should be abstracted out


  /**
   * TwitterUserInformationProvider constructor.
   * Resolves config from JVM properties 'twitter'.
   */
  public TwitterUserInformationProvider() {
    this.config = new ComponentConfigurator<>(TwitterUserInformationConfiguration.class).detectConfiguration();
  }

  public TwitterUserInformationProvider(TwitterUserInformationConfiguration config) {
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

    if ( configurationObject instanceof TwitterFollowingConfiguration ) {
      this.config = (TwitterUserInformationConfiguration) configurationObject;
    }

    streamsConfiguration = StreamsConfigurator.detectConfiguration(StreamsConfigurator.getConfig());

    try {
      lock.writeLock().lock();
      providerQueue = QueueUtils.constructQueue();
    } finally {
      lock.writeLock().unlock();
    }

    Objects.requireNonNull(providerQueue);
    Objects.requireNonNull(config);
    Objects.requireNonNull(config.getOauth());
    Objects.requireNonNull(config.getOauth().getConsumerKey());
    Objects.requireNonNull(config.getOauth().getConsumerSecret());
    Objects.requireNonNull(config.getOauth().getAccessToken());
    Objects.requireNonNull(config.getOauth().getAccessTokenSecret());
    Objects.requireNonNull(config.getInfo());
    Objects.requireNonNull(config.getThreadsPerProvider());

    try {
      client = getTwitterClient();
    } catch (InstantiationException e) {
      LOGGER.error("InstantiationException", e);
    }

    Objects.requireNonNull(client);

    for (String s : config.getInfo()) {
      if (s != null) {
        String potentialScreenName = s.replaceAll("@", "").trim().toLowerCase();

        // See if it is a long, if it is, add it to the user iD list, if it is not, add it to the
        // screen name list
        try {
          ids.add(Long.parseLong(potentialScreenName));
        } catch (Exception ex) {
          names.add(potentialScreenName);
        }
      }
    }

    executor = MoreExecutors.listeningDecorator(
      ExecutorUtils.newFixedThreadPoolWithQueueSize(
            config.getThreadsPerProvider().intValue(),
            streamsConfiguration.getQueueSize().intValue()
        )
    );

    Objects.requireNonNull(executor);

    submitUserInformationThreads(ids, names);

    LOGGER.info("tasks: {}", tasks.size());
    LOGGER.info("futures: {}", futures.size());

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

  protected void submitUserInformationThreads(List<Long> ids, List<String> names) {

    /*
    while( idsIndex < ids.size() ) {
      from = idsIndex
      to = Math.min( idsIndex + 100, ids.size() - idsIndex
    }
     */
    int idsIndex = 0;
    while( idsIndex + 100 < ids.size() ) {
      List<Long> batchIds = ids.subList(idsIndex, idsIndex + 100);
      TwitterUserInformationProviderTask providerTask = new TwitterUserInformationProviderTask(
          this,
          client,
          new UsersLookupRequest().withUserId(batchIds));
      tasks.add(providerTask);
      Future future = executor.submit((Callable)providerTask);
      futures.add(future);
      LOGGER.info("Thread Submitted: {}", providerTask.request);
      idsIndex += 100;
    }
    if (ids.size() >= idsIndex) {
      List<Long> batchIds = ids.subList(idsIndex, ids.size());
      TwitterUserInformationProviderTask providerTask = new TwitterUserInformationProviderTask(
          this,
          client,
          new UsersLookupRequest().withUserId(batchIds));
      tasks.add(providerTask);
      Future future = executor.submit((Callable)providerTask);
      futures.add(future);
      LOGGER.info("Thread Submitted: {}", providerTask.request);
    }

    int namesIndex = 0;
    while( namesIndex + 100 < names.size() ) {
      List<String> batchNames = names.subList(namesIndex, namesIndex + 100);
      TwitterUserInformationProviderTask providerTask = new TwitterUserInformationProviderTask(
          this,
          client,
          new UsersLookupRequest().withScreenName(batchNames));
      tasks.add(providerTask);
      Future future = executor.submit((Callable)providerTask);
      futures.add(future);
      LOGGER.info("Thread Submitted: {}", providerTask.request);
      namesIndex += 100;
    }
    if (names.size() >= namesIndex) {
      List<String> batchNames = names.subList(namesIndex, names.size());
      TwitterUserInformationProviderTask providerTask = new TwitterUserInformationProviderTask(
          this,
          client,
          new UsersLookupRequest().withScreenName(batchNames));
      tasks.add(providerTask);
      Future future = executor.submit((Callable)providerTask);
      futures.add(future);
      LOGGER.info("Thread Submitted: {}", providerTask.request);
    }

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

    return result;

  }

  public StreamsResultSet readNew(BigInteger sequence) {
    LOGGER.debug("{} readNew", STREAMS_ID);
    throw new NotImplementedException();
  }

  @Override
  public StreamsResultSet readRange(DateTime start, DateTime end) {
    LOGGER.debug("{} readRange", STREAMS_ID);
    throw new NotImplementedException();
  }

  protected Twitter getTwitterClient() throws InstantiationException {
    return Twitter.getInstance(config);
  }

  @Override
  public boolean isRunning() {
    LOGGER.debug("providerQueue.isEmpty: {}", providerQueue.isEmpty());
    LOGGER.debug("providerQueue.size: {}", providerQueue.size());
    LOGGER.debug("executor.isTerminated: {}", executor.isTerminated());
    LOGGER.debug("tasks.size(): {}", tasks.size());
    LOGGER.debug("futures.size(): {}", futures.size());
    boolean allTasksComplete;
    if( futures.size() > 0) {
      allTasksComplete = true;
      for(Future<?> future : futures){
        allTasksComplete |= !future.isDone(); // check if future is done
      }
    } else {
      allTasksComplete = false;
    }
    LOGGER.debug("allTasksComplete: {}", allTasksComplete);
    boolean finished = allTasksComplete && tasks.size() > 0 && tasks.size() == futures.size() && executor.isShutdown() && executor.isTerminated();
    LOGGER.debug("finished: {}", finished);
    if ( finished ) {
      running.set(false);
    }
    return running.get();
  }

  @Override
  public void cleanUp() {
    ExecutorUtils.shutdownAndAwaitTermination(executor);
  }

  @Override
  public Iterator<User> call() throws Exception {
    prepare(config);
    startStream();
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
    } while ( isRunning());
    cleanUp();
    return providerQueue.stream().map( x -> (User)x.getDocument()).iterator();
  }
}
