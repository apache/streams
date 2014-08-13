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
package org.apache.streams.util.api.requests.backoff;

/**
 * Exception that is thrown when a {@link AbstractBackOffStrategy} has attempted to
 * <code>backOff()</code> more than the {@link AbstractBackOffStrategy} was configured for.
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
