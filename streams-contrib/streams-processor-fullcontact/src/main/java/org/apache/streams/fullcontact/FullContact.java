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

package org.apache.streams.fullcontact;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.fullcontact.api.EnrichCompanyRequest;
import org.apache.streams.fullcontact.api.EnrichPersonRequest;
import org.apache.streams.fullcontact.api.EnrichPersonResponse;
import org.apache.streams.fullcontact.config.FullContactConfiguration;
import org.apache.streams.fullcontact.pojo.CompanySummary;

import com.google.common.util.concurrent.Uninterruptibles;

import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.rest.client.RestRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of api.fullcontact.com interfaces using juneau.
 */
public class FullContact implements CompanyEnrichment, PersonEnrichment {

  private static final Logger LOGGER = LoggerFactory.getLogger(FullContact.class);

  private FullContactConfiguration configuration;

  protected JsonParser parser;
  protected JsonSerializer serializer;

  protected RestClient.Builder restClientBuilder;
  protected RestClient restClient;

  private static Map<FullContactConfiguration, FullContact> INSTANCE_MAP = new ConcurrentHashMap<>();

  public static FullContact getInstance() throws InstantiationException {
    return getInstance(new ComponentConfigurator<>(FullContactConfiguration.class).detectConfiguration());
  }

  public static FullContact getInstance(FullContactConfiguration configuration) throws InstantiationException {
    if (INSTANCE_MAP.containsKey(configuration) && INSTANCE_MAP.get(configuration) != null) {
      return INSTANCE_MAP.get(configuration);
    } else {
      FullContact fullContact = new FullContact(configuration);
      INSTANCE_MAP.put(configuration, fullContact);
      return INSTANCE_MAP.get(configuration);
    }
  }

  private FullContact(FullContactConfiguration configuration) {
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
      .disableAutomaticRetries()
      .disableCookieManagement()
      .disableRedirectHandling()
      .header("Authorization", "Bearer "+configuration.getToken())
      .parser(parser)
      .serializer(serializer)
      .rootUrl(baseUrl());
    if(configuration.getDebug() == true) {
      this.restClientBuilder.debug();
    }
    this.restClient = restClientBuilder.build();
  }

  private String baseUrl() {
    return "https://api.fullcontact.com/v3/";
  }

  @Override
  public CompanySummary enrichCompany(EnrichCompanyRequest request) {
    try {
      String requestJson = serializer.serialize(request);
      RestRequest call = restClient
        .post(baseUrl() + "company.enrich")
        .accept("application/json")
        .contentType("application/json")
        .content(new StringReader(requestJson));
      String responseJson = call.getResponseAsString();
      CompanySummary result = parser.parse(responseJson, CompanySummary.class);
      return result;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new CompanySummary();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public EnrichPersonResponse enrichPerson(EnrichPersonRequest request) {
    try {
      String requestJson = serializer.serialize(request);
      RestRequest call = restClient
              .post(baseUrl() + "person.enrich")
              .accept("application/json")
              .contentType("application/json")
              .content(new StringReader(requestJson))
              .ignoreErrors();
      String responseJson = call.getResponseAsString();
      EnrichPersonResponse response = parser.parse(responseJson, EnrichPersonResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new EnrichPersonResponse().withMessage(e.getMessage());
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }
}
