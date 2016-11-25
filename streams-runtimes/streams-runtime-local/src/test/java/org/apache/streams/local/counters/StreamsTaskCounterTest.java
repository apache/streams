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
package org.apache.streams.local.counters;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Test;

import java.lang.management.ManagementFactory;
import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;

/**
 * Unit tests for {@link org.apache.streams.local.counters.StreamsTaskCounter}
 */
public class StreamsTaskCounterTest extends RandomizedTest {

  private static final String MBEAN_ID = "test_id";
  private static final String STREAM_ID = "test_stream";
  private static long STREAM_START_TIME = (new DateTime()).getMillis();

  /**
   * Remove registered mbeans from previous tests
   * @throws Exception
   */
  @After
  public void unregisterMXBean() throws Exception {
    try {
      ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(String.format(StreamsTaskCounter.NAME_TEMPLATE, MBEAN_ID, STREAM_ID, STREAM_START_TIME)));
    } catch (InstanceNotFoundException ife) {
      //No-op
    }
  }

  /**
   * Test constructor does not throw errors
   */
  @Test
  public void testConstructor() {
    try {
      new StreamsTaskCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
    } catch (Throwable t) {
      fail("Constructor threw error : "+t.getMessage());
    }
  }

  /**
   * Test emitted increments correctly and returns expected value
   * @throws Exception
   */
  @Test
  @Repeat(iterations = 3)
  public void testEmitted() throws Exception {
    StreamsTaskCounter counter = new StreamsTaskCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
    int numIncrements = randomIntBetween(1, 100000);
    for(int i=0; i < numIncrements; ++i) {
      counter.incrementEmittedCount();
    }
    assertEquals(numIncrements, counter.getNumEmitted());

    unregisterMXBean();

    counter = new StreamsTaskCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
    numIncrements = randomIntBetween(1, 100000);
    long total = 0;
    for(int i=0; i < numIncrements; ++i) {
      long delta = randomIntBetween(1, 100);
      total += delta;
      counter.incrementEmittedCount(delta);
    }
    assertEquals(total, counter.getNumEmitted());
  }

  /**
   * Test received increments correctly and returns expected value
   * @throws Exception
   */
  @Test
  @Repeat(iterations = 3)
  public void testReceived() throws Exception {
    StreamsTaskCounter counter = new StreamsTaskCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
    int numIncrements = randomIntBetween(1, 100000);
    for(int i=0; i < numIncrements; ++i) {
      counter.incrementReceivedCount();
    }
    assertEquals(numIncrements, counter.getNumReceived());

    unregisterMXBean();

    counter = new StreamsTaskCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
    numIncrements = randomIntBetween(1, 100000);
    long total = 0;
    for(int i=0; i < numIncrements; ++i) {
      long delta = randomIntBetween(1, 100);
      total += delta;
      counter.incrementReceivedCount(delta);
    }
    assertEquals(total, counter.getNumReceived());
  }

  /**
   * Test errors increments correctly and returns expected value
   * @throws Exception
   */
  @Test
  @Repeat(iterations = 3)
  public void testError() throws Exception {
    StreamsTaskCounter counter = new StreamsTaskCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
    int numIncrements = randomIntBetween(1, 100000);
    for(int i=0; i < numIncrements; ++i) {
      counter.incrementErrorCount();
    }
    assertEquals(numIncrements, counter.getNumUnhandledErrors());

    unregisterMXBean();

    counter = new StreamsTaskCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
    numIncrements = randomIntBetween(1, 100000);
    long total = 0;
    for(int i=0; i < numIncrements; ++i) {
      long delta = randomIntBetween(1, 100);
      total += delta;
      counter.incrementErrorCount(delta);
    }
    assertEquals(total, counter.getNumUnhandledErrors());
  }

  /**
   * Test error rate returns expected value
   * @throws Exception
   */
  @Test
  @Repeat(iterations = 3)
  public void testErrorRate() throws Exception {
    StreamsTaskCounter counter = new StreamsTaskCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
    assertEquals(0.0, counter.getErrorRate(), 0);
    int failures = randomIntBetween(0, 100000);
    int received = randomIntBetween(0, 100000);
    counter.incrementReceivedCount(received);
    counter.incrementErrorCount(failures);
    assertEquals((double)failures / (double)(received), counter.getErrorRate(), 0);
  }

}
