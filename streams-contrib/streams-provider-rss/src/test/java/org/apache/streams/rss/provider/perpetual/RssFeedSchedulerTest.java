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

import org.apache.streams.rss.FeedDetails;
import org.apache.streams.rss.provider.RssStreamProviderTask;

import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for {@link org.apache.streams.rss.provider.perpetual.RssFeedScheduler}
 */
public class RssFeedSchedulerTest {


  /**
   * Test that feeds are scheduled based on elapsed time correctly.
   * Takes 1 minute to run.
   */
  @Test
  public void testScheduleFeeds() {
    ExecutorService mockService = mock(ExecutorService.class);
    final List<String> queuedTasks = new ArrayList<>(5);
    doAnswer(invocationOnMock -> {
      queuedTasks.add(((RssStreamProviderTask) invocationOnMock.getArguments()[0]).getRssFeed());
      return null;
    }).when(mockService).execute(any(Runnable.class));

    RssFeedScheduler scheduler = new RssFeedScheduler(mockService, createFeedList(), new LinkedBlockingQueue<>(), 1);
    scheduler.scheduleFeeds();
    assertEquals("Expected 2 Feeds to be scheduled", 2, queuedTasks.size());
    assertEquals("Expected Feed 1 to be queued first",  "1", queuedTasks.get(0));
    assertEquals("Expected Feed 2 to be queued second", "2", queuedTasks.get(1));

    safeSleep(1);
    scheduler.scheduleFeeds();
    assertEquals("Only feed 1 should have been re-queued", 3, queuedTasks.size());
    assertEquals("Only feed 1 should have been re-queued", "1", queuedTasks.get(2));

    safeSleep(60 * 1000);
    scheduler.scheduleFeeds();
    assertEquals("Both feeds should have been re-queued", 5, queuedTasks.size());
    assertEquals("1", queuedTasks.get(3));
    assertEquals("2", queuedTasks.get(4));
  }

  private List<FeedDetails> createFeedList() {
    List<FeedDetails> list = new LinkedList<>();
    FeedDetails fd = new FeedDetails();
    fd.setPollIntervalMillis(1L);
    fd.setUrl("1");
    list.add(fd);

    fd = new FeedDetails();
    fd.setPollIntervalMillis( 60L * 1000);
    fd.setUrl("2");
    list.add(fd);
    return list;
  }

  private void safeSleep(long milliseconds) {
    try {
      Thread.sleep(milliseconds);
    } catch (InterruptedException ie) {
      Thread.currentThread().interrupt();
    }
  }

}
