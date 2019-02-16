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

import org.apache.streams.twitter.pojo.User;

import org.apache.juneau.http.annotation.Body;
import org.apache.juneau.remote.RemoteInterface;
import org.apache.juneau.rest.client.remote.RemoteMethod;

/**
 * Interface for /account methods.
 */
@RemoteInterface(path = "https://api.twitter.com/1.1/account")
public interface Account {

  /**
   * Returns settings (including current trend, geo and sleep time information) for the authenticating user.
   *
   * @return {@link org.apache.streams.twitter.api.AccountSettings}
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/manage-account-settings/api-reference/get-account-settings">https://developer.twitter.com/en/docs/accounts-and-users/manage-account-settings/api-reference/get-account-settings</a>
   *
   */
  @RemoteMethod(method = "GET", path = "/settings.json")
  public AccountSettings settings();

  /**
   * Returns user credentials for the authenticating user.
   *
   * @return {@link org.apache.streams.twitter.pojo.User}
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/manage-account-settings/api-reference/get-account-verify_credentials">https://developer.twitter.com/en/docs/accounts-and-users/manage-account-settings/api-reference/get-account-verify_credentials</a>
   *
   */
  @RemoteMethod(method = "GET", path = "/verify_credentials.json")
  public User verifyCredentials();

  /**
   * Sets some values that users are able to set under the “Account” tab of their settings page. Only the parameters specified will be updated.
   *
   * @return {@link org.apache.streams.twitter.pojo.User}
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/manage-account-settings/api-reference/post-account-update_profile">https://developer.twitter.com/en/docs/accounts-and-users/manage-account-settings/api-reference/post-account-update_profile</a>
   *
   */
  @RemoteMethod(method = "POST", path = "/update_profile.json")
  public User updateProfile(@Body UpdateProfileRequest parameters);

  /**
   * Updates the authenticating user’s settings.
   *
   * @return {@link org.apache.streams.twitter.api.AccountSettings}
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/manage-account-settings/api-reference/post-account-settings">https://developer.twitter.com/en/docs/accounts-and-users/manage-account-settings/api-reference/post-account-settings</a>
   *
   */
  @RemoteMethod(method = "POST", path = "/update_settings.json")
  public AccountSettings updateSettings(@Body UpdateProfileRequest parameters);

}
