package org.apache.streams.util.api.requests.backoff;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import org.apache.streams.util.api.requests.backoff.impl.ConstantTimeBackOffStrategy;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Unit Tests
 */
public class ConstantTimeBackOffStrategyTest extends RandomizedTest{


    @Test
    public void constantTimeBackOffStategy() {
        AbstractBackOffStrategy backOff = new ConstantTimeBackOffStrategy(1);
        assertEquals(1, backOff.calculateBackOffTime(1,1));
        assertEquals(1, backOff.calculateBackOffTime(2,1));
        assertEquals(1, backOff.calculateBackOffTime(3,1));
        assertEquals(1, backOff.calculateBackOffTime(4,1));
        assertEquals(1, backOff.calculateBackOffTime(randomIntBetween(1, Integer.MAX_VALUE),1));
    }


}
