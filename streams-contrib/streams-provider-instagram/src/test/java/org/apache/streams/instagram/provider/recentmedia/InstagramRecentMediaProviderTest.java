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
package org.apache.streams.instagram.provider.recentmedia;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.instagram.InstagramConfiguration;
import org.apache.streams.instagram.User;
import org.apache.streams.instagram.UsersInfo;
import org.apache.streams.instagram.provider.InstagramDataCollector;
import org.jinstagram.entity.users.feed.MediaFeedData;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 *
 */
public class InstagramRecentMediaProviderTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(InstagramRecentMediaProviderTest.class);

    @Test
    public void testStartStream() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final InstagramRecentMediaCollector collectorStub = new InstagramRecentMediaCollector(new ConcurrentLinkedQueue<StreamsDatum>(), createNonNullConfiguration()) {

            private volatile boolean isFinished = false;

            @Override
            public void run() {
                this.isFinished = true;
                latch.countDown();
            }

            @Override
            public boolean isCompleted() {
                return this.isFinished;
            }
        };

        InstagramRecentMediaProvider provider = new InstagramRecentMediaProvider(null) {
            @Override
            protected InstagramDataCollector getInstagramDataCollector() {
                return collectorStub;
            }
        };
        provider.prepare(null);
        provider.startStream();

        latch.await();
        assertTrue(collectorStub.isCompleted());
        StreamsResultSet result = provider.readCurrent();
        assertNotNull(result);
        assertEquals(0, result.size());
        assertTrue(!provider.isRunning());
        try {
            provider.cleanUp();
        } catch (Throwable throwable){
            throwable.printStackTrace();
            fail("Error durring clean up");
        }
    }

    @Test
    public void testReadCurrent() {
        final long seed = System.nanoTime();
        final Random rand = new Random(seed);
        final CyclicBarrier test = new CyclicBarrier(2);
        final CyclicBarrier produce = new CyclicBarrier(2);
        final AtomicInteger batchCount = new AtomicInteger(0);
        final InstagramRecentMediaProvider provider = new InstagramRecentMediaProvider(createNonNullConfiguration()) {
            @Override
            protected InstagramDataCollector getInstagramDataCollector() {
                return new InstagramRecentMediaCollector(this.dataQueue, createNonNullConfiguration()) {

                    private volatile boolean isFinished = false;



                    public int getBatchCount() {
                        return batchCount.get();
                    }

                    @Override
                    public boolean isCompleted() {
                        return isFinished;
                    }

                    @Override
                    public void run() {
                        int randInt = rand.nextInt(5);
                        while(randInt != 0) {
                            int batchSize = rand.nextInt(200);
                            for(int i=0; i < batchSize; ++i) {
                                while(!super.dataQueue.add(new StreamsDatum(null))) {
                                    Thread.yield();
                                }
                            }
                            batchCount.set(batchSize);
                            try {
                                test.await();
                                produce.await();
                            } catch (InterruptedException ie ) {
                                Thread.currentThread().interrupt();
                            } catch (BrokenBarrierException bbe) {
                                Thread.currentThread().interrupt();
                            }
                            randInt = rand.nextInt(5);
                        }
                        batchCount.set(0);
                        isFinished = true;
                        try {
                            test.await();
                            produce.await();
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                        } catch (BrokenBarrierException bbe) {
                            Thread.currentThread().interrupt();
                        }
                    }

                };
            }
        };
        provider.prepare(null);
        provider.startStream();
        while(provider.isRunning()) {
            try {
                test.await();
                assertEquals("Seed == "+seed, batchCount.get(), provider.readCurrent().size());
                produce.await();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException bbe) {
                Thread.currentThread().interrupt();
            }

        }
    }

    private InstagramConfiguration createNonNullConfiguration() {
        InstagramConfiguration configuration = new InstagramConfiguration();
        UsersInfo info = new UsersInfo();
        configuration.setUsersInfo(info);
        info.setUsers(new HashSet<User>());
        info.setAuthorizedTokens(new HashSet<String>());
        return configuration;
    }

}
