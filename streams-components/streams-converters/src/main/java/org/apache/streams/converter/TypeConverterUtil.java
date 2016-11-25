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

package org.apache.streams.converter;

import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * TypeConverterUtil supports TypeConverterProcessor in converting between String json and
 * jackson-compatible POJO objects.
 */
public class TypeConverterUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(TypeConverterUtil.class);

  private static final TypeConverterUtil INSTANCE = new TypeConverterUtil();

  public static TypeConverterUtil getInstance() {
    return INSTANCE;
  }

  public Object convert(Object object, Class outClass) {
    return TypeConverterUtil.getInstance().convert(object, outClass, StreamsJacksonMapper.getInstance());
  }

  /**
   * convert
   * @param object
   * @param outClass
   * @param mapper
   * @return
   */
  public Object convert(Object object, Class outClass, ObjectMapper mapper) {
    ObjectNode node = null;
    Object outDoc = null;
    if ( object instanceof String ) {
      try {
        node = mapper.readValue((String)object, ObjectNode.class);
      } catch (IOException ex) {
        LOGGER.warn(ex.getMessage());
        LOGGER.warn(object.toString());
      }
    } else {
      node = mapper.convertValue(object, ObjectNode.class);
    }

    if(node != null) {
      try {
        if ( outClass == String.class ) {
          outDoc = mapper.writeValueAsString(node);
        } else {
          outDoc = mapper.convertValue(node, outClass);
        }
      } catch (Throwable ex) {
        LOGGER.warn(ex.getMessage());
        LOGGER.warn(node.toString());
      }
    }

    return outDoc;
  }
}
