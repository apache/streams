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
package org.apache.streams.data.moreover;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.moreover.MoreoverConfiguration;
import org.apache.streams.moreover.MoreoverKeyData;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Basic Unit tests for {@link org.apache.streams.data.moreover.MoreoverProvider}
 */
public class TestMoreoverProvider extends RandomizedTest {

    /**
     * Basic test to make sure provider terminates and isRunning gives the correct values.
     * @throws Exception
     */
    @Test
    @Repeat(iterations = 3)
    public void testMoreoverProvider() throws Exception{
        MoreoverConfiguration config = createConfiguration();
        final int expectedNumOfDatums = randomIntBetween(1, 10000);
        final MoreoverTaskForTesting task = new MoreoverTaskForTesting(expectedNumOfDatums);
        MoreoverProvider provider = new MoreoverProvider(config) {
            @Override
            protected MoreoverProviderTask getMoreoverProviderTask(MoreoverKeyData keyData, BlockingQueue<StreamsDatum> queue) {
                task.setQueue(queue);
                return task;
            }
        };
        provider.prepare(null);
        provider.startStream();
        int receivedDatums = 0;
        while(provider.isRunning()) {
            receivedDatums += provider.readCurrent().getQueue().size();
            Thread.sleep(500);
        }
        assertEquals(expectedNumOfDatums, receivedDatums);
    }

    private MoreoverConfiguration createConfiguration() {
        MoreoverConfiguration config = new MoreoverConfiguration();
        MoreoverKeyData data = new MoreoverKeyData();
        List<MoreoverKeyData> keyData = Lists.newLinkedList();
        keyData.add(data);
        config.setApiKeys(keyData);
        return config;
    }


    private static class MoreoverTaskForTesting extends MoreoverProviderTask {

        private int numDatums;
        private BlockingQueue<StreamsDatum> queue;
        private AtomicBoolean running = new AtomicBoolean(false);
        private AtomicBoolean keepRunning = new AtomicBoolean(true);


        public MoreoverTaskForTesting(int numDatumsToOutput) {
            super("", "", null, "", false);
            this.numDatums = numDatumsToOutput;
        }

        public void setQueue(BlockingQueue<StreamsDatum> queue) {
            this.queue = queue;
        }

        @Override
        public void stopTask() {
            this.keepRunning.set(false);
        }

        public boolean getKeepRunning() {
            return this.keepRunning.get();
        }

        @Override
        public boolean isRunning() {
            return this.running.get();
        }

        @Override
        public void run() {
            this.running.set(true);
            try {
                int count = 0;
                while (this.keepRunning.get() && count < this.numDatums && !Thread.currentThread().isInterrupted()) {
                    if (randomIntBetween(0, 100) == 0) {

                        int sleepTime = randomIntBetween(1, 750);
                        Thread.sleep(sleepTime);

                    }
                    this.queue.put(new StreamsDatum(null));
                    ++count;
                }
            } catch (InterruptedException ie) {
                throw new RuntimeException(ie);
            } finally {
                this.running.set(false);
            }
        }
    }


}
