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
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.Executors;

public class LogstashUdpMessagePersister implements MessagePersister {

    private final static Logger LOGGER = LoggerFactory.getLogger(LogstashUdpMessagePersister.class);
    private String broadcastURI;
    URI uri;

    public LogstashUdpMessagePersister(String broadcastURI) {
        this.broadcastURI = broadcastURI;
        setup();
    }

    public void setup() {

        try {
            uri = new URI(broadcastURI);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

    }
    @Override
    /**
     * Given a list of messages as Strings, broadcast them to the broadcastURI
     * (if one is defined)
     * @param messages
     * @return int status code from POST response
     */
    public int persistMessages(List<String> messages) {
        int responseCode = -1;

        if(broadcastURI != null) {
            DatagramSocket socket = null;
            try {
                socket = new DatagramSocket();
            } catch (SocketException e) {
                LOGGER.error("Metrics Broadcast Setup Failed: " + e.getMessage());
            }
            try {
                ByteBuffer toWrite = ByteBuffer.wrap(serializeMessages(messages).getBytes());
                byte[] byteArray = toWrite.array();
                DatagramPacket packet = new DatagramPacket(byteArray, byteArray.length);
                socket.connect(new InetSocketAddress(uri.getHost(), uri.getPort()));
                socket.send(packet);
            } catch( Exception e ) {
                LOGGER.error("Metrics Broadcast Failed: " + e.getMessage());
            } finally {
                socket.close();
            }
        }

        return responseCode;
    }

    /**
     * Given a List of String messages, convert them to a JSON array
     * @param messages
     * @return Serialized version of this JSON array
     */
    private String serializeMessages(List<String> messages) {

        StringBuilder json_lines = new StringBuilder();
        for(String message : messages) {
            json_lines.append(message).append('\n');
        }

        return json_lines.toString();
    }

}
