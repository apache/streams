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
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test StreamsProvider that sends out StreamsDatums numbered from 0 to numMessages.
 */
public class NumericMessageProvider implements StreamsProvider {

    protected int startNumber = 0;
    protected int numMessages;

    private ResultSet resultSet;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);

    public NumericMessageProvider() {this.numMessages = 0;}
    public NumericMessageProvider(int numMessages) {
        this.numMessages = numMessages;
    }

    public NumericMessageProvider(int startNumber, int numMessages) {
        this.startNumber = startNumber;
        this.numMessages = numMessages;
    }

    public void startStream() {
        // no op
    }

    public StreamsResultSet readCurrent() {
        return this.resultSet;
    }

    public StreamsResultSet readNew(BigInteger sequence) {
        return this.resultSet;
    }

    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return this.resultSet;
    }

    @Override
    public boolean isRunning() {
        return this.isRunning.get();
    }

    public void prepare(Object configurationObject) {
        this.resultSet = new ResultSet();
    }

    public void cleanUp() {
    }

    private class ResultSet extends StreamsResultSet {

        private ResultSet() {
            super(new ConcurrentLinkedQueue<StreamsDatum>(new ArrayBlockingQueue<StreamsDatum>(1000)));
            for(int i = 0; i < numMessages; i++) {
                StreamsDatum datum = new StreamsDatum(new NumericMessageObject(startNumber + i));
                ComponentUtils.offerUntilSuccess(datum, this.getQueue());
            }

            isRunning.set(false);
        }
    }
}