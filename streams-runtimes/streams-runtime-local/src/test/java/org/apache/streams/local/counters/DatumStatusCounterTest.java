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

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Test;

import javax.management.InstanceNotFoundException;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;

/**
 *
 */
public class DatumStatusCounterTest extends RandomizedTest {

    private static final String MBEAN_ID = "test_id";
    private static final String STREAM_ID = "test_stream";
    private static long STREAM_START_TIME = (new DateTime()).getMillis();


    /**
     * Remove registered mbeans from previous tests
     * @throws Exception
     */
    @After
    public void unregisterMXBean() throws Exception {
        try {
            ManagementFactory.getPlatformMBeanServer().unregisterMBean(new ObjectName(String.format(DatumStatusCounter.NAME_TEMPLATE, MBEAN_ID, STREAM_ID, STREAM_START_TIME)));
        } catch (InstanceNotFoundException ife) {
            //No-op
        }
    }

    /**
     * Test Constructor can register the counter as an mxbean with throwing an exception.
     */
    @Test
    public void testConstructor() {
        try {
            new DatumStatusCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
        } catch (Throwable t) {
            fail("Constructor Threw Exception : "+t.getMessage());
        }
    }

    /**
     * Test that you can increment passes and it returns the correct count
     * @throws Exception
     */
    @Test
    @Repeat(iterations = 3)
    public void testPassed() throws Exception {
        DatumStatusCounter counter = new DatumStatusCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
        int numIncrements = randomIntBetween(1, 100000);
        for(int i=0; i < numIncrements; ++i) {
            counter.incrementPassedCount();
        }
        assertEquals(numIncrements, counter.getNumPassed());

        unregisterMXBean();

        counter = new DatumStatusCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
        numIncrements = randomIntBetween(1, 100000);
        long total = 0;
        for(int i=0; i < numIncrements; ++i) {
            long delta = randomIntBetween(1, 100);
            total += delta;
            counter.incrementPassedCount(delta);
        }
        assertEquals(total, counter.getNumPassed());
    }

    /**
     * Test that you can increment failed and it returns the correct count
     * @throws Exception
     */
    @Test
    @Repeat(iterations = 3)
    public void testFailed() throws Exception {
        DatumStatusCounter counter = new DatumStatusCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
        int numIncrements = randomIntBetween(1, 100000);
        for(int i=0; i < numIncrements; ++i) {
            counter.incrementFailedCount();
        }
        assertEquals(numIncrements, counter.getNumFailed());

        unregisterMXBean();

        counter = new DatumStatusCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
        numIncrements = randomIntBetween(1, 100000);
        long total = 0;
        for(int i=0; i < numIncrements; ++i) {
            long delta = randomIntBetween(1, 100);
            total += delta;
            counter.incrementFailedCount(delta);
        }
        assertEquals(total, counter.getNumFailed());
    }


    /**
     * Test failure rate returns expected values
     */
    @Test
    @Repeat(iterations = 3)
    public void testFailureRate() {
        DatumStatusCounter counter = new DatumStatusCounter(MBEAN_ID, STREAM_ID, STREAM_START_TIME);
        assertEquals(0.0, counter.getFailRate(), 0);
        int failures = randomIntBetween(0, 100000);
        int passes = randomIntBetween(0, 100000);
        counter.incrementPassedCount(passes);
        counter.incrementFailedCount(failures);
        assertEquals((double)failures / (double)(passes + failures), counter.getFailRate(), 0);
    }
}