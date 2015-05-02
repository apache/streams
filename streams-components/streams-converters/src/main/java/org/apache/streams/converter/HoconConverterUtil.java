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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * HoconConverterUtil supports HoconConverterProcessor in converting types via application
 * of hocon (https://github.com/typesafehub/config/blob/master/HOCON.md) scripts
 */
public class HoconConverterUtil {

    private final static Logger LOGGER = LoggerFactory.getLogger(HoconConverterUtil.class);

    private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public static Object convert(Object object, Class outClass, String hoconResource) {
        Config hocon = ConfigFactory.parseResources(hoconResource);
        return convert(object, outClass, hocon);
    }

    public static Object convert(Object object, Class outClass, String hoconResource, String outPath) {
        Config hocon = ConfigFactory.parseResources(hoconResource);
        return convert(object, outClass, hocon, outPath);
    }

    public static Object convert(Object object, Class outClass, Config hocon) {
        return convert(object, outClass, hocon, null);
    }

    public static Object convert(Object object, Class outClass, Config hocon, String outPath) {
        String json = null;
        Object outDoc = null;
        if( object instanceof String ) {
            json = (String) object;
        } else {
            try {
                json = mapper.writeValueAsString(object);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Failed to process input:", object);
                return outDoc;
            }
        }

        Config base = ConfigFactory.parseString(json);
        Config converted = hocon.withFallback(base);

        String outJson = null;
        try {
            if( outPath == null )
                outJson = converted.resolve().root().render(ConfigRenderOptions.concise());
            else {
                Config resolved = converted.resolve();
                ConfigObject outObject = resolved.withOnlyPath(outPath).root();
                ConfigValue outValue = outObject.get(outPath);
                outJson = outValue.render(ConfigRenderOptions.concise());
            }
        } catch (Exception e) {
            LOGGER.warn("Failed to convert:", json);
            LOGGER.warn(e.getMessage());
        }
        if( outClass == String.class )
            return outJson;
        else {
            try {
                outDoc = mapper.readValue( outJson, outClass );
            } catch (IOException e) {
                LOGGER.warn("Failed to convert:", object);
            }
        }
        return outDoc;
    }
}
