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
 * Created by steve on 11/13/14.
 */
public class CypherGraphUtil {

    private final static ObjectMapper mapper = new StreamsJacksonMapper();

    public final static String statementKey = "statement";
    public final static String paramsKey = "parameters";
    public final static String propsKey = "props";

    public final static String getVertexStatementTemplate = "MATCH (v {id: '<id>'} ) RETURN v";

    public final static String createVertexStatementTemplate = "MATCH (x {id: '<id>'}) "+
                                                                "CREATE UNIQUE (n:<type> { props }) "+
                                                                "RETURN n";

    public final static String mergeVertexStatementTemplate = "MERGE (v:<type> {id: '<id>'}) "+
                                                               "ON CREATE SET v:<type>";

    public final static String createEdgeStatementTemplate = "MATCH (s:<s_type> {id: '<s_id>'}),(d:<d_type> {id: '<d_id>'}) "+
                                                            "CREATE UNIQUE (s)-[r:<r_type> <r_props>]->(d) "+
                                                            "RETURN r";

    public static ObjectNode getVertexRequest(String id) {

        ObjectNode request = mapper.createObjectNode();

        ST getVertex = new ST(getVertexStatementTemplate);
        getVertex.add("id", id);
        request.put(statementKey, getVertex.render());

        return request;
    }

    public static ObjectNode createVertexRequest(ActivityObject activityObject) {

        Preconditions.checkNotNull(activityObject.getObjectType());

        ObjectNode request = mapper.createObjectNode();

        ST createVertex = new ST(createVertexStatementTemplate);
        createVertex.add("id", activityObject.getId());
        createVertex.add("type", activityObject.getObjectType());
        request.put(statementKey, createVertex.render());

        ObjectNode params = mapper.createObjectNode();
        ObjectNode object = mapper.convertValue(activityObject, ObjectNode.class);
        ObjectNode props = PropertyUtil.flattenToObjectNode(object, '_');
        params.put(propsKey, props);
        request.put(paramsKey, params);

        return request;
    }

    public static ObjectNode mergeVertexRequest(ActivityObject activityObject) {

        Preconditions.checkNotNull(activityObject.getObjectType());

        ObjectNode request = mapper.createObjectNode();

        ST mergeVertex = new ST(mergeVertexStatementTemplate);
        mergeVertex.add("id", activityObject.getId());
        mergeVertex.add("type", activityObject.getObjectType());

        ObjectNode object = mapper.convertValue(activityObject, ObjectNode.class);
        Map<String, Object> props = PropertyUtil.flattenToMap(object, '_');

        String statement = mergeVertex.render();
        statement += getPropertySetter(props, "v");
        statement += (" RETURN v;");
        request.put(statementKey, statement);

        return request;
    }

    public static ObjectNode createEdgeRequest(Activity activity, ActivityObject source, ActivityObject destination) {

        ObjectNode request = mapper.createObjectNode();

        // set the activityObject's and extensions null, because their properties don't need to appear on the relationship
        activity.setActor(null);
        activity.setObject(null);
        activity.setTarget(null);
        activity.getAdditionalProperties().put("extensions", null);

        ObjectNode object = mapper.convertValue(activity, ObjectNode.class);
        Map<String, Object> props = PropertyUtil.flattenToMap(object, '_');

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

    public static String getPropertySetter(Map<String, Object> map, String symbol) {
        StringBuilder builder = new StringBuilder();
        for( Map.Entry<String, Object> entry : map.entrySet()) {
            if( entry.getValue() instanceof String ) {
                String propVal = (String)(entry.getValue());
                builder.append("," + symbol + "." + entry.getKey() + " = '" + propVal + "'");
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
                parts.add(entry.getKey() + ":'" + propVal + "'");
            }
        }
        builder.append(Joiner.on(",").join(parts));
        builder.append("}");
        return builder.toString();
    }

}
