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

import org.apache.streams.twitter.pojo.WelcomeMessageRule;

import org.apache.juneau.http.annotation.Content;
import org.apache.juneau.http.annotation.Query;
import org.apache.juneau.http.remote.Remote;
import org.apache.juneau.http.remote.RemoteDelete;
import org.apache.juneau.http.remote.RemoteGet;
import org.apache.juneau.http.remote.RemotePost;

/**
 * Interface for /direct_messages/welcome_messages/rules methods.
 *
 * @see <a href="https://dev.twitter.com/rest/reference">https://dev.twitter.com/rest/reference</a>
 */
@Remote(path = "https://api.twitter.com/1.1/direct_messages/welcome_messages/rules")
public interface WelcomeMessageRules {

  /**
   * Returns a list of Welcome Message Rules.
   *
   * @return {@link org.apache.streams.twitter.api.WelcomeMessageRulesListResponse}
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/list-welcome-message-rules">https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/list-welcome-message-rules</a>
   *
   */
  @RemoteGet(path = "/list.json")
  public WelcomeMessageRulesListResponse listWelcomeMessageRules(@Query(name = "*") WelcomeMessageRulesListRequest parameters);

  /**
   * Returns a Welcome Message Rule by the given id.
   *
   * @return {@link org.apache.streams.twitter.pojo.WelcomeMessageRule}
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/get-welcome-message-rule">https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/get-welcome-message-rule</a>
   *
   */
  @RemoteGet(path = "/show.json")
  public WelcomeMessageRule showWelcomeMessageRule(@Query("id") Long id);

  /**
   * Creates a new Welcome Message Rule that determines which Welcome Message will be shown in a given conversation. Returns the created rule if successful.
   *
   * Requires a JSON POST body and Content-Type header to be set to application/json. Setting Content-Length may also be required if it is not automatically.
   *
   * Additional rule configurations are forthcoming. For the initial beta release, the most recently created Rule will always take precedence, and the assigned Welcome Message will be displayed in the conversation.
   *
   * @return {@link org.apache.streams.twitter.pojo.WelcomeMessageRule}
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/new-welcome-message-rule">https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/new-welcome-message-rule</a>
   *
   */
  @RemotePost(path = "/new.json")
  public WelcomeMessageRule newWelcomeMessageRule(@Content WelcomeMessageNewRuleRequest body);

  /**
   * Deletes a Welcome Message Rule by the given id.
   *
   * @see <a href="https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/delete-welcome-message-rule">https://developer.twitter.com/en/docs/direct-messages/welcome-messages/api-reference/delete-welcome-message-rule</a>
   *
   */
  @RemoteDelete(path = "/destroy.json")
  public void destroyWelcomeMessageRule(@Query("id") Long id);

}
