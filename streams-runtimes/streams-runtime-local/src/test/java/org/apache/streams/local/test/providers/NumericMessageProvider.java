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

package org.apache.streams.core.test.providers;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Test StreamsProvider that sends out StreamsDatums numbered from 0 to numMessages.
 */
public class NumericMessageProvider implements StreamsProvider {

    private int numMessages;

    public NumericMessageProvider(int numMessages) {
        this.numMessages = numMessages;
    }

    @Override
    public void startStream() {
        // no op
    }

    @Override
    public StreamsResultSet readCurrent() {
        return new ResultSet();
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return new ResultSet();
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return new ResultSet();
    }

    @Override
    public void prepare(Object configurationObject) {

    }

    @Override
    public void cleanUp() {

    }


    private class ResultSet extends StreamsResultSet {

        private ResultSet() {
            super(new ConcurrentLinkedQueue<StreamsDatum>());
        }

//        @Override
//        public long getStartTime() {
//            return 0;
//        }
//
//        @Override
//        public long getEndTime() {
//            return 0;
//        }
//
//        @Override
//        public String getSourceId() {
//            return null;
//        }
//
//        @Override
//        public BigInteger getMaxSequence() {
//            return null;
//        }

        @Override
        public Iterator<StreamsDatum> iterator() {
            return new Iterator<StreamsDatum>() {
                private int i = 0;

                @Override
                public boolean hasNext() {
                    return i < numMessages;
                }

                @Override
                public StreamsDatum next() {
                    return new StreamsDatum(i++);
                }

                @Override
                public void remove() {

                }
            };
        }
    }
}
