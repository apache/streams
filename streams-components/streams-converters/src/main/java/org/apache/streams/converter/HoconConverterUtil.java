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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * HoconConverterUtil supports HoconConverterProcessor in converting types via application
 * of hocon (https://github.com/typesafehub/config/blob/master/HOCON.md) scripts.
 */
public class HoconConverterUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(HoconConverterUtil.class);

  private static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  private static final HoconConverterUtil INSTANCE = new HoconConverterUtil();

  public static HoconConverterUtil getInstance() {
    return INSTANCE;
  }

  public Object convert(Object object, Class outClass, String hoconResource) {
    Config hocon = ConfigFactory.parseResources(hoconResource);
    return convert(object, outClass, hocon, null);
  }

  public Object convert(Object object, Class outClass, String hoconResource, String outPath) {
    Config hocon = ConfigFactory.parseResources(hoconResource);
    return convert(object, outClass, hocon, outPath);
  }

  public Object convert(Object object, Class outClass, String hoconResource, String inPath, String outPath) {
    Config hocon = ConfigFactory.parseResources(hoconResource);
    return convert(object, outClass, hocon, inPath, outPath);
  }

  public Object convert(Object object, Class outClass, Config hocon, String outPath) {
    return convert(object, outClass, hocon, null, outPath);
  }

  /**
   * convert.
   * @param object object
   * @param outClass outClass
   * @param hocon hocon
   * @param inPath inPath
   * @param outPath outPath
   * @return result
   */
  public Object convert(Object object, Class outClass, Config hocon, String inPath, String outPath) {
    String json;
    Object outDoc = null;
    if ( object instanceof String ) {
      json = (String) object;
    } else {
      try {
        json = mapper.writeValueAsString(object);
      } catch (JsonProcessingException ex) {
        LOGGER.warn("Failed to process input:", object);
        return outDoc;
      }
    }

    Config base;
    if( inPath == null) {
      base = ConfigFactory.parseString(json);
    } else {
      ObjectNode node;
      try {
        node = mapper.readValue(json, ObjectNode.class);
        ObjectNode root = mapper.createObjectNode();
        root.set(inPath, node);
        json = mapper.writeValueAsString(root);
        base = ConfigFactory.parseString(json);
      } catch (Exception ex) {
        LOGGER.warn("Failed to process input:", object);
        return outDoc;
      }
    }

    Config converted = hocon.withFallback(base);

    String outJson = null;
    try {
      if( outPath == null ) {
        outJson = converted.resolve().root().render(ConfigRenderOptions.concise());
      } else {
        Config resolved = converted.resolve();
        ConfigObject outObject = resolved.withOnlyPath(outPath).root();
        ConfigValue outValue = outObject.get(outPath);
        outJson = outValue.render(ConfigRenderOptions.concise());
      }
    } catch (Exception ex) {
      LOGGER.warn("Failed to convert:", json);
      LOGGER.warn(ex.getMessage());
    }
    if ( outClass == String.class )
      return outJson;
    else {
      try {
        outDoc = mapper.readValue( outJson, outClass );
      } catch (IOException ex) {
        LOGGER.warn("Failed to convert:", object);
      }
    }
    return outDoc;
  }
}
