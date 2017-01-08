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

package org.apache.streams.neo4j;

import org.apache.streams.graph.QueryGraphHelper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.util.PropertyUtil;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.lang3.StringEscapeUtils;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Supporting class for interacting with neo4j via rest API
 */
public class CypherQueryGraphHelper implements QueryGraphHelper, Serializable {

  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  private static final Logger LOGGER = LoggerFactory.getLogger(CypherQueryGraphHelper.class);

  public static final String getVertexLongIdStatementTemplate = "MATCH (v) WHERE ID(v) = <id> RETURN v";
  public static final String getVertexStringIdStatementTemplate = "MATCH (v {id: '<id>'} ) RETURN v";
  public static final String getVerticesLabelIdStatementTemplate = "MATCH (v:<type>) RETURN v";

  public final static String createVertexStatementTemplate = "MATCH (x {id: '<id>'}) "+
      "CREATE UNIQUE (v:`<type>` { props }) "+
      "ON CREATE SET v <labels> "+
      "RETURN v";

  public final static String mergeVertexStatementTemplate = "MERGE (v:`<type>` {id: '<id>'}) "+
      "ON CREATE SET v <labels>, v = { props }, v.`@timestamp` = timestamp() "+
      "ON MATCH SET v <labels>, v = { props }, v.`@timestamp` = timestamp() "+
      "RETURN v";

  public final static String createEdgeStatementTemplate = "MATCH (s:`<s_type>` {id: '<s_id>'}),(d:`<d_type>` {id: '<d_id>'}) "+
      "CREATE UNIQUE (s)-[r:`<r_type>` <r_props>]->(d) "+
      "RETURN r";

  public Pair<String, Map<String, Object>> getVertexRequest(String streamsId) {

    ST getVertex = new ST(getVertexStringIdStatementTemplate);
    getVertex.add("id", streamsId);

    Pair<String, Map<String, Object>> queryPlusParameters = new Pair(getVertex.render(), null);

    LOGGER.debug("getVertexRequest", queryPlusParameters.toString());

    return queryPlusParameters;
  }

  /**
   * getVertexRequest.
   * @param vertexId numericId
   * @return pair (streamsId, parameterMap)
   */
  public Pair<String, Map<String, Object>> getVertexRequest(Long vertexId) {

    ST getVertex = new ST(getVertexLongIdStatementTemplate);
    getVertex.add("id", vertexId);

    Pair<String, Map<String, Object>> queryPlusParameters = new Pair(getVertex.render(), null);

    LOGGER.debug("getVertexRequest", queryPlusParameters.toString());

    return queryPlusParameters;

  }

  /**
   * createVertexRequest.
   * @param activityObject activityObject
   * @return pair (query, parameterMap)
   */
  public Pair<String, Map<String, Object>> createVertexRequest(ActivityObject activityObject) {

    Objects.requireNonNull(activityObject.getObjectType());

    List<String> labels = getLabels(activityObject);

    ST createVertex = new ST(createVertexStatementTemplate);
    createVertex.add("id", activityObject.getId());
    createVertex.add("type", activityObject.getObjectType());

    if ( labels.size() > 0 ) {
      createVertex.add("labels", String.join(" ", labels));
    }

    String query = createVertex.render();

    ObjectNode object = MAPPER.convertValue(activityObject, ObjectNode.class);
    Map<String, Object> props = PropertyUtil.flattenToMap(object, '.');

    Pair<String, Map<String, Object>> queryPlusParameters = new Pair(createVertex.render(), props);

    LOGGER.debug("createVertexRequest: ({},{})", query, props);

    return queryPlusParameters;
  }

  /**
   * getVerticesRequest gets all vertices with a label.
   * @param labelId labelId
   * @return pair (query, parameterMap)
   */
  public Pair<String, Map<String, Object>> getVerticesRequest(String labelId) {
    ST getVertex = new ST(getVerticesLabelIdStatementTemplate);
    getVertex.add("type", labelId);

    Pair<String, Map<String, Object>> queryPlusParameters = new Pair(getVertex.render(), null);

    LOGGER.debug("getVertexRequest", queryPlusParameters.toString());

    return queryPlusParameters;
  }

