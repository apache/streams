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

package org.apache.streams.components.http.persist;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.streams.components.http.HttpConfigurator;
import org.apache.streams.components.http.HttpPersistWriterConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

/**
 * Created by steve on 11/12/14.
 */
public class SimpleHTTPPostPersistWriter implements StreamsPersistWriter {

    private final static String STREAMS_ID = "SimpleHTTPPostPersistWriter";

    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleHTTPPostPersistWriter.class);

    protected ObjectMapper mapper;

    protected URIBuilder uriBuilder;

    protected CloseableHttpClient httpclient;

    protected HttpPersistWriterConfiguration configuration;

    public SimpleHTTPPostPersistWriter() {
        this(HttpConfigurator.detectPersistWriterConfiguration(StreamsConfigurator.config.getConfig("http")));
    }

    public SimpleHTTPPostPersistWriter(HttpPersistWriterConfiguration configuration) {
        this.configuration = configuration;
    }


    @Override
    public void write(StreamsDatum entry) {

        ObjectNode payload = preparePayload(entry);

        Map<String, String> params = prepareParams(entry);

        URI uri = prepareURI(params);

        HttpPost httppost = prepareHttpPost(uri, payload);

        ObjectNode result = executePost(httppost);

        try {
            LOGGER.debug(mapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            LOGGER.warn("Non-json response", e.getMessage());
        }
    }

    /**
     Override this to alter request URI
     */
    protected URI prepareURI(Map<String, String> params) {
        URI uri = null;
        for( Map.Entry<String,String> param : params.entrySet()) {
            uriBuilder = uriBuilder.setParameter(param.getKey(), param.getValue());
        }
        try {
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            LOGGER.error("URI error {}", uriBuilder.toString());
        }
        return uri;
    }

    /**
     Override this to add parameters to the request
     */
    protected Map<String, String> prepareParams(StreamsDatum entry) {

        return Maps.newHashMap();
    }

    /**
     Override this to alter json payload on to the request
     */
    protected ObjectNode preparePayload(StreamsDatum entry) {

        return (ObjectNode) entry.getDocument();
    }

    /**
     Override this to add headers to the request
     */
    public HttpPost prepareHttpPost(URI uri, ObjectNode payload) {
        HttpPost httppost = new HttpPost(uri);
        httppost.addHeader("content-type", this.configuration.getContentType());
        httppost.addHeader("accept-charset", "UTF-8");
        try {
            String entity = mapper.writeValueAsString(payload);
            httppost.setEntity(new StringEntity(entity));
        } catch (JsonProcessingException e) {
            LOGGER.warn(e.getMessage());
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn(e.getMessage());
        }
        return httppost;
    }

    protected ObjectNode executePost(HttpPost httpPost) {

        Preconditions.checkNotNull(httpPost);

        ObjectNode result = null;

        CloseableHttpResponse response = null;

        String entityString = null;
        try {
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            // TODO: handle retry
            if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() >= HttpStatus.SC_OK && entity != null) {
                entityString = EntityUtils.toString(entity);
                result = mapper.readValue(entityString, ObjectNode.class);
            }
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

        mapper = StreamsJacksonMapper.getInstance();

        uriBuilder = new URIBuilder()
                .setScheme(this.configuration.getProtocol())
                .setHost(this.configuration.getHostname())
                .setPort(this.configuration.getPort().intValue())
                .setPath(this.configuration.getResourcePath());

        httpclient = HttpClients.createDefault();

    }

    @Override
    public void cleanUp() {

        LOGGER.info("shutting down SimpleHTTPPostPersistWriter");
        try {
            httpclient.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                LOGGER.error(e.getMessage());
            } finally {
                httpclient = null;
            }
        }
    }
}
