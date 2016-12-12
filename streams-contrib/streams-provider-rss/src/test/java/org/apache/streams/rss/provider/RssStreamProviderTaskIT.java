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

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Unit tests for {@link org.apache.streams.rss.provider.RssStreamProviderTask}
 */
public class RssStreamProviderTaskIT {

  /**
   * Test that a task can read a valid rss from a url and queue the data.
   * @throws Exception Exception
   */
  @Test
  public void testNonPerpetualNoTimeFramePull() throws Exception {
    com.healthmarketscience.common.util.resource.Handler.init();
    BlockingQueue<StreamsDatum> queue = new LinkedBlockingQueue<>();
    RssStreamProviderTask task = new RssStreamProviderTask(queue, "fake url");
    Set<String> batch = task.queueFeedEntries(new URL("resource:///test_rss_xml/economist1.xml"));
    assertEquals(batch.size(), queue.size(), "Expected batch size to be the same as amount of queued datums");
    task.PREVIOUSLY_SEEN.put("fake url", batch);
    //Test that  it will out previously seen articles
    queue.clear();
    batch = task.queueFeedEntries(new URL("resource:///test_rss_xml/economist1.xml"));
    assertEquals(batch.size(), queue.size(), "Expected batch size to be the same as amount of queued datums");
  }

  /**
   * Test that perpetual streams will not output previously seen articles.
   * @throws Exception Exception
   */
  @Test
  public void testPerpetualNoTimeFramePull() throws Exception {
    com.healthmarketscience.common.util.resource.Handler.init();
    BlockingQueue<StreamsDatum> queue = new LinkedBlockingQueue<>();
    RssStreamProviderTask task = new RssStreamProviderTask(queue, "fake url", new DateTime().minusYears(10), 10000, true);
    Set<String> batch = task.queueFeedEntries(new URL("resource:///test_rss_xml/economist1.xml"));
    assertEquals(batch.size(), queue.size(), "Expected batch size to be the same as amount of queued datums");
    task.PREVIOUSLY_SEEN.put("fake url", batch);
    //Test that it will not out previously seen articles
    queue.clear();
    batch = task.queueFeedEntries(new URL("resource:///test_rss_xml/economist1.xml"));
    assertEquals( queue.size(), 0 );
    assertEquals( batch.size(), 20 );
    task.PREVIOUSLY_SEEN.put("fake url", batch);
    //Test that not seen urls aren't blocked.
    queue.clear();
    batch = task.queueFeedEntries(new URL("resource:///test_rss_xml/economist2.xml"));
    assertEquals(batch.size(), queue.size());
    assertEquals( queue.size(), 25);
    assertEquals( batch.size(), 25);
  }

  /**
   * Test that you can task will only output aritcles after a certain published time.
   * @throws Exception Exception
   */
  @Test
  public void testNonPerpetualTimeFramedPull() throws Exception {
    com.healthmarketscience.common.util.resource.Handler.init();
    BlockingQueue<StreamsDatum> queue = new LinkedBlockingQueue<>();
    DateTime publishedSince = new DateTime().withYear(2014).withDayOfMonth(5).withMonthOfYear(9).withZone(DateTimeZone.UTC);
    RssStreamProviderTask task = new RssStreamProviderTask(queue, "fake url", publishedSince, 10000, false);
    Set<String> batch = task.queueFeedEntries(new URL("resource:///test_rss_xml/economist1.xml"));
    assertEquals( queue.size(), 15);
    assertEquals( batch.size(), 20);
    assertTrue( queue.size() < batch.size());
    task.PREVIOUSLY_SEEN.put("fake url", batch);
    //Test that  it will out previously seen articles
    queue.clear();
    batch = task.queueFeedEntries(new URL("resource:///test_rss_xml/economist1.xml"));
    assertEquals( queue.size(), 15);
    assertEquals( batch.size(), 20);
    assertTrue( queue.size() < batch.size());
  }

  /**
   * Test that task will only output articles after a certain published time that it has not seen before.
   * @throws Exception Exception
   */
  @Test
  public void testPerpetualTimeFramedPull() throws Exception {
    com.healthmarketscience.common.util.resource.Handler.init();
    BlockingQueue<StreamsDatum> queue = new LinkedBlockingQueue<>();
    DateTime publishedSince = new DateTime().withYear(2014).withDayOfMonth(5).withMonthOfYear(9).withZone(DateTimeZone.UTC);
    RssStreamProviderTask task = new RssStreamProviderTask(queue, "fake url", publishedSince, 10000, true);
    Set<String> batch = task.queueFeedEntries(new URL("resource:///test_rss_xml/economist1.xml"));
    assertEquals( queue.size(),15);
    assertEquals( batch.size(), 20);
    assertTrue( queue.size() < batch.size());
    task.PREVIOUSLY_SEEN.put("fake url", batch);
    //Test that  it will not out put previously seen articles
    queue.clear();
    batch = task.queueFeedEntries(new URL("resource:///test_rss_xml/economist1.xml"));
    assertEquals( queue.size(), 0);
    assertEquals( batch.size(), 20);
    assertTrue( queue.size() < batch.size());
    task.PREVIOUSLY_SEEN.put("fake url", batch);

    batch = task.queueFeedEntries(new URL("resource:///test_rss_xml/economist2.xml"));
    assertTrue( queue.size() < batch.size());
    assertEquals(queue.size(), 3);
    assertEquals(batch.size(), 25);
  }




}
