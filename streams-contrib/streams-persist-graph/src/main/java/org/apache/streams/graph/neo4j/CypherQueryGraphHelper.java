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

package org.apache.streams.graph.neo4j;

import org.apache.streams.data.util.PropertyUtil;
import org.apache.streams.graph.QueryGraphHelper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Map;

/**
 * Supporting class for interacting with neo4j via rest API
 */
public class CypherQueryGraphHelper implements QueryGraphHelper {

  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jHttpGraphHelper.class);

  public static final String getVertexLongIdStatementTemplate = "MATCH (v) WHERE ID(v) = <id> RETURN v";
  public static final String getVertexStringIdStatementTemplate = "MATCH (v {id: '<id>'} ) RETURN v";

  public static final String createVertexStatementTemplate =
      "MATCH (x {id: '<id>'}) "
          + "CREATE UNIQUE (v:<type> { props }) "
          + "ON CREATE SET v <labels> "
          + "RETURN v";



  public static final String mergeVertexStatementTemplate =
      "MERGE (v:<type> {id: '<id>'}) "
          + "ON CREATE SET v <labels>, v = { props }, v.`@timestamp` = timestamp() "
          + "ON MATCH SET v <labels>, v = { props }, v.`@timestamp` = timestamp() "
          + "RETURN v";

  public static final String createEdgeStatementTemplate =
      "MATCH (s:<s_type> {id: '<s_id>'}),(d:<d_type> {id: '<d_id>'}) "
          + "CREATE UNIQUE (s)-[r:<r_type> <r_props>]->(d) "
          + "RETURN r";

  /**
   * getVertexRequest.
   * @param streamsId streamsId
   * @return pair (streamsId, parameterMap)
   */
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
  @Override
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

    Preconditions.checkNotNull(activityObject.getObjectType());

    List<String> labels = getLabels(activityObject);

    ST createVertex = new ST(createVertexStatementTemplate);
    createVertex.add("id", activityObject.getId());
    createVertex.add("type", activityObject.getObjectType());

    if ( labels.size() > 0 ) {
      createVertex.add("labels", Joiner.on(' ').join(labels));
    }

    String query = createVertex.render();

    ObjectNode object = MAPPER.convertValue(activityObject, ObjectNode.class);
    Map<String, Object> props = PropertyUtil.flattenToMap(object, '.');

    Pair<String, Map<String, Object>> queryPlusParameters = new Pair(createVertex.render(), props);

    LOGGER.debug("createVertexRequest: ({},{})", query, props);

    return queryPlusParameters;
  }

  /**
   * mergeVertexRequest.
   * @param activityObject activityObject
   * @return pair (query, parameterMap)
   */
  public Pair<String, Map<String, Object>> mergeVertexRequest(ActivityObject activityObject) {

    Preconditions.checkNotNull(activityObject.getObjectType());

    Pair queryPlusParameters = new Pair(null, Maps.newHashMap());

    List<String> labels = getLabels(activityObject);

    ST mergeVertex = new ST(mergeVertexStatementTemplate);
    mergeVertex.add("id", activityObject.getId());
    mergeVertex.add("type", activityObject.getObjectType());
    if ( labels.size() > 0 ) {
      mergeVertex.add("labels", Joiner.on(' ').join(labels));
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
   * createEdgeRequest.
   * @param activity activity
   * @return pair (query, parameterMap)
   */
  public Pair<String, Map<String, Object>> createEdgeRequest(Activity activity) {

    Pair queryPlusParameters = new Pair(null, Maps.newHashMap());

    ObjectNode object = MAPPER.convertValue(activity, ObjectNode.class);
    Map<String, Object> props = PropertyUtil.flattenToMap(object, '.');

    ST mergeEdge = new ST(createEdgeStatementTemplate);
    mergeEdge.add("s_id", activity.getActor().getId());
    mergeEdge.add("s_type", activity.getActor().getObjectType());
    mergeEdge.add("d_id", activity.getObject().getId());
    mergeEdge.add("d_type", activity.getObject().getObjectType());
    mergeEdge.add("r_id", activity.getId());
    mergeEdge.add("r_type", activity.getVerb());
    mergeEdge.add("r_props", getPropertyCreater(props));

    // set the activityObject's and extensions null, because their properties don't need to appear on the relationship
    activity.setActor(null);
    activity.setObject(null);
    activity.setTarget(null);
    activity.getAdditionalProperties().put("extensions", null);

    String statement = mergeEdge.render();
    queryPlusParameters = queryPlusParameters.setAt0(statement);
    queryPlusParameters = queryPlusParameters.setAt1(props);

    LOGGER.debug("createEdgeRequest: ({},{})", statement, props);

    return queryPlusParameters;
  }

  /**
   * getPropertyCreater.
   * @param map paramMap
   * @return PropertyCreater string
   */
  public static String getPropertyCreater(Map<String, Object> map) {
    StringBuilder builder = new StringBuilder();
    builder.append("{");
    List<String> parts = Lists.newArrayList();
    for ( Map.Entry<String, Object> entry : map.entrySet()) {
      if ( entry.getValue() instanceof String ) {
        String propVal = (String) (entry.getValue());
        parts.add("`" + entry.getKey() + "`:'" + propVal + "'");
      }
    }
    builder.append(Joiner.on(",").join(parts));
    builder.append("}");
    return builder.toString();
  }

  private List<String> getLabels(ActivityObject activityObject) {
    List<String> labels = Lists.newArrayList(":streams");
    if ( activityObject.getAdditionalProperties().containsKey("labels") ) {
      List<String> extraLabels = (List<String>)activityObject.getAdditionalProperties().get("labels");
      for ( String extraLabel : extraLabels ) {
        labels.add(":" + extraLabel);
      }
    }
    return labels;
  }

}
