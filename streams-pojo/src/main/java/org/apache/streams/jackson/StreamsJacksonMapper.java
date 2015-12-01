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
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * StreamsJacksonMapper is the recommended interface to jackson for any streams component.
 *
 * Date-time formats that must be supported can be specified with constructor arguments.
 *
 * If no Date-time formats are specified, streams will use reflection to find formats.
 */
public class StreamsJacksonMapper extends ObjectMapper {

    private static final StreamsJacksonMapper INSTANCE = new StreamsJacksonMapper();

    public static StreamsJacksonMapper getInstance(){
        return INSTANCE;
    }

    public static StreamsJacksonMapper getInstance(String format){

        StreamsJacksonMapper instance = new StreamsJacksonMapper(Lists.newArrayList(format));

        return instance;

    }
    public static StreamsJacksonMapper getInstance(List<String> formats){

        StreamsJacksonMapper instance = new StreamsJacksonMapper(formats);

        return instance;

    }

    /*
      Use getInstance to get a globally shared thread-safe ObjectMapper,
      rather than call this constructor.  Reflection-based resolution of
      date-time formats across all modules can be slow and should only happen
      once per JVM.
     */
    protected StreamsJacksonMapper() {
        super();
        registerModule(new DefaultScalaModule());
        registerModule(new StreamsJacksonModule());
        configure();
    }

    public StreamsJacksonMapper(String format) {
        super();
        registerModule(new DefaultScalaModule());
        registerModule(new StreamsJacksonModule(Lists.newArrayList(format)));
        configure();
    }

    public StreamsJacksonMapper(List<String> formats) {
        super();
        registerModule(new DefaultScalaModule());
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
        setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

}
