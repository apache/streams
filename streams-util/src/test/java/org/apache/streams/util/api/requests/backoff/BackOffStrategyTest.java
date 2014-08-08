package org.apache.streams.util.api.requests.backoff;

import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit Tets
 */
public class BackOffStrategyTest {


    private class TestBackOff extends BackOffStrategy {

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
        BackOffStrategy backOff = new TestBackOff(1, -1);
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
        BackOffStrategy backOff = new TestBackOff(1, 2);
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
        BackOffStrategy backOff = new TestBackOff(2000, 1);
        long startTime = System.currentTimeMillis();
        backOff.backOff();
        long endTime = System.currentTimeMillis();
        assertTrue(endTime - startTime >= 2000);
    }



}
