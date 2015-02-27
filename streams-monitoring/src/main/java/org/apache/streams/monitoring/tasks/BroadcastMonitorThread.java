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
package org.apache.streams.monitoring.tasks;

import org.slf4j.Logger;

import java.util.Map;

/**
 * This thread runs inside of a Streams runtime and periodically persists information
 * from relevant JMX beans
 */
public class BroadcastMonitorThread extends LocalRuntimeBroadcastMonitorThread implements Runnable {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(BroadcastMonitorThread.class);

    public BroadcastMonitorThread(Map<String, Object> streamConfig) {
        super(streamConfig);
    }

    /**
     * Get all relevant JMX beans, convert their values to strings, and then persist them
     */
    @Override
    public void run() {
        LOGGER.info("BroadcastMonitorThread running");
        while(keepRunning) {
            try {
                persistMessages();
                Thread.sleep(getWaitTime());
            } catch (InterruptedException e) {
                LOGGER.error("Interrupted!: {}", e);
                Thread.currentThread().interrupt();
                this.keepRunning = false;
            }
        }
    }
}