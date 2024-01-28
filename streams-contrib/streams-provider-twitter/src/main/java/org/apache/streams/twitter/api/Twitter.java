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

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.config.TwitterConfiguration;
import org.apache.streams.twitter.converter.TwitterDateTimeFormat;
import org.apache.streams.twitter.pojo.DirectMessage;
import org.apache.streams.twitter.pojo.DirectMessageEvent;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.pojo.WelcomeMessage;
import org.apache.streams.twitter.pojo.WelcomeMessageRule;
import org.apache.streams.twitter.provider.TwitterProviderUtil;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.rest.client.BasicHttpRequestRetryHandler;
import org.apache.juneau.rest.client.RestCallException;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.rest.client.RestRequest;
import org.apache.juneau.swaps.TemporalDateSwap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/**
 * Implementation of all twitter interfaces using juneau.
 */
public class Twitter implements
  Account,
  AccountActivity,
  DirectMessages,
  Favorites,
  Followers,
  Friends,
  SevenDaySearch,
  Statuses,
  SuggestedUsers,
  ThirtyDaySearch,
  ThirtyDaySearchCounts,
  Users,
  WelcomeMessages,
  WelcomeMessageRules {

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

  RestClient.Builder restClientBuilder;

  RestClient restClient;

  private Twitter(TwitterConfiguration configuration) throws InstantiationException {
    this.configuration = configuration;
    this.rootUrl = TwitterProviderUtil.baseUrl(configuration);
    this.oauthInterceptor = new TwitterOAuthRequestInterceptor(configuration.getOauth());
    this.httpclient = HttpClientBuilder.create()
      .setDefaultRequestConfig(
        RequestConfig.custom()
          .setConnectionRequestTimeout(5000)
          .setConnectTimeout(60000)
          .setSocketTimeout(60000)
          .setCookieSpec("easy")
          .build()
      )
      .setMaxConnPerRoute(20)
      .setMaxConnTotal(100)
      .addInterceptorFirst(oauthInterceptor)
      .addInterceptorLast((HttpRequestInterceptor) (httpRequest, httpContext) -> LOGGER.debug(httpRequest.getRequestLine().getUri()))
      .addInterceptorLast((HttpResponseInterceptor) (httpResponse, httpContext) -> LOGGER.debug(httpResponse.getStatusLine().toString()))
      .build();
    this.restClientBuilder = RestClient.create()
      .executorService(Executors.newCachedThreadPool(), false)
      .httpClient(httpclient)
      .parser(
        JsonParser.DEFAULT.copy()
          .ignoreUnknownBeanProperties()
          .swaps(TemporalDateSwap.class)
          .build())
      .serializer(
        JsonSerializer.DEFAULT.copy()
          .swaps(TemporalDateSwap.class)
          .trimEmptyCollections(true)
          .trimEmptyMaps(true)
          .build())
      .rootUrl(rootUrl)
      .retryHandler(
        new BasicHttpRequestRetryHandler(
                configuration.getRetryMax().intValue(),
                configuration.getRetrySleepMs().intValue(),
                true
        )
      );
    if( configuration.getDebug() ) {
      restClientBuilder = restClientBuilder.debug();
    }

    this.restClient = restClientBuilder.build();
    this.mapper = StreamsJacksonMapper.getInstance();
  }

  public static Twitter getInstance() throws InstantiationException {
    return getInstance(new ComponentConfigurator<>(TwitterConfiguration.class).detectConfiguration());
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
    Statuses restStatuses = restClient.getRemote(Statuses.class, TwitterProviderUtil.baseUrl(configuration)+"/statuses");
    List<Tweet> result = restStatuses.userTimeline(parameters);
    return result;
  }

  @Override
  public List<Tweet> retweets(RetweetsRequest parameters) {
    Statuses restStatuses = restClient.getRemote(Statuses.class, TwitterProviderUtil.baseUrl(configuration)+"/statuses");
    List<Tweet> result = restStatuses.retweets(parameters);
    return result;
  }

  @Override
  public RetweeterIdsResponse retweeterIds(RetweeterIdsRequest parameters) {
    Statuses restStatuses = restClient.getRemote(Statuses.class, TwitterProviderUtil.baseUrl(configuration)+"/statuses");
    RetweeterIdsResponse result = restStatuses.retweeterIds(parameters);
    return result;
  }

  @Override
  public List<Tweet> homeTimeline(StatusesHomeTimelineRequest parameters) {
    Statuses restStatuses = restClient.getRemote(Statuses.class, TwitterProviderUtil.baseUrl(configuration)+"/statuses");
    List<Tweet> result = restStatuses.homeTimeline(parameters);
    return result;
  }

  @Override
  public List<Tweet> lookup(StatusesLookupRequest parameters) {
    Statuses restStatuses = restClient.getRemote(Statuses.class, TwitterProviderUtil.baseUrl(configuration)+"/statuses");
    List<Tweet> result = restStatuses.lookup(parameters);
    return result;
  }

  @Override
  public List<Tweet> mentionsTimeline(StatusesMentionsTimelineRequest parameters) {
    Statuses restStatuses = restClient.getRemote(Statuses.class, TwitterProviderUtil.baseUrl(configuration)+"/statuses");
    List<Tweet> result = restStatuses.mentionsTimeline(parameters);
    return result;
  }

  @Override
  public Tweet show(StatusesShowRequest parameters) {
    Statuses restStatuses = restClient.getRemote(Statuses.class, TwitterProviderUtil.baseUrl(configuration)+"/statuses");
    Tweet result = restStatuses.show(parameters);
    return result;
  }

  @Override
  public FriendsIdsResponse ids(FriendsIdsRequest parameters) {
    Friends restFriends = restClient.getRemote(Friends.class, TwitterProviderUtil.baseUrl(configuration)+"/friends");
    FriendsIdsResponse result = restFriends.ids(parameters);
    return result;
  }

  @Override
  public FriendsListResponse list(FriendsListRequest parameters) {
    Friends restFriends = restClient.getRemote(Friends.class, TwitterProviderUtil.baseUrl(configuration)+"/friends");
    FriendsListResponse result = restFriends.list(parameters);
    return result;
  }

  @Override
  public FollowersIdsResponse ids(FollowersIdsRequest parameters) {
    Followers restFollowers = restClient.getRemote(Followers.class, TwitterProviderUtil.baseUrl(configuration)+"/followers");
    FollowersIdsResponse result = restFollowers.ids(parameters);
    return result;
  }

  @Override
  public FollowersListResponse list(FollowersListRequest parameters) {
    Followers restFollowers = restClient.getRemote(Followers.class, TwitterProviderUtil.baseUrl(configuration)+"/followers");
    FollowersListResponse result = restFollowers.list(parameters);
    return result;
  }

  @Override
  public List<User> lookup(UsersLookupRequest parameters) {
    Users restUsers = restClient.getRemote(Users.class, TwitterProviderUtil.baseUrl(configuration)+"/users");
    List<User> result = restUsers.lookup(parameters);
    return result;
  }

  @Override
  public List<User> search(UsersSearchRequest parameters) {
    Users proxy = restClient.getRemote(Users.class, TwitterProviderUtil.baseUrl(configuration)+"/users");
    List<User> result = proxy.search(parameters);
    return result;
  }

  @Override
  public User show(UsersShowRequest parameters) {
    Users restUsers = restClient.getRemote(Users.class, TwitterProviderUtil.baseUrl(configuration)+"/users");
    User result = restUsers.show(parameters);
    return result;
  }

  @Override
  public List<Tweet> list(FavoritesListRequest parameters) {
    Favorites restFavorites = restClient.getRemote(Favorites.class, TwitterProviderUtil.baseUrl(configuration)+"/favorites");
    List<Tweet> result = restFavorites.list(parameters);
    return result;
  }

  @Override
  public AccountSettings settings() {
    Account restAccount = restClient.getRemote(Account.class, TwitterProviderUtil.baseUrl(configuration)+"/account");
    AccountSettings result = restAccount.settings();
    return result;
  }

  @Override
  public User verifyCredentials() {
    Account restAccount = restClient.getRemote(Account.class, TwitterProviderUtil.baseUrl(configuration)+"/account");
    User result = restAccount.verifyCredentials();
    return result;
  }

  @Override
  public User updateProfile(UpdateProfileRequest parameters) {
    throw new NotImplementedException();
  }

  @Override
  public AccountSettings updateSettings(UpdateProfileRequest parameters) {
    throw new NotImplementedException();
  }

  @Override
  public WelcomeMessagesListResponse listWelcomeMessages(WelcomeMessagesListRequest parameters) {
    WelcomeMessages proxy = restClient.getRemote(WelcomeMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages");
    return proxy.listWelcomeMessages(parameters);
  }

  @Override
  public WelcomeMessage showWelcomeMessage(Long id) {
    WelcomeMessages proxy = restClient.getRemote(WelcomeMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages");
    return proxy.showWelcomeMessage(id);
  }

  @Override
  public WelcomeMessageNewResponse newWelcomeMessage(WelcomeMessageNewRequest parameters) {
    WelcomeMessages proxy = restClient.getRemote(WelcomeMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages");
    return proxy.newWelcomeMessage(parameters);
  }

  @Override
  public void destroyWelcomeMessage(Long id) {
    WelcomeMessages proxy = restClient.getRemote(WelcomeMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages");
    proxy.destroyWelcomeMessage(id);
  }

  @Override
  public WelcomeMessageRulesListResponse listWelcomeMessageRules(WelcomeMessageRulesListRequest parameters) {
    WelcomeMessageRules proxy = restClient.getRemote(WelcomeMessageRules.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages/rules");
    return proxy.listWelcomeMessageRules(parameters);
  }

  @Override
  public WelcomeMessageRule showWelcomeMessageRule(Long id) {
    WelcomeMessageRules proxy = restClient.getRemote(WelcomeMessageRules.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages/rules");
    return proxy.showWelcomeMessageRule(id);
  }

  @Override
  public WelcomeMessageRule newWelcomeMessageRule(WelcomeMessageNewRuleRequest body) {
    WelcomeMessageRules proxy = restClient.getRemote(WelcomeMessageRules.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages/rules");
    return proxy.newWelcomeMessageRule(body);
  }

  @Override
  public void destroyWelcomeMessageRule(Long id) {
    WelcomeMessageRules proxy = restClient.getRemote(WelcomeMessageRules.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages/welcome_messages/rules");
    proxy.destroyWelcomeMessageRule(id);
  }

  @Override
  public WebhooksResponse getWebhooks() {
    AccountActivity proxy = restClient.getRemote(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
    return proxy.getWebhooks();
  }

  @Override
  public List<Webhook> getWebhooks(String env_name) {
    AccountActivity proxy = restClient.getRemote(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
    return proxy.getWebhooks(env_name);
  }

  @Override
  public Webhook registerWebhook(String env_name, String url) {
    AccountActivity proxy = restClient.getRemote(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
    return proxy.registerWebhook(env_name, url);
  }

  @Override
  public Boolean deleteWebhook(String env_name, Long webhookId) {
    AccountActivity proxy = restClient.getRemote(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
    return proxy.deleteWebhook(env_name, webhookId);
  }

  @Override
  public Boolean putWebhook(String env_name, Long webhookId) {
    AccountActivity proxy = restClient.getRemote(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
    return proxy.putWebhook(env_name, webhookId);
  }

  @Override
  public Boolean reenableWebhook(String env_name, Long webhookId) {
    URIBuilder uriBuilder = new URIBuilder()
            .setPath("/account_activity/all/"+env_name+"/webhooks/"+webhookId+".json");
    try {
      RestRequest restCall = restClient.put(uriBuilder.build().toString());
      int statusCode = restCall.run().getStatusLine().getStatusCode();
      return statusCode == 204;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return false;
  }

  @Override
  public SubscriptionsCountResponse getSubscriptionsCount() throws InvocationTargetException, RestCallException {
    AccountActivity proxy = restClient.getRemote(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
    return proxy.getSubscriptionsCount();
  }

  @Override
  public SubscriptionsListResponse getSubscriptionsList(String env_name) throws InvocationTargetException, RestCallException {
    AccountActivity proxy = restClient.getRemote(AccountActivity.class, TwitterProviderUtil.baseUrl(configuration)+"/account_activity");
    return proxy.getSubscriptionsList(env_name);
  }

  @Override
  public Boolean getSubscriptions(String env_name) {
    URIBuilder uriBuilder = new URIBuilder()
            .setPath("/account_activity/all/"+env_name+"/subscriptions.json");
    try {
      RestRequest restCall = restClient.get(uriBuilder.build().toString());
      int statusCode = restCall.run().getStatusLine().getStatusCode();
      return statusCode == 204;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return false;
  }

  @Override
  public Boolean newSubscription(String env_name) {
    URIBuilder uriBuilder = new URIBuilder()
            .setPath("/account_activity/all/"+env_name+"/subscriptions.json");
    try {
      RestRequest restCall = restClient.post(uriBuilder.build().toString());
      int statusCode = restCall.run().getStatusLine().getStatusCode();
      return statusCode == 204;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return false;
  }

  @Override
  public Boolean deleteWebhookSubscriptions(String env_name, String user_id) {
    URIBuilder uriBuilder = new URIBuilder()
        .setPath("/account_activity/all/"+env_name+"/subscriptions/"+user_id+".json");
    try {
      RestRequest restCall = restClient.delete(uriBuilder.build().toString());
      int statusCode = restCall.run().getStatusLine().getStatusCode();
      return statusCode == 204;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return false;
  }

  @Override
  public EventsListResponse listEvents(EventsListRequest parameters) {
    DirectMessages proxy = restClient.getRemote(DirectMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages");
    return proxy.listEvents(parameters);
  }

  @Override
  public EventShowResponse showEvent(Long id) {
    DirectMessages proxy = restClient.getRemote(DirectMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages");
    return proxy.showEvent(id);
  }

  @Override
  public DirectMessageEvent newEvent(MessageCreateRequest event) {
    DirectMessages proxy = restClient.getRemote(DirectMessages.class, TwitterProviderUtil.baseUrl(configuration)+"/direct_messages");
    return proxy.newEvent(event);
  }

  @Override
  public SevenDaySearchResponse sevenDaySearch(SevenDaySearchRequest event) {
    SevenDaySearch proxy = restClient.getRemote(SevenDaySearch.class, TwitterProviderUtil.baseUrl(configuration)+"/search");
    return proxy.sevenDaySearch(event);
  }

  @Override
  public ThirtyDaySearchResponse thirtyDaySearch(String environment, ThirtyDaySearchRequest searchRequest) {
    ThirtyDaySearch proxy = restClient.getRemote(ThirtyDaySearch.class, TwitterProviderUtil.baseUrl(configuration)+"/"+ThirtyDaySearch.path);
    String env = StringUtils.defaultString(environment, configuration.getEnvironment());
    return proxy.thirtyDaySearch(env, searchRequest);
  }

  @Override
  public ThirtyDaySearchCountsResponse thirtyDaySearchCounts(String environment, ThirtyDaySearchCountsRequest searchCountsRequest) {
    ThirtyDaySearchCounts proxy = restClient.getRemote(ThirtyDaySearchCounts.class, TwitterProviderUtil.baseUrl(configuration)+"/"+ThirtyDaySearch.path);
    String env = StringUtils.defaultString(environment, configuration.getEnvironment());
    return proxy.thirtyDaySearchCounts(env, searchCountsRequest);
  }

  @Override
  public DirectMessage destroy(Long id) {
    throw new NotImplementedException();
  }

  @Override
  public DirectMessage show(Long id) {
    throw new NotImplementedException();
  }

  @Override
  public List<DirectMessage> list(DirectMessagesListRequest parameters) {
    throw new NotImplementedException();
  }

  @Override
  public List<DirectMessage> sent(DirectMessagesSentRequest parameters) {
    throw new NotImplementedException();
  }

  @Override
  public DirectMessage newDM(DirectMessageNewRequest parameters) {
    throw new NotImplementedException();
  }

  @Override
  public List<SuggestedUserCategory> categories(String lang) {
    SuggestedUsers proxy = restClient.getRemote(SuggestedUsers.class, TwitterProviderUtil.baseUrl(configuration)+"/users");
    return proxy.categories(lang);
  }

  @Override
  public SuggestedUserCategory suggestions(String slug, String lang) {
    SuggestedUsers proxy = restClient.getRemote(SuggestedUsers.class, TwitterProviderUtil.baseUrl(configuration)+"/users");
    return proxy.suggestions(slug, lang);
  }

  @Override
  public List<User> members(String slug) {
    SuggestedUsers proxy = restClient.getRemote(SuggestedUsers.class, TwitterProviderUtil.baseUrl(configuration)+"/users");
    return proxy.members(slug);
  }

}
