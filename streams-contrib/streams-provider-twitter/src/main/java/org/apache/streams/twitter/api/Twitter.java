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

import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.TwitterConfiguration;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.converter.TwitterJodaDateSwap;
import org.apache.streams.twitter.pojo.DirectMessageEvent;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.pojo.WelcomeMessage;
import org.apache.streams.twitter.pojo.WelcomeMessageRule;
import org.apache.streams.twitter.provider.TwitterProviderUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.rest.client.RestCall;
import org.apache.juneau.rest.client.RestCallException;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

//import org.apache.juneau.rest.client.RestClientBuilder;

/**
 * Implementation of all twitter interfaces using juneau.
 */
public class Twitter implements Account, AccountActivity, DirectMessages, Favorites, Followers, Friends, Statuses, Users, WelcomeMessages, WelcomeMessageRules {

  private static final Logger LOGGER = LoggerFactory.getLogger(Twitter.class);

  private static Map<TwitterConfiguration, Twitter> INSTANCE_MAP = new ConcurrentHashMap<>();

  private TwitterConfiguration configuration;

  private ObjectMapper mapper;

  private String rootUrl;

  private CloseableHttpClient httpclient;

  private HttpRequestInterceptor oauthInterceptor;

  private static Map<String,Object> properties = new HashMap<String,Object>();

  static {
    properties.put("format", TwitterDateTimeFormat.TWITTER_FORMAT);
  }

  RestClient restClient;

  private Twitter(TwitterConfiguration configuration) throws InstantiationException {
    this.configuration = configuration;
    this.rootUrl = TwitterProviderUtil.baseUrl(configuration);
    this.oauthInterceptor = new TwitterOAuthRequestInterceptor(configuration.getOauth());
    this.httpclient = HttpClientBuilder.create()
        .setDefaultRequestConfig(
            RequestConfig.custom()
                .setConnectionRequestTimeout(5000)
                .setConnectTimeout(5000)
                .setSocketTimeout(5000)
                .setCookieSpec("easy")
                .build()
        )
        .setMaxConnPerRoute(20)
        .setMaxConnTotal(100)
        .addInterceptorFirst(oauthInterceptor)
        .addInterceptorLast((HttpRequestInterceptor) (httpRequest, httpContext) -> LOGGER.debug(httpRequest.getRequestLine().getUri()))
        .addInterceptorLast((HttpResponseInterceptor) (httpResponse, httpContext) -> LOGGER.debug(httpResponse.getStatusLine().toString()))
        .build();
    this.restClient = new RestClientBuilder()
        .debug()
        .httpClient(httpclient, true)
        .parser(
            JsonParser.DEFAULT.builder()
                .ignoreUnknownBeanProperties(true)
                .pojoSwaps(TwitterJodaDateSwap.class)
                .build())
        .serializer(
            JsonSerializer.DEFAULT.builder()
                .pojoSwaps(TwitterJodaDateSwap.class)
                .trimEmptyCollections(true)
                .trimEmptyMaps(true)
                .build())
        .rootUrl(rootUrl)
        .retryable(
            configuration.getRetryMax().intValue(),
            configuration.getRetrySleepMs(),
            new TwitterRetryHandler())
        .build();
    this.mapper = StreamsJacksonMapper.getInstance();
  }

  public static Twitter getInstance(TwitterConfiguration configuration) throws InstantiationException {
    if (INSTANCE_MAP.containsKey(configuration) && INSTANCE_MAP.get(configuration) != null) {
      return INSTANCE_MAP.get(configuration);
    } else {
      Twitter twitter = new Twitter(configuration);
      INSTANCE_MAP.put(configuration, twitter);
      return INSTANCE_MAP.get(configuration);
    }
  }

  @Override
  public List<Tweet> userTimeline(StatusesUserTimelineRequest parameters) {
    Statuses restStatuses = restClient.getRemoteableProxy(Statuses.class, TwitterProviderUtil.baseUrl(configuration)+"/statuses");
    List<Tweet> result = restStatuses.userTimeline(parameters);
    return result;
  }

  @Override
  public List<Tweet> homeTimeline(StatusesHomeTimelineRequest parameters) {
    Statuses restStatuses = restClient.getRemoteableProxy(Statuses.class, TwitterProviderUtil.baseUrl(configuration)+"/statuses");
    List<Tweet> result = restStatuses.homeTimeline(parameters);
    return result;
  }

