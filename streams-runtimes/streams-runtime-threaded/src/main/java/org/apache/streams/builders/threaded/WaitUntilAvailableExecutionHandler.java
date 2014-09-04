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
package org.apache.streams.builders.threaded;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;

/**
 * A rejection handler that just pauses execution and let's
 * the item wait until a free spot opens up for it to go into
 */
public class WaitUntilAvailableExecutionHandler implements RejectedExecutionHandler {


    private final Condition condition = new SimpleCondition();
    private final AtomicInteger waitCount = new AtomicInteger(0);

    public boolean isWaiting() { return this.waitCount.get() > 0; }

    public WaitUntilAvailableExecutionHandler() {

    }

    public synchronized void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // Wait until the pool is free for another item

        if (executor.getMaximumPoolSize() == executor.getQueue().size()) {
            this.waitCount.incrementAndGet();

            while (executor.getMaximumPoolSize() == executor.getQueue().size())
                safeSleep();

            this.waitCount.decrementAndGet();
        }
        executor.submit(r);
    }

    public void safeSleep() {
        try {
            // wait one tenth of a millisecond
            Thread.yield();
            Thread.sleep(5);
        }
        catch(Exception e) {
            /* no op */
        }
    }
}