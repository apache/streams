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

package org.apache.streams.thedatagroup;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.thedatagroup.api.AppendRequest;
import org.apache.streams.thedatagroup.api.DemographicsAppendResponse;
import org.apache.streams.thedatagroup.api.EmailAppendResponse;
import org.apache.streams.thedatagroup.api.EmailLookupRequest;
import org.apache.streams.thedatagroup.api.IpLookupRequest;
import org.apache.streams.thedatagroup.api.LookupResponse;
import org.apache.streams.thedatagroup.api.MobileAppendResponse;
import org.apache.streams.thedatagroup.api.PhoneAppendResponse;
import org.apache.streams.thedatagroup.api.PhoneLookupRequest;
import org.apache.streams.thedatagroup.api.VehicleAppendResponse;
import org.apache.streams.thedatagroup.config.TheDataGroupConfiguration;

import com.google.common.util.concurrent.Uninterruptibles;

import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.rest.client.RestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of api.pipl.com interfaces using juneau.
 */
public class TheDataGroup implements SyncAppend, SyncLookup {

  private static final Logger LOGGER = LoggerFactory.getLogger(TheDataGroup.class);

  private TheDataGroupConfiguration configuration;

  JsonParser parser;
  JsonSerializer serializer;

  RestClient.Builder restClientBuilder;
  RestClient restClient;

  private static Map<TheDataGroupConfiguration, TheDataGroup> INSTANCE_MAP = new ConcurrentHashMap<>();

  public static TheDataGroup getInstance() throws InstantiationException {
    return getInstance(new ComponentConfigurator<>(TheDataGroupConfiguration.class).detectConfiguration());
  }

  public static TheDataGroup getInstance(TheDataGroupConfiguration configuration) throws InstantiationException {
    if (INSTANCE_MAP.containsKey(configuration) && INSTANCE_MAP.get(configuration) != null) {
      return INSTANCE_MAP.get(configuration);
    } else {
      TheDataGroup tdg = new TheDataGroup(configuration);
      INSTANCE_MAP.put(configuration, tdg);
      return INSTANCE_MAP.get(configuration);
    }
  }

  private TheDataGroup(TheDataGroupConfiguration configuration) {
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
      .header("AuthorizationToken", configuration.getAuthorizationToken())
      .header("x-api-key", configuration.getxApiKey())
      .parser(parser)
      .serializer(serializer)
      .rootUrl(baseUrl());
    if(configuration.getDebug() == true) {
      this.restClientBuilder.debug();
    }
    this.restClient = restClientBuilder.build();
  }

  private String baseUrl() {
    return "https://api.thedatagroup.com/v3/";
  }

  @Override
  public DemographicsAppendResponse appendDemographics(AppendRequest request) {
    try {
      RestRequest call = restClient
              .post(baseUrl() + "sync/append/demographics")
              .content(request)
              .ignoreErrors();
      String responseJson = call.getResponseAsString();
      DemographicsAppendResponse response = parser.parse(responseJson, DemographicsAppendResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new DemographicsAppendResponse();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public EmailAppendResponse appendEmail(AppendRequest request) {
    try {
      RestRequest call = restClient
              .post(baseUrl() + "sync/append/email")
              .content(request)
              .ignoreErrors();
      String responseJson = call.getResponseAsString();
      EmailAppendResponse response = parser.parse(responseJson, EmailAppendResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new EmailAppendResponse();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public PhoneAppendResponse appendPhone(AppendRequest request) {
    try {
      RestRequest call = restClient
              .post(baseUrl() + "sync/append/phone")
              .content(request)
              .ignoreErrors();
      String responseJson = call.getResponseAsString();
      PhoneAppendResponse response = parser.parse(responseJson, PhoneAppendResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new PhoneAppendResponse();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public MobileAppendResponse appendMobile(AppendRequest request) {
    try {
      RestRequest call = restClient
              .post(baseUrl() + "sync/append/mobile")
              .content(request)
              .ignoreErrors();
      String responseJson = call.getResponseAsString();
      MobileAppendResponse response = parser.parse(responseJson, MobileAppendResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new MobileAppendResponse();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public VehicleAppendResponse appendVehicle(AppendRequest request) {
    try {
      RestRequest call = restClient
              .post(baseUrl() + "sync/append/vehicle")
              .content(request)
              .ignoreErrors();
      String responseJson = call.getResponseAsString();
      VehicleAppendResponse response = parser.parse(responseJson, VehicleAppendResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new VehicleAppendResponse();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public LookupResponse lookupEmail(EmailLookupRequest request) {
    try {
      RestRequest call = restClient
              .post(baseUrl() + "sync/lookup/email")
              .content(request)
              .ignoreErrors();
      String responseJson = call.getResponseAsString();
      LookupResponse response = parser.parse(responseJson, LookupResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new LookupResponse();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public LookupResponse lookupMobile(PhoneLookupRequest request) {
    try {
      RestRequest call = restClient
              .post(baseUrl() + "sync/lookup/mobile")
              .content(request)
              .ignoreErrors();
      String responseJson = call.getResponseAsString();
      LookupResponse response = parser.parse(responseJson, LookupResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new LookupResponse();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public LookupResponse lookupIp(IpLookupRequest request) {
    try {
      RestRequest call = restClient
              .post(baseUrl() + "sync/lookup/ip")
              .content(request)
              .ignoreErrors();
      String responseJson = call.getResponseAsString();
      LookupResponse response = parser.parse(responseJson, LookupResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new LookupResponse();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public LookupResponse lookupPhone(PhoneLookupRequest request) {
    try {
      RestRequest call = restClient
              .post(baseUrl() + "sync/lookup/phone")
              .content(request)
              .ignoreErrors();
      String responseJson = call.getResponseAsString();
      LookupResponse response = parser.parse(responseJson, LookupResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new LookupResponse();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }
}
