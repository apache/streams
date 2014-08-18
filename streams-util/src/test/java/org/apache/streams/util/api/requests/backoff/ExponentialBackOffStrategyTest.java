package org.apache.streams.util.api.requests.backoff;

import org.apache.streams.util.api.requests.backoff.impl.ExponentialBackOffStrategy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit Tests
 */
public class ExponentialBackOffStrategyTest {

    @Test
    public void exponentialTimeBackOffStrategyTest() {
        AbstractBackOffStrategy backOff = new ExponentialBackOffStrategy(1);
        assertEquals(5000, backOff.calculateBackOffTime(1,5));
        assertEquals(25000, backOff.calculateBackOffTime(2,5));
        assertEquals(125000, backOff.calculateBackOffTime(3,5));
        assertEquals(2000, backOff.calculateBackOffTime(1,2));
        assertEquals(16000, backOff.calculateBackOffTime(4,2));
    }

}
