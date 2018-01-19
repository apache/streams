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
import org.apache.streams.twitter.TwitterEngagersProviderConfiguration;
import org.apache.streams.twitter.TwitterTimelineProviderConfiguration;
import org.apache.streams.twitter.api.RetweetsRequest;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.Tweet;
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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
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
public class TwitterEngagersProvider extends TwitterTimelineProvider implements StreamsProvider, Serializable {

  private static final String STREAMS_ID = "TwitterEngagersProvider";

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterEngagersProvider.class);

  private TwitterEngagersProviderConfiguration config;

  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  public TwitterEngagersProviderConfiguration getConfig() {
    return config;
  }

  protected volatile Queue<StreamsDatum> providerQueue;

  protected final AtomicBoolean running = new AtomicBoolean();

  private List<ListenableFuture<Object>> futures = new ArrayList<>();

  protected ListeningExecutorService executor;

  StreamsConfiguration streamsConfiguration;

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
   * mvn exec:java -Dexec.mainClass=org.apache.streams.twitter.provider.TwitterEngagersProvider -Dexec.args="application.conf retweeters.json.txt"
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
    TwitterEngagersProviderConfiguration config = new ComponentConfigurator<>(TwitterEngagersProviderConfiguration.class).detectConfiguration(typesafe, "twitter");
    TwitterEngagersProvider provider = new TwitterEngagersProvider(config);

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

    if( configurationObject instanceof TwitterEngagersProviderConfiguration ) {
      this.config = (TwitterEngagersProviderConfiguration)configurationObject;
      super.prepare(MAPPER.convertValue(this.config, TwitterTimelineProviderConfiguration.class));
    } else if( configurationObject instanceof TwitterTimelineProviderConfiguration ) {
      super.prepare(configurationObject);
      this.config = MAPPER.convertValue(this.config, TwitterEngagersProviderConfiguration.class);
    } else {
      super.prepare(null);
    }

    streamsConfiguration = StreamsConfigurator.detectConfiguration(StreamsConfigurator.getConfig());

    try {
      lock.writeLock().lock();
      providerQueue = constructQueue();
    } finally {
      lock.writeLock().unlock();
    }

    executor = MoreExecutors.listeningDecorator(
      TwitterUserInformationProvider.newFixedThreadPoolWithQueueSize(
        config.getThreadsPerProvider().intValue(),
        streamsConfiguration.getQueueSize().intValue()
      )
    );

  }

  @Override
  public void startStream() {

    LOGGER.debug("{} startStream", STREAMS_ID);

    super.startStream();

    do {
      Uninterruptibles.sleepUninterruptibly(streamsConfiguration.getBatchFrequencyMs(), TimeUnit.MILLISECONDS);
      Iterator<StreamsDatum> iterator = super.readCurrent().iterator();
      while (iterator.hasNext()) {
        StreamsDatum datum = iterator.next();
        Tweet tweet = (Tweet) datum.getDocument();
        submitRetweeterIdsTaskThread(tweet.getId());
      }
    }
    while ( super.isRunning() );
    super.cleanUp();
    executor.shutdown();

  }

  protected void submitRetweeterIdsTaskThread( Long postId ) {

    RetweetsRequest request = new RetweetsRequest();
    request.setId(postId);
    request.setCount(100l);
    TwitterRetweetsTask providerTask = new TwitterRetweetsTask(
      this,
      client,
      request
    );
    ListenableFuture future = executor.submit(providerTask);
    super.futures.add(future);
    LOGGER.info("Thread Submitted: {}", providerTask.request);

  }

  @Override
  public StreamsResultSet readCurrent() {

    StreamsResultSet result;

    LOGGER.debug("Providing {} docs", providerQueue.size());

    try {
      lock.writeLock().lock();
      Queue<StreamsDatum> resultQueue = constructQueue();
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

  @Override
  public void cleanUp() {
    super.cleanUp();
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
