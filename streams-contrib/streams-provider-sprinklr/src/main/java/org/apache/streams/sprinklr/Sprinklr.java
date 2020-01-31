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

import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.apache.juneau.ObjectMap;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.parser.ParseException;
import org.apache.juneau.rest.client.RestCall;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.rest.client.RestClientBuilder;
import org.apache.juneau.rest.client.RestCallException;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.sprinklr.api.*;
import org.apache.streams.sprinklr.config.SprinklrConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 *  Implementation of api.sprinklr.com interfaces using juneau.
 */

public class Sprinklr implements Bootstrap, Profiles {

    private static final Logger LOGGER = LoggerFactory.getLogger(Sprinklr.class);

    private SprinklrConfiguration configuration;

    JsonParser parser;
    JsonSerializer serializer;

    RestClientBuilder restClientBuilder;
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
        this.parser = JsonParser.DEFAULT.builder()
                .ignoreUnknownBeanProperties(true)
                .build();
        this.serializer = JsonSerializer.DEFAULT.builder()
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
        if(configuration.getDebug() == true) {
            this.restClientBuilder.debug();
        }

        ArrayList<Header> defaultHeaders = new ArrayList<>();
        defaultHeaders.add(new BasicHeader("key", configuration.getKey()));
        defaultHeaders.add(new BasicHeader("Authorization", configuration.getAuthorization()));

        restClientBuilder.setDefaultHeaders(defaultHeaders);
        this.restClient = restClientBuilder.build();
    }

    private String baseUrl() { return "https://api2.sprinklr.com/api/"; }

    @Override
    public PartnerAccountsResponse getPartnerAccounts() {
        try {
            ObjectMap requestMap = new ObjectMap();
            requestMap.put("types", "PARTNER_ACCOUNTS");
            RestCall call = restClient
                    .doGet(baseUrl() + "v1/bootstrap/resources")
                    .queryIfNE(requestMap)
                    .ignoreErrors();
            String responseJson = call.getResponseAsString();
            PartnerAccountsResponse response = parser.parse(responseJson, PartnerAccountsResponse.class);
            return response;
        } catch ( Exception e ) {
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
            ObjectMap requestParams = new ObjectMap(requestJson);
            RestCall call = restClient
                    .doGet(baseUrl() + "v1/profile")
                    .accept("application/json")
                    .contentType("application/json")
                    .ignoreErrors()
                    .queryIfNE(requestParams);
            String responseJson = call.getResponseAsString();
            List<SocialProfileResponse> result = parser.parse(responseJson, List.class, SocialProfileResponse.class);
            return result;
        } catch( Exception e ) {
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
            ObjectMap requestParams = new ObjectMap(requestJson);
            RestCall call = restClient
                    .doGet(baseUrl() + "v1/profile/conversations")
                    .accept("application/json")
                    .contentType("application/json")
                    .ignoreErrors()
                    .queryIfNE(requestParams);
            String responseJson = call.getResponseAsString();
            List<ProfileConversationsResponse> result = parser.parse(responseJson, List.class, ProfileConversationsResponse.class);
            return result;
        } catch( Exception e ) {
            LOGGER.error("Exception", e);
            return new ArrayList<>();
        } finally {
            Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
        }
    }

    @Override
    public List<ProfileConversationsResponse> getAllProfileConversations(ProfileConversationsRequest request) {
        long start = 0;

        List<ProfileConversationsResponse> chunkList = new ArrayList<>();
        List<ProfileConversationsResponse> retList = new ArrayList<>();

        do {
            // Construct a new ProfileConversationsRequest object for this chunk of responses
            ProfileConversationsRequest thisRequest = new ProfileConversationsRequest()
                    .withSnType(configuration.getSnType())
                    .withSnUserId(configuration.getSnUserId())
                    .withRows(configuration.getRows())
                    .withStart(start);
            chunkList = getProfileConversations(thisRequest);
            retList.addAll(chunkList);
            start += configuration.getRows();
        } while (!(chunkList.isEmpty()));

        return retList;
    }
}