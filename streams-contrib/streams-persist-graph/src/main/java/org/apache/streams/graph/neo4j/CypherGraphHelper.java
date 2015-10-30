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
import org.apache.streams.data.util.PropertyUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Map;

/**
 * Supporting class for interacting with neo4j via rest API
 */
public class CypherGraphHelper implements org.apache.streams.graph.GraphHelper {

    private final static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public final static String statementKey = "statement";
    public final static String paramsKey = "parameters";
    public final static String propsKey = "props";

    public final static String getVertexLongIdStatementTemplate = "MATCH (v) WHERE ID(v) = <id> RETURN v";
    public final static String getVertexStringIdStatementTemplate = "MATCH (v {id: '<id>'} ) RETURN v";

    public final static String createVertexStatementTemplate = "MATCH (x {id: '<id>'}) "+
                                                                "CREATE UNIQUE (v:<type> { props }) "+
                                                                "ON CREATE SET v <labels> "+
                                                                "RETURN v";

    public final static String mergeVertexStatementTemplate = "MERGE (v:<type> {id: '<id>'}) "+
                                                               "ON CREATE SET v <labels>, v = { props }, v.`@timestamp` = timestamp() "+
                                                               "ON MATCH SET v <labels>, v = { props }, v.`@timestamp` = timestamp() "+
                                                               "RETURN v";

    public final static String createEdgeStatementTemplate = "MATCH (s:<s_type> {id: '<s_id>'}),(d:<d_type> {id: '<d_id>'}) "+
                                                            "CREATE UNIQUE (s)-[r:<r_type> <r_props>]->(d) "+
                                                            "RETURN r";

    public ObjectNode getVertexRequest(String streamsId) {

        ObjectNode request = mapper.createObjectNode();

        ST getVertex = new ST(getVertexStringIdStatementTemplate);
        getVertex.add("id", streamsId);
        request.put(statementKey, getVertex.render());

        return request;
    }

    @Override
    public ObjectNode getVertexRequest(Long vertexId) {

        ObjectNode request = mapper.createObjectNode();

        ST getVertex = new ST(getVertexLongIdStatementTemplate);
        getVertex.add("id", vertexId);
        request.put(statementKey, getVertex.render());

        return request;
    }

    public ObjectNode createVertexRequest(ActivityObject activityObject) {

        Preconditions.checkNotNull(activityObject.getObjectType());

        ObjectNode request = mapper.createObjectNode();

        List<String> labels = Lists.newArrayList();
        if( activityObject.getAdditionalProperties().containsKey("labels") ) {
            List<String> extraLabels = (List<String>)activityObject.getAdditionalProperties().get("labels");
            for( String extraLabel : extraLabels )
                labels.add(":"+extraLabel);
        }

        ST createVertex = new ST(createVertexStatementTemplate);
        createVertex.add("id", activityObject.getId());
        createVertex.add("type", activityObject.getObjectType());
        createVertex.add("labels", Joiner.on(' ').join(labels));
        request.put(statementKey, createVertex.render());

        ObjectNode params = mapper.createObjectNode();
        ObjectNode object = mapper.convertValue(activityObject, ObjectNode.class);
        ObjectNode props = PropertyUtil.flattenToObjectNode(object, '.');
        params.put(propsKey, props);
        request.put(paramsKey, params);

        return request;
    }

    public ObjectNode mergeVertexRequest(ActivityObject activityObject) {

        Preconditions.checkNotNull(activityObject.getObjectType());

        ObjectNode request = mapper.createObjectNode();

        List<String> labels = Lists.newArrayList();
        if( activityObject.getAdditionalProperties().containsKey("labels") ) {
            List<String> extraLabels = (List<String>)activityObject.getAdditionalProperties().get("labels");
            for( String extraLabel : extraLabels )
                labels.add(":"+extraLabel);
        }

        ST mergeVertex = new ST(mergeVertexStatementTemplate);
        mergeVertex.add("id", activityObject.getId());
        mergeVertex.add("type", activityObject.getObjectType());
        mergeVertex.add("labels", Joiner.on(' ').join(labels));

        ObjectNode params = mapper.createObjectNode();
        ObjectNode object = mapper.convertValue(activityObject, ObjectNode.class);
        ObjectNode props = PropertyUtil.flattenToObjectNode(object, '.');
        params.put(propsKey, props);
        request.put(paramsKey, params);

        String statement = mergeVertex.render();

        request.put(statementKey, statement);

        return request;
    }

    public ObjectNode createEdgeRequest(Activity activity, ActivityObject source, ActivityObject destination) {

        ObjectNode request = mapper.createObjectNode();

        // set the activityObject's and extensions null, because their properties don't need to appear on the relationship
        activity.setActor(null);
        activity.setObject(null);
        activity.setTarget(null);
        activity.getAdditionalProperties().put("extensions", null);

        ObjectNode object = mapper.convertValue(activity, ObjectNode.class);
        Map<String, Object> props = PropertyUtil.flattenToMap(object, '.');

        ST mergeEdge = new ST(createEdgeStatementTemplate);
        mergeEdge.add("s_id", source.getId());
        mergeEdge.add("s_type", source.getObjectType());
        mergeEdge.add("d_id", destination.getId());
        mergeEdge.add("d_type", destination.getObjectType());
        mergeEdge.add("r_id", activity.getId());
        mergeEdge.add("r_type", activity.getVerb());
        mergeEdge.add("r_props", getPropertyCreater(props));

        String statement = mergeEdge.render();
        request.put(statementKey, statement);

        return request;
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
