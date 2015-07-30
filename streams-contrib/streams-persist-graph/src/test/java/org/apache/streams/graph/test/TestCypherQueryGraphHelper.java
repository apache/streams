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
