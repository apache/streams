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

package org.apache.streams.instagram.api;

import org.apache.streams.instagram.config.InstagramConfiguration;
import org.apache.streams.instagram.pojo.UserRecentMediaRequest;
import org.apache.streams.instagram.provider.InstagramProviderUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.juneau.JodaDateSwap;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * Implementation of all instagram interfaces using juneau.
 */
public class Instagram implements Media, Relationships, Users {

  private static final Logger LOGGER = LoggerFactory.getLogger(Instagram.class);

  private static Map<InstagramConfiguration, Instagram> INSTANCE_MAP = new ConcurrentHashMap<>();

  private InstagramConfiguration configuration;

  private ObjectMapper mapper;

  private CloseableHttpClient httpclient;

  private InstagramOAuthRequestSigner oauthSigner;

  private String rootUrl;

  RestClient restClient;

  private Instagram(InstagramConfiguration configuration) throws InstantiationException {
    this.configuration = configuration;
    this.rootUrl = InstagramProviderUtil.baseUrl(configuration);
    this.oauthSigner = new InstagramOAuthRequestSigner(configuration);
    this.httpclient = HttpClientBuilder.create()
        .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectionRequestTimeout(5000)
            .setConnectTimeout(5000)
            .setSocketTimeout(5000)
            .setCookieSpec("easy")
            .build()
        )
        .disableAutomaticRetries()
        .disableRedirectHandling()
        .setMaxConnPerRoute(20)
        .setMaxConnTotal(100)
        .addInterceptorFirst(oauthSigner)
        .addInterceptorLast((HttpRequestInterceptor) (httpRequest, httpContext) -> System.out.println(httpRequest.getRequestLine()))
        .addInterceptorLast((HttpResponseInterceptor) (httpResponse, httpContext) -> System.out.println(httpResponse.getStatusLine()))
        .build();
    this.restClient = new RestClientBuilder()
        .rootUrl(rootUrl)
        .accept(APPLICATION_JSON.getMimeType())
        .httpClient(httpclient, true)
        .pooled()
        .parser(
            JsonParser.DEFAULT.builder()
                .ignoreUnknownBeanProperties(true)
                .pojoSwaps(JodaDateSwap.class)
                .build())
        .serializer(
            JsonSerializer.DEFAULT.builder()
                .pojoSwaps(JodaDateSwap.class)
                .build())
        .retryable(
            configuration.getRetryMax().intValue(),
            configuration.getRetrySleepMs(),
            new InstagramRetryHandler())
        .setRedirectStrategy(new InstagramRedirectStrategy())
        .build();
    this.mapper = StreamsJacksonMapper.getInstance();
  }

  /**
   * getInstance of Instagram from InstagramConfiguration.
   *
   * @param configuration InstagramConfiguration
   * @return Instagram
   * @throws InstantiationException InstantiationException
   */
  public static Instagram getInstance(InstagramConfiguration configuration) throws InstantiationException {
    if (INSTANCE_MAP.containsKey(configuration) && INSTANCE_MAP.get(configuration) != null) {
      return INSTANCE_MAP.get(configuration);
    } else {
      Instagram instagram = new Instagram(configuration);
      INSTANCE_MAP.put(configuration, instagram);
      return INSTANCE_MAP.get(configuration);
    }
  }

  @Override
  public UserInfoResponse self() {
    Users restUsers = restClient.getRemoteableProxy(Users.class);
    UserInfoResponse result = restUsers.self();
    return result;
  }

  @Override
  public UserInfoResponse lookupUser(String user_id) {
    Users restUsers = restClient.getRemoteableProxy(Users.class);
    UserInfoResponse result = restUsers.lookupUser(user_id);
    return result;
  }

  @Override
  public RecentMediaResponse selfMediaRecent(SelfRecentMediaRequest parameters) {
    Users restUsers = restClient.getRemoteableProxy(Users.class);
    RecentMediaResponse result = restUsers.selfMediaRecent(parameters);
    return result;
  }

  @Override
  public RecentMediaResponse userMediaRecent(UserRecentMediaRequest parameters) {
    Users restUsers = restClient.getRemoteableProxy(Users.class);
    RecentMediaResponse result = restUsers.userMediaRecent(parameters);
    return result;
  }

  @Override
  public RecentMediaResponse selfMediaLiked(SelfLikedMediaRequest parameters) {
    Users restUsers = restClient.getRemoteableProxy(Users.class);
    RecentMediaResponse result = restUsers.selfMediaLiked(parameters);
    return result;

  }

  @Override
  public SearchUsersResponse searchUser(SearchUsersRequest parameters) {
    Users restUsers = restClient.getRemoteableProxy(Users.class);
    SearchUsersResponse result = restUsers.searchUser(parameters);
    return result;
  }

  @Override
  public CommentsResponse comments(String media_id) {
    Media restMedia = restClient.getRemoteableProxy(Media.class);
    CommentsResponse result = restMedia.comments(media_id);
    return result;
  }

  @Override
  public UsersInfoResponse likes(String media_id) {
    Media restMedia = restClient.getRemoteableProxy(Media.class);
    UsersInfoResponse result = restMedia.likes(media_id);
    return result;
  }

  @Override
  public MediaResponse lookupMedia(String media_id) {
    Media restMedia = restClient.getRemoteableProxy(Media.class);
    MediaResponse result = restMedia.lookupMedia(media_id);
    return result;
  }

  @Override
  public MediaResponse shortcode(String shortcode) {
    Media restMedia = restClient.getRemoteableProxy(Media.class);
    MediaResponse result = restMedia.shortcode(shortcode);
    return result;
  }

  @Override
  public SearchMediaResponse searchMedia(SearchMediaRequest parameters) {
    Media restMedia = restClient.getRemoteableProxy(Media.class);
    SearchMediaResponse result = restMedia.searchMedia(parameters);
    return result;
  }

  @Override
  public SearchUsersResponse follows() {
    Relationships restUsers = restClient.getRemoteableProxy(Relationships.class);
    SearchUsersResponse result = restUsers.follows();
    return result;
  }

  @Override
  public SearchUsersResponse followedBy() {
    Relationships restUsers = restClient.getRemoteableProxy(Relationships.class);
    SearchUsersResponse result = restUsers.followedBy();
    return result;
  }

  @Override
  public SearchUsersResponse requestedBy() {
    Relationships restUsers = restClient.getRemoteableProxy(Relationships.class);
    SearchUsersResponse result = restUsers.requestedBy();
    return result;
  }

  @Override
  public RelationshipResponse relationship(Long user_id) {
    Relationships restUsers = restClient.getRemoteableProxy(Relationships.class);
    RelationshipResponse result = restUsers.relationship(user_id);
    return result;
  }
}
