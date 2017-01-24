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
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.riak.binary.RiakBinaryPersistWriter;
import org.apache.streams.riak.pojo.RiakConfiguration;

import com.basho.riak.client.core.query.Location;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * RiakHttpPersistWriter writes documents to riak via http.
 */
public class RiakHttpPersistWriter implements StreamsPersistWriter {

  private RiakConfiguration configuration;
  private RiakHttpClient client;

  private static final Logger LOGGER = LoggerFactory.getLogger(RiakHttpPersistWriter.class);

  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  public RiakHttpPersistWriter(RiakConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public String getId() {
    return "RiakHttpPersistWriter";
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
  public void write(StreamsDatum entry) {

    Objects.nonNull(client);

    String id = null;
    String document;
    String bucket;
    String bucketType;
    String contentType;
    String charset;
    if( StringUtils.isNotBlank(entry.getId())) {
      id = entry.getId();
    }
    if( entry.getDocument() instanceof String) {
      document = (String)entry.getDocument();
    } else {
      try {
        document = MAPPER.writeValueAsString(entry.getDocument());
      } catch( Exception e ) {
        LOGGER.warn("Exception", e);
        return;
      }
    }
    if( entry.getMetadata() != null
        && entry.getMetadata().containsKey("bucket")
        && entry.getMetadata().get("bucket") instanceof String
        && StringUtils.isNotBlank((String)entry.getMetadata().get("bucket") )) {
      bucket = (String)entry.getMetadata().get("bucket");
    } else {
      bucket = configuration.getDefaultBucket();
    }
    if( entry.getMetadata() != null
        && entry.getMetadata().containsKey("bucketType")
        && entry.getMetadata().get("bucketType") instanceof String
        && StringUtils.isNotBlank((String)entry.getMetadata().get("bucketType") )) {
      bucketType = (String)entry.getMetadata().get("bucketType");
    } else {
      bucketType = configuration.getDefaultBucketType();
    }
    if( entry.getMetadata() != null
        && entry.getMetadata().containsKey("charset")
        && entry.getMetadata().get("charset") instanceof String
        && StringUtils.isNotBlank((String)entry.getMetadata().get("charset") )) {
      charset = (String)entry.getMetadata().get("charset");
    } else {
      charset = configuration.getDefaultCharset();
    }
    if( entry.getMetadata() != null
        && entry.getMetadata().containsKey("contentType")
        && entry.getMetadata().get("contentType") instanceof String
        && StringUtils.isNotBlank((String)entry.getMetadata().get("contentType") )) {
      contentType = (String)entry.getMetadata().get("contentType");
    } else {
      contentType = configuration.getDefaultContentType();
    }

    URIBuilder uriBuilder = new URIBuilder(client.baseURI);
    if( bucket != null && StringUtils.isNotBlank(bucket)) {
      uriBuilder.setPath("/riak/"+bucket);
    }
    if( id != null && StringUtils.isNotBlank(id)) {
      uriBuilder.setPath("/riak/"+bucket+"/"+id);
    }

    URI uri;
    try {
      uri = uriBuilder.build();
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
      return;
    }

    HttpPost post = new HttpPost();
    post.setHeader("Content-Type", contentType + "; charset=" + charset);
    post.setURI(uri);
    HttpEntity entity;
    try {
      entity = new StringEntity(document);
      post.setEntity(entity);
    } catch (UnsupportedEncodingException e) {
      LOGGER.warn("UnsupportedEncodingException", e);
      return;
    }

    try {
      HttpResponse response = client.client().execute(post);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
      return;
    }

  }
}
