package org.apache.streams.util.api.requests.backoff;

import org.apache.streams.util.api.requests.backoff.impl.LinearTimeBackOffStrategy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit Tests
 */
public class LinearTimeBackOffStartegyTest {

    @Test
    public void linearTimeBackOffStrategyTest() {
        BackOffStrategy backOff = new LinearTimeBackOffStrategy(1);
        assertEquals(1000, backOff.calculateBackOffTime(1,1));
        assertEquals(2000, backOff.calculateBackOffTime(2,1));
        assertEquals(3000, backOff.calculateBackOffTime(3,1));
        assertEquals(4000, backOff.calculateBackOffTime(4,1));
        assertEquals(25000, backOff.calculateBackOffTime(5,5));
    }
}
