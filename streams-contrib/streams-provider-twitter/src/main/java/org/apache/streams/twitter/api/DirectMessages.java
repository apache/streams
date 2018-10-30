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

package org.apache.streams.twitter.api;

import org.apache.streams.twitter.pojo.DirectMessage;
import org.apache.streams.twitter.pojo.DirectMessageEvent;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.pojo.WelcomeMessage;

import org.apache.juneau.http.annotation.Body;
import org.apache.juneau.http.annotation.Query;
import org.apache.juneau.remote.RemoteInterface;
import org.apache.juneau.rest.client.remote.RemoteMethod;

import java.util.List;

/**
 * Interface for /direct_messages methods.
 *
 * @see <a href="https://dev.twitter.com/rest/reference">https://dev.twitter.com/rest/reference</a>
 */
@RemoteInterface(path = "https://api.twitter.com/1.1/direct_messages")
public interface DirectMessages {

  /**
   * Returns all Direct Message events (both sent and received) within the last 30 days. Sorted in reverse-chronological order.
   *
   * @param parameters {@link org.apache.streams.twitter.api.EventsListRequest}
   * @return {@link org.apache.streams.twitter.api.EventsListResponse}
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/list-events">https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/list-events</a>
   *
   */
  @RemoteMethod(method = "GET", path = "/events/list.json")
  public EventsListResponse listEvents(@Query(name = "*", skipIfEmpty = true) EventsListRequest parameters);

  /**
   * Returns a single Direct Message event by the given id.
   *
   * @return {@link org.apache.streams.twitter.api.EventShowResponse}
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/get-event">https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/get-event</a>
   *
   */
  @RemoteMethod(method = "GET", path = "/events/show.json")
  public EventShowResponse showEvent(@Query("id") Long id);

  /**
   * Destroys the direct message specified in the required ID parameter. The authenticating user must be the recipient of the specified direct message.
   *
   * @param event {@link org.apache.streams.twitter.api.MessageCreateRequest}
   * @return {@link org.apache.streams.twitter.pojo.DirectMessageEvent}
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/new-event">https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/new-event</a>
   *
   */
  @RemoteMethod(method = "POST", path = "/events/new.json")
  public DirectMessageEvent newEvent(@Body MessageCreateRequest event);

  /**
   * Publishes a new message_create event resulting in a Direct Message sent to a specified user from the authenticating user. Returns an event in the requested format if successful.
   *
   * @param id The ID of the direct message to delete.
   * @return {@link org.apache.streams.twitter.pojo.DirectMessage}
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/new-event">https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/new-event</a>
   *
   */
  @RemoteMethod(method = "POST", path = "/destroy.json")
  public DirectMessage destroy(@Query("id") Long id);

  /**
   * The ID of the direct message.
   *
   * @param id The ID of the direct message to delete.
   * @return {@link org.apache.streams.twitter.pojo.DirectMessage}
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/new-event">https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/new-event</a>
   *
   */
  @RemoteMethod(method = "GET", path = "/show.json")
  public DirectMessage show(@Query("id") Long id);

  /**
   * Returns the 20 most recent direct messages sent to the authenticating user. Includes detailed information about the sender and recipient user. You can request up to 200 direct messages per call, and only the most recent 200 DMs will be available using this endpoint.
   *
   * @param parameters {@link org.apache.streams.twitter.api.DirectMessagesListRequest}
   * @return {@link java.util.List}[{@link org.apache.streams.twitter.pojo.DirectMessage}]
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/get-messages">https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/get-messages</a>
   *
   */
  @RemoteMethod(method = "GET", path = ".json")
  public List<DirectMessage> list(@Query(name = "*", skipIfEmpty = true) DirectMessagesListRequest parameters);

  /**
   * Returns the 20 most recent direct messages sent to the authenticating user. Includes detailed information about the sender and recipient user. You can request up to 200 direct messages per call, and only the most recent 200 DMs will be available using this endpoint.
   *
   * @param parameters {@link org.apache.streams.twitter.api.DirectMessagesSentRequest}
   * @return {@link java.util.List}[{@link org.apache.streams.twitter.pojo.DirectMessage}]
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/get-sent-message">https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/get-sent-message</a>
   *
   */
  @RemoteMethod(method = "GET", path = "/sent.json")
  public List<DirectMessage> sent(@Query(name = "*", skipIfEmpty = true) DirectMessagesSentRequest parameters);

  /**
   * Destroys the direct message specified in the required ID parameter. The authenticating user must be the recipient of the specified direct message.
   *
   * @param parameters {@link org.apache.streams.twitter.api.DirectMessageNewRequest}
   * @return {@link org.apache.streams.twitter.pojo.DirectMessage}
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/new-event">https://developer.twitter.com/en/docs/direct-messages/sending-and-receiving/api-reference/new-event</a>
   *
   */
  @RemoteMethod(method = "POST", path = "/new.json")
  public DirectMessage newDM(@Body DirectMessageNewRequest parameters);
}
