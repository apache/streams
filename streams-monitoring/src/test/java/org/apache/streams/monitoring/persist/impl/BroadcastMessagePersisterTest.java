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

package org.apache.streams.monitoring.persist.impl;

import com.google.common.collect.Lists;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

public class BroadcastMessagePersisterTest {

  @Test
  public void testFailedPersist() {
    BroadcastMessagePersister persister = new BroadcastMessagePersister("http://fake.url.com/fake_endpointasdfasdfas");

    List<String> messages = Lists.newArrayList();
    for (int x = 0; x < 10; x++) {
      messages.add("Fake_message #" + x);
    }

    int statusCode = persister.persistMessages(messages);

    assertNotNull(statusCode);
    assertNotEquals(statusCode, 200);
  }

  @Test
  public void testInvalidUrl() {
    BroadcastMessagePersister persister = new BroadcastMessagePersister("h");

    List<String> messages = Lists.newArrayList();
    for (int x = 0; x < 10; x++) {
      messages.add("Fake_message #" + x);
    }

    int statusCode = persister.persistMessages(messages);

    assertNotNull(statusCode);
    assertEquals(statusCode, -1);
  }
}
