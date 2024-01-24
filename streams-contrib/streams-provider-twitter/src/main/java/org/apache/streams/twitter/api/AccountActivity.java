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

import org.apache.juneau.http.annotation.Path;
import org.apache.juneau.http.annotation.Query;
import org.apache.juneau.http.remote.Remote;
import org.apache.juneau.http.remote.RemoteDelete;
import org.apache.juneau.http.remote.RemoteGet;
import org.apache.juneau.http.remote.RemotePost;
import org.apache.juneau.http.remote.RemotePut;
import org.apache.juneau.rest.client.RestCallException;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * Interface for /account_activity methods.
 *
 * @see <a href="https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/overview">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/overview</a>
 */
@Remote(path = "https://api.twitter.com/1.1/account_activity")
public interface AccountActivity {

  /**
   * Returns all environments, webhook URLs and their statuses for the authenticating app. Currently, only one webhook URL can be registered to each environment.
   *
   * We mark a URL as invalid if it fails the daily validation check. In order to re-enable the URL, call the update endpoint.
   *
   * @return WebhooksResponse
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-webhook-config">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-webhook-config</a>
   *
   */
  @RemoteGet(path = "/all/webhooks.json")
  public WebhooksResponse getWebhooks();

  /**
   * Alternatively, this endpoint can be used with an environment name to only return webhook URLS for the given environment: GET account_activity/all/:env_name/webhooks (see second example)
   *
   * @param env_name Environment Name
   * @return List\<Webhook\>
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-webhook-config">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-webhook-config</a>
   *
   */
  @RemoteGet(path = "/all/{env_name}/webhooks.json")
  public List<Webhook> getWebhooks(@Path("env_name") String env_name);

  /**
   * Registers a new webhook URL for the given application context. The URL will be validated via CRC request before saving. In case the validation fails, a comprehensive error is returned. message to the requester.
   *
   * Only one webhook URL can be registered to an application.
   *
   * @param env_name Environment Name
   * @param url Webhook URL
   * @return Webhook
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/new-webhook-config">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/new-webhook-config</a>
   *
   */
  @RemotePost(path = "/all/{env_name}/webhooks.json")
  public Webhook registerWebhook(@Path("env_name") String env_name, @Query("url") String url);

  /**
   * Removes the webhook from the provided application’s configuration. The webhook ID can be accessed by making a call to GET /1.1/account_activity/webhooks.
   *
   * @param env_name Environment Name
   * @param webhookId Webhook ID. Defined in resource path.
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/delete-webhook-config">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/delete-webhook-config</a>
   *
   */
  @RemoteDelete(path = "/all/{env_name}/webhooks/{webhookId}.json")
  public Boolean deleteWebhook(@Path("env_name") String env_name, @Path("webhookId") Long webhookId);

  /**
   * Triggers the challenge response check (CRC) for the given webhook’s URL. If the check is successful, returns 204 and reenables the webhook by setting its status to valid.
   *
   * @param env_name Environment Name
   * @param webhookId Webhook ID. Defined in resource path.
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/aaa-premium</a>
   *
   */
  @RemotePut(path = "/all/{env_name}/webhooks/{webhookId}.json")
  public Boolean putWebhook(@Path("env_name") String env_name, @Path("webhookId") Long webhookId);

  /**
   * Triggers the challenge response check (CRC) for the given enviroments webhook for all activites. If the check is successful, returns 204 and reenables the webhook by setting its status to valid.
   *
   * @param env_name Environment Name
   * @param webhookId Webhook ID. Defined in resource path.
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/validate-webhook-config">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/validate-webhook-config</a>
   */
  @RemotePut(path = "/all/{env_name}/webhooks/{webhookId}.json")
  public Boolean reenableWebhook(@Path("env_name") String env_name, @Path("webhookId") Long webhookId) throws InvocationTargetException, RestCallException;

  /**
   * Returns the count of subscriptions that are currently active on your account for all activities. Note that the /count endpoint requires application-only OAuth, so that you should make requests using a bearer token instead of user context.
   *
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-subscription">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-subscription</a>
   *
   */
  @RemoteGet(path = "/all/subscriptions/count.json")
  public SubscriptionsCountResponse getSubscriptionsCount() throws InvocationTargetException, RestCallException;

  /**
   * Returns a list of the current All Activity type subscriptions. Note that the /list endpoint requires application-only OAuth, so requests should be made using a bearer token instead of user context.
   *
   * @param env_name Environment Name
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-subscription">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-subscription</a>
   *
   */
  @RemoteGet(path = "/all/{env_name}/subscriptions/list.json")
  public SubscriptionsListResponse getSubscriptionsList(@Path("env_name") String env_name) throws InvocationTargetException, RestCallException;

  /**
   * Provides a way to determine if a webhook configuration is subscribed to the provided user’s events. If the provided user context has an active subscription with provided application, returns 204 OK. If the response code is not 204, then the user does not have an active subscription. See HTTP Response code and error messages below for details.
   *
   * @param env_name Environment Name
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-subscription">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/get-subscription</a>
   *
   */
  @RemoteGet(path = "/all/{env_name}/subscriptions.json")
  public Boolean getSubscriptions(@Path("env_name") String env_name) throws InvocationTargetException, RestCallException;

  /**
   * Subscribes the provided application to all events for the provided environment for all message types. After activation, all events for the requesting user will be sent to the application’s webhook via POST request.
   *
   * @param env_name Environment Name
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/new-subscription">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/new-subscription</a>
   *
   */
  @RemotePost(path = "/all/{env_name}/subscriptions.json")
  public Boolean newSubscription(@Path("env_name") String env_name) throws InvocationTargetException, RestCallException;

  /**
   * Deactivates subscription for the specified webhook and user id. After deactivation, all events for the requesting user will no longer be sent to the webhook URL. Note, that this endpoint requires application-only OAuth, so requests should be made using a bearer token instead of user context.
   *
   * @param env_name Environment Name
   * @param user_id User ID
   * @return Boolean
   * @see <a href=https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/delete-subscription">https://developer.twitter.com/en/docs/accounts-and-users/subscribe-account-activity/api-reference/delete-subscription</a>
   *
   */
  @RemoteDelete(path = "/all/{env_name}/subscriptions/{user_id}.json")
  public Boolean deleteWebhookSubscriptions(@Path("env_name") String env_name, @Path("user_id") String user_id) throws InvocationTargetException, RestCallException;

}
