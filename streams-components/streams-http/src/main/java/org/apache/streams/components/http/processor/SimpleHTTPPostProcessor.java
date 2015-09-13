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

package org.apache.streams.components.http.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.streams.components.http.HttpConfigurator;
import org.apache.streams.components.http.HttpProcessorConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.ActivityObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Processor retrieves contents from an known url and stores the resulting object in an extension field
 */
public class SimpleHTTPPostProcessor implements StreamsProcessor {

    private final static String STREAMS_ID = "SimpleHTTPPostProcessor";

    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleHTTPPostProcessor.class);

    protected ObjectMapper mapper;

    protected URIBuilder uriBuilder;

    protected CloseableHttpClient httpclient;

    protected HttpProcessorConfiguration configuration;

    protected String authHeader;

    public SimpleHTTPPostProcessor() {
        this(HttpConfigurator.detectProcessorConfiguration(StreamsConfigurator.config.getConfig("http")));
    }

    public SimpleHTTPPostProcessor(HttpProcessorConfiguration processorConfiguration) {
        LOGGER.info("creating SimpleHTTPPostProcessor");
        LOGGER.info(processorConfiguration.toString());
        this.configuration = processorConfiguration;
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    /**
     Override this to store a result other than exact json representation of response
     */
    protected ObjectNode prepareExtensionFragment(String entityString) {

        try {
            return mapper.readValue(entityString, ObjectNode.class);
        } catch (IOException e) {
            LOGGER.warn("IOException", e);
            return null;
        }
    }

    /**
     Override this to place result in non-standard location on document
     */
    protected ObjectNode getRootDocument(StreamsDatum datum) {

        try {
            String json = datum.getDocument() instanceof String ?
                    (String) datum.getDocument() :
                    mapper.writeValueAsString(datum.getDocument());
            return mapper.readValue(json, ObjectNode.class);
        } catch (JsonProcessingException e) {
            LOGGER.warn("JsonProcessingException", e);
            return null;
        } catch (IOException e) {
            LOGGER.warn("IOException", e);
            return null;
        }

    }

    /**
     Override this to place result in non-standard location on document
     */
    protected ActivityObject getEntityToExtend(ObjectNode rootDocument) {

        if( this.configuration.getEntity().equals(HttpProcessorConfiguration.Entity.ACTIVITY))
            return mapper.convertValue(rootDocument, ActivityObject.class);
        else
            return mapper.convertValue(rootDocument.get(this.configuration.getEntity().toString()), ActivityObject.class);

    }

    /**
     Override this to place result in non-standard location on document
     */
    protected ObjectNode setEntityToExtend(ObjectNode rootDocument, ActivityObject activityObject) {

        if( this.configuration.getEntity().equals(HttpProcessorConfiguration.Entity.ACTIVITY))
            return mapper.convertValue(activityObject, ObjectNode.class);
        else
            rootDocument.set(this.configuration.getEntity().toString(), mapper.convertValue(activityObject, ObjectNode.class));

        return rootDocument;

    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        ObjectNode rootDocument = getRootDocument(entry);

        Map<String, String> params = prepareParams(entry);

        URI uri;
        for( Map.Entry<String,String> param : params.entrySet()) {
            uriBuilder = uriBuilder.setParameter(param.getKey(), param.getValue());
        }
        try {
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            LOGGER.error("URI error {}", uriBuilder.toString(), e);
            return result;
        }

        HttpEntity payload = preparePayload(entry);

        HttpPost httpPost = prepareHttpPost(uri, payload);

        CloseableHttpResponse response = null;

        String entityString = null;
        try {
            response = httpclient.execute(httpPost);
            HttpEntity entity = response.getEntity();
            // TODO: handle retry
            if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
                entityString = EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            LOGGER.error("IO error:\n{}\n{}\n{}", uri.toString(), response, e);
            return result;
        } finally {
            try {
                response.close();
            } catch (IOException e) {}
        }

        if( entityString == null )
            return result;

        LOGGER.debug(entityString);

        ObjectNode extensionFragment = prepareExtensionFragment(entityString);

        ActivityObject extensionEntity = getEntityToExtend(rootDocument);

        ExtensionUtil.getInstance().addExtension(extensionEntity, this.configuration.getExtension(), extensionFragment);

        rootDocument = setEntityToExtend(rootDocument, extensionEntity);

        entry.setDocument(rootDocument);

        result.add(entry);

        return result;

    }

    /**
     Override this to add parameters to the request
     */
    protected Map<String, String> prepareParams(StreamsDatum entry) {

        return Maps.newHashMap();
    }

    /**
     Override this to add parameters to the request
     */
    protected HttpEntity preparePayload(StreamsDatum entry) {

        HttpEntity entity = (new StringEntity("{}",
                ContentType.create("application/json")));

        return entity;
    }


    public HttpPost prepareHttpPost(URI uri, HttpEntity entity) {
        HttpPost httpPost = new HttpPost(uri);
        httpPost.addHeader("content-type", this.configuration.getContentType());
        if( !Strings.isNullOrEmpty(authHeader))
            httpPost.addHeader("Authorization", String.format("Basic %s", authHeader));
        httpPost.setEntity(entity);
        return httpPost;
    }

    @Override
    public void prepare(Object configurationObject) {

        mapper = StreamsJacksonMapper.getInstance();

        uriBuilder = new URIBuilder()
            .setScheme(this.configuration.getProtocol())
            .setHost(this.configuration.getHostname())
            .setPath(this.configuration.getResourcePath());

        if( !Strings.isNullOrEmpty(configuration.getAccessToken()) )
            uriBuilder = uriBuilder.addParameter("access_token", configuration.getAccessToken());
        if( !Strings.isNullOrEmpty(configuration.getUsername())
            && !Strings.isNullOrEmpty(configuration.getPassword())) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(configuration.getUsername());
            stringBuilder.append(":");
            stringBuilder.append(configuration.getPassword());
            String string = stringBuilder.toString();
            authHeader = Base64.encodeBase64String(string.getBytes());
        }
        httpclient = HttpClients.createDefault();
    }

    @Override
    public void cleanUp() {
        LOGGER.info("shutting down SimpleHTTPPostProcessor");
        try {
            httpclient.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                httpclient.close();
            } catch (IOException e) {
                LOGGER.error("IOException", e);
            } finally {
                httpclient = null;
            }
        }
    }
}
