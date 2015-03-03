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
package org.apache.streams.local.test.providers;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.util.ComponentUtils;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class NumericMessageProviderDelayed implements StreamsProvider {

    private final int numMessages;
    private final int delay;
    private boolean prepareCalled = false;
    private boolean cleanupCalled = false;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private StreamsResultSet streamsResultSet = new StreamsResultSet(new ArrayBlockingQueue<StreamsDatum>(500));


    public NumericMessageProviderDelayed(int numMessages) {
        this(numMessages, 0);
    }

    public NumericMessageProviderDelayed(int numMessages, int delay) {
        this.numMessages = numMessages;
        this.delay = delay;
    }

    /**
     * Start the stream
     */
    public void startStream() {
        this.running.set(true);
        new Thread(new LeakNumbers(streamsResultSet)).start();
    }

    public StreamsResultSet readCurrent() {
        return streamsResultSet;

    }

    public StreamsResultSet readNew(BigInteger sequence) {
        return streamsResultSet;
    }

    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return streamsResultSet;
    }

    @Override
    public boolean isRunning() {
        return this.running.get();
    }

    public void prepare(Object configurationObject) {
        this.prepareCalled = true;
    }

    public void cleanUp() {
        this.cleanupCalled = true;
    }

    public boolean wasPrepareCalled() {
        return prepareCalled;
    }

    public boolean wasCleanupCalled() {
        return cleanupCalled;
    }

    class LeakNumbers implements Runnable {

        private final StreamsResultSet streamsResultSet;

        LeakNumbers(StreamsResultSet streamsResultSet) {
            this.streamsResultSet = streamsResultSet;
        }

        public void run() {
            for (int i = 0; i < numMessages && !Thread.currentThread().isInterrupted() ; i++) {

                // if the thread is interrupted, then end this.
                if(Thread.currentThread().isInterrupted()) {
                    break;
                }
                StreamsDatum datum = new StreamsDatum(new NumericMessageObject(i));
                ComponentUtils.offerUntilSuccess(datum, streamsResultSet.getQueue());
                safeSleep(delay);
            }
            running.set(false);
        }
    }


    public static void safeSleep(int delay) {
        try {
            // wait one tenth of a millisecond
            Thread.sleep(delay);
        } catch (Exception e) {
            // no operation
        }
    }
}