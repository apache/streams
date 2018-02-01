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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * ComponentConfigurator supplies serializable configuration beans derived from a specified typesafe path or object.
 *
 * <p/>
 * Typically a component will select a 'default' typesafe path to be used if no other path or object is provided.
 *
 * <p/>
 * For example, streams-persist-elasticsearch will use 'elasticsearch' by default, but an implementation
 *   such as github.com/apache/streams-examples/local/elasticsearch-reindex
 *   can resolve a reader from elasticsearch.source
 *   and a writer from elasticsearch.destination.
 *
 */
public class ComponentConfigurator<T extends Serializable> {

  private Class<T> configClass;

  public ComponentConfigurator(Class<T> configClass) {
    this.configClass = configClass;
  }

  private static final Logger LOGGER = LoggerFactory.getLogger(ComponentConfigurator.class);

  private static final ObjectMapper mapper = new ObjectMapper();

  /**
   * resolve a serializable configuration pojo compiled from appropriate parts of the global JVM configuration tree.
   *
   * the entire object, or fragments of it, will be collected and merged from:
   *   - the simple class name of the configured class
   *   - the fully qualified class name of the configured class
   *   - any of the ancestor packages of the configured class
   *
   * @return result
   */
  public T detectConfiguration() {

    T pojoConfig = null;

    Config rootConfig = StreamsConfigurator.getConfig();

    Config cascadeConfig = null;
    String[] canonicalNameParts = StringUtils.split(configClass.getCanonicalName(), '.');

    for( int partIndex = 1; partIndex <= canonicalNameParts.length; partIndex++) {
      String[] partialPathParts = ArrayUtils.subarray(canonicalNameParts, 0, partIndex);
      String partialPath = StringUtils.join(partialPathParts, '.');

      if( rootConfig.hasPath(partialPath) ) {
        Config partialPathConfig = rootConfig.getConfig(partialPath);
        if( cascadeConfig == null ) {
          cascadeConfig = partialPathConfig;
        } else {
          cascadeConfig = partialPathConfig.withFallback(cascadeConfig);
        }
      }

    }

    if( rootConfig.hasPath(configClass.getSimpleName()) ) {
      Config simpleNameConfig = rootConfig.getConfig(configClass.getSimpleName());
      if( cascadeConfig == null ) {
        cascadeConfig = simpleNameConfig;
      } else {
        cascadeConfig = simpleNameConfig.withFallback(cascadeConfig);
      }
    }

    try {
      pojoConfig = mapper.readValue(cascadeConfig.root().render(ConfigRenderOptions.concise()), configClass);
    } catch (Exception ex) {
      ex.printStackTrace();
      LOGGER.warn("Could not parse:", cascadeConfig);
    }

    return pojoConfig;
  }

  /**
   * resolve a serializable configuration pojo from a given typesafe config object.
   * @param typesafeConfig typesafeConfig
   * @return result
   */
  public T detectConfiguration(Config typesafeConfig) {

    T pojoConfig = null;

    try {
      pojoConfig = mapper.readValue(typesafeConfig.root().render(ConfigRenderOptions.concise()), configClass);
    } catch (Exception ex) {
      ex.printStackTrace();
      LOGGER.warn("Could not parse:", typesafeConfig);
    }

    return pojoConfig;
  }

  /**
   * resolve a serializable configuration pojo from a portion of the JVM config object.
   * @param subConfig subConfig
   * @return result
   */
  public T detectConfiguration(String subConfig) {
    Config streamsConfig = StreamsConfigurator.getConfig();
    return detectConfiguration( streamsConfig.getConfig(subConfig));
  }

  /**
   * resolve a serializable configuration pojo from a portion of a given typesafe config object.
   * @param typesafeConfig typesafeConfig
   * @param subConfig subConfig
   * @return result
   */
  public T detectConfiguration(Config typesafeConfig, String subConfig) {
    return detectConfiguration( typesafeConfig.getConfig(subConfig));
  }
}
