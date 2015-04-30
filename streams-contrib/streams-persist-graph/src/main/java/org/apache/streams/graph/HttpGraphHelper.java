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

package org.apache.streams.graph;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.javatuples.Pair;

import java.util.Map;

/**
 * Interface for methods allowing persistance to a graph database wrapped with
 * a rest API.  CypherGraphHelper is a good example, for neo4j.
 */
public interface HttpGraphHelper {

    public ObjectNode createHttpRequest(Pair<String, Map<String, Object>> queryPlusParameters);

}
