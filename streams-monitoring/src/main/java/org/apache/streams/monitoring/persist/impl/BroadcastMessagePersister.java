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

import com.google.common.collect.Lists;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class BroadcastMessagePersister implements MessagePersister {

  private static final Logger LOGGER = LoggerFactory.getLogger(BroadcastMessagePersister.class);
  private String broadcastUri;

  public BroadcastMessagePersister(String broadcastUri) {
    this.broadcastUri = broadcastUri;
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
      try {
        HttpClient client = HttpClients.createDefault();
        HttpPost post = new HttpPost(broadcastUri);

        post.setHeader("User-Agent", "Streams");

        List<NameValuePair> urlParameters = Lists.newArrayList();
        urlParameters.add(new BasicNameValuePair("messages", serializeMessages(messages)));

        post.setEntity(new UrlEncodedFormEntity(urlParameters, "UTF-8"));

        HttpResponse response = client.execute(post);
        responseCode = response.getStatusLine().getStatusCode();

        LOGGER.debug("Broadcast {} messages to URI: {}", messages.size(), broadcastUri);
      } catch (Exception ex) {
        LOGGER.error("Failed to broadcast message to URI: {}, exception: {}", broadcastUri, ex);
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
    String ser = "{\"messages\":[";

    for (String message : messages) {
      if (messages.get(messages.size() - 1).equals(message)) {
        ser += message + "]}";
      } else {
        ser += message + ",";
      }
    }

    return ser;
  }
}
