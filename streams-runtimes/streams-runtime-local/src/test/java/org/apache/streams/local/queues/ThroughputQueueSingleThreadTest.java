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
package org.apache.streams.local.queues;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import org.apache.streams.util.ComponentUtils;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Test;

import javax.management.MBeanServer;
import javax.management.ObjectInstance;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 * Single thread unit tests for {@link org.apache.streams.local.queues.ThroughputQueue}
 */
public class ThroughputQueueSingleThreadTest extends RandomizedTest {
    private static final String MBEAN_ID = "test_id";
    private static final String STREAM_ID = "test_stream";
    private static long STREAM_START_TIME = (new DateTime()).getMillis();

    @After
    public void removeLocalMBeans() {
        try {
            ComponentUtils.removeAllMBeansOfDomain("org.apache.streams.local");
        } catch (Exception e) {
            //No op.  proceed to next test
        }
    }

    /**
     * Test that take and put queue and dequeue data as expected and all
     * measurements form the queue are returning data.
     * @throws Exception
     */
    @Test
    @Repeat(iterations = 3)
    public void testTakeAndPut() throws Exception {
        ThroughputQueue<Integer> queue = new ThroughputQueue<>();
        int putCount = randomIntBetween(1, 1000);
        for(int i=0; i < putCount; ++i) {
            queue.put(i);
            assertEquals(i+1, queue.size());
            assertEquals(queue.size(), queue.getCurrentSize());
        }
        safeSleep(100); //ensure measurable wait time
        int takeCount = randomIntBetween(1, putCount);
        for(int i=0; i < takeCount; ++i) {
            Integer element = queue.take();
            assertNotNull(element);
            assertEquals(i, element.intValue());
            assertEquals(putCount - (1+i), queue.size());
            assertEquals(queue.size(), queue.getCurrentSize());
        }
        assertEquals(putCount-takeCount, queue.size());
        assertEquals(queue.size(), queue.getCurrentSize());
        assertTrue(0.0 < queue.getMaxWait());
        assertTrue(0.0 < queue.getAvgWait());
        assertTrue(0.0 < queue.getThroughput());
        assertEquals(putCount, queue.getAdded());
        assertEquals(takeCount, queue.getRemoved());
    }

    /**
     * Test that add and remove queue and dequeue data as expected
     * and all measurements from the queue are returning data
     */
    @Test
    @Repeat(iterations = 3)
    public void testAddAndRemove() {
        ThroughputQueue<Integer> queue = new ThroughputQueue<>();
        int putCount = randomIntBetween(1, 1000);
        for(int i=0; i < putCount; ++i) {
            queue.add(i);
            assertEquals(i+1, queue.size());
            assertEquals(queue.size(), queue.getCurrentSize());
        }
        safeSleep(100); //ensure measurable wait time
        int takeCount = randomIntBetween(1, putCount);
        for(int i=0; i < takeCount; ++i) {
            Integer element = queue.remove();
            assertNotNull(element);
            assertEquals(i, element.intValue());
            assertEquals(putCount - (1+i), queue.size());
            assertEquals(queue.size(), queue.getCurrentSize());
        }
        assertEquals(putCount-takeCount, queue.size());
        assertEquals(queue.size(), queue.getCurrentSize());
        assertTrue(0.0 < queue.getMaxWait());
        assertTrue(0.0 < queue.getAvgWait());
        assertTrue(0.0 < queue.getThroughput());
        assertEquals(putCount, queue.getAdded());
        assertEquals(takeCount, queue.getRemoved());
    }

