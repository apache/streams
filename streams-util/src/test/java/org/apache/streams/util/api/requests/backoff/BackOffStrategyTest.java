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

package org.apache.streams.util.api.requests.backoff;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit Tets
 */
public class BackOffStrategyTest {


    private class TestBackOff extends AbstractBackOffStrategy {

        public TestBackOff(long sleep, int maxAttempts) {
            super(sleep, maxAttempts);
        }

        @Override
        protected long calculateBackOffTime(int attemptCount, long baseSleepTime) {
            return baseSleepTime;
        }
    }

    @Test
    public void testUnlimitedBackOff() {
        AbstractBackOffStrategy backOff = new TestBackOff(1, -1);
        try {
            for(int i=0; i < 100; ++i) {
                backOff.backOff();
            }
        } catch (BackOffException boe) {
            fail("Threw BackOffException.  Not expected action");
        }
    }

    @Test
    public void testLimitedUseBackOff()  {
        AbstractBackOffStrategy backOff = new TestBackOff(1, 2);
        try {
            backOff.backOff();
        } catch (BackOffException boe) {
            fail("Threw BackOffExpection. Not expected action");
        }
        try {
            backOff.backOff();
        } catch (BackOffException boe) {
            fail("Threw BackOffExpection. Not expected action");
        }
        try {
            backOff.backOff();
            fail("Expected BackOffException to be thrown.");
        } catch (BackOffException boe) {

        }
    }

    @Test
    public void testBackOffSleep() throws BackOffException {
        AbstractBackOffStrategy backOff = new TestBackOff(2000, 1);
        long startTime = System.currentTimeMillis();
        backOff.backOff();
        long endTime = System.currentTimeMillis();
        assertTrue(endTime - startTime >= 2000);
    }



}
