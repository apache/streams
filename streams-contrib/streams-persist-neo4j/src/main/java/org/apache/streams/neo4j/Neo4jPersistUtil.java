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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.neo4j.bolt.Neo4jBoltPersistWriter;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import org.apache.commons.lang3.StringUtils;
import org.javatuples.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by steve on 1/2/17.
 */
public class Neo4jPersistUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(Neo4jBoltPersistWriter.class);

  private static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

  private static CypherQueryGraphHelper helper = new CypherQueryGraphHelper();

  public static List<Pair<String, Map<String, Object>>> prepareStatements(StreamsDatum entry) throws Exception {

    List<Pair<String, Map<String, Object>>> statements = new ArrayList<>();

    String id = entry.getId();
    Activity activity = null;
    ActivityObject activityObject = null;
    Object document = entry.getDocument();

    if (document instanceof Activity) {
      activity = (Activity) document;
    } else if (document instanceof ActivityObject) {
      activityObject = (ActivityObject) document;
    } else {
      ObjectNode objectNode;
      if (document instanceof ObjectNode) {
        objectNode = (ObjectNode) document;
      } else if ( document instanceof String) {
        try {
          objectNode = mapper.readValue((String) document, ObjectNode.class);
        } catch (IOException ex) {
          LOGGER.error("Can't handle input: ", entry);
          throw ex;
        }
      } else {
        LOGGER.error("Can't handle input: ", entry);
        throw new Exception("Can't create statements from datum.");
      }

      if ( objectNode.get("verb") != null ) {
        try {
          activity = mapper.convertValue(objectNode, Activity.class);
          activityObject = activity.getObject();
        } catch (Exception ex) {
          activityObject = mapper.convertValue(objectNode, ActivityObject.class);
        }
      } else {
        activityObject = mapper.convertValue(objectNode, ActivityObject.class);
      }

    }

    Preconditions.checkArgument(activity != null ^ activityObject != null);

    if ( activityObject != null && !Strings.isNullOrEmpty(activityObject.getId())) {

      statements.add(vertexStatement(activityObject));

    } else if ( activity != null && !Strings.isNullOrEmpty(activity.getId())) {

      statements.addAll(vertexStatements(activity));

      statements.addAll(edgeStatements(activity));

    }

    return statements;
  }

  public static List<Pair<String, Map<String, Object>>> vertexStatements(Activity activity) {
    List<Pair<String, Map<String, Object>>> statements = new ArrayList<>();;
    ActivityObject actor = activity.getActor();
    ActivityObject object = activity.getObject();
    ActivityObject target = activity.getTarget();

    if (actor != null && StringUtils.isNotBlank(actor.getId())) {
      Pair<String, Map<String, Object>> actorStatement = vertexStatement(actor);
      statements.add(actorStatement);
    }

    if (object != null && StringUtils.isNotBlank(object.getId())) {
      Pair<String, Map<String, Object>> objectStatement = vertexStatement(object);
      statements.add(objectStatement);
    }

    if (target != null && StringUtils.isNotBlank(target.getId())) {
      Pair<String, Map<String, Object>> targetStatement = vertexStatement(target);
      statements.add(targetStatement);
    }

    return statements;
  }

  public static List<Pair<String, Map<String, Object>>> edgeStatements(Activity activity) {
    List<Pair<String, Map<String, Object>>> statements = new ArrayList<>();;
    ActivityObject actor = activity.getActor();
    ActivityObject object = activity.getObject();
    ActivityObject target = activity.getTarget();

    if (StringUtils.isNotBlank(actor.getId()) && object != null && StringUtils.isNotBlank(object.getId())) {
      Pair<String, Map<String, Object>> actorObjectEdgeStatement = helper.createActorObjectEdge(activity);
      Map<String, Object> props = new HashMap<>();
      props.put("props", actorObjectEdgeStatement.getValue1());
      actorObjectEdgeStatement = actorObjectEdgeStatement.setAt1(props);
      statements.add(actorObjectEdgeStatement);
    }

    if (StringUtils.isNotBlank(actor.getId()) && target != null && StringUtils.isNotBlank(target.getId())) {
      Pair<String, Map<String, Object>> actorTargetEdgeStatement = helper.createActorTargetEdge(activity);
      Map<String, Object> props = new HashMap<>();
      props.put("props", actorTargetEdgeStatement.getValue1());
      actorTargetEdgeStatement = actorTargetEdgeStatement.setAt1(props);
      statements.add(actorTargetEdgeStatement);
    }

    return statements;
  }

  public static Pair<String, Map<String, Object>> vertexStatement(ActivityObject activityObject) {
    Pair<String, Map<String, Object>> mergeVertexRequest = helper.mergeVertexRequest(activityObject);
    Map<String, Object> props = new HashMap<>();
    props.put("props", mergeVertexRequest.getValue1());
    mergeVertexRequest = mergeVertexRequest.setAt1(props);
    return mergeVertexRequest;
  }
}
