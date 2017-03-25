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
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.TwitterFollowingConfiguration;
import org.apache.streams.twitter.api.FollowersIdsRequest;
import org.apache.streams.twitter.api.FollowingIdsRequest;
import org.apache.streams.twitter.api.FriendsIdsRequest;
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.User;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Retrieve all follow adjacencies from a list of user ids or names.
 */
public class TwitterFollowingProvider extends TwitterUserInformationProvider {

  public static final String STREAMS_ID = "TwitterFollowingProvider";
  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFollowingProvider.class);

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  private TwitterFollowingConfiguration config;

  protected List<String> names;
  protected List<Long> ids;

  private List<ListenableFuture<Object>> futures = new ArrayList<>();

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
    super(config);
    this.config = config;
  }

  @Override
  public void prepare(Object configurationObject) {
    super.prepare(config);
    Objects.requireNonNull(getConfig().getEndpoint());

    Preconditions.checkArgument(getConfig().getEndpoint().equals("friends") || getConfig().getEndpoint().equals("followers"));

    if( config.getEndpoint().equals("friends")) {
      submitFollowersThreads(ids, names);
    } else if( config.getEndpoint().equals("followers")) {
      submitFriendsThreads(ids, names);
    }
  }

  @Override
  public void startStream() {

    Objects.requireNonNull(executor);

    Preconditions.checkArgument(idsBatches.hasNext() || screenNameBatches.hasNext());

    LOGGER.info("startStream");

    running.set(true);

    executor.shutdown();

  }

  protected void submitFollowersThreads(List<Long> ids, List<String> names) {
    Twitter client = getTwitterClient();

    for (Long id : ids) {
      TwitterFollowersIdsProviderTask providerTask =
          new TwitterFollowersIdsProviderTask(
              this,
              client,
              (FollowersIdsRequest)new FollowersIdsRequest().withId(id));

      ListenableFuture future = executor.submit(providerTask);
      futures.add(future);
      LOGGER.info("Thread Submitted: {}", providerTask.request);
    }

    for (String name : names) {
      TwitterFollowersIdsProviderTask providerTask =
          new TwitterFollowersIdsProviderTask(
              this,
              client,
              (FollowersIdsRequest)new FollowersIdsRequest().withScreenName(name));

      ListenableFuture future = executor.submit(providerTask);
      futures.add(future);
      LOGGER.info("Thread Submitted: {}", providerTask.request);
    }
  }

  protected void submitFriendsThreads(List<Long> ids, List<String> names) {
    Twitter client = getTwitterClient();

    for (Long id : ids) {
      TwitterFriendsIdsProviderTask providerTask =
          new TwitterFriendsIdsProviderTask(
              this,
              client,
              (FriendsIdsRequest)new FriendsIdsRequest().withId(id));

      ListenableFuture future = executor.submit(providerTask);
      futures.add(future);
      LOGGER.info("Thread Submitted: {}", providerTask.request);
    }

    for (String name : names) {
      TwitterFriendsIdsProviderTask providerTask =
          new TwitterFriendsIdsProviderTask(
              this,
              client,
              (FriendsIdsRequest)new FriendsIdsRequest().withScreenName(name));

      ListenableFuture future = executor.submit(providerTask);
      futures.add(future);
      LOGGER.info("Thread Submitted: {}", providerTask.request);
    }
  }

  @Override
  public StreamsResultSet readCurrent() {

    LOGGER.info("{}{} - readCurrent", idsBatches, screenNameBatches);

    StreamsResultSet result;

    try {
      lock.writeLock().lock();
      result = new StreamsResultSet(providerQueue);
      result.setCounter(new DatumStatusCounter());
      providerQueue = constructQueue();
      LOGGER.debug("{}{} - providing {} docs", idsBatches, screenNameBatches, result.size());
    } finally {
      lock.writeLock().unlock();
    }

    return result;

  }

  public boolean shouldContinuePulling(List<User> users) {
    return (users != null) && (users.size() == config.getPageSize());
  }

  @Override
  public boolean isRunning() {
    if (providerQueue.isEmpty() && executor.isTerminated() && Futures.allAsList(futures).isDone()) {
      LOGGER.info("Completed");
      running.set(false);
      LOGGER.info("Exiting");
    }
    return running.get();
  }
}