  /**
   * mergeVertexRequest.
   * @param activityObject activityObject
   * @return pair (query, parameterMap)
   */
  public Pair<String, Map<String, Object>> mergeVertexRequest(ActivityObject activityObject) {

    Objects.requireNonNull(activityObject.getObjectType());

    Pair queryPlusParameters = new Pair(null, new HashMap<>());

    List<String> labels = getLabels(activityObject);

    ST mergeVertex = new ST(mergeVertexStatementTemplate);
    mergeVertex.add("id", activityObject.getId());
    mergeVertex.add("type", activityObject.getObjectType());
    if ( labels.size() > 0 ) {
      mergeVertex.add("labels", String.join(" ", labels));
    }
    String query = mergeVertex.render();

    ObjectNode object = MAPPER.convertValue(activityObject, ObjectNode.class);
    Map<String, Object> props = PropertyUtil.flattenToMap(object, '.');

    LOGGER.debug("mergeVertexRequest: ({},{})", query, props);

    queryPlusParameters = queryPlusParameters.setAt0(query);
    queryPlusParameters = queryPlusParameters.setAt1(props);

    return queryPlusParameters;
  }

  /**
   * createActorObjectEdge.
   * @param activity activity
   * @return pair (query, parameterMap)
   */
  public Pair<String, Map<String, Object>> createActorObjectEdge(Activity activity) {

    Pair queryPlusParameters = new Pair(null, new HashMap<>());

    ObjectNode object = MAPPER.convertValue(activity, ObjectNode.class);
    Map<String, Object> props = PropertyUtil.flattenToMap(object, '.');

    ST mergeEdge = new ST(createEdgeStatementTemplate);
    mergeEdge.add("s_id", activity.getActor().getId());
    mergeEdge.add("s_type", activity.getActor().getObjectType());
    mergeEdge.add("d_id", activity.getObject().getId());
    mergeEdge.add("d_type", activity.getObject().getObjectType());
    mergeEdge.add("r_id", activity.getId());
    mergeEdge.add("r_type", activity.getVerb());
    mergeEdge.add("r_props", getActorObjectEdgePropertyCreater(props));

    String statement = mergeEdge.render();
    queryPlusParameters = queryPlusParameters.setAt0(statement);
    queryPlusParameters = queryPlusParameters.setAt1(props);

    LOGGER.debug("createActorObjectEdge: ({},{})", statement, props);

    return queryPlusParameters;
  }

  /**
   * createActorTargetEdge.
   * @param activity activity
   * @return pair (query, parameterMap)
   */
  public Pair<String, Map<String, Object>> createActorTargetEdge(Activity activity) {

    Pair queryPlusParameters = new Pair(null, new HashMap<>());

    ObjectNode object = MAPPER.convertValue(activity, ObjectNode.class);
    Map<String, Object> props = PropertyUtil.flattenToMap(object, '.');

    ST mergeEdge = new ST(createEdgeStatementTemplate);
    mergeEdge.add("s_id", activity.getActor().getId());
    mergeEdge.add("s_type", activity.getActor().getObjectType());
    mergeEdge.add("d_id", activity.getTarget().getId());
    mergeEdge.add("d_type", activity.getTarget().getObjectType());
    mergeEdge.add("r_id", activity.getId());
    mergeEdge.add("r_type", activity.getVerb());
    mergeEdge.add("r_props", getActorTargetEdgePropertyCreater(props));

    String statement = mergeEdge.render();
    queryPlusParameters = queryPlusParameters.setAt0(statement);
    queryPlusParameters = queryPlusParameters.setAt1(props);

    LOGGER.debug("createActorObjectEdge: ({},{})", statement, props);

    return queryPlusParameters;
  }

