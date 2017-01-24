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

package org.apache.streams.riak.binary;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.riak.pojo.RiakConfiguration;

import com.basho.riak.client.api.RiakClient;
import com.basho.riak.client.api.commands.kv.StoreValue;
import com.basho.riak.client.core.query.Location;
import com.basho.riak.client.core.query.Namespace;
import com.basho.riak.client.core.query.RiakObject;
import com.basho.riak.client.core.util.BinaryValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ExecutionException;

/**
 * RiakBinaryPersistWriter writes documents to riak via binary protocol.
 */
public class RiakBinaryPersistWriter implements StreamsPersistWriter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RiakBinaryPersistWriter.class);

  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  private RiakConfiguration configuration;
  private RiakBinaryClient client;

  public RiakBinaryPersistWriter(RiakConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public String getId() {
    return "RiakBinaryPersistWriter";
  }

  @Override
  public void prepare(Object configurationObject) {
    client = RiakBinaryClient.getInstance(this.configuration);
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

    try {

      RiakObject riakObject = new RiakObject();
      riakObject.setContentType(contentType);
      riakObject.setCharset(charset);
      riakObject.setValue(BinaryValue.create(document));

      Namespace ns = new Namespace(bucketType, bucket);
      StoreValue.Builder storeValueBuilder = new StoreValue.Builder(riakObject);

      if( id != null && StringUtils.isNotBlank(id)) {
        Location location = new Location(ns, id);
        storeValueBuilder = storeValueBuilder.withLocation(location);
      } else {
        storeValueBuilder = storeValueBuilder.withNamespace(ns);
      }

      StoreValue store = storeValueBuilder.build();

      StoreValue.Response storeResponse = client.client().execute(store);

      LOGGER.debug("storeResponse", storeResponse);

    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }

  }


}
