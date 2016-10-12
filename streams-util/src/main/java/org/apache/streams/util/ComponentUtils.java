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

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Common utilities for Streams components.
 */
public class ComponentUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(ComponentUtils.class);

    /**
     * Certain types of queues will fail to {@link java.util.Queue#offer(Object)} an item due to many factors
     * depending on the type of queue. <code>offerUntilSuccess</code> will not return until the item has been
     * successfully queued onto the desired queue
     * @param entry item to queue
     * @param queue queue to add the entry to
     * @param <T>
     */
    public static <T> void offerUntilSuccess(T entry, Queue<T> queue) {
        boolean success;
        do {
            success = queue.offer(entry);
            Thread.yield();
        }
        while( !success );
    }

    /**
     * Certain types of queues will return null when calling {@link java.util.Queue#poll()} due to many factors depending
     * on the type of queue.  <code>pollWhileNotEmpty</code> will poll the queue until an item from the queue is returned
     * or the queue is empty.  If the queue is empty it will return NULL.
     * @param queue
     * @param <T>
     * @return
     */
    public static <T> T pollWhileNotEmpty(Queue<T> queue) {
        T item = queue.poll();
        while(!queue.isEmpty() && item == null) {
            Thread.yield();
            item = queue.poll();
        }
        return item;
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

    /**
     * Attempts to safely {@link java.util.concurrent.ExecutorService#shutdown()} and {@link java.util.concurrent.ExecutorService#awaitTermination(long, java.util.concurrent.TimeUnit)}
     * of an {@link java.util.concurrent.ExecutorService}.
     * @param stream service to be shutdown
     * @param initialWait time in seconds to wait for currently running threads to finish execution
     * @param secondaryWait time in seconds to wait for running threads that did not terminate in the first wait to acknowledge their forced termination
     */
    public static void shutdownExecutor(ExecutorService stream, int initialWait, int secondaryWait) {
        stream.shutdown();
        try {
            if (!stream.awaitTermination(initialWait, TimeUnit.SECONDS)) {
                stream.shutdownNow();
                if (!stream.awaitTermination(secondaryWait, TimeUnit.SECONDS)) {
                    LOGGER.error("Executor Service did not terminate");
                }
            }
        } catch (InterruptedException ie) {
            stream.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Removes all mbeans registered undered a specific domain.  Made specificly to clean up at unit tests
     * @param domain
     */
    public static void removeAllMBeansOfDomain(String domain) throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        domain = domain.endsWith(":") ? domain : domain+":";
        ObjectName objectName = new ObjectName(domain+"*");
        Set<ObjectName> mbeanNames = mbs.queryNames(objectName, null);
        for(ObjectName name : mbeanNames) {
            mbs.unregisterMBean(name);
        }
    }

    /**
     * Attempts to register an object with local MBeanServer.  Throws runtime exception on errors.
     * @param name name to register bean with
     * @param mbean mbean to register
     */
    public static <V> void registerLocalMBean(String name, V mbean) {
        try {
            ObjectName objectName = new ObjectName(name);
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            mbs.registerMBean(mbean, objectName);
        } catch (MalformedObjectNameException | InstanceAlreadyExistsException | MBeanRegistrationException | NotCompliantMBeanException e) {
            LOGGER.error("Failed to register MXBean : {}", e);
            throw new RuntimeException(e);
        }
    }

}
