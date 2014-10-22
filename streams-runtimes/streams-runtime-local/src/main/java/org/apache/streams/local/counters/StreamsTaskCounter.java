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

import net.jcip.annotations.GuardedBy;
import net.jcip.annotations.ThreadSafe;
import org.apache.streams.util.ComponentUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 *
 */
@ThreadSafe
public class StreamsTaskCounter implements StreamsTaskCounterMXBean{

    public static final String NAME_TEMPLATE = "org.apache.streams.local:type=StreamsTaskCounter,name=%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(StreamsTaskCounter.class);

    private AtomicLong emitted;
    private AtomicLong received;
    private AtomicLong errors;
    private AtomicLong totalTime;
    @GuardedBy("this")
    private volatile long maxTime;

    /**
     *
     * @param id
     */
    public StreamsTaskCounter(String id) {
        this.emitted = new AtomicLong(0);
        this.received = new AtomicLong(0);
        this.errors = new AtomicLong(0);
        this.totalTime = new AtomicLong(0);
        this.maxTime = -1;
        ComponentUtils.registerLocalMBean(String.format(NAME_TEMPLATE, id), this);
    }

    /**
     * Increment emitted count
     */
    public void incrementEmittedCount() {
        this.incrementEmittedCount(1);
    }

    /**
     * Increment emitted count
     * @param delta
     */
    public void incrementEmittedCount(long delta) {
        this.emitted.addAndGet(delta);
    }

    /**
     * Increment error count
     */
    public void incrementErrorCount() {
        this.incrementErrorCount(1);
    }

    /**
     * Increment error count
     * @param delta
     */
    public void incrementErrorCount(long delta) {
        this.errors.addAndGet(delta);
    }

    /**
     * Increment received count
     */
    public void incrementReceivedCount() {
        this.incrementReceivedCount(1);
    }

    /**
     * Increment received count
     * @param delta
     */
    public void incrementReceivedCount(long delta) {
        this.received.addAndGet(delta);
    }

    /**
     * Add the time it takes to process a single datum in milliseconds
     * @param processTime
     */
    public void addTime(long processTime) {
        synchronized (this) {
            if(processTime > this.maxTime) {
                this.maxTime = processTime;
            }
        }
        this.totalTime.addAndGet(processTime);
    }

    @Override
    public double getErrorRate() {
        if(this.received.get() == 0) {
            return 0.0;
        }
        return (double) this.errors.get() / (double) this.received.get();
    }

    @Override
    public long getNumEmitted() {
        return this.emitted.get();
    }

    @Override
    public long getNumReceived() {
        return this.received.get();
    }

    @Override
    public long getNumUnhandledErrors() {
        return this.errors.get();
    }

    @Override
    public double getAvgTime() {
        long rec = this.received.get();
        long emit = this.emitted.get();
        if(rec == 0 && emit == 0 ) {
            return 0.0;
        } else if( rec == 0) { //provider instance
            return this.totalTime.get() / (double) emit;
        } else {
            return this.totalTime.get() / ((double) this.received.get() - this.errors.get());
        }
    }

    @Override
    public long getMaxTime() {
        return this.maxTime;
    }
}
