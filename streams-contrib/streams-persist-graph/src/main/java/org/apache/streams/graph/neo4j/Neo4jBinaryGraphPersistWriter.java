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
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.io.FileUtils;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.graph.GraphBinaryConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Adds activityobjects as vertices and activities as edges to a graph database file which will be
 * loaded inside of neo4j
 */
public class Neo4jBinaryGraphPersistWriter implements StreamsPersistWriter {

    public static final String STREAMS_ID = Neo4jBinaryGraphPersistWriter.class.getCanonicalName();

    private final static Logger LOGGER = LoggerFactory.getLogger(Neo4jBinaryGraphPersistWriter.class);
    private final static long MAX_WRITE_LATENCY = 1000;

    protected GraphBinaryConfiguration configuration;

    private static ObjectMapper mapper;

    public GraphDatabaseService graph;
    private Neo4jBinaryGraphUtil graphutil;
    private CypherQueryGraphHelper queryGraphHelper;
    private BinaryGraphHelper binaryGraphHelper;

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public Neo4jBinaryGraphPersistWriter() {
        this(new ComponentConfigurator<GraphBinaryConfiguration>(GraphBinaryConfiguration.class).detectConfiguration(StreamsConfigurator.config.getConfig("graph")));
    }

    public Neo4jBinaryGraphPersistWriter(GraphBinaryConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void prepare(Object configurationObject) {

        mapper = StreamsJacksonMapper.getInstance();

        boolean newGraph = true;
        if( FileUtils.getFile(configuration.getFile()).canRead())
            newGraph = false;

        graph = new GraphDatabaseFactory().newEmbeddedDatabase(configuration.getFile());

        graphutil = new Neo4jBinaryGraphUtil();

        queryGraphHelper = new CypherQueryGraphHelper();

        binaryGraphHelper = new BinaryGraphHelper();

        String globalLabel = "streams";

        if( newGraph ) {
            graphutil.addUniqueIndex(graph, globalLabel, "id", false);
            for( String field: configuration.getIndexFields()) {
                graphutil.addUniqueIndex(graph, globalLabel, field, false);
            }
        }

    }

    @Override
    public void cleanUp() {

        LOGGER.info("exiting");

    }

    @Override
    public void write(StreamsDatum entry) {

        Activity activity = null;
        ActivityObject activityObject = null;

        if (entry.getDocument() instanceof Activity) {
            activity = (Activity) entry.getDocument();
        } if (entry.getDocument() instanceof ActivityObject) {
            activityObject = (ActivityObject) entry.getDocument();
        } else if (entry.getDocument() instanceof ObjectNode) {
            try {
                activity = mapper.convertValue(entry.getDocument(), Activity.class);
            } catch( Exception e ) {
                activityObject = mapper.convertValue(entry.getDocument(), ActivityObject.class);
            }
        } else if (entry.getDocument() instanceof String) {
            try {
                activity = mapper.readValue((String) entry.getDocument(), Activity.class);
            } catch (Throwable e1) {
                try {
                    activityObject = mapper.readValue((String) entry.getDocument(), ActivityObject.class);
                } catch( Exception e2 ) {
                    LOGGER.error("Can't handle input: ", e2);
                }
            }
        }

        Preconditions.checkArgument(activity != null || activityObject != null);

        List<String> labels = Lists.newArrayList("streams");

        if( activityObject != null ) {
            if (activityObject.getObjectType() != null)
                labels.add(activityObject.getObjectType());
            Pair<String, Map<String, Object>> addNode = binaryGraphHelper.createVertexRequest(activityObject);
            graphutil.addNode(
                    graph,
                    labels,
                    addNode);
        } else if( activity != null ) {

            // always add vertices first

            if (activity.getProvider() != null &&
                    !Strings.isNullOrEmpty(activity.getProvider().getId())) {
                labels.add(activity.getProvider().getId());
            }
            if (activity.getActor() != null &&
                    !Strings.isNullOrEmpty(activity.getActor().getId())) {
                if (activity.getActor().getObjectType() != null)
                    labels.add(activity.getActor().getObjectType());
                Pair<String, Map<String, Object>> addNode = binaryGraphHelper.createVertexRequest(activity.getActor());
                graphutil.addNode(
                        graph,
                        labels,
                        addNode);
            }

            if (activity.getObject() != null &&
                    !Strings.isNullOrEmpty(activity.getObject().getId())) {
                if (activity.getObject().getObjectType() != null)
                    labels.add(activity.getObject().getObjectType());
                Pair<String, Map<String, Object>> addNode = binaryGraphHelper.createVertexRequest(activity.getObject());
                graphutil.addNode(
                        graph,
                        labels,
                        addNode);
            }

            // then add edge

            if (!Strings.isNullOrEmpty(activity.getVerb())) {
                if (activity.getVerb() != null)
                    labels.add(activity.getVerb());
                Quartet<String, String, String, Map<String, Object>> addRelationship = binaryGraphHelper.createEdgeRequest(activity);
                graphutil.addRelationship(
                        graph,
                        labels,
                        addRelationship);
            }

        }

    }


}
