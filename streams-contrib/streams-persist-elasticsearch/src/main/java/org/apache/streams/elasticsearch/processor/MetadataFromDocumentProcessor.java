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

package org.apache.streams.elasticsearch.processor;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Examines document to derive metadata fields.
 *
 * </p>
 * This is useful if you have a document with a populated 'id', and 'verb' or 'objectType' fields you want
 * to use as _id and _type respectively when indexing.
 */
public class MetadataFromDocumentProcessor implements StreamsProcessor, Serializable {

  public static final String STREAMS_ID = "MetadataFromDocumentProcessor";

  private ObjectMapper mapper;

  private static final Logger LOGGER = LoggerFactory.getLogger(MetadataFromDocumentProcessor.class);

  public MetadataFromDocumentProcessor() {
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {

    if ( mapper == null ) {
      mapper = StreamsJacksonMapper.getInstance();
    }

    List<StreamsDatum> result = Lists.newArrayList();

    Map<String, Object> metadata = entry.getMetadata();
    if ( metadata == null ) {
      metadata = Maps.newHashMap();
    }

    String id = null;
    String type = null;

    Object document = entry.getDocument();
    ObjectNode objectNode = null;
    if ( document instanceof String) {
      try {
        objectNode = mapper.readValue((String) document, ObjectNode.class);
      } catch (IOException ex) {
        LOGGER.warn("Can't deserialize to determine metadata", ex);
      }
    } else {
      try {
        objectNode = mapper.convertValue(document, ObjectNode.class);
      } catch (Exception ex) {
        LOGGER.warn("Can't deserialize to determine metadata", ex);
      }
    }
    if ( objectNode != null ) {
      if (objectNode.has("id")) {
        id = objectNode.get("id").textValue();
      }
      if (objectNode.has("verb")) {
        type = objectNode.get("verb").textValue();
      }
      if (objectNode.has("objectType")) {
        type = objectNode.get("objectType").textValue();
      }
    }

    if ( !Strings.isNullOrEmpty(id) ) {
      metadata.put("id", id);
    }
    if ( !Strings.isNullOrEmpty(type) ) {
      metadata.put("type", type);
    }

    entry.setId(id);
    entry.setMetadata(metadata);

    result.add(entry);

    return result;
  }

  @Override
  public void prepare(Object configurationObject) {
    mapper = StreamsJacksonMapper.getInstance();
    mapper.registerModule(new JsonOrgModule());
  }

  @Override
  public void cleanUp() {
    mapper = null;
  }

}
