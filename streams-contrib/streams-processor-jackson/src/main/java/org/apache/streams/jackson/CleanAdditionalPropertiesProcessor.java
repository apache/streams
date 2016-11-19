/*
Licensed to the Apache Software Foundation (ASF) under one
or more contributor license agreements.  See the NOTICE file
distributed with this work for additional information
regarding copyright ownership.  The ASF licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
*/

package org.apache.streams.jackson;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import com.google.common.collect.Lists;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This processor walks an input objectnode and corrects any artifacts
 * that may have occured from improper serialization of jackson beans.
 *
 * <p/>
 * The logic is also available for inclusion in other module via static import.
 */
public class CleanAdditionalPropertiesProcessor implements StreamsProcessor {

  public static final String STREAMS_ID = "CleanAdditionalPropertiesProcessor";

  private static final Logger LOGGER = LoggerFactory.getLogger(CleanAdditionalPropertiesProcessor.class);

  private ObjectMapper mapper;

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum datum) {
    List<StreamsDatum> result = Lists.newLinkedList();
    ObjectNode activity = this.mapper.convertValue(datum.getDocument(), ObjectNode.class);
    cleanAdditionalProperties(activity);
    datum.setDocument(activity);
    result.add(datum);
    return result;
  }

  @Override
  public void prepare(Object configurationObject) {
    this.mapper = StreamsJacksonMapper.getInstance();
    this.mapper.registerModule(new JsonOrgModule());
  }

  @Override
  public void cleanUp() {

  }

  /**
   * Recursively removes all additionalProperties maps.
   * @param node ObjectNode
   */
  public static void cleanAdditionalProperties(ObjectNode node) {
    if ( node.get("additionalProperties") != null ) {
      ObjectNode additionalProperties = (ObjectNode) node.get("additionalProperties");
      cleanAdditionalProperties(additionalProperties);
      Iterator<Map.Entry<String, JsonNode>> jsonNodeIterator = additionalProperties.fields();
      while ( jsonNodeIterator.hasNext() ) {
        Map.Entry<String, JsonNode> entry = jsonNodeIterator.next();
        node.put(entry.getKey(), entry.getValue());
      }
    }
  }
}
