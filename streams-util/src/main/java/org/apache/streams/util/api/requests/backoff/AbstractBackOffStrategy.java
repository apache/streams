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

package org.apache.streams.util.api.requests.backoff;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @see org.apache.streams.util.api.requests.backoff.BackOffStrategy
 */
public abstract class AbstractBackOffStrategy implements BackOffStrategy {

  private long baseSleepTime;
  private long lastSleepTime;
  private int maxAttempts;
  private AtomicInteger attemptsCount;

  /**
   * A BackOffStrategy that can effectively be used endlessly.
   * @param baseBackOffTime amount of time back of in seconds
   */
  public AbstractBackOffStrategy(long baseBackOffTime) {
    this(baseBackOffTime, -1);
  }

  /**
   * A BackOffStrategy that has a limited number of uses before it throws a
   * {@link org.apache.streams.util.api.requests.backoff.BackOffException}.
   * @param baseBackOffTime time to back off in milliseconds, must be greater than 0.
   * @param maximumNumberOfBackOffAttempts maximum number of attempts, must be grater than 0 or -1.
   *                                       -1 indicates there is no maximum number of attempts.
   */
  public AbstractBackOffStrategy(long baseBackOffTime, int maximumNumberOfBackOffAttempts) {
    if (baseBackOffTime <= 0) {
      throw new IllegalArgumentException("backOffTimeInMilliSeconds is not greater than 0 : " + baseBackOffTime);
    }
    if (maximumNumberOfBackOffAttempts <= 0 && maximumNumberOfBackOffAttempts != -1) {
      throw new IllegalArgumentException("maximumNumberOfBackOffAttempts is not greater than 0 : " + maximumNumberOfBackOffAttempts);
    }
    this.baseSleepTime = baseBackOffTime;
    this.maxAttempts = maximumNumberOfBackOffAttempts;
    this.attemptsCount = new AtomicInteger(0);
  }

  @Override
  public void backOff() throws BackOffException {
    int attempt = this.attemptsCount.getAndIncrement();
    if (attempt >= this.maxAttempts && this.maxAttempts != -1) {
      throw new BackOffException(attempt, this.lastSleepTime);
    } else {
      try {
        Thread.sleep(this.lastSleepTime = calculateBackOffTime(attempt, this.baseSleepTime));
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
    }
  }

  @Override
  public void reset() {
    this.attemptsCount.set(0);
  }

  /**
   * Calculate the amount of time in milliseconds that the strategy should back off for
   * @param attemptCount the number of attempts the strategy has backed off.
   *                     i.e. 1 -> this is the first attempt, 2 -> this is the second attempt, etc.
   * @param baseSleepTime the minimum amount of time it should back off for in milliseconds
   * @return the amount of time it should back off in milliseconds
   */
  protected abstract long calculateBackOffTime(int attemptCount, long baseSleepTime);

}
