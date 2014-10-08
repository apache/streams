package org.apache.streams.rss.provider;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.rss.FeedDetails;
import org.apache.streams.rss.RssStreamConfiguration;
import org.apache.streams.rss.provider.perpetual.RssFeedScheduler;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;

/**
 * Created by rebanks on 9/25/14.
 */
public class RssStreamProviderTest extends RandomizedTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(RssStreamProviderTest.class);

    @Test
    public void testRssFeedShutdownsNonPerpetual() throws Exception {
        RssStreamProvider provider = null;
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            BlockingQueue<StreamsDatum> datums = Queues.newLinkedBlockingQueue();
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

            assertTrue(provider.scheduler.isComplete());
            assertFalse(provider.isRunning());
            assertEquals(0, datums.size());
            assertEquals(20, datumCount);
            provider.cleanUp();
        } finally {
            if(provider != null)
                provider.cleanUp();
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


