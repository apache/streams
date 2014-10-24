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
import com.tinkerpop.blueprints.Edge;
import com.tinkerpop.blueprints.Element;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.blueprints.impls.rexster.RexsterGraph;
import com.tinkerpop.rexster.client.RexsterClient;
import com.tinkerpop.rexster.client.RexsterClientFactory;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BlueprintsPersistWriter implements StreamsPersistWriter {

    private final static Logger LOGGER = LoggerFactory.getLogger(BlueprintsPersistWriter.class);
    private final static long MAX_WRITE_LATENCY = 1000;

    private BlueprintsWriterConfiguration configuration;

    protected RexsterClient client;
    protected RexsterGraph graph;
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

    }

    @Override
    public void prepare(Object configurationObject) {

        connectToGraph();

        Preconditions.checkNotNull(client);

        Preconditions.checkNotNull(graph);
        Preconditions.checkNotNull(graph.getGraphURI());

    }

    @Override
    public void cleanUp() {

        graph.shutdown();

        try {
            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            client = null;
        }

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
        if( configuration.getVertices().getVerbs().contains(activity.getVerb().toString())) {

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
        if( configuration.getEdges().getVerbs().contains(activity.getVerb().toString())) {

            // what objectTypes are relevant for adding edges?
            if( configuration.getEdges().getObjectTypes().contains(activity.getActor().getObjectType())
                &&
                configuration.getEdges().getObjectTypes().contains(activity.getObject().getObjectType())) {
                elements.add(persistEdge(activity));
            }

        }
        return elements;
    }

    private synchronized void connectToGraph() {

        if( configuration.getType().equals(BlueprintsConfiguration.Type.REXSTER)) {
            try {
                client = RexsterClientFactory.open(
                        configuration.getHost(),
                        configuration.getGraph());
                StringBuilder uri = new StringBuilder()
                        .append("http://")
                        .append(configuration.getHost())
                        .append(":")
                        .append(configuration.getPort())
                        .append("/graphs/")
                        .append(configuration.getGraph());
                graph = new RexsterGraph(uri.toString());
            } catch (Exception e) {
                LOGGER.error("ERROR: ", e.getMessage());
            }
            return;

        }
    }

    protected Vertex persistVertex(ActivityObject object) {
        Iterator<Vertex> existing = graph.query().limit(1).has("id", object.getId()).vertices().iterator();
        if( !existing.hasNext()) {
            Vertex vertex = graph.addVertex(object);
            return vertex;
        } else {
            return existing.next();
        }
    }

    protected Edge persistEdge(Activity activity) {
        Iterator<Edge> existing = graph.query().limit(1).has("id", activity.getId()).edges().iterator();
        if( !existing.hasNext()) {
            Vertex s = persistVertex(activity.getActor());
            Vertex d = persistVertex(activity.getObject());
            Edge edge = graph.addEdge(activity, s, d, activity.getVerb());
            return edge;
        } else {
            return existing.next();
        }
    }
}
