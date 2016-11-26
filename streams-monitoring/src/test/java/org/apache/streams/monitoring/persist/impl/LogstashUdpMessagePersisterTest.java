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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class LogstashUdpMessagePersisterTest {

  private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(LogstashUdpMessagePersisterTest.class);

  private DatagramSocket socket = null;

  /**
   * setup.
   */
  @Before
  public void setup() {
    try {
      socket = new DatagramSocket(56789);
    } catch (SocketException ex) {
      LOGGER.error("Metrics Broadcast Test Setup Failed: " + ex.getMessage());
    }
  }


  @Test
  public void testFailedPersist() {
    LogstashUdpMessagePersister persister = new LogstashUdpMessagePersister("udp://127.0.0.1:56789");

    List<String> messageArray = new ArrayList<>();
    for (int x = 0; x < 10; x ++) {
      messageArray.add("Fake_message #" + x);
    }

    persister.persistMessages(messageArray);
    byte[] receiveData = new byte[1024];

    DatagramPacket messageDatagram = new DatagramPacket(receiveData, receiveData.length);

    try {
      socket.receive(messageDatagram);
      assertNotNull(messageDatagram);
      List<String> messages = Lists.newArrayList(Splitter.on('\n').split(new String(messageDatagram.getData())));
      assertEquals(messageArray, messages.subList(0,10));
    } catch (IOException ex) {
      LOGGER.error("Metrics Broadcast Test Failed: " + ex.getMessage());
    }

  }

}
