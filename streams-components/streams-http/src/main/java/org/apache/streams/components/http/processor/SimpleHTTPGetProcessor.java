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

package org.apache.streams.components.http.processor;

import org.apache.streams.components.http.HttpProcessorConfiguration;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.ActivityObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Processor retrieves contents from an known url and stores the resulting object in an extension field.
 */
public class SimpleHTTPGetProcessor implements StreamsProcessor {

  private static final String STREAMS_ID = "SimpleHTTPGetProcessor";

  // from root config id
  private static final String EXTENSION = "account_type";

  private static final Logger LOGGER = LoggerFactory.getLogger(SimpleHTTPGetProcessor.class);

  protected ObjectMapper mapper;

  protected URIBuilder uriBuilder;

  protected CloseableHttpClient httpclient;

  protected HttpProcessorConfiguration configuration;

  protected String authHeader;

  /**
   * SimpleHTTPGetProcessor constructor
   */
  public SimpleHTTPGetProcessor() {
    this(new ComponentConfigurator<>(HttpProcessorConfiguration.class).detectConfiguration());
  }

  /**
   * SimpleHTTPGetProcessor constructor - uses provided HttpProcessorConfiguration.
   */
  public SimpleHTTPGetProcessor(HttpProcessorConfiguration processorConfiguration) {
    LOGGER.info("creating SimpleHTTPGetProcessor");
    LOGGER.info(processorConfiguration.toString());
    this.configuration = processorConfiguration;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  /**
   Override this to store a result other than exact json representation of response.
   */
  protected ObjectNode prepareExtensionFragment(String entityString) {

    try {
      return mapper.readValue(entityString, ObjectNode.class);
    } catch (IOException ex) {
      LOGGER.warn(ex.getMessage());
      return null;
    }
  }

  /**
   Override this to place result in non-standard location on document.
   */
  protected ObjectNode getRootDocument(StreamsDatum datum) {

    try {
      String json = datum.getDocument() instanceof String
          ? (String) datum.getDocument()
          : mapper.writeValueAsString(datum.getDocument());
      return mapper.readValue(json, ObjectNode.class);
    } catch (IOException ex) {
      LOGGER.warn(ex.getMessage());
      return null;
    }

  }

  /**
   Override this to place result in non-standard location on document.
   */
  protected ActivityObject getEntityToExtend(ObjectNode rootDocument) {

    if ( this.configuration.getEntity().equals(HttpProcessorConfiguration.Entity.ACTIVITY)) {
      return mapper.convertValue(rootDocument, ActivityObject.class);
    } else {
      return mapper.convertValue(rootDocument.get(this.configuration.getEntity().toString()), ActivityObject.class);
    }
  }

  /**
   Override this to place result in non-standard location on document.
   */
  protected ObjectNode setEntityToExtend(ObjectNode rootDocument, ActivityObject activityObject) {

    if ( this.configuration.getEntity().equals(HttpProcessorConfiguration.Entity.ACTIVITY)) {
      return mapper.convertValue(activityObject, ObjectNode.class);
    } else {
      rootDocument.set(this.configuration.getEntity().toString(), mapper.convertValue(activityObject, ObjectNode.class));
    }

    return rootDocument;

  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {

    List<StreamsDatum> result = new ArrayList<>();

    ObjectNode rootDocument = getRootDocument(entry);

    Map<String, String> params = prepareParams(entry);

    URI uri = prepareURI(params);

    HttpGet httpget = prepareHttpGet(uri);

    CloseableHttpResponse response = null;

    String entityString = null;
    try {
      response = httpclient.execute(httpget);
      HttpEntity entity = response.getEntity();
      // TODO: handle retry
      if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
        entityString = EntityUtils.toString(entity);
      }
    } catch (IOException ex) {
      LOGGER.error("IO error:\n{}\n{}\n{}", uri.toString(), response, ex.getMessage());
      return result;
    } finally {
      try {
        if (response != null) {
          response.close();
        }
      } catch (IOException ignored) {
        LOGGER.trace("IOException", ignored);
      }
    }

    if( entityString == null ) {
      return result;
    }

    LOGGER.debug(entityString);

    ObjectNode extensionFragment = prepareExtensionFragment(entityString);

    ActivityObject extensionEntity = getEntityToExtend(rootDocument);

    ExtensionUtil.getInstance().addExtension(extensionEntity, this.configuration.getExtension(), extensionFragment);

    rootDocument = setEntityToExtend(rootDocument, extensionEntity);

    entry.setDocument(rootDocument);

    result.add(entry);

    return result;

  }

  /**
   Override this to alter request URI.
   */
  protected URI prepareURI(Map<String, String> params) {

    URI uri = null;
    for ( Map.Entry<String,String> param : params.entrySet()) {
      uriBuilder = uriBuilder.setParameter(param.getKey(), param.getValue());
    }
    try {
      uri = uriBuilder.build();
    } catch (URISyntaxException ex) {
      LOGGER.error("URI error {}", uriBuilder.toString());
    }
    return uri;
  }

  /**
   Override this to add parameters to the request.
   */
  protected Map<String, String> prepareParams(StreamsDatum entry) {
    return new HashMap<>();
  }

  /**
   Override this to set a payload on the request.
   */
  protected ObjectNode preparePayload(StreamsDatum entry) {
    return null;
  }

  /**
   * Override this to set the URI for the request or modify headers.
   * @param uri uri
   * @return result
   */
  public HttpGet prepareHttpGet(URI uri) {
    HttpGet httpget = new HttpGet(uri);
    httpget.addHeader("content-type", this.configuration.getContentType());
    if (StringUtils.isNotBlank(authHeader)) {
      httpget.addHeader("Authorization", String.format("Basic %s", authHeader));
    }
    return httpget;
  }

  @Override
  public void prepare(Object configurationObject) {

    mapper = StreamsJacksonMapper.getInstance();

    uriBuilder = new URIBuilder()
        .setScheme(this.configuration.getProtocol())
        .setHost(this.configuration.getHostname())
        .setPath(this.configuration.getResourcePath());

    if (StringUtils.isNotBlank(configuration.getAccessToken()) ) {
      uriBuilder = uriBuilder.addParameter("access_token", configuration.getAccessToken());
    }
    if (StringUtils.isNotBlank(configuration.getUsername())
         &&
        StringUtils.isNotBlank(configuration.getPassword())) {
      String string = configuration.getUsername() + ":" + configuration.getPassword();
      authHeader = Base64.encodeBase64String(string.getBytes());
    }
    httpclient = HttpClients.createDefault();
  }

  @Override
  public void cleanUp() {
    LOGGER.info("shutting down SimpleHTTPGetProcessor");
    try {
      httpclient.close();
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      try {
        httpclient.close();
      } catch (IOException e2) {
        e2.printStackTrace();
      } finally {
        httpclient = null;
      }
    }
  }
}
