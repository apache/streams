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

import org.apache.streams.monitoring.persist.MessagePersister;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;

public class LogstashUdpMessagePersister implements MessagePersister {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogstashUdpMessagePersister.class);
  private String broadcastUri;
  URI uri;

  public LogstashUdpMessagePersister(String broadcastUri) {
    this.broadcastUri = broadcastUri;
    setup();
  }

  /**
   * setup.
   */
  public void setup() {

    try {
      uri = new URI(broadcastUri);
    } catch (URISyntaxException ex) {
      LOGGER.error(ex.getMessage());
    }

  }

  @Override
  /**
   * Given a list of messages as Strings, broadcast them to the broadcastUri
   * (if one is defined)
   * @param messages
   * @return int status code from POST response
   */
  public int persistMessages(List<String> messages) {
    int responseCode = -1;

    if (broadcastUri != null) {
      DatagramSocket socket = null;
      try {
        socket = new DatagramSocket();
      } catch (SocketException ex) {
        LOGGER.error("Metrics Broadcast Setup Failed: " + ex.getMessage());
      }
      try {
        ByteBuffer toWrite = ByteBuffer.wrap(serializeMessages(messages).getBytes());
        byte[] byteArray = toWrite.array();
        DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length);
        socket.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));
        socket.send(packet);
      } catch ( Exception ex ) {
        LOGGER.error("Metrics Broadcast Failed: " + ex.getMessage());
      } finally {
        socket.close();
      }
    }

    return responseCode;
  }

  /**
   * Given a List of String messages, convert them to a JSON array.
   * @param messages List of String messages
   * @return Serialized version of this JSON array
   */
  private String serializeMessages(List<String> messages) {

    StringBuilder jsonLines = new StringBuilder();
    for (String message : messages) {
      jsonLines.append(message).append('\n');
    }

    return jsonLines.toString();
  }

}
