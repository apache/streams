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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.streams.data.util.PropertyUtil;
import org.apache.streams.graph.QueryGraphHelper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.javatuples.Pair;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Map;

/**
 * Supporting class for interacting with neo4j via rest API
 */
public class CypherQueryGraphHelper implements QueryGraphHelper {

    private final static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public final static String getVertexLongIdStatementTemplate = "MATCH (v) WHERE ID(v) = <id> RETURN v";
    public final static String getVertexStringIdStatementTemplate = "MATCH (v {id: '<id>'} ) RETURN v";

    public final static String createVertexStatementTemplate = "MATCH (x {id: '<id>'}) "+
                                                                "CREATE UNIQUE (n:<type> { props }) "+
                                                                "RETURN n";

    public final static String mergeVertexStatementTemplate = "MERGE (v:<type> {id: '<id>'}) "+
                                                               "ON CREATE SET v:<type>, v = { props }, v.`@timestamp` = timestamp() "+
                                                               "ON MATCH SET v = { props }, v.`@timestamp` = timestamp() "+
                                                               "RETURN v";

    public final static String createEdgeStatementTemplate = "MATCH (s:<s_type> {id: '<s_id>'}),(d:<d_type> {id: '<d_id>'}) "+
                                                            "CREATE UNIQUE (s)-[r:<r_type> <r_props>]->(d) "+
                                                            "RETURN r";

    public Pair<String, Map<String, Object>> getVertexRequest(String streamsId) {

        ST getVertex = new ST(getVertexStringIdStatementTemplate);
        getVertex.add("id", streamsId);

        Pair<String, Map<String, Object>> queryPlusParameters = new Pair(getVertex.render(), null);

        return queryPlusParameters;
    }

    @Override
    public Pair<String, Map<String, Object>> getVertexRequest(Long vertexId) {

        ST getVertex = new ST(getVertexLongIdStatementTemplate);
        getVertex.add("id", vertexId);

        Pair<String, Map<String, Object>> queryPlusParameters = new Pair(getVertex.render(), null);

        return queryPlusParameters;

    }

    public Pair<String, Map<String, Object>> createVertexRequest(ActivityObject activityObject) {

        Preconditions.checkNotNull(activityObject.getObjectType());

        ST createVertex = new ST(createVertexStatementTemplate);
        createVertex.add("id", activityObject.getId());
        createVertex.add("type", activityObject.getObjectType());

        ObjectNode object = mapper.convertValue(activityObject, ObjectNode.class);
        Map<String, Object> props = PropertyUtil.flattenToMap(object, '.');

        Pair<String, Map<String, Object>> queryPlusParameters = new Pair(createVertex.render(), props);

        return queryPlusParameters;
    }

    public Pair<String, Map<String, Object>> mergeVertexRequest(ActivityObject activityObject) {

        Preconditions.checkNotNull(activityObject.getObjectType());

        Pair queryPlusParameters = new Pair(null, Maps.newHashMap());

        ST mergeVertex = new ST(mergeVertexStatementTemplate);
        mergeVertex.add("id", activityObject.getId());
        mergeVertex.add("type", activityObject.getObjectType());
        queryPlusParameters.setAt0(mergeVertex.render());

        ObjectNode object = mapper.convertValue(activityObject, ObjectNode.class);
        Map<String, Object> props = PropertyUtil.flattenToMap(object, '.');
        queryPlusParameters.setAt1(props);

        return queryPlusParameters;
    }

    public Pair<String, Map<String, Object>> createEdgeRequest(Activity activity) {

        Pair queryPlusParameters = new Pair(null, Maps.newHashMap());

        ObjectNode object = mapper.convertValue(activity, ObjectNode.class);
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
        queryPlusParameters.setAt0(statement);
        queryPlusParameters.setAt1(props);

        return queryPlusParameters;
    }

    public static String getPropertyValueSetter(Map<String, Object> map, String symbol) {
        StringBuilder builder = new StringBuilder();
        for( Map.Entry<String, Object> entry : map.entrySet()) {
            if( entry.getValue() instanceof String ) {
                String propVal = (String)(entry.getValue());
                builder.append("," + symbol + ".`" + entry.getKey() + "` = '" + propVal + "'");
            }
        }
        return builder.toString();
    }

    public static String getPropertyParamSetter(Map<String, Object> map, String symbol) {
        StringBuilder builder = new StringBuilder();
        for( Map.Entry<String, Object> entry : map.entrySet()) {
            if( entry.getValue() instanceof String ) {
                String propVal = (String)(entry.getValue());
                builder.append("," + symbol + ".`" + entry.getKey() + "` = '" + propVal + "'");
            }
        }
        return builder.toString();
    }

    public static String getPropertyCreater(Map<String, Object> map) {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        List<String> parts = Lists.newArrayList();
        for( Map.Entry<String, Object> entry : map.entrySet()) {
            if( entry.getValue() instanceof String ) {
                String propVal = (String) (entry.getValue());
                parts.add("`"+entry.getKey() + "`:'" + propVal + "'");
            }
        }
        builder.append(Joiner.on(",").join(parts));
        builder.append("}");
        return builder.toString();
    }

}
