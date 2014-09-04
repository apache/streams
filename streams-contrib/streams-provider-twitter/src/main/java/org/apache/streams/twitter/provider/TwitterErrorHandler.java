package org.apache.streams.twitter.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;

public class TwitterErrorHandler {
    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterErrorHandler.class);

    protected static final long INITIAL_BACK_OFF = 1000;
    protected static final long MAX_BACKOFF = 1000 * 30;
    protected static long BACKOFF = INITIAL_BACK_OFF;

    public static int handleTwitterError(Twitter twitter, Exception exception) {
        if (exception instanceof TwitterException) {
            TwitterException e = (TwitterException) exception;
            if (e.exceededRateLimitation()) {
                LOGGER.warn("Rate Limit Exceeded");
                try {
                    Thread.sleep(BACKOFF *= 2);
                } catch (InterruptedException e1) {
                }
                return 1;
            } else if (e.isCausedByNetworkIssue()) {
                LOGGER.info("Twitter Network Issues Detected. Backing off...");
                LOGGER.info("{} - {}", e.getExceptionCode(), e.getLocalizedMessage());
                try {
                    Thread.sleep(BACKOFF *= 2);
                } catch (InterruptedException e1) {
                }
                return 1;
            } else if (e.isErrorMessageAvailable()) {
                if (e.getMessage().toLowerCase().contains("does not exist")) {
                    LOGGER.warn("User does not exist...");
                    return 100;
                } else
                    return 1;
            } else {
                if (e.getExceptionCode().equals("ced778ef-0c669ac0")) {
                    // This is a known weird issue, not exactly sure the cause, but you'll never be able to get the data.
                    return 5;
                } else {
                    LOGGER.warn("Unknown Twitter Exception...");
                    LOGGER.warn("  Account: {}", twitter);
                    LOGGER.warn("   Access: {}", e.getAccessLevel());
                    LOGGER.warn("     Code: {}", e.getExceptionCode());
                    LOGGER.warn("  Message: {}", e.getLocalizedMessage());
                    return 1;
                }
            }
        } else if (exception instanceof RuntimeException) {
            LOGGER.warn("TwitterGrabber: Unknown Runtime Error", exception.getMessage());
            return 1;
        } else {
            LOGGER.info("Completely Unknown Exception: {}", exception);
            return 1;
        }
    }

}