package org.apache.streams.json;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.google.common.collect.Lists;
import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

/**
 * Provides a base implementation for extracting json fields and
 * objects from datums using JsonPath syntax
 */
public class JsonPathExtractor implements StreamsProcessor {

    private final static String STREAMS_ID = "JsonPathExtractor";

    private final static Logger LOGGER = LoggerFactory.getLogger(JsonPathExtractor.class);

    private ObjectMapper mapper = new StreamsJacksonMapper();

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
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        String json = null;

        LOGGER.debug("{} processing {}", STREAMS_ID);

        if( entry.getDocument() instanceof ObjectNode ) {
            ObjectNode node = (ObjectNode) entry.getDocument();
            try {
                json = mapper.writeValueAsString(node);
            } catch (JsonProcessingException e) {
                LOGGER.warn(e.getMessage());
            }
        } else if( entry.getDocument() instanceof String ) {
            json = (String) entry.getDocument();
        }

        if( StringUtils.isNotEmpty(json)) {

            try {
                Object readResult = jsonPath.read(json);

                if (readResult instanceof String) {
                    String match = (String) readResult;
                    StreamsDatum matchDatum = new StreamsDatum(match);
                    result.add(matchDatum);
                } else if (readResult instanceof JSONObject) {
                    JSONObject match = (JSONObject) readResult;
                    ObjectNode objectNode = mapper.readValue(mapper.writeValueAsString(match), ObjectNode.class);
                    StreamsDatum matchDatum = new StreamsDatum(objectNode);
                    result.add(matchDatum);
                } else if (readResult instanceof JSONArray) {
                    JSONArray array = (JSONArray) readResult;
                    Iterator iterator = array.iterator();
                    while (iterator.hasNext()) {
                        Object item = iterator.next();
                        if( item instanceof String ) {
                            String match = (String) item;
                            StreamsDatum matchDatum = new StreamsDatum(match);
                            result.add(matchDatum);
                        } else if ( item instanceof JSONObject ) {
                            JSONObject match = (JSONObject) item;
                            ObjectNode objectNode = mapper.readValue(mapper.writeValueAsString(match), ObjectNode.class);
                            StreamsDatum matchDatum = new StreamsDatum(objectNode);
                            result.add(matchDatum);
                        }
                    }
                } else {

                }

            } catch( Exception e ) {
                LOGGER.warn(e.getMessage());
            }

        }

        return result;

    }

    @Override
    public void prepare(Object configurationObject) {
        if( configurationObject instanceof String )
            jsonPath = JsonPath.compile((String)(configurationObject));

        mapper.registerModule(new JsonOrgModule());
    }

    @Override
    public void cleanUp() {
        LOGGER.info("shutting down JsonPathExtractor for " + this.pathExpression);
    }
};
