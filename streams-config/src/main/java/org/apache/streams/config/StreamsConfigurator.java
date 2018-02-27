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

package org.apache.streams.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigResolveOptions;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * StreamsConfigurator supplies the entire typesafe tree to runtimes and modules.
 *
 * StreamsConfigurator also supplies StreamsConfiguration POJO to runtimes and modules.
 *
 */
public class StreamsConfigurator<T extends StreamsConfiguration> {

  private Class<T> configClass;

  private static final Logger LOGGER = LoggerFactory.getLogger(ComponentConfigurator.class);

  private static final ObjectMapper mapper = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);

  public StreamsConfigurator(Class<T> configClass) {
    this.configClass = configClass;
  }

  /*
      Pull all configuration files from the classpath, system properties, and environment variables
   */
  private static Config config = ConfigFactory.load();

  public static Config getConfig() {
    return config.resolve();
  }

  public static Config rawConfig() {
    return config;
  }

  public static void addConfig(Config newConfig) {
    config = newConfig.withFallback(config);
  }

  public static void addConfig(Config newConfig, String path) {
    config = newConfig.atPath(path).withFallback(config);
  }

  public static void setConfig(Config newConfig) {
    config = newConfig;
  }

  public static Config resolveConfig(String configUrl) throws MalformedURLException {
    URL url = new URL(configUrl);
    Config urlConfig = ConfigFactory.parseURL(url);
    urlConfig.resolve();
    config = urlConfig;
    return config;
  }
  
  public static StreamsConfiguration detectConfiguration() {
    return detectConfiguration(config);
  }

  public static StreamsConfiguration detectConfiguration(Config typesafeConfig) {

    Config streamsConfigurationRoot = null;
    if( typesafeConfig.hasPath(StreamsConfiguration.class.getCanonicalName())) {
      streamsConfigurationRoot = typesafeConfig.getConfig(StreamsConfiguration.class.getCanonicalName());
    } else if (typesafeConfig.hasPath(StreamsConfiguration.class.getSimpleName())) {
      streamsConfigurationRoot = typesafeConfig.getConfig(StreamsConfiguration.class.getSimpleName());
    } else {
      streamsConfigurationRoot = typesafeConfig;
    }

    StreamsConfiguration pojoConfig = null;

    try {
      pojoConfig = mapper.readValue(streamsConfigurationRoot.resolve().root().render(ConfigRenderOptions.concise()), StreamsConfiguration.class);
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.warn("Could not parse:", typesafeConfig);
    }

    return pojoConfig;
  }

  public static StreamsConfiguration mergeConfigurations(Config base, Config delta) {

    Config merged = delta.withFallback(base);

    StreamsConfiguration pojoConfig = null;

    try {
      pojoConfig = mapper.readValue(merged.resolve().root().render(ConfigRenderOptions.concise()), StreamsConfiguration.class);
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.warn("Failed to merge.");
    }

    return pojoConfig;
  }

  public T detectCustomConfiguration() {
    Config rootConfig = getConfig();
    return detectCustomConfiguration(rootConfig);
  }

  public T detectCustomConfiguration(String path) {
    Config rootConfig = getConfig();
    return detectCustomConfiguration(rootConfig, path);
  }

  public T detectCustomConfiguration(Config rootConfig) {
    return detectCustomConfiguration(rootConfig, "");
  }

  public T detectCustomConfiguration(Config rootConfig, String path) {

    if( StringUtils.isNotBlank(path) && rootConfig.hasPath(path) ) {
      rootConfig = rootConfig.getConfig(path);
    }

    // for each field of the top-level configuration,
    //    populate using a ComponentConfigurator from its type.

    ComponentConfigurator<StreamsConfiguration> streamsConfigConfigurator = new ComponentConfigurator(configClass);
    StreamsConfiguration streamsConfiguration = streamsConfigConfigurator.detectConfiguration();

    Map<String, Object> pojoMap = new HashMap<>();

    try {
      pojoMap.putAll(mapper.convertValue(streamsConfiguration, Map.class));
      pojoMap.putAll(mapper.readValue(rootConfig.resolve().root().render(ConfigRenderOptions.concise()), Map.class));
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.warn("Could not parse:", rootConfig);
    }

    Field[] fields = configClass.getDeclaredFields();

    for( Field field : fields ) {
      Class type = field.getType();
      if( type != String.class && !ClassUtils.isPrimitiveOrWrapper(type) ) {
        ComponentConfigurator configurator = new ComponentConfigurator(type);
        Serializable fieldValue = configurator.detectConfiguration(field.getName());
        pojoMap.put(field.getName(), fieldValue);
      }
    }

    T pojoConfig = null;

    try {
      pojoConfig = mapper.convertValue(pojoMap, configClass);
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.warn("Could not parse:", rootConfig);
    }

    return pojoConfig;
  }

}
