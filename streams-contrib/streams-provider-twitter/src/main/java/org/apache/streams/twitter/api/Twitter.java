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
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.provider.TwitterProviderUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.parser.ParseException;
import org.apache.juneau.plaintext.PlainTextSerializer;
import org.apache.juneau.rest.client.RestCall;
import org.apache.juneau.rest.client.RestCallException;
import org.apache.juneau.rest.client.RestClient;
//import org.apache.juneau.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Implementation of all twitter interfaces using juneau.
 */
public class Twitter implements Followers, Friends, Statuses, Users {

  private static final Logger LOGGER = LoggerFactory.getLogger(Twitter.class);

  private static Map<TwitterConfiguration, Twitter> INSTANCE_MAP = new ConcurrentHashMap<>();

  private TwitterConfiguration configuration;

  private ObjectMapper mapper;

  private String rootUrl;

  private CloseableHttpClient httpclient;

  private HttpRequestInterceptor oauthInterceptor;

  RestClient restClient;

  private Twitter(TwitterConfiguration configuration) throws InstantiationException {
    this.configuration = configuration;
    this.rootUrl = TwitterProviderUtil.baseUrl(configuration);
    this.oauthInterceptor = new TwitterOAuthRequestInterceptor(configuration.getOauth());
    this.httpclient = HttpClientBuilder.create()
        .addInterceptorFirst(oauthInterceptor).build();
//  TODO: juneau-6.2.0-incubating
//  this.restClient = new RestClientBuilder()
//        .httpClient(httpclient, true)
//        .parser(JsonParser.class)
//        .rootUrl(rootUrl)
//        .retryable(
//            configuration.getRetryMax().intValue(),
//            configuration.getRetrySleepMs(),
//            new TwitterRetryHandler())
//        .build();
    this.restClient = new RestClient()
        .setHttpClient(httpclient)
        .setParser(JsonParser.class)
        .setRootUrl(rootUrl)
        .setRetryHandler(new TwitterRetryHandler());
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
    try {
//  TODO: juneau-6.2.0-incubating
//      Statuses restStatuses = restClient.getRemoteableProxy("/statuses/user_timeline.json", Statuses.class);
//      List<Tweet> result = restStatuses.userTimeline(parameters);
//      return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/statuses/user_timeline.json");
      if( Objects.nonNull(parameters.getUserId()) && StringUtils.isNotBlank(parameters.getUserId().toString())) {
        uriBuilder.addParameter("user_id", parameters.getUserId().toString());
      }
      if( Objects.nonNull(parameters.getScreenName()) && StringUtils.isNotBlank(parameters.getScreenName())) {
        uriBuilder.addParameter("screen_name", parameters.getScreenName());
      }
      if( Objects.nonNull(parameters.getSinceId()) && StringUtils.isNotBlank(parameters.getSinceId().toString())) {
        uriBuilder.addParameter("since_id", parameters.getSinceId().toString());
      }
      if( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if( Objects.nonNull(parameters.getMaxId()) && StringUtils.isNotBlank(parameters.getMaxId().toString())) {
        uriBuilder.addParameter("max_id", parameters.getMaxId().toString());
      }
      if( Objects.nonNull(parameters.getTrimUser()) && StringUtils.isNotBlank(parameters.getTrimUser().toString())) {
        uriBuilder.addParameter("trim_user", parameters.getTrimUser().toString());
      }
      if( Objects.nonNull(parameters.getExcludeReplies()) && StringUtils.isNotBlank(parameters.getExcludeReplies().toString())) {
        uriBuilder.addParameter("exclude_replies", parameters.getExcludeReplies().toString());
      }
      if( Objects.nonNull(parameters.getContributorDetails()) && StringUtils.isNotBlank(parameters.getContributorDetails().toString())) {
        uriBuilder.addParameter("contributor_details", parameters.getContributorDetails().toString());
      }
      if( Objects.nonNull(parameters.getIncludeRts()) && StringUtils.isNotBlank(parameters.getIncludeRts().toString())) {
        uriBuilder.addParameter("include_rts", parameters.getIncludeRts().toString());
      }
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      String restResponseEntity = restCall.getResponseAsString();
      ArrayNode resultArrayNode = mapper.readValue(restResponseEntity, ArrayNode.class);
      List<Tweet> result = new ArrayList();
      resultArrayNode.iterator().forEachRemaining(item -> result.add(mapper.convertValue(item, Tweet.class)));
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return new ArrayList<>();
  }

  @Override
  public List<Tweet> lookup(StatusesLookupRequest parameters) {
//  TODO: juneau-6.2.0-incubating
//      Statuses restStatuses = restClient.getRemoteableProxy("/statuses/lookup.json", Statuses.class);
//      List<Tweet> result = restStatuses.lookup(parameters);
//      return result;
    String ids = StringUtils.join(parameters.getId(), ',');
    try {
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/statuses/lookup.json");
      if( Objects.nonNull(parameters.getId()) && StringUtils.isNotBlank(parameters.getId().toString())) {
        uriBuilder.addParameter("id", parameters.getId().toString());
      }
      if( Objects.nonNull(parameters.getTrimUser()) && StringUtils.isNotBlank(parameters.getTrimUser().toString())) {
        uriBuilder.addParameter("trim_user", parameters.getTrimUser().toString());
      }
      if( Objects.nonNull(parameters.getIncludeEntities()) && StringUtils.isNotBlank(parameters.getIncludeEntities().toString())) {
        uriBuilder.addParameter("include_entities", parameters.getIncludeEntities().toString());
      }
      if( Objects.nonNull(parameters.getMap()) && StringUtils.isNotBlank(parameters.getMap().toString())) {
        uriBuilder.addParameter("map", parameters.getMap().toString());
      }
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      String restResponseEntity = restCall.getResponseAsString();
      ArrayNode resultArrayNode = mapper.readValue(restResponseEntity, ArrayNode.class);
      List<Tweet> result = new ArrayList();
      resultArrayNode.iterator().forEachRemaining(item -> result.add(mapper.convertValue(item, Tweet.class)));
      //List<Tweet> result = restCall.getResponse(LinkedList.class, Tweet.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Tweet show(StatusesShowRequest parameters) {
//  TODO: juneau-6.2.0-incubating
//      Statuses restStatuses = restClient.getRemoteableProxy("/statuses/show.json", Statuses.class);
//      Tweet result = restStatuses.show(parameters);
//      return result;
    try {
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/statuses/show.json");
      if( Objects.nonNull(parameters.getId()) && StringUtils.isNotBlank(parameters.getId().toString())) {
        uriBuilder.addParameter("id", parameters.getId().toString());
      }
      if( Objects.nonNull(parameters.getTrimUser()) && StringUtils.isNotBlank(parameters.getTrimUser().toString())) {
        uriBuilder.addParameter("trim_user", parameters.getTrimUser().toString());
      }
      if( Objects.nonNull(parameters.getIncludeEntities()) && StringUtils.isNotBlank(parameters.getIncludeEntities().toString())) {
        uriBuilder.addParameter("include_entities", parameters.getIncludeEntities().toString());
      }
      if( Objects.nonNull(parameters.getIncludeMyRetweet()) && StringUtils.isNotBlank(parameters.getIncludeMyRetweet().toString())) {
        uriBuilder.addParameter("include_my_retweet", parameters.getIncludeMyRetweet().toString());
      }
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      String restResponseEntity = restCall.getResponseAsString();
      //Tweet result = restCall.getResponse(Tweet.class);
      Tweet result = mapper.readValue(restResponseEntity, Tweet.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return null;
  }

  @Override
  public FriendsIdsResponse ids(FriendsIdsRequest parameters) {
//  TODO: juneau-6.2.0-incubating
//      Friends restFriends = restClient.getRemoteableProxy("/friends/ids.json", Friends.class);
//      FriendsIdsResponse result = restFriends.ids(parameters);
//      return result;
    try {
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/friends/ids.json");
      if( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if( Objects.nonNull(parameters.getCurser()) && StringUtils.isNotBlank(parameters.getCurser().toString())) {
        uriBuilder.addParameter("curser", parameters.getCurser().toString());
      }
      if( Objects.nonNull(parameters.getId()) && StringUtils.isNotBlank(parameters.getId().toString())) {
        uriBuilder.addParameter("id", parameters.getId().toString());
      }
      if( Objects.nonNull(parameters.getScreenName()) && StringUtils.isNotBlank(parameters.getScreenName())) {
        uriBuilder.addParameter("screen_name", parameters.getScreenName());
      }
      if( Objects.nonNull(parameters.getStringifyIds()) && StringUtils.isNotBlank(parameters.getStringifyIds().toString())) {
        uriBuilder.addParameter("stringify_ids", parameters.getStringifyIds().toString());
      }
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      String restResponseEntity = restCall.getResponseAsString();
      //FriendsIdsResponse result = restCall.getResponse(FriendsIdsResponse.class);
      FriendsIdsResponse result = mapper.readValue(restResponseEntity, FriendsIdsResponse.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return null;
  }

  @Override
  public FriendsListResponse list(FriendsListRequest parameters) {
//  TODO: juneau-6.2.0-incubating
//      Friends restFriends = restClient.getRemoteableProxy("/friends/list.json", Friends.class);
//      FriendsListResponse result = restFriends.list(parameters);
//      return result;
    try {
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/friends/list.json");
      if( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if( Objects.nonNull(parameters.getCurser()) && StringUtils.isNotBlank(parameters.getCurser().toString())) {
        uriBuilder.addParameter("curser", parameters.getCurser().toString());
      }
      if( Objects.nonNull(parameters.getId()) && StringUtils.isNotBlank(parameters.getId().toString())) {
        uriBuilder.addParameter("id", parameters.getId().toString());
      }
      if( Objects.nonNull(parameters.getIncludeUserEntities()) && StringUtils.isNotBlank(parameters.getIncludeUserEntities().toString())) {
        uriBuilder.addParameter("include_user_entities", parameters.getIncludeUserEntities().toString());
      }
      if( Objects.nonNull(parameters.getScreenName()) && StringUtils.isNotBlank(parameters.getScreenName())) {
        uriBuilder.addParameter("screen_name", parameters.getScreenName());
      }
      if( Objects.nonNull(parameters.getSkipStatus()) && StringUtils.isNotBlank(parameters.getSkipStatus().toString())) {
        uriBuilder.addParameter("skip_status", parameters.getSkipStatus().toString());
      }
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      String restResponseEntity = restCall.getResponseAsString();
      //FriendsListResponse result = restCall.getResponse(FriendsListResponse.class);
      FriendsListResponse result = mapper.readValue(restResponseEntity, FriendsListResponse.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return null;
  }

  @Override
  public FollowersIdsResponse ids(FollowersIdsRequest parameters) {
//  TODO: juneau-6.2.0-incubating
//      Followers restFollowers = restClient.getRemoteableProxy("/friends/list.json", Followers.class);
//      FollowersIdsResponse result = restFollowers.ids(parameters);
//      return result;
    try {
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/followers/ids.json");
      if( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if( Objects.nonNull(parameters.getCurser()) && StringUtils.isNotBlank(parameters.getCurser().toString())) {
        uriBuilder.addParameter("curser", parameters.getCurser().toString());
      }
      if( Objects.nonNull(parameters.getId()) && StringUtils.isNotBlank(parameters.getId().toString())) {
        uriBuilder.addParameter("id", parameters.getId().toString());
      }
      if( Objects.nonNull(parameters.getScreenName()) && StringUtils.isNotBlank(parameters.getScreenName())) {
        uriBuilder.addParameter("screen_name", parameters.getScreenName());
      }
      if( Objects.nonNull(parameters.getStringifyIds()) && StringUtils.isNotBlank(parameters.getStringifyIds().toString())) {
        uriBuilder.addParameter("stringify_ids", parameters.getStringifyIds().toString());
      }
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      String restResponseEntity = restCall.getResponseAsString();
      //FollowersIdsResponse result = restCall.getResponse(FollowersIdsResponse.class);
      FollowersIdsResponse result = mapper.readValue(restResponseEntity, FollowersIdsResponse.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return null;
  }

  @Override
  public FollowersListResponse list(FollowersListRequest parameters) {
//  TODO: juneau-6.2.0-incubating
//      Followers restFollowers = restClient.getRemoteableProxy("/friends/list.json", Followers.class);
//      FollowersListResponse result = restFollowers.list(parameters);
//      return result;
    try {
      URIBuilder uriBuilder =
          new URIBuilder()
              .setPath("/followers/list.json");
      if( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if( Objects.nonNull(parameters.getCurser()) && StringUtils.isNotBlank(parameters.getCurser().toString())) {
        uriBuilder.addParameter("curser", parameters.getCurser().toString());
      }
      if( Objects.nonNull(parameters.getId()) && StringUtils.isNotBlank(parameters.getId().toString())) {
        uriBuilder.addParameter("id", parameters.getId().toString());
      }
      if( Objects.nonNull(parameters.getIncludeUserEntities()) && StringUtils.isNotBlank(parameters.getIncludeUserEntities().toString())) {
        uriBuilder.addParameter("include_user_entities", parameters.getIncludeUserEntities().toString());
      }
      if( Objects.nonNull(parameters.getScreenName()) && StringUtils.isNotBlank(parameters.getScreenName())) {
        uriBuilder.addParameter("screen_name", parameters.getScreenName());
      }
      if( Objects.nonNull(parameters.getSkipStatus()) && StringUtils.isNotBlank(parameters.getSkipStatus().toString())) {
        uriBuilder.addParameter("skip_status", parameters.getSkipStatus().toString());
      }
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      String restResponseEntity = restCall.getResponseAsString();
      //FollowersListResponse result = restCall.getResponse(FollowersListResponse.class);
      FollowersListResponse result = mapper.readValue(restResponseEntity, FollowersListResponse.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return null;
  }

  @Override
  public List<User> lookup(UsersLookupRequest parameters) {
//  TODO: juneau-6.2.0-incubating
//      Users restUsers = restClient.getRemoteableProxy("/users/lookup.json", Users.class);
//      List<User> result = restUsers.lookup(parameters);
//      return result;
    String user_ids = StringUtils.join(parameters.getUserId(), ',');
    String screen_names = StringUtils.join(parameters.getScreenName(), ',');
    try {
      URIBuilder uriBuilder =
          new URIBuilder()
              .setPath("/users/lookup.json");
      if( Objects.nonNull(parameters.getIncludeEntities()) && StringUtils.isNotBlank(parameters.getIncludeEntities().toString())) {
        uriBuilder.addParameter("include_entities", parameters.getIncludeEntities().toString());
      }
      if( Objects.nonNull(screen_names) && StringUtils.isNotBlank(screen_names)) {
        uriBuilder.addParameter("screen_name", screen_names);
      }
      if( Objects.nonNull(user_ids) && StringUtils.isNotBlank(user_ids)) {
        uriBuilder.addParameter("user_id", user_ids);
      }
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
//      List<User> result = restCall.getResponse(LinkedList.class, User.class);
      String restResponseEntity = restCall.getResponseAsString();
      ArrayNode resultArrayNode = mapper.readValue(restResponseEntity, ArrayNode.class);
      List<User> result = new ArrayList();
      resultArrayNode.iterator().forEachRemaining(item -> result.add(mapper.convertValue(item, User.class)));
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return new ArrayList<>();
  }

  @Override
  public User show(UsersShowRequest parameters) {
//  TODO: juneau-6.2.0-incubating
//      Users restUsers = restClient.getRemoteableProxy("/users/lookup.json", Users.class);
//      User result = restUsers.show(parameters);
//      return result;
    try {
      URIBuilder uriBuilder =
          new URIBuilder()
              .setPath("/users/show.json");
      if( Objects.nonNull(parameters.getIncludeEntities()) && StringUtils.isNotBlank(parameters.getIncludeEntities().toString())) {
        uriBuilder.addParameter("include_entities", parameters.getIncludeEntities().toString());
      }
      if( Objects.nonNull(parameters.getScreenName()) && StringUtils.isNotBlank(parameters.getScreenName())) {
        uriBuilder.addParameter("screen_name", parameters.getScreenName());
      }
      if( Objects.nonNull(parameters.getUserId()) && StringUtils.isNotBlank(parameters.getUserId().toString())) {
        uriBuilder.addParameter("user_id", parameters.getUserId().toString());
      }
      RestCall restCall = restClient.doGet(uriBuilder.build().toString()
      );
      String restResponseEntity = restCall.getResponseAsString();
      User result = mapper.readValue(restResponseEntity, User.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return null;
  }

}