  @Override
  public List<Tweet> lookup(StatusesLookupRequest parameters) {
    Statuses restStatuses = restClient.getRemoteableProxy(Statuses.class, TwitterProviderUtil.baseUrl(configuration)+"/statuses");
    List<Tweet> result = restStatuses.lookup(parameters);
    return result;
  }

  @Override
  public List<Tweet> mentionsTimeline(StatusesMentionsTimelineRequest parameters) {
    Statuses restStatuses = restClient.getRemoteableProxy(Statuses.class, TwitterProviderUtil.baseUrl(configuration)+"/statuses");
    List<Tweet> result = restStatuses.mentionsTimeline(parameters);
    return result;
  }

  @Override
  public Tweet show(StatusesShowRequest parameters) {
    Statuses restStatuses = restClient.getRemoteableProxy(Statuses.class, TwitterProviderUtil.baseUrl(configuration)+"/statuses");
    Tweet result = restStatuses.show(parameters);
    return result;
  }

  @Override
  public FriendsIdsResponse ids(FriendsIdsRequest parameters) {
    Friends restFriends = restClient.getRemoteableProxy(Friends.class, TwitterProviderUtil.baseUrl(configuration)+"/friends");
    FriendsIdsResponse result = restFriends.ids(parameters);
    return result;
  }

  @Override
  public FriendsListResponse list(FriendsListRequest parameters) {
    Friends restFriends = restClient.getRemoteableProxy(Friends.class, TwitterProviderUtil.baseUrl(configuration)+"/friends");
    FriendsListResponse result = restFriends.list(parameters);
    return result;
  }

  @Override
  public FollowersIdsResponse ids(FollowersIdsRequest parameters) {
    Followers restFollowers = restClient.getRemoteableProxy(Followers.class, TwitterProviderUtil.baseUrl(configuration)+"/followers");
    FollowersIdsResponse result = restFollowers.ids(parameters);
    return result;
  }

  @Override
  public FollowersListResponse list(FollowersListRequest parameters) {
    Followers restFollowers = restClient.getRemoteableProxy(Followers.class, TwitterProviderUtil.baseUrl(configuration)+"/followers");
    FollowersListResponse result = restFollowers.list(parameters);
    return result;
  }

  @Override
  public List<User> lookup(UsersLookupRequest parameters) {
    Users restUsers = restClient.getRemoteableProxy(Users.class, TwitterProviderUtil.baseUrl(configuration)+"/users");
    List<User> result = restUsers.lookup(parameters);
    return result;
  }

  @Override
  public User show(UsersShowRequest parameters) {
    Users restUsers = restClient.getRemoteableProxy(Users.class, TwitterProviderUtil.baseUrl(configuration)+"/users");
    User result = restUsers.show(parameters);
    return result;
  }

  @Override
  public List<Tweet> list(FavoritesListRequest parameters) {
    Favorites restFavorites = restClient.getRemoteableProxy(Favorites.class, TwitterProviderUtil.baseUrl(configuration)+"/favorites");
    List<Tweet> result = restFavorites.list(parameters);
    return result;
  }

  @Override
  public AccountSettings settings() {
    Account restAccount = restClient.getRemoteableProxy(Account.class, TwitterProviderUtil.baseUrl(configuration)+"/account");
    AccountSettings result = restAccount.settings();
    return result;
  }

  @Override
  public User verifyCredentials() {
    Account restAccount = restClient.getRemoteableProxy(Account.class, TwitterProviderUtil.baseUrl(configuration)+"/account");
    User result = restAccount.verifyCredentials();
    return result;
  }

