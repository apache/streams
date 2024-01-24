/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance *
http://www.apache.org/licenses/LICENSE-2.0 *
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License. */

package org.apache.streams.instagram.provider;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.instagram.api.Instagram;
import org.apache.streams.instagram.api.SearchUsersRequest;
import org.apache.streams.instagram.api.SearchUsersResponse;
import org.apache.streams.instagram.config.InstagramConfiguration;
import org.apache.streams.instagram.config.InstagramOAuthConfiguration;
import org.apache.streams.util.ComponentUtils;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.streams.util.api.requests.backoff.impl.ExponentialBackOffStrategy;
import org.apache.streams.util.oauth.tokens.tokenmanager.SimpleTokenManager;
import org.apache.streams.util.oauth.tokens.tokenmanager.impl.BasicTokenManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Executes on all of the Instagram requests to collect the Instagram data.
 * <p></p>
 * If errors/exceptions occur when trying to gather data for a particular user, that user is skipped and the collector
 * move on to the next user.  If a rate limit exception occurs it employs an exponential back off strategy.
 */
public abstract class InstagramDataCollector<T> implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramDataCollector.class);

  protected Queue<StreamsDatum> dataQueue; //exposed for testing
  private InstagramConfiguration config;
  protected AtomicBoolean isCompleted;
  private SimpleTokenManager<InstagramOAuthConfiguration> tokenManger;
  protected int consecutiveErrorCount;
  protected BackOffStrategy backOffStrategy;
  protected Instagram instagram;

  /**
   * InstagramDataCollector constructor.
   * @param queue Queue of StreamsDatum
   * @param config InstagramConfiguration
   */
  public InstagramDataCollector(Instagram instagram, Queue<StreamsDatum> queue, InstagramConfiguration config) {
    this.dataQueue = queue;
    this.config = config;
    this.isCompleted = new AtomicBoolean(false);
    this.tokenManger = new BasicTokenManager<InstagramOAuthConfiguration>();
    this.consecutiveErrorCount = 0;
    this.backOffStrategy = new ExponentialBackOffStrategy(2);
    this.instagram = instagram;
  }


  /**
   * If there are authorized tokens available, it sets a new token for the client and returns
   * the client.  If there are no available tokens, it simply returns the client that was
   * initialized in the constructor with client id.
   * @return result
   */
  protected Instagram getNextInstagramClient() {
    //    if (this.tokenManger.numAvailableTokens() > 0) {
    //      this.instagram.setAccessToken(this.tokenManger.getNextAvailableToken());
    //    }
    return this.instagram;
  }

  /**
   * Return the number of available tokens for this data collector.
   * @return numbeer of available tokens
   */
  protected int numAvailableTokens() {
    return this.tokenManger.numAvailableTokens();
  }

  /**
   * Queues the Instagram data to be output by the provider.
   * @param userData data to queue
   * @param userId user id who the data came from
   */
  protected void queueData(Collection<T> userData, String userId) {
    if (userData == null) {
      LOGGER.warn("User id, {}, returned a NULL data from instagram.", userId);
    } else {
      for (T data : userData) {
        ComponentUtils.offerUntilSuccess(convertToStreamsDatum(data), this.dataQueue);
      }
    }
  }

  /**
   * @return true when the collector has queued all of the available Instagram data for the provided users.
   */
  public boolean isCompleted() {
    return this.isCompleted.get();
  }

  /**
   * Takes an Instagram Object and sets it as the document of a streams datum and sets the id of the streams datum.
   * @param item item
   * @return StreamsDatum
   */
  protected abstract StreamsDatum convertToStreamsDatum(T item);

  public String swapUsernameForId(String username) {
    SearchUsersRequest searchUsersRequest = new SearchUsersRequest()
      .withQ(username);
    SearchUsersResponse searchUsersResponse =
      getNextInstagramClient().searchUser(searchUsersRequest);
    if( searchUsersResponse.getData().size() > 0 &&
      searchUsersResponse.getData().get(0).getUsername().equals(username)) {
      return searchUsersResponse.getData().get(0).getId();
    }
    return null;
  }
}
