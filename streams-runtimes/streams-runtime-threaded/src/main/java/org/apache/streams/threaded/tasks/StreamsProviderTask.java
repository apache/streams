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
package org.apache.streams.threaded.tasks;

import org.apache.streams.threaded.controller.SimpleCondition;
import org.apache.streams.threaded.controller.ThreadingController;
import org.apache.streams.threaded.controller.ThreadingControllerCallback;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;

public class StreamsProviderTask extends BaseStreamsTask implements Runnable {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsProviderTask.class);

    private StreamsProvider provider;
    private final Type type;
    private final AtomicInteger outStanding = new AtomicInteger(0);
    private final AtomicBoolean keepRunning = new AtomicBoolean(true);
    private final Condition lock = new SimpleCondition();

    public static enum Type {
        PERPETUAL,
        READ_CURRENT,
        READ_NEW,
        READ_RANGE
    }

    private static final int TIMEOUT = 100000000;

    public StreamsProviderTask(ThreadingController threadingController, String id, Map<String, Object> config, StreamsProvider provider, Type type) {
        super(threadingController, id, config, provider);
        this.provider = provider;
        this.type = type;
    }

    public boolean isRunning() {
        return this.keepRunning.get() || this.outStanding.get() > 0;
    }

    public Condition getLock() {
        return this.lock;
    }

    @Override
    public void run() {

        try {
            // TODO allow for configuration objects
            StreamsResultSet resultSet = null;
            switch (this.type) {
                case PERPETUAL:
                    provider.startStream();
                    int zeros = 0;
                    while (this.keepRunning.get()) {
                        resultSet = provider.readCurrent();
                        if (resultSet.size() == 0)
                            zeros++;
                        else {
                            zeros = 0;
                        }
                        flushResults(resultSet);
                        // the way this works needs to change...
                        if (zeros > TIMEOUT)
                            this.keepRunning.set(false);
                        safeQuickRest(10);
                    }
                    break;
                case READ_CURRENT:
                    resultSet = this.provider.readCurrent();
                    break;
                case READ_NEW:
                case READ_RANGE:
                default:
                    throw new RuntimeException("Type has not been added to StreamsProviderTask.");
            }

            if(resultSet != null && resultSet.getQueue() != null) {
                if(provider == null) {
                    throw new RuntimeException("Unknown Error - Provider is equal to Null.");
                }

                while(provider.isRunning() || resultSet.getQueue().size() > 0) {
                    // Is there anything to do?
                    if (resultSet.getQueue().isEmpty()) {
                        safeQuickRest(1);
                    } else {
                        flushResults(resultSet);    // then work
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.error("There was an unknown error while attempting to read from the provider. This provider cannot continue with this exception.");
            LOGGER.error("The stream will continue running, but not with this provider.");
            LOGGER.error("Exception: {}", e);
        } finally {
            this.keepRunning.set(false);
            checkLockSignal();
        }
    }

    public void flushResults(StreamsResultSet streamsResultSet) {
        try {
            StreamsDatum datum;
            while (!streamsResultSet.getQueue().isEmpty() && (datum = streamsResultSet.getQueue().poll()) != null) {
                /**
                 * This is meant to be a hard exit from the system. If we are running
                 * and this flag gets set to false, we are to exit immediately and
                 * abandon anything that is in this queue. The remaining processors
                 * will shutdown gracefully once they have depleted their queue
                 */
                if (!this.keepRunning.get())
                    break;

                workMe(datum);
            }
        }
        catch(Throwable e) {
            e.printStackTrace();
            LOGGER.warn("Unknown problem reading the queue, no datums affected: {}", e.getMessage());
        }
    }

    private void workMe(final StreamsDatum datum) {

        outStanding.incrementAndGet();

        getThreadingController().execute(new Runnable() {
            @Override
            public void run() {
                sendToChildren(datum);
            }
        }, new ThreadingControllerCallback() {

            @Override
            public void onSuccess(Object o) {
                outStanding.decrementAndGet();
                checkLockSignal();
            }

            @Override
            public void onFailure(Throwable t) {
                outStanding.decrementAndGet();
                checkLockSignal();
            }
        });
    }

    private void checkLockSignal() {
        if(this.outStanding.get() == 0 && !this.keepRunning.get()) {
            this.lock.signalAll();
        }
    }

    @Override
    protected Collection<StreamsDatum> processInternal(StreamsDatum datum) {
        return null;
    }

    protected void safeQuickRest(int waitTime) {
        // The queue is empty, we might as well sleep.
        Thread.yield();
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException ie) {
            // No Operation
        }
        Thread.yield();
    }

}
