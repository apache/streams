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

package org.apache.streams.riak.http;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistReader;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.riak.pojo.RiakConfiguration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Queues;
import org.apache.commons.lang.NotImplementedException;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by sblackmon on 1/16/17.
 */
public class RiakHttpPersistReader implements StreamsPersistReader {

  private RiakConfiguration configuration;
  private RiakHttpClient client;

  private static final Logger LOGGER = LoggerFactory.getLogger(RiakHttpPersistReader.class);

  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  public RiakHttpPersistReader(RiakConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public String getId() {
    return "RiakHttpPersistReader";
  }

  @Override
  public void prepare(Object configurationObject) {
    client = RiakHttpClient.getInstance(this.configuration);
  }

  @Override
  public void cleanUp() {
    client = null;
  }

  @Override
  public StreamsResultSet readAll() {

    Queue<StreamsDatum> readAllQueue = constructQueue();

    URIBuilder lk = null;

    try {

      lk = new URIBuilder(client.baseURI.toString());
      lk.setPath(client.baseURI.getPath().concat("/buckets/"+configuration.getDefaultBucket()+"/keys"));
      lk.setParameter("keys", "true");

    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }

    HttpResponse lkResponse = null;
    try {
      HttpGet lkGet = new HttpGet(lk.build());
      lkResponse = client.client().execute(lkGet);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
      return null;
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
      return null;
    }

    String lkEntityString = null;
    try {
      lkEntityString = EntityUtils.toString(lkResponse.getEntity());
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
      return null;
    }

    JsonNode lkEntityNode = null;
    try {
      lkEntityNode = MAPPER.readValue(lkEntityString, JsonNode.class);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
      return null;
    }

    ArrayNode keysArray = null;
    keysArray = (ArrayNode) lkEntityNode.get("keys");
    Iterator<JsonNode> keysIterator = keysArray.iterator();

    while( keysIterator.hasNext()) {
      JsonNode keyNode = keysIterator.next();
      String key = keyNode.asText();

      URIBuilder gk = null;

      try {

        gk = new URIBuilder(client.baseURI.toString());
        gk.setPath(client.baseURI.getPath().concat("/buckets/"+configuration.getDefaultBucket()+"/keys/"+key));

      } catch (URISyntaxException e) {
        LOGGER.warn("URISyntaxException", e);
        continue;
      }

      HttpResponse gkResponse = null;
      try {
        HttpGet gkGet = new HttpGet(gk.build());
        gkResponse = client.client().execute(gkGet);
      } catch (IOException e) {
        LOGGER.warn("IOException", e);
        continue;
      } catch (URISyntaxException e) {
        LOGGER.warn("URISyntaxException", e);
        continue;
      }

      String gkEntityString = null;
      try {
        gkEntityString = EntityUtils.toString(gkResponse.getEntity());
      } catch (IOException e) {
        LOGGER.warn("IOException", e);
        continue;
      }

      readAllQueue.add(new StreamsDatum(gkEntityString, key));
    }

    return new StreamsResultSet(readAllQueue);
  }

  @Override
  public void startStream() {
    throw new NotImplementedException();
  }

  @Override
  public StreamsResultSet readCurrent() {
    throw new NotImplementedException();
  }

  @Override
  public StreamsResultSet readNew(BigInteger sequence) {
    throw new NotImplementedException();
  }

  @Override
  public StreamsResultSet readRange(DateTime start, DateTime end) {
    throw new NotImplementedException();
  }

  @Override
  public boolean isRunning() {
    return false;
  }

  private Queue<StreamsDatum> constructQueue() {
    return Queues.synchronizedQueue(new LinkedBlockingQueue<StreamsDatum>(10000));
  }
}
