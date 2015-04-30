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
import org.javatuples.Quartet;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Map;

/**
 * Supporting class for interacting with neo4j via rest API
 */
public class BinaryGraphHelper {

    private final static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    public Pair<String, Map<String, Object>> createVertexRequest(ActivityObject activityObject) {

        Preconditions.checkNotNull(activityObject.getObjectType());

        ObjectNode object = mapper.convertValue(activityObject, ObjectNode.class);
        Map<String, Object> props = PropertyUtil.flattenToMap(object, '.');

        Pair<String, Map<String, Object>> queryPlusParameters = new Pair(props.get("id"), props);

        return queryPlusParameters;
    }

    public Quartet<String, String, String, Map<String, Object>> createEdgeRequest(Activity activity) {

        ObjectNode object = mapper.convertValue(activity, ObjectNode.class);
        Map<String, Object> props = PropertyUtil.flattenToMap(object, '.');

        Quartet createEdgeRequest = new Quartet(
                activity.getActor().getId(),
                activity.getObject().getId(),
                activity.getId(),
                props);

        return createEdgeRequest;
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
