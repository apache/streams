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
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Uninterruptibles;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Retrieve all follow adjacencies from a list of user ids or names.
 */
public class TwitterFollowingProvider extends TwitterUserInformationProvider {

  public static final String STREAMS_ID = "TwitterFollowingProvider";
  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterFollowingProvider.class);

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  private TwitterFollowingConfiguration config;

  List<ListenableFuture<Object>> futures = new ArrayList<>();

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

    ObjectMapper mapper = new StreamsJacksonMapper(Lists.newArrayList(TwitterDateTimeFormat.TWITTER_FORMAT));

    PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
    provider.prepare(config);
    provider.startStream();
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
      Iterator<StreamsDatum> iterator = provider.readCurrent().iterator();
      while (iterator.hasNext()) {
        StreamsDatum datum = iterator.next();
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
    Preconditions.checkNotNull(getConfig().getEndpoint());
    Preconditions.checkArgument(getConfig().getEndpoint().equals("friends") || getConfig().getEndpoint().equals("followers"));
    return;
  }

  @Override
  public void startStream() {

    Preconditions.checkNotNull(executor);

    Preconditions.checkArgument(idsBatches.hasNext() || screenNameBatches.hasNext());

    LOGGER.info("startStream");

    running.set(true);

    while (idsBatches.hasNext()) {
      submitFollowingThreads(idsBatches.next());
    }
    while (screenNameBatches.hasNext()) {
      submitFollowingThreads(screenNameBatches.next());
    }

    executor.shutdown();

  }

  protected void submitFollowingThreads(Long[] ids) {
    Twitter client = getTwitterClient();

    for (int i = 0; i < ids.length; i++) {
      TwitterFollowingProviderTask providerTask = new TwitterFollowingProviderTask(this, client, ids[i]);
      ListenableFuture future = executor.submit(providerTask);
      futures.add(future);
      LOGGER.info("submitted {}", ids[i]);
    }
  }

  protected void submitFollowingThreads(String[] screenNames) {
    Twitter client = getTwitterClient();

    for (int i = 0; i < screenNames.length; i++) {
      TwitterFollowingProviderTask providerTask = new TwitterFollowingProviderTask(this, client, screenNames[i]);
      ListenableFuture future = executor.submit(providerTask);
      futures.add(future);
      LOGGER.info("submitted {}", screenNames[i]);
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
