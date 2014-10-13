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
import org.junit.After;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.concurrent.*;

/**
 * MultiThread unit tests for {@link org.apache.streams.local.queues.ThroughputQueue}
 */
public class ThroughputQueueMulitThreadTest extends RandomizedTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(ThroughputQueueMulitThreadTest.class);
    private static final String MBEAN_ID = "testQueue";

    /**
     * Remove registered mbeans from previous tests
     * @throws Exception
     */
    @After
    public void unregisterMXBean() throws Exception {
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(String.format(ThroughputQueue.NAME_TEMPLATE, MBEAN_ID)));
        } catch (InstanceNotFoundException ife) {
            //No-op
        }
    }


    /**
     * Test that queue will block on puts when the queue is full
     * @throws InterruptedException
     */
    @Test
    public void testBlockOnFullQueue() throws InterruptedException {
        int queueSize = randomIntBetween(1, 3000);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch full = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        ThroughputQueue queue = new ThroughputQueue(queueSize);
        BlocksOnFullQueue testThread = new BlocksOnFullQueue(full, finished, queue, queueSize);
        executor.submit(testThread);
        full.await();
        assertEquals(queueSize, queue.size());
        assertEquals(queueSize, queue.getCurrentSize());
        assertFalse(testThread.isComplete()); //test that it is blocked
        safeSleep(1000);
        assertFalse(testThread.isComplete()); //still blocked
        queue.take();
        finished.await();
        assertEquals(queueSize, queue.size());
        assertEquals(queueSize, queue.getCurrentSize());
        assertTrue(testThread.isComplete());
        executor.shutdownNow();
        executor.awaitTermination(500, TimeUnit.MILLISECONDS);
    }

    /**
     * Test that queue will block on Take when queue is empty
     * @throws InterruptedException
     */
    @Test
    public void testBlockOnEmptyQueue() throws InterruptedException {
        int queueSize = randomIntBetween(1, 3000);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch empty = new CountDownLatch(1);
        CountDownLatch finished = new CountDownLatch(1);
        ThroughputQueue queue = new ThroughputQueue();
        BlocksOnEmptyQueue testThread = new BlocksOnEmptyQueue(empty, finished, queueSize, queue);
        for(int i=0; i < queueSize; ++i) {
            queue.put(i);
        }
        executor.submit(testThread);
        empty.await();
        assertEquals(0, queue.size());
        assertEquals(0, queue.getCurrentSize());
        assertFalse(testThread.isComplete());
        safeSleep(1000);
        assertFalse(testThread.isComplete());
        queue.put(1);
        finished.await();
        assertEquals(0, queue.size());
        assertEquals(0, queue.getCurrentSize());
        assertTrue(testThread.isComplete());
        executor.shutdownNow();
        executor.awaitTermination(500, TimeUnit.MILLISECONDS);
    }


    /**
     * Test multiple threads putting and taking from the queue while
     * this thread repeatedly calls the MXBean measurement methods.
     * Should hammer the queue with request from multiple threads
     * of all request types.  Purpose is to expose current modification exceptions
     * and/or dead locks.
     */
    @Test
    @Repeat(iterations = 3)
    public void testMultiThreadAccessAndInteruptResponse() throws Exception {
        int putTakeThreadCount = randomIntBetween(1, 10);
        int dataCount = randomIntBetween(1, 2000000);
        int pollCount = randomIntBetween(1, 2000000);
        int maxSize = randomIntBetween(1, 1000);
        CountDownLatch finished = new CountDownLatch(putTakeThreadCount);
        ThroughputQueue queue = new ThroughputQueue(maxSize, MBEAN_ID);
        ExecutorService executor = Executors.newFixedThreadPool(putTakeThreadCount * 2);
        for(int i=0; i < putTakeThreadCount; ++i) {
            executor.submit(new PutData(finished, queue, dataCount));
            executor.submit(new TakeData(queue));
        }
        for(int i=0; i < pollCount; ++i) {
            queue.getAvgWait();
            queue.getAdded();
            queue.getCurrentSize();
            queue.getMaxWait();
            queue.getRemoved();
            queue.getThroughput();
        }
        finished.await();
        while(!queue.isEmpty()) {
            LOGGER.info("Waiting for queue to be emptied...");
            safeSleep(500);
        }
        long totalData = ((long) dataCount) * putTakeThreadCount;
        assertEquals(totalData, queue.getAdded());
        assertEquals(totalData, queue.getRemoved());
        executor.shutdown();
        executor.awaitTermination(1000, TimeUnit.MILLISECONDS); //shutdown puts
        executor.shutdownNow();
        executor.awaitTermination(1000, TimeUnit.MILLISECONDS); //shutdown takes
        //Randomized should not report thread leak
    }



    private void safeSleep(long sleep) {
        try {
            Thread.sleep(sleep);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }




    /**
     * Helper runnable for test {@link ThroughputQueueMulitThreadTest#testBlockOnFullQueue()}
     */
    private class BlocksOnFullQueue implements Runnable {

        private CountDownLatch full;
        volatile private boolean complete;
        private int queueSize;
        private CountDownLatch finished;
        private BlockingQueue queue;

        public BlocksOnFullQueue(CountDownLatch latch, CountDownLatch finished, BlockingQueue queue, int queueSize) {
            this.full = latch;
            this.complete = false;
            this.queueSize = queueSize;
            this.finished = finished;
            this.queue = queue;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < this.queueSize; ++i) {
                    this.queue.put(i);
                }
                this.full.countDown();
                this.queue.put(0);
                this.complete = true;
                this.finished.countDown();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        public boolean isComplete() {
            return this.complete;
        }
    }


    /**
     * Helper runnable class for test {@link ThroughputQueueMulitThreadTest#testBlockOnEmptyQueue()}
     */
    private class BlocksOnEmptyQueue implements Runnable {

        private CountDownLatch full;
        volatile private boolean complete;
        private int queueSize;
        private CountDownLatch finished;
        private BlockingQueue queue;

        public BlocksOnEmptyQueue(CountDownLatch full, CountDownLatch finished, int queueSize, BlockingQueue queue) {
            this.full = full;
            this.finished = finished;
            this.queueSize = queueSize;
            this.queue = queue;
            this.complete = false;
        }


        @Override
        public void run() {
            try {
                for(int i=0; i < this.queueSize; ++i) {
                    this.queue.take();
                }
                this.full.countDown();
                this.queue.take();
                this.complete = true;
                this.finished.countDown();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }

        public boolean isComplete() {
            return this.complete;
        }
    }


    private class PutData implements Runnable {

        private BlockingQueue queue;
        private int dataCount;
        private CountDownLatch finished;

        public PutData(CountDownLatch finished, BlockingQueue queue, int dataCount) {
            this.queue = queue;
            this.dataCount = dataCount;
            this.finished = finished;
        }


        @Override
        public void run() {
            try {
                for(int i=0; i < this.dataCount; ++i) {
                    this.queue.put(i);
                }
            } catch (InterruptedException ie) {
                LOGGER.error("PUT DATA interupted !");
                Thread.currentThread().interrupt();
            }
            this.finished.countDown();
        }
    }


    private class TakeData implements Runnable {

        private BlockingQueue queue;

        public TakeData(BlockingQueue queue) {
            this.queue = queue;
        }


        @Override
        public void run() {
            try {
                while(true) {
                    this.queue.take();
                }
            } catch (InterruptedException ie) {
                LOGGER.error("PUT DATA interupted !");
                Thread.currentThread().interrupt();
            }
        }
    }

}
