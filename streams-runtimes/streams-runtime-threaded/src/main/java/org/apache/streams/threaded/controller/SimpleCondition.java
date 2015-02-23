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
package org.apache.streams.threaded.controller;

import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;

public class SimpleCondition implements Condition {

    boolean set;

    public SimpleCondition() {
    }

    public synchronized void await() throws InterruptedException {
        while(!this.set) {
            this.wait();
        }
    }

    public synchronized void reset() {
        this.set = false;
    }

    public synchronized boolean await(long time, TimeUnit unit) throws InterruptedException {
        assert unit == TimeUnit.DAYS || unit == TimeUnit.HOURS || unit == TimeUnit.MINUTES || unit == TimeUnit.SECONDS || unit == TimeUnit.MILLISECONDS;

        long end = System.currentTimeMillis() + unit.convert(time, TimeUnit.MILLISECONDS);

        while(!this.set && end > System.currentTimeMillis()) {
            TimeUnit.MILLISECONDS.timedWait(this, end - System.currentTimeMillis());
        }

        return this.set;
    }

    public synchronized void signal() {
        this.set = true;
        this.notify();
    }

    public synchronized void signalAll() {
        this.set = true;
        this.notifyAll();
    }

    public synchronized boolean isSignaled() {
        return this.set;
    }

    public void awaitUninterruptibly() {
        throw new UnsupportedOperationException();
    }

    public long awaitNanos(long nanosTimeout) throws InterruptedException {
        throw new UnsupportedOperationException();
    }

    public boolean awaitUntil(Date deadline) throws InterruptedException {
        throw new UnsupportedOperationException();
    }
}