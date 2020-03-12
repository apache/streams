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

import org.apache.streams.pojo.StreamsJacksonMapperConfiguration;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * StreamsJacksonMapper is the recommended interface to jackson for any streams component.
 *
 * <p></p>
 * Date-time formats that must be supported can be specified with constructor arguments.
 *
 * <p></p>
 * If no Date-time formats are specified, streams will use reflection to find formats.
 */
public class StreamsJacksonMapper extends ObjectMapper {

  private static Map<StreamsJacksonMapperConfiguration, StreamsJacksonMapper> INSTANCE_MAP = new ConcurrentHashMap<>();

  private StreamsJacksonMapperConfiguration configuration = new StreamsJacksonMapperConfiguration();

  /**
   * get default StreamsJacksonMapper.
   * @return StreamsJacksonMapper
   */
  public static StreamsJacksonMapper getInstance() {
    return getInstance(new StreamsJacksonMapperConfiguration());
  }

  /**
   * get custom StreamsJacksonMapper.
   * @param configuration StreamsJacksonMapperConfiguration
   * @return StreamsJacksonMapper
   */
  public static StreamsJacksonMapper getInstance(StreamsJacksonMapperConfiguration configuration) {
    if ( INSTANCE_MAP.containsKey(configuration)
         &&
         INSTANCE_MAP.get(configuration) != null) {
      return INSTANCE_MAP.get(configuration);
    } else {
      INSTANCE_MAP.put(configuration, new StreamsJacksonMapper(configuration));
      return INSTANCE_MAP.get(configuration);
    }
  }

  /**
   * get custom StreamsJacksonMapper.
   * @param format format
   * @return StreamsJacksonMapper
   */
  @Deprecated
  public static StreamsJacksonMapper getInstance(String format) {

    return new StreamsJacksonMapper(Collections.singletonList(format));

  }

  /**
   * get custom StreamsJacksonMapper.
   * @param formats formats
   * @return StreamsJacksonMapper
   */
  @Deprecated
  public static StreamsJacksonMapper getInstance(List<String> formats) {

    return new StreamsJacksonMapper(formats);

  }

  /*
    Use getInstance to get a globally shared thread-safe ObjectMapper,
    rather than call this constructor.  Reflection-based resolution of
    date-time formats across all modules can be slow and should only happen
    once per JVM.
   */
  protected StreamsJacksonMapper() {
    super();
    registerModule(new StreamsJacksonModule(configuration.getDateFormats()));
    if ( configuration.getEnableScala()) {
      registerModule(new DefaultScalaModule());
    }
    configure();
  }

  @Deprecated
  public StreamsJacksonMapper(String format) {
    super();
    registerModule(new StreamsJacksonModule(Collections.singletonList(format)));
    if ( configuration.getEnableScala()) {
      registerModule(new DefaultScalaModule());
    }
    configure();
  }

  @Deprecated
  public StreamsJacksonMapper(List<String> formats) {
    super();
    registerModule(new StreamsJacksonModule(formats));
    if ( configuration.getEnableScala()) {
      registerModule(new DefaultScalaModule());
    }
    configure();
  }

  public StreamsJacksonMapper(StreamsJacksonMapperConfiguration configuration) {
    super();
    registerModule(new StreamsJacksonModule(configuration.getDateFormats()));
    if ( configuration.getEnableScala()) {
      registerModule(new DefaultScalaModule());
    }
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
