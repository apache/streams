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
import org.apache.streams.twitter.TwitterUserInformationConfiguration;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;

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
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Retrieve recent posts from a list of user ids or names.
 */
public class TwitterTimelineProvider implements StreamsProvider, Serializable {

  private static final String STREAMS_ID = "TwitterTimelineProvider";

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterTimelineProvider.class);

  public static final int MAX_NUMBER_WAITING = 10000;

  private TwitterUserInformationConfiguration config;

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  public TwitterUserInformationConfiguration getConfig() {
    return config;
  }

  public void setConfig(TwitterUserInformationConfiguration config) {
    this.config = config;
  }

  protected Collection<String[]> screenNameBatches;
  protected Collection<Long> ids;

  protected volatile Queue<StreamsDatum> providerQueue;

  protected int idsCount;
  protected Twitter client;

  protected ListeningExecutorService executor;

  protected DateTime start;
  protected DateTime end;

  protected final AtomicBoolean running = new AtomicBoolean();

  private List<ListenableFuture<Object>> futures = new ArrayList<>();

  Boolean jsonStoreEnabled;
  Boolean includeEntitiesEnabled;

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
   * mvn exec:java -Dexec.mainClass=org.apache.streams.twitter.provider.TwitterTimelineProvider -Dexec.args="application.conf tweets.json"
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
    TwitterUserInformationConfiguration config = new ComponentConfigurator<>(TwitterUserInformationConfiguration.class).detectConfiguration(typesafe, "twitter");
    TwitterTimelineProvider provider = new TwitterTimelineProvider(config);

    ObjectMapper mapper = new StreamsJacksonMapper(Stream.of(TwitterDateTimeFormat.TWITTER_FORMAT).collect(Collectors.toList()));

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
    while ( provider.isRunning() );
    provider.cleanUp();
    outStream.flush();
  }

  public TwitterTimelineProvider(TwitterUserInformationConfiguration config) {
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

    try {
      lock.writeLock().lock();
      providerQueue = constructQueue();
    } finally {
      lock.writeLock().unlock();
    }

    Objects.requireNonNull(providerQueue);
    Objects.requireNonNull(config.getOauth().getConsumerKey());
    Objects.requireNonNull(config.getOauth().getConsumerSecret());
    Objects.requireNonNull(config.getOauth().getAccessToken());
    Objects.requireNonNull(config.getOauth().getAccessTokenSecret());
    Objects.requireNonNull(config.getInfo());

    consolidateToIDs();

    if (ids.size() > 1) {
      executor = MoreExecutors.listeningDecorator(TwitterUserInformationProvider.newFixedThreadPoolWithQueueSize(5, ids.size()));
    } else {
      executor = MoreExecutors.listeningDecorator(newSingleThreadExecutor());
    }
  }

  @Override
  public void startStream() {

    LOGGER.debug("{} startStream", STREAMS_ID);

    Preconditions.checkArgument(!ids.isEmpty());

    running.set(true);

    submitTimelineThreads(ids.toArray(new Long[0]));

    executor.shutdown();

  }

  public boolean shouldContinuePulling(List<Status> statuses) {
    return (statuses != null) && (statuses.size() > 0);
  }

  protected void submitTimelineThreads(Long[] ids) {

    Twitter client = getTwitterClient();

    for (int i = 0; i < ids.length; i++) {

      TwitterTimelineProviderTask providerTask = new TwitterTimelineProviderTask(this, client, ids[i]);
      ListenableFuture future = executor.submit(providerTask);
      futures.add(future);
      LOGGER.info("submitted {}", ids[i]);
    }

  }

  private Collection<Long> retrieveIds(String[] screenNames) {
    Twitter client = getTwitterClient();

    List<Long> ids = new ArrayList<>();
    try {
      for (User twitterUser : client.lookupUsers(screenNames)) {
        ids.add(twitterUser.getId());
      }
    } catch (TwitterException ex) {
      LOGGER.error("Failure retrieving user details.", ex.getMessage());
    }
    return ids;
  }

  @Override
  public StreamsResultSet readCurrent() {

    StreamsResultSet result;

    LOGGER.debug("Providing {} docs", providerQueue.size());

    try {
      lock.writeLock().lock();
      result = new StreamsResultSet(providerQueue);
      result.setCounter(new DatumStatusCounter());
      providerQueue = constructQueue();
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

  protected Queue<StreamsDatum> constructQueue() {
    return new LinkedBlockingQueue<StreamsDatum>();
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
   * Using the "info" list that is contained in the configuration, ensure that all
   * account identifiers are converted to IDs (Longs) instead of screenNames (Strings).
   */
  protected void consolidateToIDs() {
    List<String> screenNames = new ArrayList<>();
    ids = new ArrayList<>();

    for (String account : config.getInfo()) {
      try {
        if (new Long(account) != null) {
          ids.add(Long.parseLong(Objects.toString(account, null)));
        } else {
          screenNames.add(account);
        }
      } catch (Exception ex) {
        LOGGER.error("Exception while trying to add ID: {{}}, {}", account, ex);
      }
    }

    // Twitter allows for batches up to 100 per request, but you cannot mix types
    screenNameBatches = new ArrayList<>();
    while (screenNames.size() >= 100) {
      screenNameBatches.add(screenNames.subList(0, 100).toArray(new String[0]));
      screenNames = screenNames.subList(100, screenNames.size());
    }

    if (screenNames.size() > 0) {
      screenNameBatches.add(screenNames.toArray(new String[ids.size()]));
    }

    for (String[] screenNameBatche : screenNameBatches) {
      Collection<Long> batchIds = retrieveIds(screenNameBatche);
      ids.addAll(batchIds);
    }
  }

  /**
   * get Twitter Client from TwitterUserInformationConfiguration.
   * @return result
   */
  public Twitter getTwitterClient() {

    String baseUrl = TwitterProviderUtil.baseUrl(config);

    ConfigurationBuilder builder = new ConfigurationBuilder()
        .setOAuthConsumerKey(config.getOauth().getConsumerKey())
        .setOAuthConsumerSecret(config.getOauth().getConsumerSecret())
        .setOAuthAccessToken(config.getOauth().getAccessToken())
        .setOAuthAccessTokenSecret(config.getOauth().getAccessTokenSecret())
        .setIncludeEntitiesEnabled(true)
        .setJSONStoreEnabled(true)
        .setAsyncNumThreads(3)
        .setRestBaseURL(baseUrl)
        .setIncludeMyRetweetEnabled(Boolean.TRUE)
        .setPrettyDebugEnabled(Boolean.TRUE);

    return new TwitterFactory(builder.build()).getInstance();
  }

  @Override
  public void cleanUp() {
    shutdownAndAwaitTermination(executor);
  }

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
