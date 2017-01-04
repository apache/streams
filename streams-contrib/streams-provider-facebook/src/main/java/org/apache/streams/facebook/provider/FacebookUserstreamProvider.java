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

package org.apache.streams.facebook.provider;

import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.DatumStatusCounter;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.facebook.FacebookUserInformationConfiguration;
import org.apache.streams.facebook.FacebookUserstreamConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.util.ComponentUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import facebook4j.Facebook;
import facebook4j.FacebookException;
import facebook4j.FacebookFactory;
import facebook4j.Post;
import facebook4j.ResponseList;
import facebook4j.conf.ConfigurationBuilder;
import facebook4j.json.DataObjectFactory;
import org.apache.commons.lang.NotImplementedException;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class FacebookUserstreamProvider implements StreamsProvider, Serializable {

  private static final String STREAMS_ID = "FacebookUserstreamProvider";

  private static final Logger LOGGER = LoggerFactory.getLogger(FacebookUserstreamProvider.class);

  private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  private static final String ALL_PERMISSIONS = "read_stream";
  private FacebookUserstreamConfiguration configuration;

  private Class klass;
  protected final ReadWriteLock lock = new ReentrantReadWriteLock();

  protected volatile Queue<StreamsDatum> providerQueue = new LinkedBlockingQueue<>();

  public FacebookUserstreamConfiguration getConfig() {
    return configuration;
  }

  public void setConfig(FacebookUserstreamConfiguration config) {
    this.configuration = config;
  }

  protected ExecutorService executor;

  protected DateTime start;
  protected DateTime end;

  protected final AtomicBoolean running = new AtomicBoolean();

  private DatumStatusCounter countersCurrent = new DatumStatusCounter();
  private DatumStatusCounter countersTotal = new DatumStatusCounter();

  protected Facebook client;

  private static ExecutorService newFixedThreadPoolWithQueueSize(int numThreads, int queueSize) {
    return new ThreadPoolExecutor(numThreads, numThreads,
        5000L, TimeUnit.MILLISECONDS,
        new ArrayBlockingQueue<>(queueSize, true), new ThreadPoolExecutor.CallerRunsPolicy());
  }

  /**
   * FacebookUserstreamProvider constructor.
   */
  public FacebookUserstreamProvider() {
    Config config = StreamsConfigurator.config.getConfig("facebook");
    FacebookUserInformationConfiguration facebookUserInformationConfiguration;
    try {
      facebookUserInformationConfiguration = mapper.readValue(config.root().render(ConfigRenderOptions.concise()), FacebookUserInformationConfiguration.class);
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  /**
   * FacebookUserstreamProvider constructor.
   * @param config config
   */
  public FacebookUserstreamProvider(FacebookUserstreamConfiguration config) {
    this.configuration = config;
  }

  /**
   * FacebookUserstreamProvider constructor.
   * @param klass output Class
   */
  public FacebookUserstreamProvider(Class klass) {
    Config config = StreamsConfigurator.config.getConfig("facebook");
    FacebookUserInformationConfiguration facebookUserInformationConfiguration;
    try {
      facebookUserInformationConfiguration = mapper.readValue(config.root().render(ConfigRenderOptions.concise()), FacebookUserInformationConfiguration.class);
    } catch (IOException ex) {
      ex.printStackTrace();
      return;
    }
    this.klass = klass;
  }

  public FacebookUserstreamProvider(FacebookUserstreamConfiguration config, Class klass) {
    this.configuration = config;
    this.klass = klass;
  }

  public Queue<StreamsDatum> getProviderQueue() {
    return this.providerQueue;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void startStream() {

    client = getFacebookClient();

    if ( configuration.getInfo() != null
         &&
         configuration.getInfo().size() > 0 ) {
      for ( String id : configuration.getInfo()) {
        executor.submit(new FacebookFeedPollingTask(this, id));
      }
      running.set(true);
    } else {
      try {
        String id = client.getMe().getId();
        executor.submit(new FacebookFeedPollingTask(this, id));
        running.set(true);
      } catch (FacebookException ex) {
        LOGGER.error(ex.getMessage());
        running.set(false);
      }
    }
  }

  @Override
  public StreamsResultSet readCurrent() {

    StreamsResultSet current;

    synchronized (FacebookUserstreamProvider.class) {
      current = new StreamsResultSet(new ConcurrentLinkedQueue<>(providerQueue));
      current.setCounter(new DatumStatusCounter());
      current.getCounter().add(countersCurrent);
      countersTotal.add(countersCurrent);
      countersCurrent = new DatumStatusCounter();
      providerQueue.clear();
    }

    return current;

  }

  @Override
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
    return (StreamsResultSet) providerQueue.iterator();
  }

  @Override
  public boolean isRunning() {
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

  @Override
  public void prepare(Object configurationObject) {

    executor = newFixedThreadPoolWithQueueSize(5, 20);

    Objects.requireNonNull(providerQueue);
    Objects.requireNonNull(this.klass);
    Objects.requireNonNull(configuration.getOauth().getAppId());
    Objects.requireNonNull(configuration.getOauth().getAppSecret());
    Objects.requireNonNull(configuration.getOauth().getUserAccessToken());

    client = getFacebookClient();

    if ( configuration.getInfo() != null
         &&
         configuration.getInfo().size() > 0 ) {

      List<String> ids = new ArrayList<>();
      List<String[]> idsBatches = new ArrayList<>();

      for (String s : configuration.getInfo()) {
        if (s != null) {
          ids.add(s);

          if (ids.size() >= 100) {
            // add the batch
            idsBatches.add(ids.toArray(new String[ids.size()]));
            // reset the Ids
            ids = new ArrayList<>();
          }

        }
      }
    }
  }

  protected Facebook getFacebookClient() {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true)
        .setOAuthAppId(configuration.getOauth().getAppId())
        .setOAuthAppSecret(configuration.getOauth().getAppSecret())
        .setOAuthAccessToken(configuration.getOauth().getUserAccessToken())
        .setOAuthPermissions(ALL_PERMISSIONS)
        .setJSONStoreEnabled(true);

    FacebookFactory ff = new FacebookFactory(cb.build());

    return ff.getInstance();
  }

  @Override
  public void cleanUp() {
    shutdownAndAwaitTermination(executor);
  }

  private class FacebookFeedPollingTask implements Runnable {

    FacebookUserstreamProvider provider;
    Facebook client;
    String id;

    private Set<Post> priorPollResult = new HashSet<>();

    public FacebookFeedPollingTask(FacebookUserstreamProvider facebookUserstreamProvider) {
      this.provider = facebookUserstreamProvider;
    }

    public FacebookFeedPollingTask(FacebookUserstreamProvider facebookUserstreamProvider, String id) {
      this.provider = facebookUserstreamProvider;
      this.client = provider.client;
      this.id = id;
    }

    @Override
    public void run() {
      while (provider.isRunning()) {
        ResponseList<Post> postResponseList;
        try {
          postResponseList = client.getFeed(id);

          Set<Post> update = new HashSet<>(postResponseList);
          Set<Post> repeats = priorPollResult.stream().filter(update::contains).collect(Collectors.toSet());
          Set<Post> entrySet = update.stream().filter((x) -> !repeats.contains(x)).collect(Collectors.toSet());
          LOGGER.debug(this.id + " response: " + update.size() + " previous: " + repeats.size() + " new: " + entrySet.size());
          for (Post item : entrySet) {
            String json = DataObjectFactory.getRawJSON(item);
            org.apache.streams.facebook.Post post = mapper.readValue(json, org.apache.streams.facebook.Post.class);
            try {
              lock.readLock().lock();
              ComponentUtils.offerUntilSuccess(new StreamsDatum(post), providerQueue);
              countersCurrent.incrementAttempt();
            } finally {
              lock.readLock().unlock();
            }
          }
          priorPollResult = update;
        } catch (Exception ex) {
          ex.printStackTrace();
        } finally {
          try {
            Thread.sleep(configuration.getPollIntervalMillis());
          } catch (InterruptedException interrupt) {
            Thread.currentThread().interrupt();
          }
        }
      }
    }
  }
}
