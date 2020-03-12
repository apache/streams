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
import org.apache.streams.core.DatumStatus;
import org.apache.streams.core.DatumStatusCountable;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.config.TwitterStreamConfiguration;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.util.ComponentUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import com.twitter.hbc.ClientBuilder;
import com.twitter.hbc.core.Constants;
import com.twitter.hbc.core.Hosts;
import com.twitter.hbc.core.HttpHosts;
import com.twitter.hbc.core.endpoint.StatusesFilterEndpoint;
import com.twitter.hbc.core.endpoint.StatusesFirehoseEndpoint;
import com.twitter.hbc.core.endpoint.StatusesSampleEndpoint;
import com.twitter.hbc.core.endpoint.StreamingEndpoint;
import com.twitter.hbc.core.endpoint.UserstreamEndpoint;
import com.twitter.hbc.httpclient.BasicClient;
import com.twitter.hbc.httpclient.auth.Authentication;
import com.twitter.hbc.httpclient.auth.BasicAuth;
import com.twitter.hbc.httpclient.auth.OAuth1;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import org.apache.commons.lang.NotImplementedException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TwitterStreamProvider wraps a hosebird client and passes received documents
 * to subscribing components.
 */
public class TwitterStreamProvider implements StreamsProvider, Serializable, DatumStatusCountable {

