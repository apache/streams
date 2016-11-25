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
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.util.ComponentUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * Retrieve current profile status from a list of user ids or names.
 */
public class TwitterUserInformationProvider implements StreamsProvider, Serializable {

  public static final String STREAMS_ID = "TwitterUserInformationProvider";

  private static ObjectMapper MAPPER = new StreamsJacksonMapper(Lists.newArrayList(TwitterDateTimeFormat.TWITTER_FORMAT));

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterUserInformationProvider.class);

  public static final int MAX_NUMBER_WAITING = 1000;

  private TwitterUserInformationConfiguration config;

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
   * mvn exec:java -Dexec.mainClass=org.apache.streams.twitter.provider.TwitterUserInformationProvider -Dexec.args="application.conf tweets.json"
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
    TwitterUserInformationProvider provider = new TwitterUserInformationProvider(config);

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
          json = MAPPER.writeValueAsString(datum.getDocument());
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

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  protected volatile Queue<StreamsDatum> providerQueue;

  public TwitterUserInformationConfiguration getConfig() {
    return config;
  }

  public void setConfig(TwitterUserInformationConfiguration config) {
    this.config = config;
  }

  protected Iterator<Long[]> idsBatches;
  protected Iterator<String[]> screenNameBatches;

  protected ListeningExecutorService executor;

  protected DateTime start;
  protected DateTime end;

  protected final AtomicBoolean running = new AtomicBoolean();

  // TODO: this should be abstracted out
  public static ExecutorService newFixedThreadPoolWithQueueSize(int numThreads, int queueSize) {
    return new ThreadPoolExecutor(numThreads, numThreads,
        5000L, TimeUnit.MILLISECONDS,
        new ArrayBlockingQueue<Runnable>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
  }

  /**
   * TwitterUserInformationProvider constructor.
   * Resolves config from JVM properties 'twitter'.
   */
  public TwitterUserInformationProvider() {
    this.config = new ComponentConfigurator<>(TwitterUserInformationConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig().getConfig("twitter"));
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
      config = (TwitterUserInformationConfiguration) configurationObject;
    }

    Preconditions.checkNotNull(config);
    Preconditions.checkNotNull(config.getOauth());
    Preconditions.checkNotNull(config.getOauth().getConsumerKey());
    Preconditions.checkNotNull(config.getOauth().getConsumerSecret());
    Preconditions.checkNotNull(config.getOauth().getAccessToken());
    Preconditions.checkNotNull(config.getOauth().getAccessTokenSecret());
    Preconditions.checkNotNull(config.getInfo());

    try {
      lock.writeLock().lock();
      providerQueue = constructQueue();
    } finally {
      lock.writeLock().unlock();
    }

    Preconditions.checkNotNull(providerQueue);

    List<String> screenNames = new ArrayList<String>();
    List<String[]> screenNameBatches = new ArrayList<String[]>();

    List<Long> ids = new ArrayList<Long>();
    List<Long[]> idsBatches = new ArrayList<Long[]>();

    for (String s : config.getInfo()) {
      if (s != null) {
        String potentialScreenName = s.replaceAll("@", "").trim().toLowerCase();

        // See if it is a long, if it is, add it to the user iD list, if it is not, add it to the
        // screen name list
        try {
          ids.add(Long.parseLong(potentialScreenName));
        } catch (Exception ex) {
          screenNames.add(potentialScreenName);
        }

        // Twitter allows for batches up to 100 per request, but you cannot mix types

        if (ids.size() >= 100) {
          // add the batch
          idsBatches.add(ids.toArray(new Long[ids.size()]));
          // reset the Ids
          ids = new ArrayList<Long>();
        }

        if (screenNames.size() >= 100) {
          // add the batch
          screenNameBatches.add(screenNames.toArray(new String[ids.size()]));
          // reset the Ids
          screenNames = new ArrayList<String>();
        }
      }
    }


    if (ids.size() > 0) {
      idsBatches.add(ids.toArray(new Long[ids.size()]));
    }

    if (screenNames.size() > 0) {
      screenNameBatches.add(screenNames.toArray(new String[ids.size()]));
    }

    if (ids.size() + screenNames.size() > 0) {
      executor = MoreExecutors.listeningDecorator(newFixedThreadPoolWithQueueSize(5, (ids.size() + screenNames.size())));
    } else {
      executor = MoreExecutors.listeningDecorator(newSingleThreadExecutor());
    }

    Preconditions.checkNotNull(executor);

    this.idsBatches = idsBatches.iterator();
    this.screenNameBatches = screenNameBatches.iterator();
  }

  @Override
  public void startStream() {

    Preconditions.checkNotNull(executor);

    Preconditions.checkArgument(idsBatches.hasNext() || screenNameBatches.hasNext());

    LOGGER.info("{}{} - startStream", idsBatches, screenNameBatches);

    while (idsBatches.hasNext()) {
      loadBatch(idsBatches.next());
    }

    while (screenNameBatches.hasNext()) {
      loadBatch(screenNameBatches.next());
    }

    running.set(true);

    executor.shutdown();
  }

  protected void loadBatch(Long[] ids) {
    Twitter client = getTwitterClient();
    int keepTrying = 0;

    // keep trying to load, give it 5 attempts.
    //while (keepTrying < 10)
    while (keepTrying < 1) {
      try {
        long[] toQuery = new long[ids.length];

        for (int i = 0; i < ids.length; i++) {
          toQuery[i] = ids[i];
        }

        for (twitter4j.User twitterUser : client.lookupUsers(toQuery)) {
          String json = DataObjectFactory.getRawJSON(twitterUser);
          try {
            User user = MAPPER.readValue(json, org.apache.streams.twitter.pojo.User.class);
            ComponentUtils.offerUntilSuccess(new StreamsDatum(user), providerQueue);
          } catch (Exception exception) {
            LOGGER.warn("Failed to read document as User ", twitterUser);
          }
        }
        keepTrying = 10;
      } catch (TwitterException twitterException) {
        keepTrying += TwitterErrorHandler.handleTwitterError(client, twitterException);
      } catch (Exception ex) {
        keepTrying += TwitterErrorHandler.handleTwitterError(client, ex);
      }
    }
  }

  protected void loadBatch(String[] ids) {
    Twitter client = getTwitterClient();
    int keepTrying = 0;

    // keep trying to load, give it 5 attempts.
    //while (keepTrying < 10)
    while (keepTrying < 1) {
      try {
        for (twitter4j.User twitterUser : client.lookupUsers(ids)) {
          String json = DataObjectFactory.getRawJSON(twitterUser);
          try {
            User user = MAPPER.readValue(json, org.apache.streams.twitter.pojo.User.class);
            ComponentUtils.offerUntilSuccess(new StreamsDatum(user), providerQueue);
          } catch (Exception exception) {
            LOGGER.warn("Failed to read document as User ", twitterUser);
          }
        }
        keepTrying = 10;
      } catch (TwitterException twitterException) {
        keepTrying += TwitterErrorHandler.handleTwitterError(client, twitterException);
      } catch (Exception ex) {
        keepTrying += TwitterErrorHandler.handleTwitterError(client, ex);
      }
    }
  }

  @Override
  public StreamsResultSet readCurrent() {

    LOGGER.debug("{}{} - readCurrent", idsBatches, screenNameBatches);

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

  protected Queue<StreamsDatum> constructQueue() {
    return new LinkedBlockingQueue<StreamsDatum>();
  }

  public StreamsResultSet readNew(BigInteger sequence) {
    LOGGER.debug("{} readNew", STREAMS_ID);
    throw new NotImplementedException();
  }

  @Override
  public StreamsResultSet readRange(DateTime start, DateTime end) {
    LOGGER.debug("{} readRange", STREAMS_ID);
    this.start = start;
    this.end = end;
    readCurrent();
    StreamsResultSet result = (StreamsResultSet)providerQueue.iterator();
    return result;
  }

  @Override
  public boolean isRunning() {

    if ( providerQueue.isEmpty() && executor.isTerminated() ) {
      LOGGER.info("{}{} - completed", idsBatches, screenNameBatches);

      running.set(false);

      LOGGER.info("Exiting");
    }

    return running.get();
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


  // TODO: abstract out, also appears in TwitterTimelineProvider
  protected Twitter getTwitterClient() {
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

  protected void callback() {


  }

  @Override
  public void cleanUp() {
    shutdownAndAwaitTermination(executor);
  }
}
