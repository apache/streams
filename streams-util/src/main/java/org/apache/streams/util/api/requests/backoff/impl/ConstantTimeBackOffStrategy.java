package org.apache.streams.util.api.requests.backoff.impl;

import org.apache.streams.util.api.requests.backoff.BackOffStrategy;

/**
 * A {@link org.apache.streams.util.api.requests.backoff.BackOffStrategy} that causes the current thread to sleep the
 * same amount of time each time <code>backOff()</code> is called.
 *
 */
public class ConstantTimeBackOffStrategy extends BackOffStrategy {

    /**
     * A ConstantTimeBackOffStrategy that can effectively be used endlessly.
     * @param baseBackOffTimeInMiliseconds amount of time back of in milliseconds
     */
    public ConstantTimeBackOffStrategy(long baseBackOffTimeInMiliseconds) {
        this(baseBackOffTimeInMiliseconds, -1);
    }

    /**
     * A ConstantTimeBackOffStrategy that has a limited number of uses before it throws a {@link org.apache.streams.util.api.requests.backoff.BackOffException}
     * @param baseBackOffTimeInMiliseconds time to back off in milliseconds, must be greater than 0.
     * @param maximumNumberOfBackOffAttempts maximum number of attempts, must be grater than 0 or -1. -1 indicates there is no maximum number of attempts.
     */
    public ConstantTimeBackOffStrategy(long baseBackOffTimeInMiliseconds, int maximumNumberOfBackOffAttempts) {
        super(baseBackOffTimeInMiliseconds, maximumNumberOfBackOffAttempts);
    }

    @Override
    protected long calculateBackOffTime(int attemptCount, long baseSleepTime) {
        return baseSleepTime;
    }
}