  public static final String STREAMS_ID = "TwitterStreamProvider";

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterStreamProvider.class);

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
   *
   * <p></p>
   * Launch using:
   *
   * <p></p>
   * mvn exec:java -Dexec.mainClass=org.apache.streams.twitter.provider.TwitterStreamProvider -Dexec.args="application.conf tweets.json"
   *
   * @param args
   */
  public static void main(String[] args) {

    Preconditions.checkArgument(args.length >= 2);

    String configfile = args[0];
    String outfile = args[1];

    Config reference = ConfigFactory.load();
    File file = new File(configfile);
    assert (file.exists());
    Config testResourceConfig = ConfigFactory.parseFileAnySyntax(file, ConfigParseOptions.defaults().setAllowMissing(false));

    Config typesafe  = testResourceConfig.withFallback(reference).resolve();

    StreamsConfiguration streamsConfiguration = StreamsConfigurator.detectConfiguration(typesafe);
    TwitterStreamConfiguration config = new ComponentConfigurator<>(TwitterStreamConfiguration.class).detectConfiguration();
    TwitterStreamProvider provider = new TwitterStreamProvider(config);

    ObjectMapper mapper = StreamsJacksonMapper.getInstance(Collections.singletonList(TwitterDateTimeFormat.TWITTER_FORMAT));

    PrintStream outStream;
    try {
      outStream = new PrintStream(new BufferedOutputStream(new FileOutputStream(outfile)));
    } catch (FileNotFoundException ex) {
      LOGGER.error("FileNotFoundException", ex);
      return;
    }
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
    while ( provider.isRunning());
    provider.cleanUp();
    outStream.flush();
  }

  private static final int MAX_BATCH = 1000;

  private TwitterStreamConfiguration config;

  public TwitterStreamConfiguration getConfig() {
    return config;
  }

  public void setConfig(TwitterStreamConfiguration config) {
    this.config = config;
  }

  private volatile Queue<Future<List<StreamsDatum>>> providerQueue;

  private Authentication auth;
  protected StreamingEndpoint endpoint;
  private BasicClient client;
  private AtomicBoolean running = new AtomicBoolean(false);
  protected TwitterStreamHelper processor = new TwitterStreamHelper(this);
  private DatumStatusCounter countersCurrent = new DatumStatusCounter();
  private DatumStatusCounter countersTotal = new DatumStatusCounter();

  public TwitterStreamProvider() {
    this.config = new ComponentConfigurator<>(TwitterStreamConfiguration.class).detectConfiguration();
  }

  public TwitterStreamProvider(TwitterStreamConfiguration config) {
    this.config = config;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void startStream() {
    client.connect();
    running.set(true);
  }

  @Override
  public synchronized StreamsResultSet readCurrent() {

    StreamsResultSet current;
    synchronized (this) {
      Queue<StreamsDatum> drain = new LinkedBlockingDeque<>();
      drainTo(drain);
      current = new StreamsResultSet(drain);
      current.setCounter(new DatumStatusCounter());
      current.getCounter().add(countersCurrent);
      countersTotal.add(countersCurrent);
      countersCurrent = new DatumStatusCounter();
    }

    return current;
  }

  @Override
  public StreamsResultSet readNew(BigInteger sequence) {
    throw new NotImplementedException();
  }

  @Override
  public StreamsResultSet readRange(DateTime start, DateTime end)  {
    throw new NotImplementedException();
  }

  @Override
  public boolean isRunning() {
    return this.running.get() && !client.isDone();
  }

  @Override
  public void prepare(Object configurationObject) {

    Objects.requireNonNull(config.getEndpoint());

    Hosts hosebirdHosts;
    if (config.getEndpoint().equals("userstream") ) {

      hosebirdHosts = new HttpHosts(Constants.USERSTREAM_HOST);

      UserstreamEndpoint userstreamEndpoint = new UserstreamEndpoint();
      userstreamEndpoint.withFollowings(true);
      userstreamEndpoint.withUser(false);
      userstreamEndpoint.allReplies(false);
      endpoint = userstreamEndpoint;

    } else if (config.getEndpoint().equals("sample") ) {

      hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);

      boolean track = config.getTrack() != null && !config.getTrack().isEmpty();
      boolean follow = config.getFollow() != null && !config.getFollow().isEmpty();

      if ( track || follow ) {
        LOGGER.debug("***\tPRESENT\t***");
        StatusesFilterEndpoint statusesFilterEndpoint = new StatusesFilterEndpoint();
        if ( track ) {
          statusesFilterEndpoint.trackTerms(config.getTrack());
        }
        if ( follow ) {
          statusesFilterEndpoint.followings(config.getFollow());
        }
        this.endpoint = statusesFilterEndpoint;
      } else {
        endpoint = new StatusesSampleEndpoint();
      }

    } else if ( config.getEndpoint().endsWith("firehose")) {
      hosebirdHosts = new HttpHosts(Constants.STREAM_HOST);
      endpoint = new StatusesFirehoseEndpoint();
    } else {
      LOGGER.error("NO ENDPOINT RESOLVED");
      return;
    }

    if ( config.getBasicauth() != null ) {

      Objects.requireNonNull(config.getBasicauth().getUsername());
      Objects.requireNonNull(config.getBasicauth().getPassword());

      auth = new BasicAuth(
          config.getBasicauth().getUsername(),
          config.getBasicauth().getPassword()
      );

    } else if ( config.getOauth() != null ) {

      Objects.requireNonNull(config.getOauth().getConsumerKey());
      Objects.requireNonNull(config.getOauth().getConsumerSecret());
      Objects.requireNonNull(config.getOauth().getAccessToken());
      Objects.requireNonNull(config.getOauth().getAccessTokenSecret());

      auth = new OAuth1(config.getOauth().getConsumerKey(),
          config.getOauth().getConsumerSecret(),
          config.getOauth().getAccessToken(),
          config.getOauth().getAccessTokenSecret());

    } else {
      LOGGER.error("NO AUTH RESOLVED");
      return;
    }

    LOGGER.debug("host={}\tendpoint={}\taut={}", hosebirdHosts, endpoint, auth);

    providerQueue = new LinkedBlockingQueue<>(MAX_BATCH);

    client = new ClientBuilder()
        .name("apache/streams/streams-contrib/streams-provider-twitter")
        .hosts(hosebirdHosts)
        .endpoint(endpoint)
        .authentication(auth)
        .connectionTimeout(1200000)
        .processor(processor)
        .build();

  }

  @Override
  public void cleanUp() {
    this.client.stop();
    this.processor.cleanUp();
    this.running.set(false);
  }

  @Override
  public DatumStatusCounter getDatumStatusCounter() {
    return countersTotal;
  }

  protected boolean addDatum(Future<List<StreamsDatum>> future) {
    try {
      ComponentUtils.offerUntilSuccess(future, providerQueue);
      countersCurrent.incrementStatus(DatumStatus.SUCCESS);
      return true;
    } catch (Exception ex) {
      countersCurrent.incrementStatus(DatumStatus.FAIL);
      LOGGER.warn("Unable to enqueue item from Twitter stream");
      return false;
    }
  }

  protected void drainTo(Queue<StreamsDatum> drain) {
    int count = 0;
    while (!providerQueue.isEmpty() && count <= MAX_BATCH) {
      for (StreamsDatum datum : pollForDatum()) {
        ComponentUtils.offerUntilSuccess(datum, drain);
        count++;
      }
    }
  }

  protected List<StreamsDatum> pollForDatum()  {
    try {
      return providerQueue.poll().get();
    } catch (InterruptedException ex) {
      LOGGER.warn("Interrupted while waiting for future.  Initiate shutdown.");
      this.cleanUp();
      Thread.currentThread().interrupt();
      return new ArrayList<>();
    } catch (ExecutionException ex) {
      LOGGER.warn("Error getting tweet from future");
      return new ArrayList<>();
    }
  }
}
