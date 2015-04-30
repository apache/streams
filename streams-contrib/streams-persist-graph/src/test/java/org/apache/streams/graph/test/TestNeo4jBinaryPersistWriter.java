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

package org.apache.streams.graph.test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import junit.framework.Assert;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.graph.GraphBinaryConfiguration;
import org.apache.streams.graph.GraphReaderConfiguration;
import org.apache.streams.graph.GraphVertexReader;
import org.apache.streams.graph.neo4j.Neo4jBinaryGraphPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.DynamicLabel;
import org.neo4j.graphdb.DynamicRelationshipType;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

/**
 * Unit test for
 * @see {@link GraphVertexReader}
 *
 * Test that graph db responses can be converted to streams data
 */
public class TestNeo4jBinaryPersistWriter {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestNeo4jBinaryPersistWriter.class);

    private final static ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    private final String testGraphFile = "target/graph.neo4j";

    private GraphBinaryConfiguration testConfiguration;

    private Neo4jBinaryGraphPersistWriter graphPersistWriter;

    @Before
    public void prepareTest() throws IOException {

        testConfiguration = new GraphBinaryConfiguration();
        testConfiguration.setType(GraphBinaryConfiguration.Type.NEO_4_J);
        testConfiguration.setFile(testGraphFile);

        graphPersistWriter = new Neo4jBinaryGraphPersistWriter(testConfiguration);

        graphPersistWriter.prepare(testConfiguration);

        assert(graphPersistWriter.graph.isAvailable(5000));

    }

    @Test
    public void testNeo4jBinaryPersistWriter() throws Exception {

        InputStream testActivityFolderStream = TestNeo4jBinaryPersistWriter.class.getClassLoader()
                .getResourceAsStream("activities");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        for( String file : files) {
            LOGGER.info("File: " + file);
            InputStream testActivityFileStream = TestNeo4jBinaryPersistWriter.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = mapper.readValue(testActivityFileStream, Activity.class);
            activity.getActor().setId(activity.getActor().getObjectType());
            activity.getObject().setId(activity.getObject().getObjectType());
            if( !Strings.isNullOrEmpty((String)activity.getObject().getAdditionalProperties().get("verb"))) {
                activity.getObject().setObjectType((String) activity.getObject().getAdditionalProperties().get("verb"));
                activity.getObject().setId(activity.getObject().getObjectType());
            }
            if( !Strings.isNullOrEmpty(activity.getActor().getId())) {
                StreamsDatum actorDatum = new StreamsDatum(activity.getActor(), activity.getActor().getId());
                graphPersistWriter.write( actorDatum );
            }
            if( !Strings.isNullOrEmpty(activity.getObject().getId())) {
                StreamsDatum objectDatum = new StreamsDatum(activity.getObject(), activity.getObject().getId());
                graphPersistWriter.write( objectDatum );
            }
            if( !Strings.isNullOrEmpty(activity.getVerb()) &&
                !Strings.isNullOrEmpty(activity.getActor().getId()) &&
                !Strings.isNullOrEmpty(activity.getObject().getId())) {
                StreamsDatum activityDatum = new StreamsDatum(activity, activity.getVerb());
                graphPersistWriter.write( activityDatum );
            }
            LOGGER.info("Wrote: " + activity.getVerb());
        }

        graphPersistWriter.cleanUp();

        graphPersistWriter.graph.beginTx();
        Node organization = graphPersistWriter.graph.findNodes(DynamicLabel.label("streams"), "id", "organization").next();
        Node person = graphPersistWriter.graph.findNodes(DynamicLabel.label("streams"), "id", "person").next();
        Assert.assertNotNull(organization);
        Assert.assertTrue(organization.hasLabel(DynamicLabel.label("streams")));
        Assert.assertTrue(organization.hasLabel(DynamicLabel.label("organization")));
        Assert.assertNotNull(person);
        Assert.assertTrue(person.hasLabel(DynamicLabel.label("streams")));
        Assert.assertTrue(person.hasLabel(DynamicLabel.label("person")));
        Assert.assertTrue(person.hasRelationship());
        Assert.assertTrue(person.hasRelationship(Direction.OUTGOING));
        Assert.assertTrue(person.hasRelationship(DynamicRelationshipType.withName("join"), Direction.OUTGOING));
        Assert.assertTrue(person.hasRelationship(DynamicRelationshipType.withName("leave"), Direction.OUTGOING));
//        Iterable < Relationship > relationships = person.getRelationships(Direction.OUTGOING);
//        List<Relationship> relationshipList = Lists.newArrayList(relationships);
//        Assert.assertEquals(relationshipList.size(), 2);
        Relationship joinRelationship = person.getSingleRelationship(DynamicRelationshipType.withName("join"), Direction.OUTGOING);
        Assert.assertNotNull(joinRelationship);
        Node joinRelationshipStart = joinRelationship.getStartNode();
        Node joinRelationshipEnd = joinRelationship.getEndNode();
        Assert.assertEquals(joinRelationshipStart, person);
        Assert.assertEquals(joinRelationshipEnd, organization);
        Relationship leaveRelationship = person.getSingleRelationship(DynamicRelationshipType.withName("leave"), Direction.OUTGOING);
        Assert.assertNotNull(leaveRelationship);
        Node leaveRelationshipStart = leaveRelationship.getStartNode();
        Node leaveRelationshipEnd = leaveRelationship.getEndNode();
        Assert.assertEquals(leaveRelationshipStart, person);
        Assert.assertEquals(leaveRelationshipEnd, organization);


    }
}
