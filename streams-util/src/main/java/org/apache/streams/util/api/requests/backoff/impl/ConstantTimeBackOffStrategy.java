/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements. See the NOTICE file
distributed with this work for additional information
regarding copyright ownership. The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance *
http://www.apache.org/licenses/LICENSE-2.0 *
Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied. See the License for the
specific language governing permissions and limitations
under the License. */

package org.apache.streams.util.api.requests.backoff.impl;

import org.apache.streams.util.api.requests.backoff.AbstractBackOffStrategy;

/**
 * A {@link org.apache.streams.util.api.requests.backoff.AbstractBackOffStrategy} that causes the current thread to sleep the
 * same amount of time each time <code>backOff()</code> is called.
 *
 */
public class ConstantTimeBackOffStrategy extends AbstractBackOffStrategy {

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
