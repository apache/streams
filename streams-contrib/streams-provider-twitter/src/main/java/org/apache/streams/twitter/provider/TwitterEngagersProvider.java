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
import org.apache.streams.twitter.api.RetweetsRequest;
import org.apache.streams.twitter.api.Twitter;
import org.apache.streams.twitter.config.TwitterEngagersProviderConfiguration;
import org.apache.streams.twitter.config.TwitterTimelineProviderConfiguration;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
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

import static org.apache.streams.twitter.provider.TwitterUserInformationProvider.MAPPER;

/**
 * Retrieve posts from a list of user ids or names, then provide all of the users who retweeted those posts.
 */
public class TwitterEngagersProvider implements Callable<Iterator<User>>, StreamsProvider, Serializable {

  private static final String STREAMS_ID = "TwitterEngagersProvider";

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterEngagersProvider.class);

  private TwitterEngagersProviderConfiguration config;

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  public TwitterEngagersProviderConfiguration getConfig() {
    return config;
  }

  protected volatile Queue<StreamsDatum> providerQueue;

  protected final AtomicBoolean running = new AtomicBoolean();

  protected Twitter client;

  private List<Callable<Object>> tasks = new ArrayList<>();
  private List<Future<Object>> futures = new ArrayList<>();

  public static ExecutorService executor;

  StreamsConfiguration streamsConfiguration;

  RetweetsRequest baseRetweetsRequest;

  TwitterTimelineProvider timelineProvider;

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
   * mvn exec:java -Dexec.mainClass=org.apache.streams.twitter.provider.TwitterEngagersProvider -Dexec.args="application.conf retweeters.json.txt"
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

    Config testResourceConfig = ConfigFactory.parseFileAnySyntax(file, ConfigParseOptions.defaults().setAllowMissing(false)).withFallback(StreamsConfigurator.getConfig());
    StreamsConfigurator.addConfig(testResourceConfig);

    PrintStream outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
    ObjectMapper mapper = StreamsJacksonMapper.getInstance(new StreamsJacksonMapperConfiguration().withDateFormats(Stream.of(TwitterDateTimeFormat.TWITTER_FORMAT).collect(Collectors.toList())));

    TwitterEngagersProviderConfiguration config = new ComponentConfigurator<>(TwitterEngagersProviderConfiguration.class).detectConfiguration();
    TwitterEngagersProvider provider = new TwitterEngagersProvider(config);

    Iterator<User> results = provider.call();

    results.forEachRemaining(d -> {
      try {
        outStream.println(mapper.writeValueAsString(d));
      } catch( Exception e ) {
        LOGGER.warn("Exception", e);
      }
    });

    outStream.flush();
  }

  public TwitterEngagersProvider(TwitterEngagersProviderConfiguration config) {
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

    timelineProvider = new TwitterTimelineProvider(config);

    if( configurationObject instanceof TwitterEngagersProviderConfiguration ) {
      this.config = (TwitterEngagersProviderConfiguration)configurationObject;
      timelineProvider.prepare(MAPPER.convertValue(this.config, TwitterTimelineProviderConfiguration.class));
    } else if( configurationObject instanceof TwitterTimelineProviderConfiguration ) {
      timelineProvider.prepare(configurationObject);
      this.config = MAPPER.convertValue(this.config, TwitterEngagersProviderConfiguration.class);
    } else {
      timelineProvider.prepare(null);
    }

    streamsConfiguration = StreamsConfigurator.detectConfiguration(StreamsConfigurator.getConfig());

    try {
      lock.writeLock().lock();
      providerQueue = QueueUtils.constructQueue();
    } finally {
      lock.writeLock().unlock();
    }

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

    baseRetweetsRequest = new ComponentConfigurator<>(RetweetsRequest.class).detectConfiguration();

  }

  /**
   * get Twitter Client from TwitterUserInformationConfiguration.
   * @return result
   */
  public Twitter getTwitterClient() throws InstantiationException {

    return Twitter.getInstance(config);

  }

  @Override
  public void startStream() {

    LOGGER.debug("{} startStream", STREAMS_ID);

    Iterator<Tweet> timelineIterator = timelineProvider.call();

    List<Tweet> timelineList = Lists.newArrayList(timelineIterator);

    LOGGER.info("running: {}", running.get());

    timelineList.forEach(tweet -> submitRetweeterIdsTaskThread(tweet.getId()));

    ExecutorUtils.shutdownAndAwaitTermination(executor);

    LOGGER.info("running: {}", running.get());

  }

  protected void submitRetweeterIdsTaskThread( Long postId ) {

    Callable<Object> callable = createTask(postId);
    LOGGER.info("Thread Created: {}", postId);
    tasks.add(callable);
    futures.add(executor.submit(callable));
    LOGGER.info("Thread Submitted: {}", postId);

  }

  protected Callable createTask( Long postId ) {
    RetweetsRequest request = new ComponentConfigurator<>(RetweetsRequest.class).detectConfiguration();
    request.setId(postId);
    Callable callable = new TwitterRetweetsTask(this, client, request);
    return callable;
  }

  @Override
  public StreamsResultSet readCurrent() {

    StreamsResultSet result;

    LOGGER.debug("Providing {} docs", providerQueue.size());

    try {
      lock.writeLock().lock();
      Queue<StreamsDatum> resultQueue = QueueUtils.constructQueue();
      providerQueue.iterator().forEachRemaining(
        datum -> {
          Tweet tweet = ((Tweet) datum.getDocument());
          resultQueue.add(
            new StreamsDatum(
              new User()
                .withId(tweet.getUser().getId())
                .withIdStr(tweet.getUser().getId().toString())
            )
          );
        }
      );
      result = new StreamsResultSet(resultQueue);
      providerQueue = QueueUtils.constructQueue();
    } finally {
      lock.writeLock().unlock();
    }

    if ( result.size() == 0 && providerQueue.isEmpty() && executor.isShutdown() && executor.isTerminated() ) {
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

  @Override
  public void cleanUp() {
    ExecutorUtils.shutdownAndAwaitTermination(executor);
  }

  public boolean isRunning() {
    LOGGER.debug("timelineProvider.isRunning: {}", timelineProvider.isRunning());
    LOGGER.debug("providerQueue.isEmpty: {}", providerQueue.isEmpty());
    LOGGER.debug("providerQueue.size: {}", providerQueue.size());
    LOGGER.debug("executor.isTerminated: {}", executor.isTerminated());
    LOGGER.debug("tasks.size(): {}", tasks.size());
    LOGGER.debug("futures.size(): {}", futures.size());
    if ( timelineProvider.isRunning() == false && tasks.size() > 0 && tasks.size() == futures.size() && executor.isTerminated() ) {
      running.set(false);
    }
    LOGGER.debug("isRunning: {}", running.get());
    return running.get();
  }

  @Override
  public Iterator<User> call() {
    prepare(config);
    startStream();
    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
    } while ( isRunning());
    cleanUp();
    return providerQueue.stream().map( x -> ((Tweet)x.getDocument()).getUser()).distinct().iterator();
  }
}
