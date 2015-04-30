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
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.util.EntityUtils;
import org.apache.streams.components.http.HttpPersistWriterConfiguration;
import org.apache.streams.components.http.persist.SimpleHTTPPostPersistWriter;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.graph.neo4j.CypherQueryGraphHelper;
import org.apache.streams.graph.neo4j.Neo4jHttpGraphHelper;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Adds activityobjects as vertices and activities as edges to a graph database with
 * an http rest endpoint (such as neo4j)
 */
public class GraphHttpPersistWriter extends SimpleHTTPPostPersistWriter {

    public static final String STREAMS_ID = GraphHttpPersistWriter.class.getCanonicalName();

    private final static Logger LOGGER = LoggerFactory.getLogger(GraphHttpPersistWriter.class);
    private final static long MAX_WRITE_LATENCY = 1000;

    protected GraphHttpConfiguration configuration;

    protected QueryGraphHelper queryGraphHelper;
    protected HttpGraphHelper httpGraphHelper;

    private static ObjectMapper mapper;

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    public GraphHttpPersistWriter() {
        this(new ComponentConfigurator<GraphHttpConfiguration>(GraphHttpConfiguration.class).detectConfiguration(StreamsConfigurator.config.getConfig("graph")));
    }

    public GraphHttpPersistWriter(GraphHttpConfiguration configuration) {
        super(StreamsJacksonMapper.getInstance().convertValue(configuration, HttpPersistWriterConfiguration.class));
        if( configuration.getType().equals(GraphHttpConfiguration.Type.NEO_4_J)) {
            super.configuration.setResourcePath("/db/" + configuration.getGraph() + "/transaction/commit");
        }
        else if( configuration.getType().equals(GraphHttpConfiguration.Type.REXSTER)) {
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

        List<String> labels = Lists.newArrayList();
        if( activity.getProvider() != null &&
                !Strings.isNullOrEmpty(activity.getProvider().getId()) ) {
            labels.add(activity.getProvider().getId());
        }

        if( activity.getActor() != null &&
                !Strings.isNullOrEmpty(activity.getActor().getId()) ) {
            if( activity.getActor().getObjectType() != null )
                labels.add(activity.getActor().getObjectType());
            statements.add(httpGraphHelper.createHttpRequest(queryGraphHelper.mergeVertexRequest(activity.getActor())));
        }

        if( activity.getObject() != null &&
                !Strings.isNullOrEmpty(activity.getObject().getId()) ) {
            if( activity.getObject().getObjectType() != null )
                labels.add(activity.getObject().getObjectType());
            statements.add(httpGraphHelper.createHttpRequest(queryGraphHelper.mergeVertexRequest(activity.getObject())));
        }

        // then add edge

        if( !Strings.isNullOrEmpty(activity.getVerb()) ) {
            statements.add(httpGraphHelper.createHttpRequest(queryGraphHelper.createEdgeRequest(activity)));
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

        if( configuration.getType().equals(GraphHttpConfiguration.Type.NEO_4_J)) {
            queryGraphHelper = new CypherQueryGraphHelper();
            httpGraphHelper = new Neo4jHttpGraphHelper();
        }

        Preconditions.checkNotNull(queryGraphHelper);
        Preconditions.checkNotNull(httpGraphHelper);
    }

    @Override
    public void cleanUp() {

        LOGGER.info("exiting");

    }

}
