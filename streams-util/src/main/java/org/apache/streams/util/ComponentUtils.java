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

package org.apache.streams.util;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Common utilities for Streams components.
 */
public class ComponentUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentUtils.class);

    public static <T> void offerUntilSuccess(T entry, Queue<T> queue) {

        boolean success;
        do {
            success = queue.offer(entry);
            Thread.yield();
        }
        while( !success );
    }

    public static String pollUntilStringNotEmpty(Queue queue) {

        String result = null;
        do {
            synchronized( ComponentUtils.class ) {
                try {
                    result = (String) queue.remove();
                } catch( Exception e ) {}
            }
            Thread.yield();
        }
        while( result == null && !StringUtils.isNotEmpty(result) );

        return result;
    }

    public static void shutdownExecutor(ExecutorService stream, int initialWait, int secondaryWait) {
        stream.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!stream.awaitTermination(initialWait, TimeUnit.SECONDS)) {
                stream.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!stream.awaitTermination(secondaryWait, TimeUnit.SECONDS)) {
                    LOGGER.error("Executor Service did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            stream.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

}
