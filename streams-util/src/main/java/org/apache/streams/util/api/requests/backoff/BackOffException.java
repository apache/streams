package org.apache.streams.util.api.requests.backoff;

/**
 * Exception that is thrown when a {@link org.apache.streams.util.api.requests.backoff.BackOffStrategy} has attempted to
 * <code>backOff()</code> more than the {@link org.apache.streams.util.api.requests.backoff.BackOffStrategy} was configured for.
 */
public class BackOffException extends Exception {

    private int attemptCount;
    private long sleepTime;

    public BackOffException() {
        this(-1, -1);
    }

    public BackOffException(String message) {
        this(message, -1, -1);
    }

    public BackOffException(int attemptCount, long maxSleepTime) {
        this.attemptCount = attemptCount;
        this.sleepTime = maxSleepTime;
    }

    public BackOffException(String message, int attemptCount, long maxSleepTime) {
        super(message);
        this.attemptCount = attemptCount;
        this.sleepTime = maxSleepTime;
    }

    /**
     * Gets the number of back off attempts that happened before the exception was thrown. If the function that
     * initialized this exception does not set the number of attempts, -1 will be returned.
     * @return number of attempts
     */
    public int getNumberOfBackOffsAttempted() {
        return this.attemptCount;
    }

    /**
     * Gets the longest sleep period that the strategy attempted. If the function that
     * initialized this exception does not set the longest sleep period, -1 will be returned.
     * @return
     */
    public long getLongestBackOff() {
        return this.sleepTime;
    }
}
