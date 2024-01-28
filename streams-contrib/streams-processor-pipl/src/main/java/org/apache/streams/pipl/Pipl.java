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

package org.apache.streams.pipl;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.pipl.api.BasicSearchRequest;
import org.apache.streams.pipl.api.FullPersonSearchRequest;
import org.apache.streams.pipl.api.SearchPointerRequest;
import org.apache.streams.pipl.api.SearchResponse;
import org.apache.streams.pipl.config.PiplConfiguration;

import com.google.common.util.concurrent.Uninterruptibles;

import org.apache.juneau.collections.JsonMap;
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
public class Pipl implements Search {

  private static final Logger LOGGER = LoggerFactory.getLogger(Pipl.class);

  private PiplConfiguration configuration;

  JsonParser parser;
  JsonSerializer serializer;

  RestClient.Builder restClientBuilder;
  RestClient restClient;

  private static Map<PiplConfiguration, Pipl> INSTANCE_MAP = new ConcurrentHashMap<>();

  public static Pipl getInstance() throws InstantiationException {
    return getInstance(new ComponentConfigurator<>(PiplConfiguration.class).detectConfiguration());
  }

  public static Pipl getInstance(PiplConfiguration configuration) throws InstantiationException {
    if (INSTANCE_MAP.containsKey(configuration) && INSTANCE_MAP.get(configuration) != null) {
      return INSTANCE_MAP.get(configuration);
    } else {
      Pipl pipl = new Pipl(configuration);
      INSTANCE_MAP.put(configuration, pipl);
      return INSTANCE_MAP.get(configuration);
    }
  }

  private Pipl(PiplConfiguration configuration) {
    this.configuration = configuration;
    this.parser = JsonParser.DEFAULT.copy()
      .ignoreUnknownBeanProperties()
      .build();
    this.serializer = JsonSerializer.DEFAULT.copy()
      .trimEmptyCollections(true)
      .trimEmptyMaps(true)
      .build();
    this.restClientBuilder = RestClient.create()
      .disableCookieManagement()
      .disableRedirectHandling()
      .json()
      .parser(parser)
      .serializer(serializer)
      .rootUrl(baseUrl());
    if(configuration.getDebug() == true) {
      this.restClientBuilder.debug();
    }
    this.restClient = restClientBuilder.build();
  }

  private String baseUrl() {
    return "https://api.pipl.com/";
  }

  @Override
  public SearchResponse basicSearch(BasicSearchRequest request) {
    try {
      String requestJson = serializer.serialize(request);
      JsonMap requestMap = parser.parse(requestJson, JsonMap.class);
      requestMap.put("key", configuration.getKey());
      RestRequest call = restClient
              .get(baseUrl() + "search")
              .queryDataBean(requestMap)
              .ignoreErrors();
      String responseJson = call.getResponseAsString();
      SearchResponse response = parser.parse(responseJson, SearchResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new SearchResponse()
              .withHttpStatusCode(500l);
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public SearchResponse fullPersonSearch(FullPersonSearchRequest request) {
    try {
      String requestJson = serializer.serialize(request);
      JsonMap requestMap = parser.parse(requestJson, JsonMap.class);
      Object person = requestMap.remove("person");
      String personJson = serializer.serialize(person);
      requestMap.put("person", personJson);
      RestRequest call = restClient
          .post(baseUrl() + "search")
          .queryData("key", configuration.getKey())
          .queryDataBean(requestMap)
          .ignoreErrors();
      String responseJson = call.getResponseAsString();
      SearchResponse response = parser.parse(responseJson, SearchResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new SearchResponse()
              .withHttpStatusCode(500l);
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

  @Override
  public SearchResponse pointerSearch(SearchPointerRequest request) {
    try {
      RestRequest call = restClient
              .post(baseUrl() + "search")
              .queryData("key", configuration.getKey())
              .queryDataBean(request.getSearchPointer())
              .ignoreErrors();
      String responseJson = call.getResponseAsString();
      SearchResponse response = parser.parse(responseJson, SearchResponse.class);
      return response;
    } catch( Exception e ) {
      LOGGER.error("Exception", e);
      return new SearchResponse()
              .withHttpStatusCode(500l);
    } finally {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }
  }

}
