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

/**
 * BackOffStrategy will cause the current thread to sleep for a specific amount of time. This is used to adhere to
 * api rate limits.
 *
 * <p></p>
 * The example below illustrates using a BackOffStrategy to slow down requests when you hit a rate limit exception.
 *
 * <code>
 *     public void pollApi(ApiClient apiClient, BackOffStrategy backOffStrategy) throws BackOffException {
 *          while( apiClient.hasMoreData() ) {
 *              try {
 *                  apiClient.pollData();
 *              } catch (RateLimitException rle) {
 *                  backOffStrategy.backOff();
 *              }
 *          }
 *     }
 * </code>
 *
 */
public interface BackOffStrategy {

  /**
   * Cause the current thread to sleep for an amount of time based on the implemented strategy. If limits are set
   * on the number of times the backOff can be called, an exception will be thrown.
   * @throws BackOffException BackOffException
   */
  public void backOff() throws BackOffException;

  /**
   * Rests the back off strategy to its original state.
   * After the call the strategy will act as if {@link AbstractBackOffStrategy#backOff()}
   * has never been called.
   */
  public void reset();
}
