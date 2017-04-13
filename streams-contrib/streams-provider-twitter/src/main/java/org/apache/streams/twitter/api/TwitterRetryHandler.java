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

package org.apache.streams.twitter.api;

import org.apache.streams.util.api.requests.backoff.AbstractBackOffStrategy;
import org.apache.streams.util.api.requests.backoff.BackOffException;
import org.apache.streams.util.api.requests.backoff.impl.LinearTimeBackOffStrategy;

import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;
import org.apache.juneau.rest.client.RetryOn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  Handle expected and unexpected exceptions.
 */
public class TwitterRetryHandler implements RetryOn {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterRetryHandler.class);

  private static AbstractBackOffStrategy backoff_strategy;

// TODO: once request context is available, we can maintain multiple BackoffStrategy one per request path / params
//  private static Map<String, AbstractBackOffStrategy> backoffs = new ConcurrentHashMap<>();

// This is everything we used to check via twitter4j to decide whether to retry.
//
// @Deprecated
//  public static int handleTwitterError(Twitter twitter, Exception exception) {
//    return handleTwitterError( twitter, null, exception);
//  }
//
//
//  public static int handleTwitterError(Twitter twitter, Long id, Exception exception) {
//
//    if (exception instanceof TwitterException) {
//      TwitterException twitterException = (TwitterException)exception;
//
//      if (twitterException.exceededRateLimitation()) {
//
//        long millisUntilReset = retry;
//
//        final RateLimitStatus rateLimitStatus = twitterException.getRateLimitStatus();
//        if (rateLimitStatus != null) {
//          millisUntilReset = rateLimitStatus.getSecondsUntilReset() * 1000;
//        }
//
//        LOGGER.warn("Rate Limit Exceeded. Will retry in {} seconds...", millisUntilReset / 1000);
//
//        try {
//          Thread.sleep(millisUntilReset);
//        } catch (InterruptedException e1) {
//          Thread.currentThread().interrupt();
//        }
//
//        return 1;
//      } else if (twitterException.isCausedByNetworkIssue()) {
//        LOGGER.info("Twitter Network Issues Detected. Backing off...");
//        LOGGER.info("{} - {}", twitterException.getExceptionCode(), twitterException.getLocalizedMessage());
//        try {
//          Thread.sleep(retry);
//        } catch (InterruptedException e1) {
//          Thread.currentThread().interrupt();
//        }
//        return 1;
//      } else if (twitterException.isErrorMessageAvailable()) {
//        if (twitterException.getMessage().toLowerCase().contains("does not exist")) {
//          if ( id != null ) {
//            LOGGER.warn("User does not exist: {}", id);
//          } else {
//            LOGGER.warn("User does not exist");
//          }
//          return (int)retryMax;
//        } else {
//          return (int)retryMax / 3;
//        }
//      } else {
//        if (twitterException.getExceptionCode().equals("ced778ef-0c669ac0")) {
//          // This is a known weird issue, not exactly sure the cause, but you'll never be able to get the data.
//          return (int)retryMax / 3;
//        } else if (twitterException.getExceptionCode().equals("4be80492-0a7bf7c7")) {
//          // This is a 401 reflecting credentials don't have access to the requested resource.
//          if ( id != null ) {
//            LOGGER.warn("Authentication Exception accessing id: {}", id);
//          } else {
//            LOGGER.warn("Authentication Exception");
//          }
//          return (int)retryMax;
//        } else {
//          LOGGER.warn("Unknown Twitter Exception...");
//          LOGGER.warn("   Access: {}", twitterException.getAccessLevel());
//          LOGGER.warn("     Code: {}", twitterException.getExceptionCode());
//          LOGGER.warn("  Message: {}", twitterException.getLocalizedMessage());
//          return (int)retryMax / 10;
//        }
//      }
//    } else if (exception instanceof RuntimeException) {
//      LOGGER.warn("TwitterGrabber: Unknown Runtime Error", exception.getMessage());
//      return (int)retryMax / 3;
//    } else {
//      LOGGER.info("Completely Unknown Exception: {}", exception);
//      return (int)retryMax / 3;
//    }
//  }
//  TODO: juneau 6.3.x-incubating
//  @Override
//  public boolean onCode(int httpResponseCode) {
//
//    LOGGER.warn("TwitterRetryHandler: {}", httpResponseCode);
//
//    if( httpResponseCode > 400 ) {
//      return true;
//    } else {
//      return false;
//    }
//
//  }

  @Override
  public boolean onCode(int httpResponseCode) {
//    if( backoff_strategy == null ) {
//      backoff_strategy = new LinearTimeBackOffStrategy(retrySleepMs / 1000, retryMax);
//    }
//    if( httpResponseCode > 400 ) {
//      try {
//        backoff_strategy.backOff();
//        return true;
//      } catch (BackOffException boe) {
//        backoff_strategy.reset();
//        return false;
//      }
//    } else {
//      return false;
//    }
    if( httpResponseCode > 400 ) {
      return true;
    } else {
      return false;
    }
  }
}
