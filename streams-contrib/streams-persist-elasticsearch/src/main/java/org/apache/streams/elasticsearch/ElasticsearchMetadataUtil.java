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

package org.apache.streams.elasticsearch;

import org.apache.streams.core.StreamsDatum;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Utility class for handling Elasticsearch Metadata maps.
 */
public class ElasticsearchMetadataUtil {

  /**
   * get Index to use based on supplied parameters.
   *
   * @param metadata metadata
   * @param config config
   * @return result
   */
  public static String getIndex(Map<String, Object> metadata, ElasticsearchWriterConfiguration config) {

    String index = null;

    if ( metadata != null && metadata.containsKey("index")) {
      index = (String) metadata.get("index");
    }

    if ( index == null || (config.getForceUseConfig() != null && config.getForceUseConfig())) {
      index = config.getIndex();
    }

    return index;
  }

  /**
   * get Index to use based on supplied parameters.
   *
   * @param metadata metadata
   * @param config config
   * @return result
   */
  public static String getIndex(Map<String, Object> metadata, ElasticsearchReaderConfiguration config) {

    String index = null;

    if ( metadata != null && metadata.containsKey("index")) {
      index = (String) metadata.get("index");
    }

    if ( index == null ) {
      index = config.getIndexes().get(0);
    }

    return index;
  }

  /**
   * get Type to use based on supplied parameters.
   *
   * @param metadata metadata
   * @param config config
   * @return result
   */
  public static String getType(Map<String, Object> metadata, ElasticsearchWriterConfiguration config) {

    String type = null;

    if ( metadata != null && metadata.containsKey("type")) {
      type = (String) metadata.get("type");
    }

    if (type == null || (config.getForceUseConfig() != null && config.getForceUseConfig())) {
      type = config.getType();
    }

    return type;
  }

  /**
   * get Type to use based on supplied parameters.
   *
   * @param metadata metadata
   * @param config config
   * @return result
   */
  public static String getType(Map<String, Object> metadata, ElasticsearchReaderConfiguration config) {

    String type = null;

    if ( metadata != null && metadata.containsKey("type")) {
      type = (String) metadata.get("type");
    }

    if (type == null) {
      type = config.getTypes().get(0);
    }


    return type;
  }

  /**
   * get id to use based on supplied parameters.
   *
   * @param datum datum
   * @return result
   */
  public static String getId(StreamsDatum datum) {

    String id = datum.getId();

    Map<String, Object> metadata = datum.getMetadata();

    if ( id == null && metadata != null && metadata.containsKey("id")) {
      id = (String) datum.getMetadata().get("id");
    }

    return id;
  }

  /**
   * get id to use based on supplied parameters.
   *
   * @param metadata metadata
   * @return result
   */
  public static String getId(Map<String, Object> metadata) {

    return (String) metadata.get("id");

  }

  /**
   * get parent id to use based on supplied parameters.
   *
   * @param datum datum
   * @return result
   */
  static String getParent(StreamsDatum datum) {

    String parent = null;

    Map<String, Object> metadata = datum.getMetadata();

    if (metadata != null && metadata.containsKey("parent")) {
      parent = (String) datum.getMetadata().get("parent");
    }

    return parent;
  }

  /**
   * get routing id to use based on supplied parameters.
   *
   * @param datum datum
   * @return result
   */
  static String getRouting(StreamsDatum datum) {

    String routing = null;

    Map<String, Object> metadata = datum.getMetadata();

    if (metadata != null && metadata.containsKey("routing")) {
      routing = (String) datum.getMetadata().get("routing");
    }

    return routing;
  }

  /**
   * get JsonNode as Map.
   * @param node node
   * @return result
   */
  // TODO: move this to a utility package
  public static Map<String, Object> asMap(JsonNode node) {

    Iterator<Map.Entry<String, JsonNode>> iterator = node.fields();
    Map<String, Object> ret = new HashMap<>();

    Map.Entry<String, JsonNode> entry;

    while (iterator.hasNext()) {
      entry = iterator.next();
      if ( entry.getValue().asText() != null ) {
        ret.put(entry.getKey(), entry.getValue().asText());
      }
    }

    return ret;
  }
}
