package org.apache.streams.util.api.requests.backoff;

/**
 * BackOffStrategy will cause the current thread to sleep for a specific amount of time. This is used to adhere to
 * api rate limits.
 *
 * The example below illustrates using a BackOffStrategy to slow down requests when you hit a rate limit exception.
 *
 * <code>
 *     public void pollApi(ApiClient apiClient, BackOffStrategy backOffStrategy) throws BackOffException {
 *          while( apiClient.hasMoreData() ) {
 *              try {
 *                  apiClient.pollData();
 *              } catch (RateLimitException rle) {
 *                  backOffStrategy.backOff();
 *              }
 *          }
 *     }
 * </code>
 *
 */
public abstract class BackOffStrategy {

    private long baseSleepTime;
    private long lastSleepTime;
    private int maxAttempts;
    private int attemptsCount;

    /**
     * A BackOffStrategy that can effectively be used endlessly.
     * @param baseBackOffTime amount of time back of in seconds
     */
    public BackOffStrategy(long baseBackOffTime) {
        this(baseBackOffTime, -1);
    }

    /**
     * A BackOffStrategy that has a limited number of uses before it throws a {@link org.apache.streams.util.api.requests.backoff.BackOffException}
     * @param baseBackOffTime time to back off in milliseconds, must be greater than 0.
     * @param maximumNumberOfBackOffAttempts maximum number of attempts, must be grater than 0 or -1. -1 indicates there is no maximum number of attempts.
     */
    public BackOffStrategy(long baseBackOffTime, int maximumNumberOfBackOffAttempts) {
        if(baseBackOffTime <= 0) {
            throw new IllegalArgumentException("backOffTimeInMilliSeconds is not greater than 0 : "+baseBackOffTime);
        }
        if(maximumNumberOfBackOffAttempts<=0 && maximumNumberOfBackOffAttempts != -1) {
            throw new IllegalArgumentException("maximumNumberOfBackOffAttempts is not greater than 0 : "+maximumNumberOfBackOffAttempts);
        }
        this.baseSleepTime = baseBackOffTime;
        this.maxAttempts = maximumNumberOfBackOffAttempts;
        this.attemptsCount = 0;
    }

    /**
     * Cause the current thread to sleep for an amount of time based on the implemented strategy. If limits are set
     * on the number of times the backOff can be called, an exception will be thrown.
     * @throws BackOffException
     */
    public void backOff() throws BackOffException {
        if(this.attemptsCount++ >= this.maxAttempts && this.maxAttempts != -1) {
            throw new BackOffException(this.attemptsCount-1, this.lastSleepTime);
        } else {
            try {
                Thread.sleep(this.lastSleepTime = calculateBackOffTime(this.attemptsCount, this.baseSleepTime));
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Rests the back off strategy to its original state.  After the call the strategy will act as if {@link BackOffStrategy#backOff()}
     * has never been called.
     */
    public void reset() {
        this.attemptsCount = 0;
    }

    /**
     * Calculate the amount of time in milliseconds that the strategy should back off for
     * @param attemptCount the number of attempts the strategy has backed off. i.e. 1 -> this is the first attempt, 2 -> this is the second attempt, etc.
     * @param baseSleepTime the minimum amount of time it should back off for in milliseconds
     * @return the amount of time it should back off in milliseconds
     */
    protected abstract long calculateBackOffTime(int attemptCount, long baseSleepTime);

}
