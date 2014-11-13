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

import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BroadcastMonitorThreadTest {
    private ExecutorService executor;

    @Test
    public void testThreadNullConfig() {
        BroadcastMonitorThread thread = new BroadcastMonitorThread(null);
    }

    @Test
    public void testThread() {
        Map<String, Object> config = Maps.newHashMap();
        config.put("broadcastURI", "http://fakeurl.com/fake");

        BroadcastMonitorThread thread = new BroadcastMonitorThread(config);
        thread.setDefaultWaitTime(30000L);
        long testRunLength = thread.getDefaultWaitTime() * 1;
        executor = Executors.newFixedThreadPool(1);
        executor.submit(thread);

        try {
            Thread.sleep(testRunLength);
        } catch(InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        executor.shutdown();
    }

}
