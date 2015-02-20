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
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;

import java.math.BigInteger;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class NumericMessageThrowExceptionProvider extends NumericMessageProvider {

    private final int numErrorsToThrow;
    private int numErrorsThrown;

    public NumericMessageThrowExceptionProvider(int numMessages, int numErrorsToThrow) {
        super(numMessages);

        //Throw at least one error
        this.numErrorsToThrow = numErrorsToThrow <= 0 ? 1 : numErrorsToThrow;
        this.numErrorsThrown = 0;
    }

    @Override
    public StreamsResultSet readCurrent() {
        return new ResultSet(this.numMessages);
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return new ResultSet(this.numMessages);
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return new ResultSet(this.numMessages);
    }

    private class ResultSet extends StreamsResultSet {

        private ResultSet(int numMessages) {
            super(new ConcurrentLinkedQueue<StreamsDatum>());
            for(int i = 0; i < numMessages; i++) {
                if (numErrorsThrown++ < numErrorsToThrow) {
                    throw new RuntimeException();
                } else {
                    this.getQueue().add(new StreamsDatum(new NumericMessageObject(i)));
                }
            }
        }
    }
}
