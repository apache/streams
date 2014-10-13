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

package org.apache.streams.components.http.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.streams.components.http.HttpConfigurator;
import org.apache.streams.components.http.HttpProviderConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Provider retrieves contents from an known set of urls and passes all resulting objects downstream
 */
public class SimpleHTTPGetProvider implements StreamsProvider {

    private final static String STREAMS_ID = "SimpleHTTPGetProcessor";

    // from root config id
    private final static String EXTENSION = "account_type";

    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleHTTPGetProvider.class);

    protected ObjectMapper mapper;

    protected URIBuilder uriBuilder;

    protected CloseableHttpClient httpclient;

    protected HttpProviderConfiguration configuration;

    protected volatile Queue<StreamsDatum> providerQueue = new ConcurrentLinkedQueue<StreamsDatum>();

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    //    // authorized only
//    //private PeoplePatternConfiguration peoplePatternConfiguration = null;
//    //private String authHeader;
//
    public SimpleHTTPGetProvider() {
        this(HttpConfigurator.detectProviderConfiguration(StreamsConfigurator.config.getConfig("http")));
    }

    public SimpleHTTPGetProvider(HttpProviderConfiguration providerConfiguration) {
        LOGGER.info("creating SimpleHTTPGetProvider");
        LOGGER.info(providerConfiguration.toString());
        this.configuration = providerConfiguration;
    }

    /**
      Override this to add parameters to the request
     */
    protected Map<String, String> prepareParams(StreamsDatum entry) {

        return Maps.newHashMap();
    }

    public HttpGet prepareHttpGet(URI uri) {
        HttpGet httpget = new HttpGet(uri);
        httpget.addHeader("content-type", this.configuration.getContentType());
        return httpget;
    }

    @Override
    public void prepare(Object configurationObject) {

        mapper = StreamsJacksonMapper.getInstance();

        uriBuilder = new URIBuilder()
            .setScheme(this.configuration.getProtocol())
            .setHost(this.configuration.getHostname())
            .setPath(this.configuration.getResourcePath());

        httpclient = HttpClients.createDefault();
    }

    @Override
    public void cleanUp() {

        LOGGER.info("shutting down SimpleHTTPGetProvider");
        try {
            httpclient.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                httpclient = null;
            }
        }
    }

    @Override
    public void startStream() {

    }

    @Override
    public StreamsResultSet readCurrent() {
        StreamsResultSet current;

        uriBuilder = uriBuilder.setPath(
            Joiner.on("/").skipNulls().join(uriBuilder.getPath(), configuration.getResource(), configuration.getResourcePostfix())
        );

        URI uri;
        try {
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            uri = null;
        }

        List<ObjectNode> results = executeGet(uri);

        lock.writeLock().lock();

        for( ObjectNode item : results ) {
            providerQueue.add(new StreamsDatum(item, item.get("id").asText(), new DateTime(item.get("timestamp").asText())));
        }

        LOGGER.debug("Creating new result set for {} items", providerQueue.size());
        current = new StreamsResultSet(providerQueue);

        return current;
    }

    protected List<ObjectNode> executeGet(URI uri) {

        Preconditions.checkNotNull(uri);

        List<ObjectNode> results = new ArrayList<>();

        HttpGet httpget = prepareHttpGet(uri);

        CloseableHttpResponse response = null;

        String entityString = null;
        try {
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            // TODO: handle retry
            if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
                entityString = EntityUtils.toString(entity);
                if( !entityString.equals("{}") && !entityString.equals("[]") ) {
                    JsonNode jsonNode = mapper.readValue(entityString, JsonNode.class);
                    if (jsonNode != null && jsonNode instanceof ObjectNode ) {

                        results.add((ObjectNode) jsonNode);
                    } else if (jsonNode != null && jsonNode instanceof ArrayNode) {
                        ArrayNode arrayNode = (ArrayNode) jsonNode;
                        Iterator<JsonNode> iterator = arrayNode.elements();
                        while (iterator.hasNext()) {
                            ObjectNode element = (ObjectNode) iterator.next();

                            results.add(element);
                        }
                    }
                }
            }
        } catch (IOException e) {
            LOGGER.error("IO error:\n{}\n{}\n{}", uri.toString(), response, e.getMessage());
        } finally {
            try {
                response.close();
            } catch (IOException e) {}
        }
        return results;
    }

    @Override
    public StreamsResultSet readNew(BigInteger sequence) {
        return null;
    }

    @Override
    public StreamsResultSet readRange(DateTime start, DateTime end) {
        return null;
    }

    @Override
    public boolean isRunning() {
        return true;
    }
}
