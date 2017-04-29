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

package org.apache.streams.json;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Provides a base implementation for extracting json fields and
 * objects from datums using JsonPath syntax.
 */
public class JsonPathExtractor implements StreamsProcessor {

  private static final String STREAMS_ID = "JsonPathExtractor";

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonPathExtractor.class);

  private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  private String pathExpression;
  private JsonPath jsonPath;

  public JsonPathExtractor() {
    LOGGER.info("creating JsonPathExtractor");
  }

  public JsonPathExtractor(String pathExpression) {
    this.pathExpression = pathExpression;
    LOGGER.info("creating JsonPathExtractor for " + this.pathExpression);
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {

    List<StreamsDatum> result = new ArrayList<>();

    String json = null;

    LOGGER.debug("{} processing {}", STREAMS_ID);

    if ( entry.getDocument() instanceof ObjectNode ) {
      ObjectNode node = (ObjectNode) entry.getDocument();
      try {
        json = mapper.writeValueAsString(node);
      } catch (JsonProcessingException ex) {
        LOGGER.warn(ex.getMessage());
      }
    } else if ( entry.getDocument() instanceof String ) {
      json = (String) entry.getDocument();
    }

    if ( StringUtils.isNotEmpty(json)) {

      try {
        Object readResult = jsonPath.read(json);

        if (readResult instanceof String) {
          String match = (String) readResult;
          LOGGER.info("Matched String: " + match);
          StreamsDatum matchDatum = new StreamsDatum(match);
          result.add(matchDatum);
        } else if (readResult instanceof JSONObject) {
          JSONObject match = (JSONObject) readResult;
          LOGGER.info("Matched Object: " + match);
          ObjectNode objectNode = mapper.readValue(mapper.writeValueAsString(match), ObjectNode.class);
          StreamsDatum matchDatum = new StreamsDatum(objectNode);
          result.add(matchDatum);
        } else if (readResult instanceof JSONArray) {
          LOGGER.info("Matched Array:");
          JSONArray array = (JSONArray) readResult;
          for (Object item : array) {
            if (item instanceof String) {
              LOGGER.info("String Item:" + item);
              String match = (String) item;
              StreamsDatum matchDatum = new StreamsDatum(match);
              result.add(matchDatum);
            } else if (item instanceof JSONObject) {
              LOGGER.info("Object Item:" + item);
              JSONObject match = (JSONObject) item;
              ObjectNode objectNode = mapper.readValue(mapper.writeValueAsString(match), ObjectNode.class);
              StreamsDatum matchDatum = new StreamsDatum(objectNode);
              result.add(matchDatum);
            } else {
              LOGGER.info("Other Item:" + item.toString());
            }
          }
        } else {
          LOGGER.info("Other Match:" + readResult.toString());
        }

      } catch ( Exception ex ) {
        LOGGER.warn(ex.getMessage());
      }

    } else {
      LOGGER.warn("result empty");
    }

    return result;

  }

  @Override
  public void prepare(Object configurationObject) {
    if ( configurationObject instanceof String ) {
      jsonPath = JsonPath.compile((String) (configurationObject));
    } else if ( configurationObject instanceof String[] ) {
      jsonPath = JsonPath.compile(((String[]) (configurationObject))[0]);
    }
  }

  @Override
  public void cleanUp() {
    LOGGER.info("shutting down JsonPathExtractor for " + this.pathExpression);
  }
}
