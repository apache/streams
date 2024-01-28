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

package org.apache.streams.sprinklr;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.sprinklr.api.PartnerAccountsResponse;
import org.apache.streams.sprinklr.api.ProfileConversationsRequest;
import org.apache.streams.sprinklr.api.ProfileConversationsResponse;
import org.apache.streams.sprinklr.api.SocialProfileRequest;
import org.apache.streams.sprinklr.api.SocialProfileResponse;
import org.apache.streams.sprinklr.config.SprinklrConfiguration;

import com.google.common.util.concurrent.Uninterruptibles;

import org.apache.juneau.collections.JsonMap;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.rest.client.RestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of api.sprinklr.com interfaces using juneau.
 */

public class Sprinklr implements Bootstrap, Profiles {

  private static final Logger LOGGER = LoggerFactory.getLogger(Sprinklr.class);

  private SprinklrConfiguration configuration;

  JsonParser parser;
  JsonSerializer serializer;

  RestClient.Builder restClientBuilder;
  RestClient restClient;

  public static Map<SprinklrConfiguration, Sprinklr> INSTANCE_MAP = new ConcurrentHashMap<>();

  public static Sprinklr getInstance() throws InstantiationException {
    return getInstance(new ComponentConfigurator<>(SprinklrConfiguration.class).detectConfiguration());
  }

  public static Sprinklr getInstance(SprinklrConfiguration configuration) throws InstantiationException {
    if (INSTANCE_MAP.containsKey(configuration) && INSTANCE_MAP.get(configuration) != null) {
      return INSTANCE_MAP.get(configuration);
    } else {
      Sprinklr sprinklr = new Sprinklr(configuration);
      INSTANCE_MAP.put(configuration, sprinklr);
      return INSTANCE_MAP.get(configuration);
    }
  }

  private Sprinklr(SprinklrConfiguration configuration) {
    this.configuration = configuration;
    this.parser = JsonParser.DEFAULT.copy()
        .ignoreUnknownBeanProperties()
        .build();
    this.serializer = JsonSerializer.DEFAULT.copy()
        .trimEmptyCollections(true)
        .trimEmptyMaps(true)
        .build();
    this.restClientBuilder = RestClient.create()
        .accept("application/json")
        .contentType("application/json")
        .disableCookieManagement()
        .disableRedirectHandling()
        .parser(parser)
        .serializer(serializer)
        .rootUrl(baseUrl());
    if (configuration.getDebug() == true) {
      this.restClientBuilder.debug();
    }

    restClientBuilder.header("key", configuration.getKey());
    restClientBuilder.header("Authorization", configuration.getAuthorization());

    this.restClient = restClientBuilder.build();
  }

  private String baseUrl() {
    return "https://api2.sprinklr.com/api/";
  }

  @Override
  public PartnerAccountsResponse getPartnerAccounts() {
    try {
      JsonMap requestMap = new JsonMap();
      requestMap.put("types", "PARTNER_ACCOUNTS");
      RestRequest call = restClient
          .get(baseUrl() + "v1/bootstrap/resources")
          .queryDataBean(requestMap)
          .ignoreErrors();
      String responseJson = call.getResponseAsString();
      PartnerAccountsResponse response = parser.parse(responseJson, PartnerAccountsResponse.class);
      return response;
    } catch (Exception e) {
      LOGGER.error("Exception", e);
      return new PartnerAccountsResponse();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public List<SocialProfileResponse> getSocialProfile(SocialProfileRequest request) {
    try {
      String requestJson = serializer.serialize(request);
      JsonMap requestParams = new JsonMap(requestJson);
      RestRequest call = restClient
          .get(baseUrl() + "v1/profile")
          .accept("application/json")
          .contentType("application/json")
          .ignoreErrors()
          .queryDataBean(requestParams);
      String responseJson = call.getResponseAsString();
      List<SocialProfileResponse> result = parser.parse(responseJson, List.class, SocialProfileResponse.class);
      return result;
    } catch (Exception e) {
      LOGGER.error("Exception", e);
      return new ArrayList<>();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public List<ProfileConversationsResponse> getProfileConversations(ProfileConversationsRequest request) {
    try {
      String requestJson = serializer.serialize(request);
      JsonMap requestParams = new JsonMap(requestJson);
      RestRequest call = restClient
          .get(baseUrl() + "v1/profile/conversations")
          .accept("application/json")
          .contentType("application/json")
          .ignoreErrors()
          .queryDataBean(requestParams);
      String responseJson = call.getResponseAsString();
      List<ProfileConversationsResponse> result = parser.parse(responseJson, List.class, ProfileConversationsResponse.class);
      return result;
    } catch (Exception e) {
      LOGGER.error("Exception", e);
      return new ArrayList<>();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public List<ProfileConversationsResponse> getAllProfileConversations(ProfileConversationsRequest request) {
    long start = 0;

    String snType = request.getSnType();
    String snUserId = request.getSnUserId();
    Long rows = request.getRows();

    List<ProfileConversationsResponse> chunkList = new ArrayList<>();
    List<ProfileConversationsResponse> retList = new ArrayList<>();

    do {
      // Construct a new ProfileConversationsRequest object for this chunk of responses
      ProfileConversationsRequest thisRequest = new ProfileConversationsRequest()
          .withSnType(snType)
          .withSnUserId(snUserId)
          .withRows(rows)
          .withStart(start);
      chunkList = getProfileConversations(thisRequest);
      retList.addAll(chunkList);
      start += rows;
    } while (!(chunkList.isEmpty()));

    return retList;
  }
}
