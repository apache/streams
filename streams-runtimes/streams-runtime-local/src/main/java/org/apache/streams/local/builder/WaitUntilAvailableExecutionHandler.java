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
package org.apache.streams.local.builder;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * A rejection handler that just pauses execution and let's
 * the item wait until a free spot opens up for it to go into
 */
public class WaitUntilAvailableExecutionHandler implements RejectedExecutionHandler {
    public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
        // Wait until the pool is free for another item
        while(executor.getMaximumPoolSize() == executor.getQueue().size()) {
            safeSleep();
        }
        executor.submit(r);
    }

    public static void safeSleep() {
        Thread.yield();
        try {
            // wait one tenth of a millisecond
            Thread.sleep(1);
        }
        catch(Exception e) {
            // no operation
        }
    }
}