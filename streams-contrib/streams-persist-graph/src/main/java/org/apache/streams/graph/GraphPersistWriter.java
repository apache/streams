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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.apache.streams.components.http.HttpPersistWriterConfiguration;
import org.apache.streams.components.http.persist.SimpleHTTPPostPersistWriter;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.graph.neo4j.CypherGraphHelper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Adds activityobjects as vertices and activities as edges to a graph database with
 * an http rest endpoint (such as neo4j)
 */
public class GraphPersistWriter extends SimpleHTTPPostPersistWriter {

    public static final String STREAMS_ID = GraphPersistWriter.class.getCanonicalName();

    private final static Logger LOGGER = LoggerFactory.getLogger(GraphPersistWriter.class);
    private final static long MAX_WRITE_LATENCY = 1000;

    protected GraphWriterConfiguration configuration;

    protected GraphHelper graphHelper;

    private static ObjectMapper mapper;

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public GraphPersistWriter() {
        this(new ComponentConfigurator<GraphWriterConfiguration>(GraphWriterConfiguration.class).detectConfiguration(StreamsConfigurator.config.getConfig("graph")));
    }

    public GraphPersistWriter(GraphWriterConfiguration configuration) {
        super(StreamsJacksonMapper.getInstance().convertValue(configuration, HttpPersistWriterConfiguration.class));
        if( configuration.getType().equals(GraphConfiguration.Type.NEO_4_J)) {
            super.configuration.setResourcePath("/db/" + configuration.getGraph() + "/transaction/commit");
        }
        else if( configuration.getType().equals(GraphConfiguration.Type.REXSTER)) {
            super.configuration.setResourcePath("/graphs/" + configuration.getGraph());
        }
        this.configuration = configuration;
    }

    @Override
    protected ObjectNode preparePayload(StreamsDatum entry) {

        Activity activity = null;

        if (entry.getDocument() instanceof Activity) {
            activity = (Activity) entry.getDocument();
        } else if (entry.getDocument() instanceof ObjectNode) {
            activity = mapper.convertValue(entry.getDocument(), Activity.class);
        } else if (entry.getDocument() instanceof String) {
            try {
                activity = mapper.readValue((String) entry.getDocument(), Activity.class);
            } catch (Throwable e) {
                LOGGER.warn(e.getMessage());
            }
        }

        Preconditions.checkNotNull(activity);

        ObjectNode request = mapper.createObjectNode();
        ArrayNode statements = mapper.createArrayNode();

        activity.getActor().setObjectType("page");

        // always add vertices first
        // what types of verbs are relevant for adding vertices?
        if( configuration.getVertices().getVerbs().contains(activity.getVerb())) {

            // what objects and objectTypes are relevant for adding vertices?
            if( configuration.getVertices().getObjects().contains("actor") &&
                configuration.getVertices().getObjectTypes().contains(activity.getActor().getObjectType())) {
                statements.add(graphHelper.mergeVertexRequest(activity.getActor()));
            }
            if( configuration.getVertices().getObjects().contains("object") &&
                configuration.getVertices().getObjectTypes().contains(activity.getObject().getObjectType())) {
                statements.add(graphHelper.mergeVertexRequest(activity.getObject()));
            }
            if( configuration.getVertices().getObjects().contains("provider") &&
                configuration.getVertices().getObjectTypes().contains(activity.getProvider().getObjectType())) {
                statements.add(graphHelper.mergeVertexRequest(activity.getProvider()));
            }
            if( configuration.getVertices().getObjects().contains("target") &&
                configuration.getVertices().getObjectTypes().contains(activity.getTarget().getObjectType())) {
                statements.add(graphHelper.mergeVertexRequest(activity.getProvider()));
            }

        }

        // what types of verbs are relevant for adding edges?
        if( configuration.getEdges().getVerbs().contains(activity.getVerb())) {

            // what objects and objectTypes are relevant for adding edges?
            if( configuration.getEdges().getObjects().contains("actor") &&
                configuration.getEdges().getObjects().contains("object") &&
                configuration.getEdges().getObjectTypes().contains(activity.getActor().getObjectType()) &&
                configuration.getEdges().getObjectTypes().contains(activity.getObject().getObjectType())) {
                statements.add(graphHelper.createEdgeRequest(activity, activity.getActor(), activity.getObject()));
            }
            if( configuration.getEdges().getObjects().contains("actor") &&
                    configuration.getEdges().getObjects().contains("target") &&
                    configuration.getEdges().getObjectTypes().contains(activity.getActor().getObjectType()) &&
                    configuration.getEdges().getObjectTypes().contains(activity.getTarget().getObjectType())) {
                statements.add(graphHelper.createEdgeRequest(activity, activity.getActor(), activity.getTarget()));
            }
            if( configuration.getEdges().getObjects().contains("provider") &&
                configuration.getEdges().getObjects().contains("actor") &&
                configuration.getEdges().getObjectTypes().contains(activity.getProvider().getObjectType()) &&
                configuration.getEdges().getObjectTypes().contains(activity.getActor().getObjectType())) {
                statements.add(graphHelper.createEdgeRequest(activity, activity.getProvider(), activity.getActor()));
            }
        }

        request.put("statements", statements);
        return request;

    }

    @Override
    protected ObjectNode executePost(HttpPost httpPost) {

        Preconditions.checkNotNull(httpPost);

        ObjectNode result = null;

        CloseableHttpResponse response = null;

        String entityString = null;
        try {
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (response.getStatusLine().getStatusCode() == 200 || response.getStatusLine().getStatusCode() == 201 && entity != null) {
                entityString = EntityUtils.toString(entity);
                result = mapper.readValue(entityString, ObjectNode.class);
            }
            LOGGER.debug("Writer response:\n{}\n{}\n{}", httpPost.toString(), response.getStatusLine().getStatusCode(), entityString);
            if( result == null ||
                    (
                        result.get("errors") != null &&
                        result.get("errors").isArray() &&
                        result.get("errors").iterator().hasNext()
                    )
                ) {
                LOGGER.error("Write Error: " + result.get("errors"));
            } else {
                LOGGER.info("Write Success");
            }
        } catch (IOException e) {
            LOGGER.error("IO error:\n{}\n{}\n{}", httpPost.toString(), response, e.getMessage());
        } catch (Exception e) {
            LOGGER.error("Write Exception:\n{}\n{}\n{}", httpPost.toString(), response, e.getMessage());
        } finally {
            try {
                if( response != null) response.close();
            } catch (IOException e) {}
        }
        return result;
    }

    @Override
    public void prepare(Object configurationObject) {

        super.prepare(configurationObject);
        mapper = StreamsJacksonMapper.getInstance();

        if( configuration.getType().equals(GraphConfiguration.Type.NEO_4_J)) {
            graphHelper = new CypherGraphHelper();
        }

        Preconditions.checkNotNull(graphHelper);
    }

    @Override
    public void cleanUp() {

        LOGGER.info("exiting");

    }

}
