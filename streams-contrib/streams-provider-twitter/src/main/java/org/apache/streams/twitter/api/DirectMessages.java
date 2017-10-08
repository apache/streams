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

import org.apache.streams.twitter.pojo.DirectMessageEvent;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.pojo.WelcomeMessage;

import org.apache.juneau.remoteable.Body;
import org.apache.juneau.remoteable.Query;
import org.apache.juneau.remoteable.QueryIfNE;
import org.apache.juneau.remoteable.RemoteMethod;
import org.apache.juneau.remoteable.Remoteable;
import org.apache.juneau.remoteable.RequestBean;

/**
 * Interface for /direct_messages methods.
 *
 * @see <a href="https://dev.twitter.com/rest/reference">https://dev.twitter.com/rest/reference</a>
 */
@Remoteable(path = "https://api.twitter.com/1.1/direct_messages")
public interface DirectMessages {

  /**
   * Returns all Direct Message events (both sent and received) within the last 30 days. Sorted in reverse-chronological order.
   *
   * @return {@link org.apache.streams.twitter.api.EventsListResponse}
   * @see <a href="https://dev.twitter.com/rest/reference/get/direct_messages/events/list">https://dev.twitter.com/rest/reference/get/direct_messages/events/list</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/events/list.json")
  public EventsListResponse listEvents(@QueryIfNE EventsListRequest parameters);

  /**
   * Returns a single Direct Message event by the given id.
   *
   * @return {@link org.apache.streams.twitter.api.EventShowResponse}
   * @see <a href="https://dev.twitter.com/rest/reference/get/direct_messages/events/show">https://dev.twitter.com/rest/reference/get/direct_messages/events/show</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/events/show.json")
  public EventShowResponse showEvent(@Query("id") Long id);

  /**
   * Publishes a new message_create event resulting in a Direct Message sent to a specified user from the authenticating user. Returns an event in the requested format if successful.
   *
   * @return {@link org.apache.streams.twitter.pojo.DirectMessageEvent}
   * @see <a href="https://dev.twitter.com/rest/reference/get/direct_messages/events/new">https://dev.twitter.com/rest/reference/get/direct_messages/events/new</a>
   *
   */
  @RemoteMethod(httpMethod = "POST", path = "/events/new.json")
  public DirectMessageEvent newEvent(@Body MessageCreateRequest event);

}
