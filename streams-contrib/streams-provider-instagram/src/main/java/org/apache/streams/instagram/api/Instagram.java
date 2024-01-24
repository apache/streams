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

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.StringUtils;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.apache.http.entity.ContentType.APPLICATION_JSON;

/**
 * Implementation of all instagram interfaces using juneau.
 */
public class Instagram implements Media, Users {

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
    this.oauthSigner = new InstagramOAuthRequestSigner(configuration.getOauth());
    this.httpclient = HttpClientBuilder.create()
        .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectionRequestTimeout(5000)
            .setConnectTimeout(5000)
            .setSocketTimeout(5000)
            .setCookieSpec("easy")
            .build()
        )
        .setMaxConnPerRoute(20)
        .setMaxConnTotal(100)
        .build();
    this.restClient = RestClient.create()
        .rootUrl(rootUrl)
        .accept(APPLICATION_JSON.getMimeType())
        .httpClient(httpclient)
        .pooled()
        .parser(
            JsonParser.DEFAULT.copy()
                .ignoreUnknownBeanProperties()
                .build())
        .serializer(
            JsonSerializer.DEFAULT.copy()
                .build())
        .retryHandler(
            new BasicHttpRequestRetryHandler(
                configuration.getRetryMax().intValue(),
                configuration.getRetrySleepMs().intValue(),
                true
            )
        ).build();
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
    try {
      //  TODO: use juneau @Remotable
      // Users restUsers = restClient.getRemoteableProxy("/users", Users.class);
      // UserInfoResponse result = restUsers.lookupUser(parameters);
      // return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/users/self/");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestRequest restCall = restClient.get(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .getResponseAsString();
        UserInfoResponse result = mapper.readValue(restResponseEntity, UserInfoResponse.class);
        return result;
      } catch (RestCallException ex) {
        LOGGER.warn("RestCallException", ex);
      }
    } catch (IOException ex) {
      LOGGER.warn("IOException", ex);
    } catch (URISyntaxException ex) {
      LOGGER.warn("URISyntaxException", ex);
    } catch (Exception ex) {
      LOGGER.warn("Exception", ex);
    }
    return null;
  }

  @Override
  public UserInfoResponse lookupUser(String user_id) {
    try {
      //  TODO: use juneau @Remotable
      // Users restUsers = restClient.getRemoteableProxy("/users", Users.class);
      // UserInfoResponse result = restUsers.lookupUser(parameters);
      // return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/users/" + user_id);
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestRequest restCall = restClient.get(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .getResponseAsString();
        UserInfoResponse result = mapper.readValue(restResponseEntity, UserInfoResponse.class);
        return result;
      } catch (RestCallException ex) {
        LOGGER.warn("RestCallException", ex);
      }
    } catch (IOException ex) {
      LOGGER.warn("IOException", ex);
    } catch (URISyntaxException ex) {
      LOGGER.warn("URISyntaxException", ex);
    } catch (Exception ex) {
      LOGGER.warn("Exception", ex);
    }
    return null;
  }

  @Override
  public RecentMediaResponse selfMediaRecent(SelfRecentMediaRequest parameters) {
    try {
      //  TODO: use juneau @Remotable
      //  Users restUsers = restClient.getRemoteableProxy("/users", Users.class);
      //  RecentMediaResponse result = restUsers.selfMediaRecent(parameters);
      //  return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/users/self/media/recent");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      if ( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if ( Objects.nonNull(parameters.getMaxId()) && StringUtils.isNotBlank(parameters.getMaxId().toString())) {
        uriBuilder.addParameter("max_id", parameters.getMaxId().toString());
      }
      if ( Objects.nonNull(parameters.getMinId()) && StringUtils.isNotBlank(parameters.getMinId().toString())) {
        uriBuilder.addParameter("min", parameters.getMinId().toString());
      }
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestRequest restCall = restClient.get(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .getResponseAsString();
        RecentMediaResponse result = mapper.readValue(restResponseEntity, RecentMediaResponse.class);
        return result;
      } catch (RestCallException ex) {
        LOGGER.warn("RestCallException", ex);
      }
    } catch (IOException ex) {
      LOGGER.warn("IOException", ex);
    } catch (URISyntaxException ex) {
      LOGGER.warn("URISyntaxException", ex);
    } catch (Exception ex) {
      LOGGER.warn("Exception", ex);
    }
    return null;
  }

  @Override
  public RecentMediaResponse userMediaRecent(UserRecentMediaRequest parameters) {
    try {
      //  TODO: use juneau @Remotable
      //  Users restUsers = restClient.getRemoteableProxy("/users", Users.class);
      //  RecentMediaResponse result = restUsers.userMediaRecent(parameters);
      //  return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/users/" + parameters.getUserId() + "/media/recent");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      if ( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if ( Objects.nonNull(parameters.getMaxId()) && StringUtils.isNotBlank(parameters.getMaxId().toString())) {
        uriBuilder.addParameter("max_id", parameters.getMaxId().toString());
      }
      if ( Objects.nonNull(parameters.getMinId()) && StringUtils.isNotBlank(parameters.getMinId().toString())) {
        uriBuilder.addParameter("min", parameters.getMinId().toString());
      }
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestRequest restCall = restClient.get(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .getResponseAsString();
        RecentMediaResponse result = mapper.readValue(restResponseEntity, RecentMediaResponse.class);
        return result;
      } catch (RestCallException ex) {
        LOGGER.warn("RestCallException", ex);
      }
    } catch (IOException ex) {
      LOGGER.warn("IOException", ex);
    } catch (URISyntaxException ex) {
      LOGGER.warn("URISyntaxException", ex);
    } catch (Exception ex) {
      LOGGER.warn("Exception", ex);
    }
    return null;
  }

  @Override
  public RecentMediaResponse selfMediaLiked(SelfLikedMediaRequest parameters) {
    try {
      //  TODO: use juneau @Remotable
      //  Users restUsers = restClient.getRemoteableProxy("/users", Users.class);
      //  RecentMediaResponse result = restUsers.selfMediaLiked(parameters);
      //  return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/users/self/media/liked");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      if ( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if ( Objects.nonNull(parameters.getMaxLikeId()) && StringUtils.isNotBlank(parameters.getMaxLikeId().toString())) {
        uriBuilder.addParameter("max_like_id", parameters.getMaxLikeId().toString());
      }
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestRequest restCall = restClient.get(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .getResponseAsString();
        RecentMediaResponse result = mapper.readValue(restResponseEntity, RecentMediaResponse.class);
        return result;
      } catch (RestCallException ex) {
        LOGGER.warn("RestCallException", ex);
      }
    } catch (IOException ex) {
      LOGGER.warn("IOException", ex);
    } catch (URISyntaxException ex) {
      LOGGER.warn("URISyntaxException", ex);
    } catch (Exception ex) {
      LOGGER.warn("Exception", ex);
    }
    return null;
  }

  @Override
  public SearchUsersResponse searchUser(SearchUsersRequest parameters) {
    try {
      //  TODO: use juneau @Remotable
      //  Users restUsers = restClient.getRemoteableProxy("/users", Users.class);
      //  SearchUsersResponse result = restUsers.searchUser(parameters);
      //  return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/users/search");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      if ( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if ( Objects.nonNull(parameters.getQ()) && StringUtils.isNotBlank(parameters.getQ().toString())) {
        uriBuilder.addParameter("q", parameters.getQ().toString());
      }
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestRequest restCall = restClient.get(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .getResponseAsString();
        SearchUsersResponse result = mapper.readValue(restResponseEntity, SearchUsersResponse.class);
        return result;
      } catch (RestCallException ex) {
        LOGGER.warn("RestCallException", ex);
      }
    } catch (IOException ex) {
      LOGGER.warn("IOException", ex);
    } catch (URISyntaxException ex) {
      LOGGER.warn("URISyntaxException", ex);
    } catch (Exception ex) {
      LOGGER.warn("Exception", ex);
    }
    return null;
  }

  @Override
  public CommentsResponse comments(String media_id) {
    try {
      //  TODO: use juneau @Remotable
      //  Media restMedia = restClient.getRemoteableProxy("/media", Media.class);
      //  CommentsResponse result = restMedia.comments(media_id);
      //  return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/media/" + media_id + "/comments");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      uriBuilder.addParameter("media_id", media_id);
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestRequest restCall = restClient.get(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .getResponseAsString();
        CommentsResponse result = mapper.readValue(restResponseEntity, CommentsResponse.class);
        return result;
      } catch (RestCallException ex) {
        LOGGER.warn("RestCallException", ex);
      }
    } catch (IOException ex) {
      LOGGER.warn("IOException", ex);
    } catch (URISyntaxException ex) {
      LOGGER.warn("URISyntaxException", ex);
    } catch (Exception ex) {
      LOGGER.warn("Exception", ex);
    }
    return null;
  }

  @Override
  public UsersInfoResponse likes(String media_id) {
    try {
      //  TODO: use juneau @Remotable
      //  Media restMedia = restClient.getRemoteableProxy("/media", Media.class);
      //  UsersInfoResponse result = restMedia.likes(media_id);
      //  return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/media/" + media_id + "/likes");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestRequest restCall = restClient.get(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .getResponseAsString();
        UsersInfoResponse result = mapper.readValue(restResponseEntity, UsersInfoResponse.class);
        return result;
      } catch (RestCallException ex) {
        LOGGER.warn("RestCallException", ex);
      }
    } catch (IOException ex) {
      LOGGER.warn("IOException", ex);
    } catch (URISyntaxException ex) {
      LOGGER.warn("URISyntaxException", ex);
    } catch (Exception ex) {
      LOGGER.warn("Exception", ex);
    }
    return null;
  }

  @Override
  public MediaResponse lookupMedia(String media_id) {
    try {
      //  TODO: use juneau @Remotable
      //  Media restMedia = restClient.getRemoteableProxy("/media", Media.class);
      //  MediaResponse result = restMedia.lookupMedia(media_id);
      //  return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/media/" + media_id);
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestRequest restCall = restClient.get(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .getResponseAsString();
        MediaResponse result = mapper.readValue(restResponseEntity, MediaResponse.class);
        return result;
      } catch (RestCallException ex) {
        LOGGER.warn("RestCallException", ex);
      }
    } catch (IOException ex) {
      LOGGER.warn("IOException", ex);
    } catch (URISyntaxException ex) {
      LOGGER.warn("URISyntaxException", ex);
    } catch (Exception ex) {
      LOGGER.warn("Exception", ex);
    }
    return null;
  }

  @Override
  public MediaResponse shortcode(String shortcode) {
    try {
      //  TODO: use juneau @Remotable
//      Media restMedia = restClient.getRemoteableProxy("/media", Media.class);
//      MediaResponse result = restMedia.lookupMedia(media_id);
//      return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/media/shortcode/" + shortcode);
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestRequest restCall = restClient.get(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .getResponseAsString();
        MediaResponse result = mapper.readValue(restResponseEntity, MediaResponse.class);
        return result;
      } catch (RestCallException ex) {
        LOGGER.warn("RestCallException", ex);
      }
    } catch (IOException ex) {
      LOGGER.warn("IOException", ex);
    } catch (URISyntaxException ex) {
      LOGGER.warn("URISyntaxException", ex);
    } catch (Exception ex) {
      LOGGER.warn("Exception", ex);
    }
    return null;
  }

  @Override
  public SearchMediaResponse searchMedia(SearchMediaRequest parameters) {
    try {
      //  TODO: use juneau @Remotable
      //  Media restMedia = restClient.getRemoteableProxy("/media", Media.class);
      //  SearchMediaResponse result = restMedia.lookupMedia(media_id);
      //  return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/media/search");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      uriBuilder.addParameter("distance", parameters.getDistance().toString());
      uriBuilder.addParameter("lat", parameters.getLat().toString());
      uriBuilder.addParameter("lng", parameters.getLng().toString());
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestRequest restCall = restClient.get(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .getResponseAsString();
        SearchMediaResponse result = mapper.readValue(restResponseEntity, SearchMediaResponse.class);
        return result;
      } catch (RestCallException ex) {
        LOGGER.warn("RestCallException", ex);
      }
    } catch (IOException ex) {
      LOGGER.warn("IOException", ex);
    } catch (URISyntaxException ex) {
      LOGGER.warn("URISyntaxException", ex);
    } catch (Exception ex) {
      LOGGER.warn("Exception", ex);
    }
    return null;
  }
}
