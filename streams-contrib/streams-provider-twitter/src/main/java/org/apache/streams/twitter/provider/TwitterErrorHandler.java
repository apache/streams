/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.streams.twitter.provider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.RateLimitStatus;

/**
 *  Handle expected and unexpected exceptions.
 */
public class TwitterErrorHandler
{
    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterErrorHandler.class);

    // selected because 3 * 5 + n >= 15 for positive n
    protected static final long retry = 3*60*1000;

    @Deprecated
    public static int handleTwitterError(Twitter twitter, Exception exception) {
        return handleTwitterError(twitter, null, exception);
    }

    public static int handleTwitterError(Twitter twitter, Long id, Exception exception)
    {
        if(exception instanceof TwitterException)
        {
            TwitterException e = (TwitterException)exception;
            if(e.exceededRateLimitation())
            {
                long millisUntilReset = retry;

                final RateLimitStatus rateLimitStatus = e.getRateLimitStatus();
                if (rateLimitStatus != null) {
                    millisUntilReset = rateLimitStatus.getSecondsUntilReset() * 1000;
                }

                LOGGER.warn("Rate Limit Exceeded. Will retry in {} seconds...", millisUntilReset / 1000);

                try {
                    Thread.sleep(millisUntilReset);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }

                return 1;
            }
            else if(e.isCausedByNetworkIssue())
            {
                LOGGER.info("Twitter Network Issues Detected. Backing off...");
                LOGGER.info("{} - {}", e.getExceptionCode(), e.getLocalizedMessage());
                try {
                    Thread.sleep(retry);
                } catch (InterruptedException e1) {
                    Thread.currentThread().interrupt();
                }
                return 1;
            }
            else if(e.isErrorMessageAvailable())
            {
                if(e.getMessage().toLowerCase().contains("does not exist"))
                {
                    if( id != null )
                        LOGGER.warn("User does not exist: {}", id);
                    else
                        LOGGER.warn("User does not exist");
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
                else if(e.getExceptionCode().equals("4be80492-0a7bf7c7")) {
                    // This is a 401 reflecting credentials don't have access to the requested resource.
                    if( id != null )
                        LOGGER.warn("Authentication Exception accessing id: {}", id);
                    else
                        LOGGER.warn("Authentication Exception");
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
