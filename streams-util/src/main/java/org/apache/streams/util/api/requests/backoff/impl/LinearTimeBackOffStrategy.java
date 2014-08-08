package org.apache.streams.util.api.requests.backoff.impl;

import org.apache.streams.util.api.requests.backoff.BackOffStrategy;

/**
 * A {@link org.apache.streams.util.api.requests.backoff.BackOffStrategy} that causes back offs in linear increments. Each
 * attempt cause an increase back off period.
 * Calculated by attemptNumber * baseBackOffAmount.
 */
public class LinearTimeBackOffStrategy extends BackOffStrategy{


    public LinearTimeBackOffStrategy(int baseBackOffTimeInSeconds) {
        this(baseBackOffTimeInSeconds, -1);
    }

    public LinearTimeBackOffStrategy(int baseBackOffTimeInSeconds, int maxAttempts) {
        super(baseBackOffTimeInSeconds, -1);
    }

    @Override
    protected long calculateBackOffTime(int attemptCount, long baseSleepTime) {
        return 1000L * attemptCount * baseSleepTime;
    }
}
