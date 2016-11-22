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

package org.apache.streams.moreover;

import org.apache.streams.core.StreamsDatum;

import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;

/**
 * Task that pulls from the Morever API on behalf of MoreoverProvider.
 */
public class MoreoverProviderTask implements Runnable {

  public static final int LATENCY = 10;
  public static final int REQUIRED_LATENCY = LATENCY * 1000;
  private static Logger logger = LoggerFactory.getLogger(MoreoverProviderTask.class);

  private String lastSequence;
  private final String apiKey;
  private final String apiId;
  private final Queue<StreamsDatum> results;
  private final MoreoverClient moClient;
  private boolean started = false;

  /**
   * MoreoverProviderTask constructor.
   * @param apiId apiId
   * @param apiKey apiKey
   * @param results results
   * @param lastSequence lastSequence
   */
  public MoreoverProviderTask(String apiId, String apiKey, Queue<StreamsDatum> results, String lastSequence) {
    //logger.info("Constructed new task {} for {} {} {}", UUID.randomUUID().toString(), apiId, apiKey, lastSequence);
    this.apiId = apiId;
    this.apiKey = apiKey;
    this.results = results;
    this.lastSequence = lastSequence;
    this.moClient = new MoreoverClient(this.apiId, this.apiKey, this.lastSequence);
    initializeClient(moClient);
  }

  @Override
  public void run() {
    while (true) {
      try {
        ensureTime(moClient);
        MoreoverResult result = moClient.getArticlesAfter(lastSequence, 500);
        started = true;
        lastSequence = result.process().toString();
        for (StreamsDatum entry : ImmutableSet.copyOf(result.iterator())) {
          results.offer(entry);
        }
        logger.info("ApiKey={}\tlastSequenceid={}", this.apiKey, lastSequence);

      } catch (Exception ex) {
        logger.error("Exception while polling moreover", ex);
      }
    }
  }

  private void ensureTime(MoreoverClient moClient) {
    try {
      long gap = System.currentTimeMillis() - moClient.pullTime;
      if (gap < REQUIRED_LATENCY) {
        Thread.sleep(REQUIRED_LATENCY - gap);
      }
    } catch (Exception ex) {
      logger.warn("Error sleeping for latency");
    }
  }

  private void initializeClient(MoreoverClient moClient) {
    try {
      moClient.getArticlesAfter(this.lastSequence, 2);
    } catch (Exception ex) {
      logger.error("Failed to start stream, {}", this.apiKey);
      logger.error("Exception : ", ex);
      throw new IllegalStateException("Unable to initialize stream", ex);
    }
  }
}
