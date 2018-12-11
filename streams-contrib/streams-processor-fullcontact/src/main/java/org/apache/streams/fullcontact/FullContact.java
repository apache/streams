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

import org.apache.streams.fullcontact.api.EnrichCompanyRequest;
import org.apache.streams.fullcontact.api.EnrichPersonRequest;
import org.apache.streams.fullcontact.config.FullContactConfiguration;
import org.apache.streams.fullcontact.pojo.CompanySummary;
import org.apache.streams.fullcontact.pojo.PersonSummary;

import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.rest.client.RestCall;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Implementation of api.fullcontact.com interfaces using juneau.
 */
public class FullContact implements CompanyEnrichment, PersonEnrichment {

  private static final Logger LOGGER = LoggerFactory.getLogger(FullContact.class);

  private FullContactConfiguration configuration;

  JsonParser parser;
  JsonSerializer serializer;

  RestClientBuilder restClientBuilder;
  RestClient restClient;

  private FullContact(FullContactConfiguration configuration) throws InstantiationException {
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
      .disableAutomaticRetries()
      .disableCookieManagement()
      .disableRedirectHandling()
      .header("Authorization", "Bearer "+configuration.getToken())
      .parser(parser)
      .serializer(serializer)
      .rootUrl(baseUrl());
    this.restClient = restClientBuilder.build();
  }

  private String baseUrl() {
    return "https://api.fullcontact.com/v3/";
  }

  @Override
  public CompanySummary enrichCompany(EnrichCompanyRequest request) {
    try {
      RestCall call = restClient
        .doPost(baseUrl() + "company.enrich")
        .input(request);
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
  public PersonSummary enrichPerson(EnrichPersonRequest request) {
    try {
      RestCall call = restClient
        .doPost(baseUrl() + "company.enrich")
        .input(request);
      String responseJson = call.getResponseAsString();
      PersonSummary result = parser.parse(responseJson, PersonSummary.class);
      return result;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new PersonSummary();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }
}