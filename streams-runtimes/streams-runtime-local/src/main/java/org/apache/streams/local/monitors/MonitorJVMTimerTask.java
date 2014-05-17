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
package org.apache.streams.local.monitors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.TimerTask;

public class MonitorJVMTimerTask extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorJVMTimerTask.class);

    public MonitorJVMTimerTask() {

    }

    public void run() {
        /**
         * Note:
         * Quick class and method to let us see what is going on with the JVM. We need to make sure
         * that everything is running with as little memory as possible. If we are generating a heap
         * overflow, this will be very apparent by the information shown here.
         */
        MemoryUsage memoryUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();

        String maxMemory = memoryUsage.getMax() == Long.MAX_VALUE ? "NO_LIMIT" :
                humanReadableByteCount(memoryUsage.getMax(), true);

        String usedMemory = humanReadableByteCount(memoryUsage.getUsed(), true);

        LOGGER.info("[monitor] Used Memory: {}, Max: {}",
                usedMemory,
                maxMemory);
    }

    public String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

}
