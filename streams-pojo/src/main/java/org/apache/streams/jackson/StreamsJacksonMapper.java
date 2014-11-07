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

package org.apache.streams.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.List;

/**
 * Created by sblackmon on 3/27/14.
 */
public class StreamsJacksonMapper extends ObjectMapper {

    private static final StreamsJacksonMapper INSTANCE = new StreamsJacksonMapper();

    public static StreamsJacksonMapper getInstance(){
        return INSTANCE;
    }

    public static StreamsJacksonMapper getInstance(List<String> formats){

        StreamsJacksonMapper instance = new StreamsJacksonMapper(formats);

        return instance;

    }

    public StreamsJacksonMapper() {
        super();
        registerModule(new StreamsJacksonModule());
        configure();
    }

    public StreamsJacksonMapper(List<String> formats) {
        super();
        registerModule(new StreamsJacksonModule(formats));
        configure();
    }

    public void configure() {
        disable(com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, Boolean.FALSE);
        configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, Boolean.TRUE);
        configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, Boolean.TRUE);
        configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, Boolean.TRUE);
        configure(DeserializationFeature.WRAP_EXCEPTIONS, Boolean.FALSE);
        configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, Boolean.TRUE);
        // If a user has an 'object' that does not have an explicit mapping, don't cause the serialization to fail.
        configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, Boolean.FALSE);
        configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, Boolean.FALSE);
        configure(SerializationFeature.WRITE_NULL_MAP_VALUES, Boolean.FALSE);
        setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.DEFAULT);
    }

}
