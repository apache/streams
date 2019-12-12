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

package org.apache.streams.peopledatalabs;

import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.apache.juneau.ObjectMap;
import org.apache.juneau.rest.client.RestCallException;
import org.apache.streams.config.ComponentConfigurator;

import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.rest.client.RestCall;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.rest.client.RestClientBuilder;
import org.apache.streams.peopledatalabs.api.BulkEnrichPersonRequest;
import org.apache.streams.peopledatalabs.api.BulkEnrichPersonResponseItem;
import org.apache.streams.peopledatalabs.api.EnrichPersonRequest;
import org.apache.streams.peopledatalabs.api.EnrichPersonResponse;
import org.apache.streams.peopledatalabs.config.PeopleDataLabsConfiguration;
import org.apache.streams.peopledatalabs.pojo.Person;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Implementation of api.peopledatalabs.com interfaces using juneau.
 */
public class PeopleDataLabs implements PersonEnrichment {

  private static final Logger LOGGER = LoggerFactory.getLogger(PeopleDataLabs.class);

  private PeopleDataLabsConfiguration configuration;

  JsonParser parser;
  JsonSerializer serializer;

  RestClientBuilder restClientBuilder;
  RestClient restClient;

  private static Map<PeopleDataLabsConfiguration, PeopleDataLabs> INSTANCE_MAP = new ConcurrentHashMap<>();

  public static PeopleDataLabs getInstance() throws InstantiationException {
    return getInstance(new ComponentConfigurator<>(PeopleDataLabsConfiguration.class).detectConfiguration());
  }

  public static PeopleDataLabs getInstance(PeopleDataLabsConfiguration configuration) throws InstantiationException {
    if (INSTANCE_MAP.containsKey(configuration) && INSTANCE_MAP.get(configuration) != null) {
      return INSTANCE_MAP.get(configuration);
    } else {
      PeopleDataLabs peopleDataLabs = new PeopleDataLabs(configuration);
      INSTANCE_MAP.put(configuration, peopleDataLabs);
      return INSTANCE_MAP.get(configuration);
    }
  }

  private PeopleDataLabs(PeopleDataLabsConfiguration configuration) {
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
      .header("X-Api-Key", configuration.getToken())
      .parser(parser)
      .serializer(serializer)
      .rootUrl(baseUrl());
    if(configuration.getDebug() == true) {
      this.restClientBuilder.debug();
    }
    this.restClient = restClientBuilder.build();
  }

  private String baseUrl() {
    return "https://api.peopledatalabs.com/v4/";
  }

  @Override
  public EnrichPersonResponse enrichPerson(EnrichPersonRequest request) {
    try {
      // TODO: use juneau remoting here once upgraded and tested
      //      PersonEnrichment personEnrichment = restClient.getRemoteResource(PersonEnrichment.class);
      String requestJson = serializer.serialize(request);
      ObjectMap requestParams = new ObjectMap(requestJson);
      RestCall call = restClient
          .doGet(baseUrl() + "person")
          .accept("application/json")
          .contentType("application/json")
          .ignoreErrors()
          .queryIfNE(requestParams);
      String responseJson = call.getResponseAsString();
      EnrichPersonResponse result = parser.parse(responseJson, EnrichPersonResponse.class);
      return result;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new EnrichPersonResponse()
              .withStatus(500l);
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public List<BulkEnrichPersonResponseItem> bulkEnrichPerson(BulkEnrichPersonRequest request) {
    try {
      // TODO: use juneau remoting here once upgraded and tested
      //    PersonEnrichment personEnrichment = restClient.getRemoteResource(PersonEnrichment.class);
      //    List<EnrichPersonResponse> result = personEnrichment.bulkEnrichPerson(request);
      String requestJson = serializer.serialize(request);
      RestCall call = restClient
        .doPost(baseUrl() + "person/bulk")
        .accept("application/json")
        .contentType("application/json")
        .ignoreErrors()
        .body(new StringReader(requestJson));
      String responseJson = call.getResponseAsString();
      List<BulkEnrichPersonResponseItem> result = parser.parse(responseJson, List.class, BulkEnrichPersonResponseItem.class);
      return result;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new ArrayList<>();
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }
}