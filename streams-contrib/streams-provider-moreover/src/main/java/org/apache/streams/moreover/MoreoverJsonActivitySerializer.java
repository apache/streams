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

package org.apache.streams.moreover;

import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.moreover.api.Article;
import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

/**
 * Deserializes Moreover JSON format into Activities.
 */
public class MoreoverJsonActivitySerializer implements ActivitySerializer<String> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MoreoverJsonActivitySerializer.class);

  public MoreoverJsonActivitySerializer() {
  }

  @Override
  public String serializationFormat() {
    return "application/json+vnd.moreover.com.v1";
  }

  @Override
  public String serialize(Activity deserialized) {
    throw new UnsupportedOperationException("Cannot currently serialize to Moreover JSON");
  }

  @Override
  public Activity deserialize(String serialized) {
    serialized = serialized.replaceAll("\\[[ ]*\\]", "null");

    LOGGER.debug(serialized);

    ObjectMapper mapper = new ObjectMapper();
    AnnotationIntrospector introspector = new JaxbAnnotationIntrospector(mapper.getTypeFactory());
    mapper.setAnnotationIntrospector(introspector);
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
    mapper.configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, Boolean.FALSE);
    mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
    mapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
    mapper.configure(DeserializationFeature.WRAP_EXCEPTIONS, Boolean.TRUE);

    Article article;
    try {
      ObjectNode node = (ObjectNode)mapper.readTree(serialized);
      node.remove("tags");
      node.remove("locations");
      node.remove("companies");
      node.remove("topics");
      node.remove("media");
      node.remove("outboundUrls");
      ObjectNode jsonNodes = (ObjectNode) node.get("source").get("feed");
      jsonNodes.remove("editorialTopics");
      jsonNodes.remove("tags");
      jsonNodes.remove("autoTopics");
      article = mapper.convertValue(node, Article.class);
    } catch (IOException ex) {
      throw new IllegalArgumentException("Unable to deserialize", ex);
    }
    return MoreoverUtils.convert(article);
  }

  @Override
  public List<Activity> deserializeAll(List<String> serializedList) {
    throw new NotImplementedException("Not currently implemented");
  }


}
