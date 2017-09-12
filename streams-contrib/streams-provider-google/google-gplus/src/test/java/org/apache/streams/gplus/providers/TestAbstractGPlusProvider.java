/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.gplus.providers;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.google.gplus.GPlusConfiguration;
import org.apache.streams.google.gplus.GPlusOAuthConfiguration;
import org.apache.streams.google.gplus.configuration.UserInfo;
import org.apache.streams.gplus.provider.AbstractGPlusProvider;
import org.apache.streams.util.api.requests.backoff.BackOffStrategy;

import com.carrotsearch.randomizedtesting.RandomizedTest;
import com.carrotsearch.randomizedtesting.annotations.Repeat;
import com.google.api.services.plus.Plus;
import org.junit.Test;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import static org.mockito.Mockito.mock;

import org.junit.Assert;


/**
 * Unit tests for {@link org.apache.streams.gplus.provider.AbstractGPlusProvider}
 */
public class TestAbstractGPlusProvider extends RandomizedTest {

  /**
   * Test that every collector will be run and that data queued from the collectors will be processed.
   */
  @Test
  @Repeat(iterations = 3)
  public void testDataCollectorRunsPerUser() {
    int numUsers = randomIntBetween(1, 1000);
    List<UserInfo> userList = new LinkedList<>();
    for (int i = 0; i < numUsers; ++i) {
      userList.add(new UserInfo());
    }
    GPlusConfiguration config = new GPlusConfiguration();
    GPlusOAuthConfiguration oauth = new GPlusOAuthConfiguration();
    oauth.setAppName("a");
    oauth.setPathToP12KeyFile("a");
    oauth.setServiceAccountEmailAddress("a");
    config.setOauth(oauth);
    config.setGooglePlusUsers(userList);
    AbstractGPlusProvider provider = new AbstractGPlusProvider(config) {

      @Override
      protected Plus createPlusClient() throws IOException {
        return mock(Plus.class);
      }

      @Override
      protected Runnable getDataCollector(BackOffStrategy strategy, BlockingQueue<StreamsDatum> queue, Plus plus, UserInfo userInfo) {
        final BlockingQueue<StreamsDatum> q = queue;
        return () -> {
          try {
            q.put(new StreamsDatum(null));
          } catch (InterruptedException ie) {
            Assert.fail("Test was interrupted");
          }
        };
      }
    };

    try {
      provider.prepare(null);
      provider.startStream();
      int datumCount = 0;
      while (provider.isRunning()) {
        datumCount += provider.readCurrent().size();
      }
      Assert.assertEquals(numUsers, datumCount);
    } finally {
      provider.cleanUp();
    }
  }


}
