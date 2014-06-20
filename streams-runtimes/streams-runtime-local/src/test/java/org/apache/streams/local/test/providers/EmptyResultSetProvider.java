/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import com.google.common.collect.Queues;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.joda.time.DateTime;

import java.math.BigInteger;

/**
 * Provides new, empty instances of result set.
 */
public class EmptyResultSetProvider implements StreamsProvider {
    @Override
    public void startStream() {
        //NOP
    }

    @Override
    public StreamsResultSet readCurrent() {
        return new StreamsResultSet(Queues.<StreamsDatum>newLinkedBlockingQueue());
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return new StreamsResultSet(Queues.<StreamsDatum>newLinkedBlockingQueue());
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return new StreamsResultSet(Queues.<StreamsDatum>newLinkedBlockingQueue());
    }

    @Override
    public boolean isRunning() {
        return false;
    }

    @Override
    public void prepare(Object configurationObject) {
        //NOP
    }

    @Override
    public void cleanUp() {
        //NOP
    }
}