  /**
   * getPropertyValueSetter.
   * @param map paramMap
   * @return PropertyValueSetter string
   */
  public static String getPropertyValueSetter(Map<String, Object> map, String symbol) {
    StringBuilder builder = new StringBuilder();
    for( Map.Entry<String, Object> entry : map.entrySet()) {
      if( entry.getValue() instanceof String ) {
        String propVal = (String)(entry.getValue());
        builder.append("," + symbol + ".`" + entry.getKey() + "` = '" + StringEscapeUtils.escapeJava(propVal) + "'");
      }
    }
    return builder.toString();
  }

  /**
   * getPropertyParamSetter.
   * @param map paramMap
   * @return PropertyParamSetter string
   */
  public static String getPropertyParamSetter(Map<String, Object> map, String symbol) {
    StringBuilder builder = new StringBuilder();
    for( Map.Entry<String, Object> entry : map.entrySet()) {
      if( entry.getValue() instanceof String ) {
        String propVal = (String)(entry.getValue());
        builder.append("," + symbol + ".`" + entry.getKey() + "` = '" + StringEscapeUtils.escapeJava(propVal) + "'");
      }
    }
    return builder.toString();
  }

  /**
   * getPropertyCreater.
   * @param map paramMap
   * @return PropertyCreater string
   */
  public static String getPropertyCreater(Map<String, Object> map) {
    StringBuilder builder = new StringBuilder();
    builder.append("{ ");
    List<String> parts = new ArrayList<>();
    for( Map.Entry<String, Object> entry : map.entrySet()) {
      if( entry.getValue() instanceof String ) {
        String propVal = (String) (entry.getValue());
        parts.add("`"+entry.getKey() + "`:'" + StringEscapeUtils.escapeJava(propVal) + "'");
      }
    }
    builder.append(String.join(" ", parts));
    builder.append(" }");
    return builder.toString();
  }

  private String getActorObjectEdgePropertyCreater(Map<String, Object> map) {
    StringBuilder builder = new StringBuilder();
    builder.append("{ ");
    List<String> parts = new ArrayList<>();
    for( Map.Entry<String, Object> entry : map.entrySet()) {
      if( entry.getValue() instanceof String ) {
        String propVal = (String) (entry.getValue());
        if( !entry.getKey().contains(".")) {
          parts.add("`"+entry.getKey() + "`: '" + StringEscapeUtils.escapeJava(propVal) + "'");
        }
      }
    }
    builder.append(String.join(", ", parts));
    builder.append(" }");
    return builder.toString();
  }

  private String getActorTargetEdgePropertyCreater(Map<String, Object> map) {
    StringBuilder builder = new StringBuilder();
    builder.append("{ ");
    List<String> parts = new ArrayList<>();
    for( Map.Entry<String, Object> entry : map.entrySet()) {
      if( entry.getValue() instanceof String ) {
        String propVal = (String) (entry.getValue());
        if( !entry.getKey().contains(".")) {
          parts.add("`"+entry.getKey() + "`: '" + StringEscapeUtils.escapeJava(propVal) + "'");
        } else if( entry.getKey().startsWith("object.") && !entry.getKey().contains(".id")) {
          parts.add("`"+entry.getKey().substring("object.".length()) + "`: '" + StringEscapeUtils.escapeJava(propVal) + "'");
        }
      }
    }
    builder.append(String.join(", ", parts));
    builder.append(" }");
    return builder.toString();
  }

  /**
   * getLabels.
   * @param activityObject activityObject
   * @return PropertyCreater string
   */
  public static List<String> getLabels(ActivityObject activityObject) {
    List<String> labels = Collections.singletonList(":streams");
    if ( activityObject.getAdditionalProperties().containsKey("labels") ) {
      List<String> extraLabels = (List<String>)activityObject.getAdditionalProperties().get("labels");
      for ( String extraLabel : extraLabels ) {
        labels.add(":" + extraLabel);
      }
    }
    return labels;
  }

}
