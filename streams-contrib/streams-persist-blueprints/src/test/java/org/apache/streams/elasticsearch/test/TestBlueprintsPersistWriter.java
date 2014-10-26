package org.apache.streams.elasticsearch.test;

import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.impls.GraphTest;
import com.tinkerpop.blueprints.impls.tg.TinkerGraph;
import org.apache.streams.blueprints.BlueprintsPersistWriter;
import org.apache.streams.blueprints.BlueprintsWriterConfiguration;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Created by sblackmon on 10/20/14.
 */
@Ignore
public class TestBlueprintsPersistWriter extends GraphTest {

    private final String TEST_INDEX = "TestBlueprintsPersistWriter".toLowerCase();

    private BlueprintsWriterConfiguration testConfiguration;

    private BlueprintsPersistWriter testPersistWriter;

    private Graph graph = generateGraph();

    @Test
    public void testActivity1() {

        assert(graph.getVertices().iterator().hasNext() == false);

        Activity testActivity1 =
                new Activity()
                        .withId("activityid");
        Actor testActor1 = new Actor();
        testActor1.setId("actorid");
        testActivity1.setActor(testActor1);
        ActivityObject testObject1 = new ActivityObject();
        testObject1.setId("objectid");
        testActivity1.setObject(testObject1);

        testPersistWriter.write(new StreamsDatum(
                testActivity1));
        testPersistWriter.cleanUp();

        assert(graph.getVertices().iterator().hasNext() == true);
    }

    @Override
    public Graph generateGraph() {
        return new TinkerGraph();
    }

    @Override
    public Graph generateGraph(String s) {
        return new TinkerGraph();
    }

    @Override
    public void doTestSuite(com.tinkerpop.blueprints.TestSuite testSuite) throws Exception {

        testConfiguration = new BlueprintsWriterConfiguration();
        testConfiguration.setHost("localhost");

        testPersistWriter = new BlueprintsPersistWriter(testConfiguration);

        testPersistWriter.prepare(null);

    }
}
