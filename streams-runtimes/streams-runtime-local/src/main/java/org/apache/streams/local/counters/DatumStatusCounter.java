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

import net.jcip.annotations.ThreadSafe;
import org.apache.streams.local.builders.LocalStreamBuilder;
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
public class DatumStatusCounter implements DatumStatusCounterMXBean{

    public static final String NAME_TEMPLATE = "org.apache.streams.local:type=DatumCounter,name=%s,identifier=%s,startedAt=%s";
    private static final Logger LOGGER = LoggerFactory.getLogger(DatumStatusCounter.class);

    private AtomicLong failed;
    private AtomicLong passed;

    public DatumStatusCounter(String id) {
        this(id, LocalStreamBuilder.DEFAULT_STREAM_IDENTIFIER, -1);
    }

    public DatumStatusCounter(String id, String streamIdentifier, long startedAt) {
        this.failed = new AtomicLong(0);
        this.passed = new AtomicLong(0);
        ComponentUtils.registerLocalMBean(String.format(NAME_TEMPLATE, id, streamIdentifier, startedAt), this);
    }

    public void incrementFailedCount() {
        this.incrementFailedCount(1);
    }

    public void incrementFailedCount(long delta) {
        this.failed.addAndGet(delta);
    }

    public void incrementPassedCount() {
        this.incrementPassedCount(1);
    }

    public void incrementPassedCount(long delta) {
        this.passed.addAndGet(delta);
    }


    @Override
    public double getFailRate() {
        double failed = this.failed.get();
        double passed = this.passed.get();
        if(failed == 0.0 && passed == 0) {
            return 0.0;
        }
        return failed / (passed + failed);
    }

    @Override
    public long getNumFailed() {
        return this.failed.get();
    }

    @Override
    public long getNumPassed() {
        return this.passed.get();
    }
}
