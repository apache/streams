/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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
package org.apache.streams.graph.test;

import org.apache.streams.graph.neo4j.CypherQueryGraphHelper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.javatuples.Pair;
import org.junit.Test;

import java.util.Map;

/**
 * Created by sblackmon on 6/24/15.
 */
public class TestCypherQueryGraphHelper {

    CypherQueryGraphHelper helper = new CypherQueryGraphHelper();

    @Test
    public void getVertexRequestIdTest() throws Exception {

        Pair<String, Map<String, Object>> queryAndParams = helper.getVertexRequest("id");
        assert(queryAndParams != null);
        assert(queryAndParams.getValue0() != null);

    }

    @Test
    public void getVertexRequestLongTest() throws Exception {

        Pair<String, Map<String, Object>> queryAndParams = helper.getVertexRequest(new Long(1));

        assert(queryAndParams != null);
        assert(queryAndParams.getValue0() != null);

    }

    @Test
    public void createVertexRequestTest() throws Exception {

        ActivityObject activityObject = new ActivityObject();
        activityObject.setId("id");
        activityObject.setObjectType("type");
        activityObject.setContent("content");

        Pair<String, Map<String, Object>> queryAndParams = helper.createVertexRequest(activityObject);
        assert(queryAndParams != null);
        assert(queryAndParams.getValue0() != null);
        assert(queryAndParams.getValue1() != null);

    }

    @Test
    public void mergeVertexRequestTest() throws Exception {

        ActivityObject activityObject = new ActivityObject();
        activityObject.setId("id");
        activityObject.setObjectType("type");
        activityObject.setContent("content");

        Pair<String, Map<String, Object>> queryAndParams = helper.mergeVertexRequest(activityObject);
        assert(queryAndParams != null);
        assert(queryAndParams.getValue0() != null);
        assert(queryAndParams.getValue1() != null);

    }

    @Test
    public void createEdgeRequestTest() throws Exception {

        Actor actor = new Actor();
        actor.setId("actor");
        actor.setObjectType("type");
        actor.setContent("content");

        ActivityObject activityObject = new ActivityObject();
        activityObject.setId("object");
        activityObject.setObjectType("type");
        activityObject.setContent("content");

        Activity activity = new Activity();
        activity.setId("activity");
        activity.setVerb("verb");
        activity.setContent("content");

        activity.setActor(actor);
        activity.setObject(activityObject);

        Pair<String, Map<String, Object>> queryAndParams = helper.createEdgeRequest(activity);

        assert(queryAndParams != null);
        assert(queryAndParams.getValue0() != null);
        assert(queryAndParams.getValue1() != null);

    }
}
