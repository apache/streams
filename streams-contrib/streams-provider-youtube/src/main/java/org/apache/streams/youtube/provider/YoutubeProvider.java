/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.youtube.provider;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.streams.util.api.requests.backoff.impl.ExponentialBackOffStrategy;
import org.apache.streams.youtube.YoutubeConfiguration;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public abstract class YoutubeProvider implements StreamsProvider {

  private static final String STREAMS_ID = "YoutubeProvider";

  private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeProvider.class);
  private static final int MAX_BATCH_SIZE = 1000;
  /**
   * Define a global instance of the HTTP transport.
   */
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
  /**
   * Define a global instance of the JSON factory.
   */
  private static final JsonFactory JSON_FACTORY = new JacksonFactory();
  private static final int DEFAULT_THREAD_POOL_SIZE = 5;
  protected YouTube youtube;
  protected YoutubeConfiguration config;
  // This OAuth 2.0 access scope allows for full read/write access to the
  // authenticated user's account.
  private List<String> scopes = Collections.singletonList("https://www.googleapis.com/auth/youtube");
  private List<ListenableFuture<Object>> futures = new ArrayList<>();
  private ListeningExecutorService executor;
  private BlockingQueue<StreamsDatum> datumQueue;
  private AtomicBoolean isComplete;
  private boolean previousPullWasEmpty;

  /**
   * YoutubeProvider constructor.
   */
  public YoutubeProvider() {
    this.config = new ComponentConfigurator<>(YoutubeConfiguration.class)
        .detectConfiguration();

    Objects.requireNonNull(this.config.getApiKey());
  }

  /**
   * YoutubeProvider constructor - uses supplied YoutubeConfiguration.
   * @param config YoutubeConfiguration
   */
  public YoutubeProvider(YoutubeConfiguration config) {
    this.config = config;

    Objects.requireNonNull(this.config.getApiKey());
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void prepare(Object configurationObject) {
    try {
      this.youtube = createYouTubeClient();
    } catch (IOException | GeneralSecurityException ex) {
      LOGGER.error("Failed to created oauth for YouTube : {}", ex);
      throw new RuntimeException(ex);
    }

    this.executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE));
    this.datumQueue = new LinkedBlockingQueue<>(1000);
    this.isComplete = new AtomicBoolean(false);
    this.previousPullWasEmpty = false;
  }

  @Override
  public void startStream() {
    BackOffStrategy backOffStrategy = new ExponentialBackOffStrategy(2);

    for (UserInfo user : this.config.getYoutubeUsers()) {
      if (this.config.getDefaultAfterDate() != null && user.getAfterDate() == null) {
        user.setAfterDate(this.config.getDefaultAfterDate());
      }
      if (this.config.getDefaultBeforeDate() != null && user.getBeforeDate() == null) {
        user.setBeforeDate(this.config.getDefaultBeforeDate());
      }

      ListenableFuture future = executor.submit(getDataCollector(backOffStrategy, this.datumQueue, this.youtube, user));
      futures.add(future);
    }

    this.executor.shutdown();
  }

  protected abstract Runnable getDataCollector(BackOffStrategy strategy, BlockingQueue<StreamsDatum> queue, YouTube youtube, UserInfo userInfo);

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
  protected YouTube createYouTubeClient() throws IOException, GeneralSecurityException {
    GoogleCredential.Builder credentialBuilder = new GoogleCredential.Builder()
        .setTransport(HTTP_TRANSPORT)
        .setJsonFactory(JSON_FACTORY)
        .setServiceAccountId(getConfig().getOauth().getServiceAccountEmailAddress())
        .setServiceAccountScopes(scopes);

    if (StringUtils.isNotEmpty(getConfig().getOauth().getPathToP12KeyFile())) {
      File p12KeyFile = new File(getConfig().getOauth().getPathToP12KeyFile());
      if (p12KeyFile.exists() && p12KeyFile.isFile() && p12KeyFile.canRead()) {
        credentialBuilder = credentialBuilder.setServiceAccountPrivateKeyFromP12File(p12KeyFile);
      }
    }
    Credential credential = credentialBuilder.build();
    return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName("Streams Application").build();
  }

  @Override
  public void cleanUp() {
    ComponentUtils.shutdownExecutor(this.executor, 10, 10);
    this.executor = null;
  }

  public YoutubeConfiguration getConfig() {
    return config;
  }

  public void setConfig(YoutubeConfiguration config) {
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
   * @param userIds Set of String userIds
   */
  public void setUserInfoWithDefaultDates(Set<String> userIds) {
    List<UserInfo> youtubeUsers = new LinkedList<>();

    for (String userId : userIds) {
      UserInfo user = new UserInfo();
      user.setUserId(userId);
      user.setAfterDate(this.config.getDefaultAfterDate());
      user.setBeforeDate(this.config.getDefaultBeforeDate());
      youtubeUsers.add(user);
    }

    this.config.setYoutubeUsers(youtubeUsers);
  }

  /**
   * Set and overwrite user into from teh configuration file. Only sets after dater.
   * @param usersAndAfterDates usersAndAfterDates
   */
  public void setUserInfoWithAfterDate(Map<String, DateTime> usersAndAfterDates) {
    List<UserInfo> youtubeUsers = new LinkedList<>();

    for (String userId : usersAndAfterDates.keySet()) {
      UserInfo user = new UserInfo();
      user.setUserId(userId);
      user.setAfterDate(usersAndAfterDates.get(userId));
      youtubeUsers.add(user);
    }

    this.config.setYoutubeUsers(youtubeUsers);
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
