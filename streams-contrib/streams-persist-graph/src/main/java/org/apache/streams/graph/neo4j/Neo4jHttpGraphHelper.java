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
import org.apache.streams.graph.HttpGraphHelper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;

import java.util.List;
import java.util.Map;

/**
 * Supporting class for interacting with neo4j via rest API
 */
public class Neo4jHttpGraphHelper implements HttpGraphHelper {

    private final static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    private final static Logger LOGGER = LoggerFactory.getLogger(Neo4jHttpGraphHelper.class);

    public final static String statementKey = "statement";
    public final static String paramsKey = "parameters";
    public final static String propsKey = "props";

    public ObjectNode createHttpRequest(Pair<String, Map<String, Object>> queryPlusParameters) {

        LOGGER.debug("createHttpRequest: ", queryPlusParameters);

        Preconditions.checkNotNull(queryPlusParameters);
        Preconditions.checkNotNull(queryPlusParameters.getValue0());
        Preconditions.checkNotNull(queryPlusParameters.getValue1());

        ObjectNode request = MAPPER.createObjectNode();

        request.put(statementKey, queryPlusParameters.getValue0());

        ObjectNode params = MAPPER.createObjectNode();
        ObjectNode props = MAPPER.convertValue(queryPlusParameters.getValue1(), ObjectNode.class);

        params.put(propsKey, props);
        request.put(paramsKey, params);

        LOGGER.debug("createHttpRequest: ", request);

        return request;
    }

}
