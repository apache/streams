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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.facebook.FacebookConfiguration;
import org.apache.streams.facebook.IdConfig;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;
import org.apache.streams.util.api.requests.backoff.impl.ExponentialBackOffStrategy;
import org.apache.streams.util.oauth.tokens.tokenmanager.SimpleTokenManager;
import org.apache.streams.util.oauth.tokens.tokenmanager.impl.BasicTokenManager;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import facebook4j.Facebook;
import facebook4j.FacebookFactory;
import facebook4j.conf.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Abstract data collector for Facebook.  Iterates over ids and queues data to be output
 * by a {@link org.apache.streams.core.StreamsProvider}
 */
public abstract class FacebookDataCollector implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacebookDataCollector.class);
  private static final String READ_ONLY = "read_streams";

  @VisibleForTesting
  protected AtomicBoolean isComplete;
  protected BackOffStrategy backOff;

  private FacebookConfiguration config;
  private BlockingQueue<StreamsDatum> queue;
  private SimpleTokenManager<String> authTokens;

  /**
   * FacebookDataCollector constructor.
   * @param config config
   * @param queue queue
   */
  public FacebookDataCollector(FacebookConfiguration config, BlockingQueue<StreamsDatum> queue) {
    this.config = config;
    this.queue = queue;
    this.isComplete = new AtomicBoolean(false);
    this.backOff = new ExponentialBackOffStrategy(5);
    this.authTokens = new BasicTokenManager<>();
    if (config.getUserAccessTokens() != null) {
      for (String token : config.getUserAccessTokens()) {
        this.authTokens.addTokenToPool(token);
      }
    }
  }

  /**
   * Returns true when the collector has finished querying facebook and has queued all data
   * for the provider.
   * @return isComplete
   */
  public boolean isComplete() {
    return this.isComplete.get();
  }

  /**
   * Queues facebook data.
   * @param data data
   * @param id id
   */
  protected void outputData(Object data, String id) {
    try {
      this.queue.put(new StreamsDatum(data, id));
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Gets a Facebook client.  If multiple authenticated users for this app are available
   * it will rotate through the users oauth credentials
   * @return client
   */
  protected Facebook getNextFacebookClient() {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true);
    cb.setOAuthPermissions(READ_ONLY);
    cb.setOAuthAppId(this.config.getOauth().getAppId());
    cb.setOAuthAppSecret(this.config.getOauth().getAppSecret());
    if (this.authTokens.numAvailableTokens() > 0) {
      cb.setOAuthAccessToken(this.authTokens.getNextAvailableToken());
    } else {
      cb.setOAuthAccessToken(this.config.getOauth().getAppAccessToken());
      LOGGER.debug("appAccessToken : {}", this.config.getOauth().getAppAccessToken());
    }
    cb.setJSONStoreEnabled(true);
    if (!Strings.isNullOrEmpty(config.getVersion())) {
      cb.setRestBaseURL("https://graph.facebook.com/" + config.getVersion() + "/");
    }
    LOGGER.debug("appId : {}", this.config.getOauth().getAppId());
    LOGGER.debug("appSecret: {}", this.config.getOauth().getAppSecret());
    FacebookFactory ff = new FacebookFactory(cb.build());
    return  ff.getInstance();
  }

  /**
   * Queries facebook and queues the resulting data.
   * @param id id
   * @throws Exception Exception
   */
  protected abstract void getData(IdConfig id) throws Exception;


  @Override
  public void run() {
    for ( IdConfig id : this.config.getIds()) {
      try {
        getData(id);
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      } catch (Exception ex) {
        LOGGER.error("Caught Exception while trying to poll data for page : {}", id);
        LOGGER.error("Exception while getting page feed data: {}", ex);
      }
    }
    this.isComplete.set(true);
  }

  @VisibleForTesting
  protected BlockingQueue<StreamsDatum> getQueue() {
    return queue;
  }
}
