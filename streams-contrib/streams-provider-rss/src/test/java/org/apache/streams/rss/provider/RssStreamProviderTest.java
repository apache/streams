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

package org.apache.streams.rss.provider;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.rss.RssStreamConfiguration;
import org.apache.streams.rss.provider.perpetual.RssFeedScheduler;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Assert;

/**
 * Unit tests for {@link org.apache.streams.rss.provider.RssStreamProvider}
 */
public class RssStreamProviderTest extends RandomizedTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(RssStreamProviderTest.class);

  @Test
  public void testRssFeedShutdownsNonPerpetual() throws Exception {
    RssStreamProvider provider = null;
    try {
      final CountDownLatch latch = new CountDownLatch(1);
      BlockingQueue<StreamsDatum> datums = new LinkedBlockingQueue<>();
      provider = new RssStreamProvider(new RssStreamConfiguration()) {
        @Override
        protected RssFeedScheduler getScheduler(BlockingQueue<StreamsDatum> queue) {
          return new MockScheduler(latch, queue);
        }
      };
      provider.prepare(null);
      int datumCount = 0;
      provider.startStream();
      while (!provider.scheduler.isComplete()) {
        StreamsResultSet batch = provider.readCurrent();
        LOGGER.debug("Batch size : {}", batch.size());
        datumCount += batch.size();
        Thread.sleep(randomIntBetween(0, 3000));
      }
      latch.await();

      //one last pull incase of race condition
      StreamsResultSet batch = provider.readCurrent();
      LOGGER.debug("Batch size : {}", batch.size());
      datumCount += batch.size();
      if (batch.size() != 0) {
        //if race condition happened, pull again
        batch = provider.readCurrent();
        Assert.assertEquals(0, batch.size());
      }

      Assert.assertTrue(provider.scheduler.isComplete());
      Assert.assertEquals(20, datumCount);
      Assert.assertFalse(provider.isRunning());
      Assert.assertEquals(0, datums.size());
      provider.cleanUp();
    } finally {
      if (provider != null) {
        provider.cleanUp();
      }
    }
  }


  private class MockScheduler extends RssFeedScheduler {

    private BlockingQueue<StreamsDatum> queue;
    private CountDownLatch latch;
    private volatile boolean complete = false;

    public MockScheduler(CountDownLatch latch, BlockingQueue<StreamsDatum> dataQueue) {
      super(null, null, dataQueue);
      this.latch = latch;
      this.queue = dataQueue;
    }

    @Override
    public void run() {
      try {
        for (int i = 0; i < 20; ++i) {
          this.queue.put(new StreamsDatum(null));
          Thread.sleep(randomIntBetween(0, 5000));
        }
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      } finally {
        this.complete = true;
        this.latch.countDown();
      }
    }


    @Override
    public boolean isComplete() {
      return this.complete;
    }
  }
}


