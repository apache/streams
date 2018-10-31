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

import org.apache.streams.twitter.pojo.WelcomeMessage;

import org.apache.juneau.http.annotation.Body;
import org.apache.juneau.http.annotation.Query;
import org.apache.juneau.remote.RemoteInterface;
import org.apache.juneau.rest.client.remote.RemoteMethod;

/**
 * Interface for /direct_messages/welcome_messages methods.
 *
 * @see <a href="https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference">https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference</a>
 */
@RemoteInterface(path = "https://api.twitter.com/1.1/direct_messages/welcome_messages")
public interface WelcomeMessages {

  /**
   * Returns a list of Welcome Messages.
   *
   * @return {@link org.apache.streams.twitter.api.WelcomeMessagesListResponse}
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/list-welcome-messages">https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/list-welcome-messages</a>
   *
   */
  @RemoteMethod(method ="GET", path = "/list.json")
  public WelcomeMessagesListResponse listWelcomeMessages(@Query(name = "*", skipIfEmpty = true) WelcomeMessagesListRequest parameters);

  /**
   * Returns a Welcome Message by the given id.
   *
   * @return {@link org.apache.streams.twitter.pojo.WelcomeMessage}
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/get-welcome-message">https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/get-welcome-message</a>
   *
   */
  @RemoteMethod(method ="GET", path = "/show.json")
  public WelcomeMessage showWelcomeMessage(@Query("id") Long id);

  /**
   * Creates a new Welcome Message that will be stored and sent in the future from the authenticating user in defined circumstances. Returns the message template in the requested format if successful. Supports publishing with the same elements as Direct Messages (e.g. Quick Replies, media attachments).
   *
   * @return {@link org.apache.streams.twitter.pojo.WelcomeMessage}
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/new-welcome-message">https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/new-welcome-message</a>
   *
   */
  @RemoteMethod(method ="POST", path = "/new.json")
  public WelcomeMessageNewResponse newWelcomeMessage(@Body WelcomeMessageNewRequest messageNewRequest);

  /**
   * Deletes a Welcome Message by the given id.
   *
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/delete-welcome-message">https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/delete-welcome-message</a>
   */
  @RemoteMethod(method ="DELETE", path = "/destroy.json")
  public void destroyWelcomeMessage(@Query("id") Long id);

}
