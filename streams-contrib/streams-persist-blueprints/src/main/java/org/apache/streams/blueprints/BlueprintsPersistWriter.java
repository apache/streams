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

package org.apache.streams.blueprints;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.impls.rexster.RexsterGraph;
import com.tinkerpop.blueprints.util.wrappers.id.IdGraph;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BlueprintsPersistWriter implements StreamsPersistWriter {

    public static final String STREAMS_ID = BlueprintsPersistWriter.class.getCanonicalName();

    private final static Logger LOGGER = LoggerFactory.getLogger(BlueprintsPersistWriter.class);
    private final static long MAX_WRITE_LATENCY = 1000;

    private BlueprintsWriterConfiguration configuration;

    protected Graph graph;

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();
    private volatile AtomicLong lastWrite = new AtomicLong(System.currentTimeMillis());
    private ScheduledExecutorService backgroundFlushTask = Executors.newSingleThreadScheduledExecutor();

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public BlueprintsPersistWriter() {
        this(BlueprintsConfigurator.detectWriterConfiguration(StreamsConfigurator.config.getConfig("blueprints")));
    }

    public BlueprintsPersistWriter(BlueprintsWriterConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public void write(StreamsDatum streamsDatum) {

        List<Element> affected;
        try {
            affected = persistElements(streamsDatum);
            LOGGER.info("wrote datum - " + affected.size() + " elements affected");
            for( Element element : affected )
                element = null;
            affected = null;
        } catch( Throwable e ) {
            LOGGER.warn(e.getMessage());
        }
    }

    @Override
    public void prepare(Object configurationObject) {

        LOGGER.info("initializing - " + configuration.toString());

        graph = connectToGraph();

        Preconditions.checkNotNull(graph);

        LOGGER.info("initialized - " + graph.toString());

    }

    @Override
    public void cleanUp() {

        graph.shutdown();

        LOGGER.info("exiting");

    }

    protected List<Element> persistElements(StreamsDatum streamsDatum) {
        List<Element> elements = Lists.newArrayList();
        Activity activity = null;
        if (streamsDatum.getDocument() instanceof Activity) {
            activity = (Activity) streamsDatum.getDocument();
        } else if (streamsDatum.getDocument() instanceof ObjectNode) {
            activity = mapper.convertValue(streamsDatum.getDocument(), Activity.class);
        } else if (streamsDatum.getDocument() instanceof String) {
            try {
                activity = mapper.readValue((String) streamsDatum.getDocument(), Activity.class);
            } catch (Throwable e) {
                LOGGER.warn(e.getMessage());
                return elements;
            }
        } else {
            return elements;
        }

        // what gets persisted is configurable
        // we may add vertices and/or edges

        // always add vertices first
        // what types of verbs are relevant for adding vertices?
        if( configuration.getVertices().getVerbs().contains(activity.getVerb())) {

            // what objectTypes are relevant for adding vertices?
            if( configuration.getVertices().getObjectTypes().contains(activity.getActor().getObjectType())) {
                elements.add(persistVertex(activity.getActor()));
            }

            if( configuration.getVertices().getObjectTypes().contains(activity.getObject().getObjectType())) {
                elements.add(persistVertex(activity.getObject()));
            }

        }

        // always add edges last
        // what types of verbs are relevant for adding edges?
        if( configuration.getEdges().getVerbs().contains(activity.getVerb())) {

            // what objectTypes are relevant for adding edges?
            if( configuration.getEdges().getObjectTypes().contains(activity.getActor().getObjectType())
                &&
                configuration.getEdges().getObjectTypes().contains(activity.getObject().getObjectType())) {
                elements.add(persistEdge(activity));
            }

        }
        return elements;
    }

    private Graph connectToGraph() {

        Graph graph;

        if( configuration.getType().equals(BlueprintsConfiguration.Type.REXSTER)) {
            try {
                StringBuilder uri = new StringBuilder()
                        .append("http://")
                        .append(configuration.getHost())
                        .append(":")
                        .append(configuration.getPort())
                        .append("/graphs/")
                        .append(configuration.getGraph());
                KeyIndexableGraph graph1 = new RexsterGraph(uri.toString());
                graph = new IdGraph(graph1, true, false);
            } catch (Throwable e) {
                LOGGER.error("ERROR: " + e.getMessage());
                return null;
            }
            return graph;

        } else {
            return null;
        }
    }

    protected Vertex persistVertex(ActivityObject object) {
        Preconditions.checkNotNull(object);
        Preconditions.checkNotNull(object.getId());
        LOGGER.info("stream vertex: " + object.getId());
        Vertex existing = graph.getVertex(object.getId());
        if( existing == null ) {
            LOGGER.info(object.getId() + " is new");
            Vertex vertex = null;
            if( !Strings.isNullOrEmpty(object.getId()) ) {
                vertex = graph.addVertex(object.getId());
                if (vertex != null) {
                    if (!Strings.isNullOrEmpty(object.getDisplayName()))
                        vertex.setProperty("displayName", object.getDisplayName());
                    if (!Strings.isNullOrEmpty(object.getObjectType()))
                        vertex.setProperty("objectType", object.getObjectType());
                    if (!Strings.isNullOrEmpty(object.getUrl()))
                        vertex.setProperty("url", object.getUrl());
                    LOGGER.info(vertex.toString());
                } else {
                    LOGGER.warn("Vertex null after add");
                }
            } else {
                LOGGER.warn("Can't persist vertex without id");
            }
            return vertex;
        } else {
            LOGGER.info(object.getId() + " already exists");
            return existing;
        }
    }

    protected Edge persistEdge(Activity activity) {
        Edge existing = graph.getEdge(activity.getId());
        if( existing == null ) {
            Vertex s = persistVertex(activity.getActor());
            Vertex d = persistVertex(activity.getObject());
            Edge edge = graph.addEdge(activity, s, d, activity.getVerb());
            return edge;
        } else {
            return existing;
        }
    }
}
