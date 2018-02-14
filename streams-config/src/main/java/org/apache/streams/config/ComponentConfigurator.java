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
import com.typesafe.config.ConfigRenderOptions;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
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

  private static final ObjectMapper mapper = new ObjectMapper()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    .configure(DeserializationFeature.FAIL_ON_INVALID_SUBTYPE, false);

  /**
   * resolve a serializable configuration pojo.
   *
   * the entire object, or fragments of it, will be collected and merged from:
   *   - the simple class name of the configured class
   *   - the fully qualified class name of the configured class
   *   - any of the ancestor classes of the configured class
   *   - the configured class's package
   *   - any of the parent packages of the configured class's package
   *
   * @return result
   */
  public T detectConfiguration() {
    Config rootConfig = StreamsConfigurator.getConfig();
    return detectConfiguration(rootConfig, "");
  }

  /**
   * resolve a serializable configuration pojo compiled from a specified path on a provided configuration tree.
   *
   * @return result
   */
  public T detectConfiguration(Config typesafe, String path) {

    T pojoConfig = null;

    Config rootConfig = typesafe;

    Config cascadeConfig = null;

    String[] canonicalNameParts = StringUtils.split(configClass.getCanonicalName(), '.');

    /*
     * Resolve from any of the parent packages of the configured class's package
     */
    for( int partIndex = 1; partIndex < canonicalNameParts.length; partIndex++) {
      String[] partialPathParts = ArrayUtils.subarray(canonicalNameParts, 0, partIndex);
      String partialPath = StringUtils.join(partialPathParts, '.');

      LOGGER.debug("partialPath: ", partialPath);

      if( rootConfig.hasPath(partialPath) ) {
        Config partialPathConfig = rootConfig.getConfig(partialPath);
        partialPathConfig = valuesOnly(partialPathConfig);
        if (!partialPathConfig.isEmpty() && !partialPathConfig.root().isEmpty()) {
          if (cascadeConfig == null) {
            cascadeConfig = partialPathConfig;
          } else {
            cascadeConfig = partialPathConfig.withFallback(cascadeConfig);
          }
        }
      }
    }

    /*
     * Resolve from any of the superclasses of the configured class
     */
    List<Class> superclasses = getSuperClasses(configClass);

    for( Class superclass : superclasses) {
      String superclassCanonicalName = superclass.getCanonicalName();
      if( rootConfig.hasPath(superclassCanonicalName)) {
        Config superclassConfig = rootConfig.getConfig(superclassCanonicalName);
        if (cascadeConfig == null) {
          cascadeConfig = superclassConfig;
        } else {
          cascadeConfig = superclassConfig.withFallback(cascadeConfig);
        }
      }
    }

    /*
     * Resolve from any of the configured class simple name
     */
    if( rootConfig.hasPath(configClass.getSimpleName()) ) {
      Config simpleNameConfig = rootConfig.getConfig(configClass.getSimpleName());
      if( cascadeConfig == null ) {
        cascadeConfig = simpleNameConfig;
      } else {
        cascadeConfig = simpleNameConfig.withFallback(cascadeConfig);
      }
    }

    /*
     * Resolve from any of the configured class canonical name
     */
    if( rootConfig.hasPath(configClass.getCanonicalName()) ) {
      Config canonicalNameConfig = rootConfig.getConfig(configClass.getCanonicalName());
      if( cascadeConfig == null ) {
        cascadeConfig = canonicalNameConfig;
      } else {
        cascadeConfig = canonicalNameConfig.withFallback(cascadeConfig);
      }
    }

    if( StringUtils.isNotBlank(path) && rootConfig.hasPath(path) ) {
      Config pathConfig = rootConfig.getConfig(path);
      if( cascadeConfig == null ) {
        cascadeConfig = pathConfig;
      } else {
        cascadeConfig = pathConfig.withFallback(cascadeConfig);
      }
    }
    else if( StringUtils.isBlank(path) ) {
      if( cascadeConfig == null ) {
        cascadeConfig = valuesOnly(rootConfig);
      } else {
        cascadeConfig = valuesOnly(rootConfig).withFallback(cascadeConfig);
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
   * resolve a serializable configuration pojo from a portion of the JVM config object.
   *
   * @param path path
   * @return result
   */
  public T detectConfiguration(String path) {
    Config streamsConfig = StreamsConfigurator.getConfig();
    return detectConfiguration(streamsConfig, path);
  }

  /**
   * resolve a serializable configuration pojo from a portion of the JVM config object.
   *
   * @param config config
   * @return result
   */
  public T detectConfiguration(Config config) {
    return detectConfiguration(config, null);
  }

  /**
   * inspect and return class hierarchy in order from furthest to nearest ancestor.
   *
   * @param clazz class to analyze
   * @return result
   */
  public static List<Class> getSuperClasses(Class clazz) {
    List<Class> classList = new ArrayList<Class>();
    Class superclass = clazz.getSuperclass();
    while (superclass != null && !superclass.isInterface() && superclass != java.lang.Object.class) {
      classList.add(0, superclass);
      superclass = superclass.getSuperclass();
    }
    return classList;
  }

  /**
   * returns a version of a config with only values - no nested objects.
   *
   * @param config config
   * @return result
   */
  private Config valuesOnly(Config config) {
    for (String key : config.root().keySet()) {
      LOGGER.debug("key: ", key);
      ConfigValue value = config.getValue(key);
      LOGGER.debug("child type: ", value.valueType());
      if (value.valueType() == ConfigValueType.OBJECT) {
        config = config.withoutPath(key);
      }
    }
    return config;
  }
}
