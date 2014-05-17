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
package org.apache.streams.local.test.writer;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple counter to count how many times the 'write' was
 * called.
 */
public class DatumCounterWriter implements StreamsPersistWriter{

    protected final AtomicInteger counter = new AtomicInteger(0);
    private final int delayInMilliseconds;

    public DatumCounterWriter() {
        this(0);
    }

    public DatumCounterWriter(int delayInMilliseconds) {
        this.delayInMilliseconds = delayInMilliseconds;
    }

    protected void safeSleep() {
        if(this.delayInMilliseconds > 0) {
            try {
                Thread.sleep(this.delayInMilliseconds);
            } catch (InterruptedException ie) {
                // no Operation
            }
        }
    }

    public void write(StreamsDatum entry) {
        safeSleep();
        this.counter.incrementAndGet();
    }

    public void prepare(Object configurationObject) {

    }

    public void cleanUp() {

    }

    public int getDatumsCounted() {
        return this.counter.get();
    }
}