    /**
     * Test that offer and poll queue and dequeue data as expected
     * and all measurements from the queue are returning data
     */
    @Test
    @Repeat(iterations = 3)
    public void testOfferAndPoll() {
        ThroughputQueue<Integer> queue = new ThroughputQueue<>();
        int putCount = randomIntBetween(1, 1000);
        for(int i=0; i < putCount; ++i) {
            queue.offer(i);
            assertEquals(i+1, queue.size());
            assertEquals(queue.size(), queue.getCurrentSize());
        }
        safeSleep(100); //ensure measurable wait time
        int takeCount = randomIntBetween(1, putCount);
        for(int i=0; i < takeCount; ++i) {
            Integer element = queue.poll();
            assertNotNull(element);
            assertEquals(i, element.intValue());
            assertEquals(putCount - (1+i), queue.size());
            assertEquals(queue.size(), queue.getCurrentSize());
        }
        assertEquals(putCount-takeCount, queue.size());
        assertEquals(queue.size(), queue.getCurrentSize());
        assertTrue(0.0 < queue.getMaxWait());
        assertTrue(0.0 < queue.getAvgWait());
        assertTrue(0.0 < queue.getThroughput());
        assertEquals(putCount, queue.getAdded());
        assertEquals(takeCount, queue.getRemoved());
    }



    /**
     * Test that max wait and avg wait return expected values
     * @throws Exception
     */
    @Test
    public void testWait() throws Exception {
        ThroughputQueue queue = new ThroughputQueue();
        int wait = 1000;

        for(int i=0; i < 3; ++i) {
            queue.put(1);
            safeSleep(wait);
            queue.take();
            assertTrue(queue.getMaxWait() >= wait && queue.getMaxWait() <= (wait * 2));//can't calculate exactly, making sure its close.
            assertTrue(queue.getAvgWait() >= wait && queue.getAvgWait() <= (wait * 2));
        }
        queue.put(1);
        queue.take();
        assertTrue(queue.getMaxWait() >= wait && queue.getMaxWait() <= (wait * 2));//can't calculate exactly, making sure its close.
        assertTrue(queue.getAvgWait() <= 5000 );
        assertTrue(queue.getAvgWait() >= 500);
    }

    /**
     * Test that throughput returns expected values.
     * @throws Exception
     */
    @Test
    public void testThroughput() throws Exception {
        ThroughputQueue queue = new ThroughputQueue();
        int wait = 100;
        for(int i=0; i < 10; ++i) {
            queue.put(1);
            safeSleep(wait);
            queue.take();
        }
        double throughput = queue.getThroughput();
        assertTrue(throughput <= 15 ); //can't calculate exactly, making sure its close.
        assertTrue(throughput >= 5);

        queue = new ThroughputQueue();
        wait = 1000;
        for(int i=0; i < 10; ++i) {
            queue.put(1);
        }
        for(int i=0; i < 10; ++i) {
            queue.take();
        }
        safeSleep(wait);
        throughput = queue.getThroughput();
        assertTrue(throughput <= 15 ); //can't calculate exactly, making sure its close.
        assertTrue(throughput >= 5);
    }


    /**
     * Test that the mbean registers
     */
    @Test
    public void testMBeanRegistration() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Integer beanCount = mbs.getMBeanCount();
            ThroughputQueue queue = new ThroughputQueue(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
            assertEquals("Expected bean to be registered", new Integer(beanCount+1), mbs.getMBeanCount());
            ObjectInstance mBean = mbs.getObjectInstance(new ObjectName(String.format(ThroughputQueue.NAME_TEMPLATE, MBEAN_ID, STREAM_ID, STREAM_START_TIME)));
            assertNotNull(mBean);
        } catch (Exception e) {
            fail("Failed to register MXBean : "+e.getMessage());
        }
    }

    /**
     * Test that mulitple mbeans of the same type with a different name can be registered
     */
    @Test
    public void testMultipleMBeanRegistrations() {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            Integer beanCount = mbs.getMBeanCount();
            int numReg = randomIntBetween(2, 100);
            for(int i=0; i < numReg; ++i) {
                ThroughputQueue queue = new ThroughputQueue(MBEAN_ID + "" + i, STREAM_ID, STREAM_START_TIME);
                assertEquals("Expected bean to be registered", new Integer(beanCount + (i+1)), mbs.getMBeanCount());
                ObjectInstance mBean = mbs.getObjectInstance(new ObjectName(String.format(ThroughputQueue.NAME_TEMPLATE, MBEAN_ID + "" + i, STREAM_ID, STREAM_START_TIME)));
                assertNotNull(mBean);
            }
        } catch (Exception e) {
            fail("Failed to register MXBean : "+e.getMessage());
        }
    }


    private void safeSleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }




}
