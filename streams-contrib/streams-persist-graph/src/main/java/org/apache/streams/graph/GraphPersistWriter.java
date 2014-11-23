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
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.graph.neo4j.CypherGraphUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class GraphPersistWriter extends SimpleHTTPPostPersistWriter {

    public static final String STREAMS_ID = GraphPersistWriter.class.getCanonicalName();

    private final static Logger LOGGER = LoggerFactory.getLogger(GraphPersistWriter.class);
    private final static long MAX_WRITE_LATENCY = 1000;

    protected GraphWriterConfiguration configuration;

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();
    private volatile AtomicLong lastWrite = new AtomicLong(System.currentTimeMillis());
    private ScheduledExecutorService backgroundFlushTask = Executors.newSingleThreadScheduledExecutor();

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public GraphPersistWriter() {
        this(GraphConfigurator.detectWriterConfiguration(StreamsConfigurator.config.getConfig("blueprints")));
    }

    public GraphPersistWriter(GraphWriterConfiguration configuration) {
        super((HttpPersistWriterConfiguration)configuration);
        if( configuration.getType().equals(GraphConfiguration.Type.NEO_4_J))
            super.configuration.setResourcePath("/db/" + configuration.getGraph() + "/transaction/commit");
        else if( configuration.getType().equals(GraphConfiguration.Type.REXSTER))
            super.configuration.setResourcePath("/graphs/" + configuration.getGraph());
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
                statements.add(CypherGraphUtil.mergeVertexRequest(activity.getActor()));
            }
            if( configuration.getVertices().getObjects().contains("object") &&
                configuration.getVertices().getObjectTypes().contains(activity.getObject().getObjectType())) {
                statements.add(CypherGraphUtil.mergeVertexRequest(activity.getObject()));
            }
            if( configuration.getVertices().getObjects().contains("provider") &&
                configuration.getVertices().getObjectTypes().contains(activity.getProvider().getObjectType())) {
                statements.add(CypherGraphUtil.mergeVertexRequest(activity.getProvider()));
            }
            if( configuration.getVertices().getObjects().contains("target") &&
                configuration.getVertices().getObjectTypes().contains(activity.getTarget().getObjectType())) {
                statements.add(CypherGraphUtil.mergeVertexRequest(activity.getProvider()));
            }

        }

        // what types of verbs are relevant for adding edges?
        if( configuration.getEdges().getVerbs().contains(activity.getVerb())) {

            // what objects and objectTypes are relevant for adding edges?
            if( configuration.getEdges().getObjects().contains("actor") &&
                configuration.getEdges().getObjects().contains("object") &&
                configuration.getEdges().getObjectTypes().contains(activity.getActor().getObjectType()) &&
                configuration.getEdges().getObjectTypes().contains(activity.getObject().getObjectType())) {
                statements.add(CypherGraphUtil.createEdgeRequest(activity, activity.getActor(), activity.getObject()));
            }
            if( configuration.getEdges().getObjects().contains("actor") &&
                    configuration.getEdges().getObjects().contains("target") &&
                    configuration.getEdges().getObjectTypes().contains(activity.getActor().getObjectType()) &&
                    configuration.getEdges().getObjectTypes().contains(activity.getTarget().getObjectType())) {
                statements.add(CypherGraphUtil.createEdgeRequest(activity, activity.getActor(), activity.getTarget()));
            }
            if( configuration.getEdges().getObjects().contains("provider") &&
                configuration.getEdges().getObjects().contains("actor") &&
                configuration.getEdges().getObjectTypes().contains(activity.getProvider().getObjectType()) &&
                configuration.getEdges().getObjectTypes().contains(activity.getActor().getObjectType())) {
                statements.add(CypherGraphUtil.createEdgeRequest(activity, activity.getProvider(), activity.getActor()));
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
        } catch (IOException e) {
            LOGGER.error("IO error:\n{}\n{}\n{}", httpPost.toString(), response, e.getMessage());
        } finally {
            try {
                response.close();
            } catch (IOException e) {}
        }
        return result;
    }

    @Override
    public void prepare(Object configurationObject) {

        super.prepare(configurationObject);

    }

    @Override
    public void cleanUp() {

        LOGGER.info("exiting");

    }

}
