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
import org.apache.streams.twitter.api.FollowersIdsRequest;
import org.apache.streams.twitter.api.FollowersListRequest;
import org.apache.streams.twitter.api.FriendsIdsRequest;
import org.apache.streams.twitter.api.FriendsListRequest;
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.config.TwitterFollowingConfiguration;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.Follow;
import org.apache.streams.twitter.pojo.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.common.util.concurrent.Uninterruptibles;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Retrieve all follow adjacencies from a list of user ids or names.
 */
public class TwitterFollowingProvider implements Callable<Iterator<Follow>>, StreamsProvider, Serializable {

  public static final String STREAMS_ID = "TwitterFollowingProvider";
  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFollowingProvider.class);

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private StreamsConfiguration streamsConfiguration;
  private TwitterFollowingConfiguration config;

  protected List<String> names = new ArrayList<>();
  protected List<Long> ids = new ArrayList<>();

  protected Twitter client;

  public static ExecutorService executor;

  private List<Callable<Object>> tasks = new ArrayList<>();
  private List<Future<Object>> futures = new ArrayList<>();

  protected final AtomicBoolean running = new AtomicBoolean();

  protected volatile Queue<StreamsDatum> providerQueue;

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
   * mvn exec:java -Dexec.mainClass=org.apache.streams.twitter.provider.TwitterFollowingProvider -Dexec.args="application.conf tweets.json"
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

    Config configFile = ConfigFactory.parseFileAnySyntax(file, ConfigParseOptions.defaults());
    StreamsConfigurator.addConfig(configFile);

    PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
    ObjectMapper mapper = StreamsJacksonMapper.getInstance(new StreamsJacksonMapperConfiguration().withDateFormats(Stream.of(TwitterDateTimeFormat.TWITTER_FORMAT).collect(Collectors.toList())));

    TwitterFollowingConfiguration config = new ComponentConfigurator<>(TwitterFollowingConfiguration.class).detectConfiguration();
    TwitterFollowingProvider provider = new TwitterFollowingProvider(config);

    Iterator<Follow> results = provider.call();

    results.forEachRemaining(d -> {
      try {
        outStream.println(mapper.writeValueAsString(d));
      } catch( Exception e ) {
        LOGGER.warn("Exception", e);
      }
    });

    outStream.flush();
  }

  public TwitterFollowingConfiguration getConfig() {
    return config;
  }

  public static final int MAX_NUMBER_WAITING = 10000;

  public TwitterFollowingProvider() {
    this.config = new ComponentConfigurator<>(TwitterFollowingConfiguration.class).detectConfiguration();
  }

  public TwitterFollowingProvider(TwitterFollowingConfiguration configurationObject) {
    this.config = configurationObject;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  public void prepare(Object configurationObject) {

    this.streamsConfiguration = StreamsConfigurator.detectConfiguration();

    if( configurationObject instanceof TwitterFollowingConfiguration) {
      this.config = (TwitterFollowingConfiguration) configurationObject;
    }

    Objects.requireNonNull(config);
    Objects.requireNonNull(config.getOauth());
    Objects.requireNonNull(config.getOauth().getConsumerKey());
    Objects.requireNonNull(config.getOauth().getConsumerSecret());
    Objects.requireNonNull(config.getOauth().getAccessToken());
    Objects.requireNonNull(config.getOauth().getAccessTokenSecret());
    Objects.requireNonNull(config.getInfo());
    Objects.requireNonNull(config.getThreadsPerProvider());

    StreamsConfiguration streamsConfiguration = StreamsConfigurator.detectConfiguration();

    try {
      client = getTwitterClient();
    } catch (InstantiationException e) {
      LOGGER.error("InstantiationException", e);
    }

    Objects.requireNonNull(client);

    try {
      lock.writeLock().lock();
      providerQueue = QueueUtils.constructQueue();
    } finally {
      lock.writeLock().unlock();
    }

    Objects.requireNonNull(providerQueue);

    // abstract this out
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

    Objects.requireNonNull(getConfig().getEndpoint());

    executor = MoreExecutors.listeningDecorator(
      ExecutorUtils.newFixedThreadPoolWithQueueSize(
            config.getThreadsPerProvider().intValue(),
            streamsConfiguration.getQueueSize().intValue()
        )
    );

    Preconditions.checkArgument(getConfig().getEndpoint().equals("friends") || getConfig().getEndpoint().equals("followers"));

    for (Long id : ids) {
      Callable<Object> callable = createTask(id, null);
      LOGGER.info("Thread Created: {}", id);
      tasks.add(callable);
      futures.add(executor.submit(callable));
      LOGGER.info("Thread Submitted: {}", id);
    }

    for (String name : names) {
      Callable<Object> callable = createTask(null, name);
      LOGGER.info("Thread Created: {}", name);
      tasks.add(callable);
      futures.add(executor.submit(callable));
      LOGGER.info("Thread Submitted: {}", name);
    }

  }

  public void startStream() {

    Objects.requireNonNull(executor);

    LOGGER.info("startStream");

    running.set(true);

    LOGGER.info("running: {}", running.get());

    ExecutorUtils.shutdownAndAwaitTermination(executor);

    LOGGER.info("running: {}", running.get());

  }

  protected Callable createTask(Long id, String name) {
    if( config.getEndpoint().equals("friends") && config.getIdsOnly() == true ) {
      FriendsIdsRequest request = (FriendsIdsRequest)new FriendsIdsRequest().withId(id).withScreenName(name);
      return new TwitterFriendsIdsProviderTask(
        this,
        client,
        request);
    } else if( config.getEndpoint().equals("friends") && config.getIdsOnly() == false ) {
      FriendsListRequest request = (FriendsListRequest)new FriendsListRequest().withId(id).withScreenName(name);
      return new TwitterFriendsListProviderTask(
        this,
        client,
        request);
    } else if( config.getEndpoint().equals("followers") && config.getIdsOnly() == true ) {
      FollowersIdsRequest request = (FollowersIdsRequest)new FollowersIdsRequest().withId(id).withScreenName(name);
      return new TwitterFollowersIdsProviderTask(
        this,
        client,
        request);
    } else if( config.getEndpoint().equals("followers") && config.getIdsOnly() == false ) {
      FollowersListRequest request = (FollowersListRequest)new FollowersListRequest().withId(id).withScreenName(name);
      return new TwitterFollowersListProviderTask(
        this,
        client,
        request);
    } else return null;
  }

  protected Twitter getTwitterClient() throws InstantiationException {
    return Twitter.getInstance(config);
  }

  public StreamsResultSet readCurrent() {

    StreamsResultSet result;

    try {
      lock.writeLock().lock();
      result = new StreamsResultSet(providerQueue);
      providerQueue = QueueUtils.constructQueue();
      LOGGER.debug("readCurrent: {} Documents", result.size());
    } finally {
      lock.writeLock().unlock();
    }

    return result;

  }

  @Override
  public StreamsResultSet readNew(BigInteger sequence) {
    throw new NotImplementedException();
  }

  @Override
  public StreamsResultSet readRange(DateTime start, DateTime end) {
    throw new NotImplementedException();
  }

  public boolean shouldContinuePulling(List<User> users) {
    return (users != null) && (users.size() == config.getPageSize());
  }

  public boolean isRunning() {
    LOGGER.debug("providerQueue.isEmpty: {}", providerQueue.isEmpty());
    LOGGER.debug("providerQueue.size: {}", providerQueue.size());
    LOGGER.debug("executor.isShutdown: {}", executor.isShutdown());
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
    boolean finished = tasks.size() > 0 && tasks.size() == futures.size() && executor.isShutdown() && executor.isTerminated() && allTasksComplete && providerQueue.size() == 0;
    LOGGER.debug("finished: {}", finished);
    if ( finished ) {
      running.set(false);
    }
    return running.get();
  }

  public void cleanUp() {
    // cleanUp
  }

  @Override
  public Iterator<Follow> call() {
    prepare(config);
    startStream();
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
    } while ( isRunning());
    cleanUp();
    return providerQueue.stream().map( x -> (Follow)x.getDocument()).iterator();
  }

}
