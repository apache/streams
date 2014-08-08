package org.apache.streams.util.api.requests.backoff.impl;

import org.apache.streams.util.api.requests.backoff.BackOffStrategy;

/**
 * Exponential backk strategy.  Caluclated by baseBackOffTimeInSeconds raised the attempt-count power.
 */
public class ExponentialBackOffStrategy extends BackOffStrategy {


    /**
     * Unlimited use ExponentialBackOffStrategy
     * @param baseBackOffTimeInSeconds
     */
    public ExponentialBackOffStrategy(int baseBackOffTimeInSeconds) {
        this(baseBackOffTimeInSeconds, -1);
    }

    /**
     * Limited use ExponentialBackOffStrategy
     * @param baseBackOffTimeInSeconds
     * @param maxNumAttempts
     */
    public ExponentialBackOffStrategy(int baseBackOffTimeInSeconds, int maxNumAttempts) {
        super(baseBackOffTimeInSeconds, maxNumAttempts);
    }

    @Override
    protected long calculateBackOffTime(int attemptCount, long baseSleepTime) {
        return Math.round(Math.pow(baseSleepTime, attemptCount)) * 1000;
    }
}
