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

package org.apache.streams.gplus.provider;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.google.gplus.GPlusConfiguration;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.streams.util.api.requests.backoff.impl.ExponentialBackOffStrategy;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.plus.Plus;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Provider that creates a GPlus client and will run task that queue data to an outing queue.
 */
public abstract class AbstractGPlusProvider implements StreamsProvider {

  public static final String STREAMS_ID = "AbstractGPlusProvider";

  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractGPlusProvider.class);
  private static final Set<String> SCOPE = new HashSet<String>() {
    {
      add("https://www.googleapis.com/auth/plus.login");
    }
  };
  private static final int MAX_BATCH_SIZE = 1000;

  private static final HttpTransport TRANSPORT = new NetHttpTransport();
  private static final JacksonFactory JSON_FACTORY = new JacksonFactory();
  private static final Gson GSON = new Gson();

  private GPlusConfiguration config;

  List<ListenableFuture<Object>> futures = new ArrayList<>();

  private ListeningExecutorService executor;

  private BlockingQueue<StreamsDatum> datumQueue;
  private AtomicBoolean isComplete;
  private boolean previousPullWasEmpty;

  protected GoogleCredential credential;
  protected Plus plus;

  public AbstractGPlusProvider() {
    this.config = new ComponentConfigurator<>(GPlusConfiguration.class).detectConfiguration();
  }

  public AbstractGPlusProvider(GPlusConfiguration config) {
    this.config = config;
  }

  @Override
  public void prepare(Object configurationObject) {

    Objects.requireNonNull(config.getOauth().getPathToP12KeyFile());
    Objects.requireNonNull(config.getOauth().getAppName());
    Objects.requireNonNull(config.getOauth().getServiceAccountEmailAddress());

    try {
      this.plus = createPlusClient();
    } catch (IOException | GeneralSecurityException ex) {
      LOGGER.error("Failed to created oauth for GPlus : {}", ex);
      throw new RuntimeException(ex);
    }
    // GPlus rate limits you to 5 calls per second, so there is not a need to execute more than one
    // collector unless you have multiple oauth tokens
    //TODO make this configurable based on the number of oauth tokens
    this.executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(1));
    this.datumQueue = new LinkedBlockingQueue<>(1000);
    this.isComplete = new AtomicBoolean(false);
    this.previousPullWasEmpty = false;
  }

  @Override
  public void startStream() {

    BackOffStrategy backOffStrategy = new ExponentialBackOffStrategy(2);
    for (UserInfo user : this.config.getGooglePlusUsers()) {
      if (this.config.getDefaultAfterDate() != null && user.getAfterDate() == null) {
        user.setAfterDate(this.config.getDefaultAfterDate());
      }
      if (this.config.getDefaultBeforeDate() != null && user.getBeforeDate() == null) {
        user.setBeforeDate(this.config.getDefaultBeforeDate());
      }
      this.executor.submit(getDataCollector(backOffStrategy, this.datumQueue, this.plus, user));
    }
    this.executor.shutdown();
  }

  protected abstract Runnable getDataCollector(BackOffStrategy strategy, BlockingQueue<StreamsDatum> queue, Plus plus, UserInfo userInfo);

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public StreamsResultSet readCurrent() {
    BlockingQueue<StreamsDatum> batch = new LinkedBlockingQueue<>();
    int batchCount = 0;
    while (!this.datumQueue.isEmpty() && batchCount < MAX_BATCH_SIZE) {
      StreamsDatum datum = ComponentUtils.pollWhileNotEmpty(this.datumQueue);
      if (datum != null) {
        ++batchCount;
        ComponentUtils.offerUntilSuccess(datum, batch);
      }
    }
    boolean pullIsEmpty = batch.isEmpty() && this.datumQueue.isEmpty() && this.executor.isTerminated();
    this.isComplete.set(this.previousPullWasEmpty && pullIsEmpty);
    this.previousPullWasEmpty = pullIsEmpty;
    return new StreamsResultSet(batch);
  }

  @Override
  public StreamsResultSet readNew(BigInteger sequence) {
    return null;
  }

  @Override
  public StreamsResultSet readRange(DateTime start, DateTime end) {
    return null;
  }

  @VisibleForTesting
  protected Plus createPlusClient() throws IOException, GeneralSecurityException {
    credential = new GoogleCredential.Builder()
        .setJsonFactory(JSON_FACTORY)
        .setTransport(TRANSPORT)
        .setServiceAccountScopes(SCOPE)
        .setServiceAccountId(this.config.getOauth().getServiceAccountEmailAddress())
        .setServiceAccountPrivateKeyFromP12File(new File(this.config.getOauth().getPathToP12KeyFile()))
        .build();
    return new Plus.Builder(TRANSPORT,JSON_FACTORY, credential).setApplicationName(this.config.getOauth().getAppName()).build();
  }

  @Override
  public void cleanUp() {
    ComponentUtils.shutdownExecutor(this.executor, 10, 10);
    this.executor = null;
  }

  public GPlusConfiguration getConfig() {
    return config;
  }

  public void setConfig(GPlusConfiguration config) {
    this.config = config;
  }

  /**
   * Set and overwrite the default before date that was read from the configuration file.
   * @param defaultBeforeDate defaultBeforeDate
   */
  public void setDefaultBeforeDate(DateTime defaultBeforeDate) {
    this.config.setDefaultBeforeDate(defaultBeforeDate);
  }

  /**
   * Set and overwrite the default after date that was read from teh configuration file.
   * @param defaultAfterDate defaultAfterDate
   */
  public void setDefaultAfterDate(DateTime defaultAfterDate) {
    this.config.setDefaultAfterDate(defaultAfterDate);
  }

  /**
   * Sets and overwrite the user info from the configuaration file.  Uses the defaults before and after dates.
   * @param userIds userIds
   */
  public void setUserInfoWithDefaultDates(Set<String> userIds) {
    List<UserInfo> gplusUsers = new LinkedList<>();
    for (String userId : userIds) {
      UserInfo user = new UserInfo();
      user.setUserId(userId);
      user.setAfterDate(this.config.getDefaultAfterDate());
      user.setBeforeDate(this.config.getDefaultBeforeDate());
      gplusUsers.add(user);
    }
    this.config.setGooglePlusUsers(gplusUsers);
  }

  /**
   * Set and overwrite user into from the configuration file. Only sets after date.
   * @param usersAndAfterDates usersAndAfterDates
   */
  public void setUserInfoWithAfterDate(Map<String, DateTime> usersAndAfterDates) {
    List<UserInfo> gplusUsers = new LinkedList<>();
    for (String userId : usersAndAfterDates.keySet()) {
      UserInfo user = new UserInfo();
      user.setUserId(userId);
      user.setAfterDate(usersAndAfterDates.get(userId));
      gplusUsers.add(user);
    }
    this.config.setGooglePlusUsers(gplusUsers);
  }

  @Override
  public boolean isRunning() {
    if (datumQueue.isEmpty() && executor.isTerminated() && Futures.allAsList(futures).isDone()) {
      LOGGER.info("Completed");
      isComplete.set(true);
      LOGGER.info("Exiting");
    }
    return !isComplete.get();
  }


}
