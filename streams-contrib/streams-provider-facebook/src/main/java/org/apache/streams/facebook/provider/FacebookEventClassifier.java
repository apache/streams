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

package org.apache.streams.facebook.provider;

import org.apache.streams.facebook.Page;
import org.apache.streams.facebook.Post;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

/**
 * FacebookEventClassifier classifies facebook events.
 */
public class FacebookEventClassifier {

  private static final Logger LOGGER = LoggerFactory.getLogger(FacebookEventClassifier.class);

  /**
   * detectClass from json string.
   * @param json json string
   * @return detected Class
   */
  public static Class detectClass( String json ) {

    Objects.requireNonNull(json);
    Preconditions.checkArgument(StringUtils.isNotEmpty(json));

    ObjectNode objectNode;
    try {
      objectNode = (ObjectNode) StreamsJacksonMapper.getInstance().readTree(json);
    } catch (IOException ex) {
      LOGGER.error("Exception while trying to detect class: {}", ex.getMessage());
      return null;
    }

    if ( objectNode.findValue("about") != null) {
      return Page.class;
    } else if ( objectNode.findValue("statusType") != null ) {
      return Post.class;
    } else {
      return Post.class;
    }
  }
}
