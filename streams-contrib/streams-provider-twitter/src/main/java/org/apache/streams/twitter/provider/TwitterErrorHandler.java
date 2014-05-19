package org.apache.streams.twitter.provider;

/*
 * #%L
 * streams-provider-twitter
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;

/**
 * Created by steveblackmon on 2/8/14.
 */
public class TwitterErrorHandler
{
    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterErrorHandler.class);

    protected static final long initial_backoff = 1000;
    protected static long backoff = initial_backoff;

    public static int handleTwitterError(Twitter twitter, Exception exception)
    {
        if(exception instanceof TwitterException)
        {
            TwitterException e = (TwitterException)exception;
            if(e.exceededRateLimitation())
            {
                LOGGER.warn("Rate Limit Exceeded");
                try {
                    Thread.sleep(backoff *= 2);
                } catch (InterruptedException e1) {}
                return 1;
            }
            else if(e.isCausedByNetworkIssue())
            {
                LOGGER.info("Twitter Network Issues Detected. Backing off...");
                LOGGER.info("{} - {}", e.getExceptionCode(), e.getLocalizedMessage());
                try {
                    Thread.sleep(backoff *= 2);
                } catch (InterruptedException e1) {}
                return 1;
            }
            else if(e.isErrorMessageAvailable())
            {
                if(e.getMessage().toLowerCase().contains("does not exist"))
                {
                    LOGGER.warn("User does not exist...");
                    return 100;
                }
                else
                {
                    return 1;
                }
            }
            else
            {
                if(e.getExceptionCode().equals("ced778ef-0c669ac0"))
                {
                    // This is a known weird issue, not exactly sure the cause, but you'll never be able to get the data.
                    return 5;
                }
                else
                {
                    LOGGER.warn("Unknown Twitter Exception...");
                    LOGGER.warn("  Account: {}", twitter);
                    LOGGER.warn("   Access: {}", e.getAccessLevel());
                    LOGGER.warn("     Code: {}", e.getExceptionCode());
                    LOGGER.warn("  Message: {}", e.getLocalizedMessage());
                    return 1;
                }
            }
        }
        else if(exception instanceof RuntimeException)
        {
            LOGGER.warn("TwitterGrabber: Unknown Runtime Error", exception.getMessage());
            return 1;
        }
        else
        {
            LOGGER.info("Completely Unknown Exception: {}", exception);
            return 1;
        }
    }

}