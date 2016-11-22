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

package org.apache.streams.rss.provider.perpetual;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.rss.FeedDetails;
import org.apache.streams.rss.provider.RssStreamProviderTask;

import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * RssFeedScheduler launches threads to collect data from rss feeds.
 */
public class RssFeedScheduler implements Runnable {

  private static final Logger LOGGER = LoggerFactory.getLogger(RssFeedScheduler.class);
  private static final int DEFAULT_PEROID = 10; // 1 minute

  private ExecutorService service;
  private List<FeedDetails> feedDetailsList;
  private int peroid;
  private AtomicBoolean keepRunning;
  private AtomicBoolean complete;
  private Map<String, Long> lastScheduled;
  private BlockingQueue<StreamsDatum> dataQueue;

  public RssFeedScheduler(ExecutorService service, List<FeedDetails> feedDetailsList, BlockingQueue<StreamsDatum> dataQueue) {
    this(service, feedDetailsList, dataQueue,  DEFAULT_PEROID);
  }

  /**
   * RssFeedScheduler constructor.
   * @param service service
   * @param feedDetailsList feedDetailsList
   * @param dataQueue dataQueue
   * @param peroid peroid
   */
  public RssFeedScheduler(ExecutorService service, List<FeedDetails> feedDetailsList, BlockingQueue<StreamsDatum> dataQueue, int peroid) {
    this.service = service;
    this.feedDetailsList = feedDetailsList;
    this.peroid = peroid;
    this.keepRunning = new AtomicBoolean(true);
    this.lastScheduled = Maps.newHashMap();
    this.dataQueue = dataQueue;
    this.complete = new AtomicBoolean(false);
  }

  public void stop() {
    this.keepRunning.set(false);
  }

  public boolean isComplete() {
    return this.complete.get();
  }

  @Override
  public void run() {
    this.complete.set(false);
    try {
      if (this.peroid <= 0) {
        scheduleFeeds();
      } else {
        while (this.keepRunning.get()) {
          scheduleFeeds();
          Thread.sleep(this.peroid * 60000);
        }
      }
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    } finally {
      this.service = null;
      LOGGER.info("{} completed scheduling of feeds.", this.getClass().getName());
      this.complete.set(true);
    }
  }

  /**
   * Schedule Feeds.
   */
  public void scheduleFeeds() {
    for (FeedDetails detail : this.feedDetailsList) {
      Long lastTime = null;
      if ((lastTime = this.lastScheduled.get(detail.getUrl())) == null) {
        lastTime = 0L;
      }
      long currentTime = System.currentTimeMillis();
      long pollInterval;
      if (detail.getPollIntervalMillis() == null) {
        pollInterval = 0;
      } else {
        pollInterval = detail.getPollIntervalMillis();
      }
      if (currentTime - lastTime > pollInterval) {
        this.service.execute(new RssStreamProviderTask(this.dataQueue, detail.getUrl()));
        this.LOGGER.trace("Scheduled data collection on rss feed, {}", detail.getUrl());
        this.lastScheduled.put(detail.getUrl(), currentTime);
      }
    }
  }
}
