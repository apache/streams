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

import org.apache.juneau.remoteable.Path;
import org.apache.juneau.remoteable.Query;
import org.apache.juneau.remoteable.RemoteMethod;
import org.apache.juneau.remoteable.Remoteable;
import org.apache.juneau.rest.client.RestCallException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Interface for /account_activity methods.
 *
 * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/overview">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/overview</a>
 */
@Remoteable(path = "https://api.twitter.com/1.1/account_activity")
public interface AccountActivity {

  /**
   * Returns all URLs and their statuses for the given app. Currently, only one webhook URL can be registered to an application.
   *
   * We mark a URL as invalid if it fails the daily validation check. In order to re-enable the URL, call the update endpoint.
   *
   * @return List\<Webhook\>
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-webhook-config">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-webhook-config</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/webhooks.json")
  public List<Webhook> getWebhooks();

  /**
   * Registers a new webhook URL for the given application context. The URL will be validated via CRC request before saving. In case the validation fails, a comprehensive error is returned. message to the requester.
   *
   * Only one webhook URL can be registered to an application.
   *
   * @return Webhook
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/new-webhook-config">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/new-webhook-config</a>
   *
   */
  @RemoteMethod(httpMethod = "POST", path = "/webhooks.json")
  public Webhook registerWebhook(@Query("url") String url);

  /**
   * Removes the webhook from the provided application’s configuration. The webhook ID can be accessed by making a call to GET /1.1/account_activity/webhooks.
   *
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/delete-webhook-config">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/delete-webhook-config</a>
   *
   */
  @RemoteMethod(httpMethod = "DELETE", path = "/webhooks/{webhookId}.json")
  public Boolean deleteWebhook(@Path("webhookId") Long webhookId);

  /**
   * Triggers the challenge response check (CRC) for the given webhook’s URL. If the check is successful, returns 204 and reenables the webhook by setting its status to valid.
   *
   * @param webhookId Webhook ID. Defined in resource path.
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/validate-webhook-config">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/validate-webhook-config</a>
   *
   */
  @RemoteMethod(httpMethod = "PUT", path = "/webhooks/{webhookId}.json")
  public Boolean putWebhook(@Path("webhookId") Long webhookId);

  /**
   * Provides a way to determine if a webhook configuration is subscribed to the provided user’s Direct Messages. If the provided user context has an active subscription with the provided app, returns 204 OK. If the response code is not 204, then the user does not have an active subscription. See HTTP Response code and error messages below for details.
   *
   * @param webhookId Webhook ID. Defined in resource path.
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-subscription">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-subscription</a>
   *
   */
  @RemoteMethod(httpMethod = "GET", path = "/webhooks/{webhookId}/subscriptions.json")
  public Boolean getWebhookSubscription(@Path("webhookId") Long webhookId)
      throws InvocationTargetException, RestCallException
  ;

  /**
   * Subscribes the provided app to events for the provided user context. When subscribed, all DM events for the provided user will be sent to the app’s webhook via POST request.
   *
   * @param webhookId Webhook ID. Defined in resource path.
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/new-subscription">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/new-subscription</a>
   *
   */
  @RemoteMethod(httpMethod = "POST", path = "/webhooks/{webhookId}/subscriptions.json")
  public Boolean registerWebhookSubscriptions(@Path("webhookId") Long webhookId)
      throws InvocationTargetException, RestCallException
  ;

  /**
   * Deactivates subscription for the provided user context and app. After deactivation, all DM events for the requesting user will no longer be sent to the webhook URL.
   *
   * @param webhookId Webhook ID. Defined in resource path.
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/delete-subscription">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/delete-subscription</a>
   *
   */
  @RemoteMethod(httpMethod = "DELETE", path = "/webhooks/{webhookId}/subscriptions.json")
  public Boolean deleteWebhookSubscriptions(@Path("webhookId") Long webhookId)
      throws InvocationTargetException, RestCallException
  ;


}
