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

import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.local.monitoring.MonitoringConfiguration;

import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BroadcastMonitorThreadTest {

  private ExecutorService executor;

  @Test
  public void testThreadEmptyBeanConfig() {
    StreamsConfiguration streamsConfiguration = StreamsConfigurator.detectConfiguration();
    BroadcastMonitorThread thread = new BroadcastMonitorThread(streamsConfiguration);
    testThread(thread);
  }




  @Test
  public void testThreadStreamsConfig() {

    StreamsConfiguration streams = new StreamsConfiguration();
    MonitoringConfiguration monitoring = new MonitoringConfiguration();
    monitoring.setBroadcastURI("http://fakeurl.com/fake");
    monitoring.setMonitoringBroadcastIntervalMs(30000L);
    streams.setAdditionalProperty("monitoring", monitoring);
    BroadcastMonitorThread thread = new BroadcastMonitorThread(streams);
    testThread(thread);
  }

  /**
   * Base Test.
   * @param thread BroadcastMonitorThread
   */
  public void testThread(BroadcastMonitorThread thread) {
    long testRunLength = thread.getWaitTime() * 1;
    executor = Executors.newFixedThreadPool(1);
    executor.submit(thread);

    try {
      Thread.sleep(testRunLength);
    } catch (InterruptedException ex) {
      Thread.currentThread().interrupt();
    }

    executor.shutdown();
  }

}
