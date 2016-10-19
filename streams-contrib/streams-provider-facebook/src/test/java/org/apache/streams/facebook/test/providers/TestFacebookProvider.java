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
package org.apache.streams.facebook.test.providers;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.facebook.FacebookConfiguration;
import org.apache.streams.facebook.IdConfig;
import org.apache.streams.facebook.provider.FacebookDataCollector;
import org.apache.streams.facebook.provider.FacebookProvider;
import org.junit.Test;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.*;

/**
 * Unit Tests For {@link org.apache.streams.facebook.provider.FacebookProvider}
 */
public class TestFacebookProvider {

    @Test
    public void testFacebookProvider() throws Exception {
        //Test that streams starts and shut downs.
        final CyclicBarrier barrier = new CyclicBarrier(2);
        FacebookProvider provider = new FacebookProvider(new FacebookConfiguration()) {
            @Override
            protected FacebookDataCollector getDataCollector() {
                return new TestFacebookDataCollector(barrier, super.configuration, super.datums);
            }
        };
        provider.prepare(null);
        provider.startStream();
        assertTrue(provider.isRunning());
        barrier.await();
        assertTrue(provider.isRunning());
        assertEquals(5, provider.readCurrent().size());
        barrier.await();
        assertEquals(0, provider.readCurrent().size());
        assertFalse(provider.isRunning());
        provider.cleanUp();
    }

    private class TestFacebookDataCollector extends FacebookDataCollector {

        private CyclicBarrier barrier;
        private BlockingQueue<StreamsDatum> queue;

        public TestFacebookDataCollector(CyclicBarrier barrier, FacebookConfiguration config, BlockingQueue<StreamsDatum> queue) {
            super(config, queue);
            this.barrier = barrier;
            this.queue = queue;

        }

        @Override
        protected void getData(IdConfig id) throws Exception {

        }

        @Override
        public void run() {
            try {
                for(int i=0; i < 5; ++i) {
                    super.outputData(new Integer(i), ""+i);
                }
                this.barrier.await();
                super.isComplete.set(true);
                this.barrier.await();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (BrokenBarrierException bbe) {
                fail();
            }
        }
    }



}
