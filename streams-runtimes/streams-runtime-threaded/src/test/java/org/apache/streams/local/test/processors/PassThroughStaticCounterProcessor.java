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
package org.apache.streams.local.test.processors;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Pass-through processor that counts the elements and stores them in memory
 */
public class PassThroughStaticCounterProcessor implements StreamsProcessor {

    protected final AtomicInteger count = new AtomicInteger(0);
    private final int delay;

    public PassThroughStaticCounterProcessor() {
        this(0);
    }

    public PassThroughStaticCounterProcessor(int delay) {
        this.delay = delay;
    }

    /**
     * How many messages we saw
     * @return
     * The number of messages this instance saw
     */
    public int getMessageCount() {
        return this.count.get();
    }

    public List<StreamsDatum> process(StreamsDatum entry) {
        sleepSafely();
        this.count.incrementAndGet();
        List<StreamsDatum> result = new LinkedList<StreamsDatum>();
        result.add(entry);
        return result;
    }

    protected void sleepSafely() {
        try {
            Thread.sleep(this.delay);
        } catch(InterruptedException ie) {
            // no Operation
        }
    }


    public void prepare(Object configurationObject) {
        // claim an id
    }

    public void cleanUp() {

    }
}