  @Override
  public WelcomeMessagesListResponse listWelcomeMessages(WelcomeMessagesListRequest parameters) {
    WelcomeMessages proxy = restClient.getRemoteableProxy(WelcomeMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages");
    return proxy.listWelcomeMessages(parameters);
  }

  @Override
  public WelcomeMessage showWelcomeMessage(Long id) {
    WelcomeMessages proxy = restClient.getRemoteableProxy(WelcomeMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages");
    return proxy.showWelcomeMessage(id);
  }

  @Override
  public WelcomeMessageNewResponse newWelcomeMessage(WelcomeMessageNewRequest parameters) {
    WelcomeMessages proxy = restClient.getRemoteableProxy(WelcomeMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages");
    return proxy.newWelcomeMessage(parameters);
  }

  @Override
  public void destroyWelcomeMessage(Long id) {
    WelcomeMessages proxy = restClient.getRemoteableProxy(WelcomeMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages");
    proxy.destroyWelcomeMessage(id);
  }

  @Override
  public WelcomeMessageRulesListResponse listWelcomeMessageRules(WelcomeMessageRulesListRequest parameters) {
    WelcomeMessageRules proxy = restClient.getRemoteableProxy(WelcomeMessageRules.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages/rules");
    return proxy.listWelcomeMessageRules(parameters);
  }

  @Override
  public WelcomeMessageRule showWelcomeMessageRule(Long id) {
    WelcomeMessageRules proxy = restClient.getRemoteableProxy(WelcomeMessageRules.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages/rules");
    return proxy.showWelcomeMessageRule(id);
  }

  @Override
  public WelcomeMessageRule newWelcomeMessageRule(WelcomeMessageNewRuleRequest body) {
    WelcomeMessageRules proxy = restClient.getRemoteableProxy(WelcomeMessageRules.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages/rules");
    return proxy.newWelcomeMessageRule(body);
  }

  @Override
  public void destroyWelcomeMessageRule(Long id) {
    WelcomeMessageRules proxy = restClient.getRemoteableProxy(WelcomeMessageRules.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages/rules");
    proxy.destroyWelcomeMessageRule(id);
  }

  @Override
  public List<Webhook> getWebhooks() {
    AccountActivity proxy = restClient.getRemoteableProxy(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
    return proxy.getWebhooks();
  }

  @Override
  public Webhook registerWebhook(String url) {
    AccountActivity proxy = restClient.getRemoteableProxy(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
    return proxy.registerWebhook(url);
  }

  @Override
  public Boolean deleteWebhook(Long webhookId) {
    AccountActivity proxy = restClient.getRemoteableProxy(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
    return proxy.deleteWebhook(webhookId);
  }

  @Override
  public Boolean putWebhook(Long webhookId) {
    AccountActivity proxy = restClient.getRemoteableProxy(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
    return proxy.putWebhook(webhookId);
  }

  @Override
  public Boolean getWebhookSubscription(Long webhookId) {
//    AccountActivity proxy = restClient.getRemoteableProxy(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
//    return proxy.getWebhookSubscription(webhookId);
    try {
      URIBuilder uriBuilder =
          new URIBuilder()
              .setPath("/account_activity/webhooks/"+webhookId+"/subscriptions.json");
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      try {
        int statusCode = restCall
            .getResponse().getStatusLine().getStatusCode();
        return statusCode == 204;
      } catch (RestCallException e) {
        LOGGER.warn("RestCallException", e);
      }
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return false;
  }

  @Override
  public Boolean registerWebhookSubscriptions(Long webhookId) {
//    AccountActivity proxy = restClient.getRemoteableProxy(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
//    return proxy.registerWebhookSubscriptions(webhookId);
    try {
      URIBuilder uriBuilder =
          new URIBuilder()
              .setPath("/account_activity/webhooks/"+webhookId+"/subscriptions.json");
      RestCall restCall = restClient.doPost(uriBuilder.build().toString());
      try {
        int statusCode = restCall.getResponse().getStatusLine().getStatusCode();
        return statusCode == 204;
      } catch (RestCallException e) {
        LOGGER.warn("RestCallException", e);
      }
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return false;
  }

  @Override
  public Boolean deleteWebhookSubscriptions(Long webhookId) {
//    AccountActivity proxy = restClient.getRemoteableProxy(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
//      return proxy.deleteWebhookSubscriptions(webhookId);
    try {
      URIBuilder uriBuilder =
          new URIBuilder()
              .setPath("/account_activity/webhooks/"+webhookId+"/subscriptions.json");
      RestCall restCall = restClient.doDelete(uriBuilder.build().toString());
      try {
        int statusCode = restCall.getResponse().getStatusLine().getStatusCode();
        return statusCode == 204;
      } catch (RestCallException e) {
        LOGGER.warn("RestCallException", e);
      }
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return false;
  }

  @Override
  public EventsListResponse listEvents(EventsListRequest parameters) {
    DirectMessages proxy = restClient.getRemoteableProxy(DirectMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages");
    return proxy.listEvents(parameters);
  }

  @Override
  public EventShowResponse showEvent(Long id) {
    DirectMessages proxy = restClient.getRemoteableProxy(DirectMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages");
    return proxy.showEvent(id);
  }

  @Override
  public DirectMessageEvent newEvent(MessageCreateRequest event) {
    DirectMessages proxy = restClient.getRemoteableProxy(DirectMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages");
    return proxy.newEvent(event);
  }
}
