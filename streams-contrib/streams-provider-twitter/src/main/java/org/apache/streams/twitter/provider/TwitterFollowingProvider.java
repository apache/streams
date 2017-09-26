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
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.TwitterFollowingConfiguration;
import org.apache.streams.twitter.TwitterUserInformationConfiguration;
import org.apache.streams.twitter.api.FollowersIdsRequest;
import org.apache.streams.twitter.api.FollowersListRequest;
import org.apache.streams.twitter.api.FollowingIdsRequest;
import org.apache.streams.twitter.api.FriendsIdsRequest;
import org.apache.streams.twitter.api.FriendsListRequest;
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Retrieve all follow adjacencies from a list of user ids or names.
 */
public class TwitterFollowingProvider implements StreamsProvider, Serializable {

  public static final String STREAMS_ID = "TwitterFollowingProvider";
  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFollowingProvider.class);

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private TwitterFollowingConfiguration config;

  protected List<String> names = new ArrayList<>();
  protected List<Long> ids = new ArrayList<>();

  protected Twitter client;

  protected ListeningExecutorService executor;

  private List<ListenableFuture<Object>> futures = new ArrayList<>();

  protected final AtomicBoolean running = new AtomicBoolean();

  protected volatile Queue<StreamsDatum> providerQueue;

  /**
   * To use from command line:
   *
   * <p/>
   * Supply (at least) the following required configuration in application.conf:
   *
   * <p/>
   * twitter.oauth.consumerKey
   * twitter.oauth.consumerSecret
   * twitter.oauth.accessToken
   * twitter.oauth.accessTokenSecret
   * twitter.info
   *
   * <p/>
   * Launch using:
   *
   * <p/>
   * mvn exec:java -Dexec.mainClass=org.apache.streams.twitter.provider.TwitterFollowingProvider -Dexec.args="application.conf tweets.json"
   *
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
    TwitterFollowingConfiguration config = new ComponentConfigurator<>(TwitterFollowingConfiguration.class).detectConfiguration(typesafe, "twitter");
    TwitterFollowingProvider provider = new TwitterFollowingProvider(config);

    ObjectMapper mapper = new StreamsJacksonMapper(Collections.singletonList(TwitterDateTimeFormat.TWITTER_FORMAT));

    PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
    provider.prepare(config);
    provider.startStream();
    do {
      Thread.sleep(streamsConfiguration.getBatchFrequencyMs());
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

  public TwitterFollowingConfiguration getConfig() {
    return config;
  }

  public static final int MAX_NUMBER_WAITING = 10000;

  public TwitterFollowingProvider() {
    this.config = new ComponentConfigurator<>(TwitterFollowingConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter"));
  }

  public TwitterFollowingProvider(TwitterFollowingConfiguration config) {
    this.config = config;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  public void prepare(Object configurationObject) {

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
      providerQueue = constructQueue();
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
        TwitterUserInformationProvider.newFixedThreadPoolWithQueueSize(
            config.getThreadsPerProvider().intValue(),
            streamsConfiguration.getQueueSize().intValue()
        )
    );

    Preconditions.checkArgument(getConfig().getEndpoint().equals("friends") || getConfig().getEndpoint().equals("followers"));

    for (Long id : ids) {
      submitTask(createTask(id, null));
      LOGGER.info("Thread Submitted: {}", id);
    }

    for (String name : names) {
      submitTask(createTask(null, name));
      LOGGER.info("Thread Submitted: {}", name);
    }

  }

  public void startStream() {

    Objects.requireNonNull(executor);

    LOGGER.info("startStream");

    running.set(true);

    LOGGER.info("isRunning");

    executor.shutdown();

  }

  protected Runnable createTask(Long id, String name) {
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

  protected void submitTask(Runnable providerTask) {
    ListenableFuture future = executor.submit(providerTask);
    futures.add(future);
  }

  protected Twitter getTwitterClient() throws InstantiationException {
    return Twitter.getInstance(config);
  }

  public StreamsResultSet readCurrent() {

    StreamsResultSet result;

    try {
      lock.writeLock().lock();
      result = new StreamsResultSet(providerQueue);
      result.setCounter(new DatumStatusCounter());
      providerQueue = constructQueue();
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
    if ( providerQueue.isEmpty() && executor.isTerminated() && Futures.allAsList(futures).isDone() ) {
      LOGGER.info("All Threads Completed");
      running.set(false);
      LOGGER.info("Exiting");
    }
    return running.get();
  }

  // abstract this out
  protected Queue<StreamsDatum> constructQueue() {
    return new LinkedBlockingQueue<>();
  }

  // abstract this out
  void shutdownAndAwaitTermination(ExecutorService pool) {
    pool.shutdown(); // Disable new tasks from being submitted
    try {
      // Wait a while for existing tasks to terminate
      if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
        pool.shutdownNow(); // Cancel currently executing tasks
        // Wait a while for tasks to respond to being cancelled
        if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
          System.err.println("Pool did not terminate");
        }
      }
    } catch (InterruptedException ie) {
      // (Re-)Cancel if current thread also interrupted
      pool.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
    }
  }

  public void cleanUp() {
    shutdownAndAwaitTermination(executor);
  }
}
