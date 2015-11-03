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
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.commons.lang.NotImplementedException;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
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
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Provider retrieves contents from an known set of urls and passes all resulting objects downstream
 */
public class SimpleHttpProvider implements StreamsProvider {

    private final static String STREAMS_ID = "SimpleHttpProvider";

    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleHttpProvider.class);

    protected ObjectMapper mapper;

    protected URIBuilder uriBuilder;

    protected CloseableHttpClient httpclient;

    protected HttpProviderConfiguration configuration;

    protected volatile Queue<StreamsDatum> providerQueue = new ConcurrentLinkedQueue<StreamsDatum>();

    protected final ReadWriteLock lock = new ReentrantReadWriteLock();

    private ExecutorService executor;

    public SimpleHttpProvider() {
        this(HttpConfigurator.detectProviderConfiguration(StreamsConfigurator.config.getConfig("http")));
    }

    public SimpleHttpProvider(HttpProviderConfiguration providerConfiguration) {
        LOGGER.info("creating SimpleHttpProvider");
        LOGGER.info(providerConfiguration.toString());
        this.configuration = providerConfiguration;
    }

    @Override
    public String getId() {
        return STREAMS_ID;
    }

    /**
      Override this to add parameters to the request
     */
    protected Map<String, String> prepareParams(StreamsDatum entry) {

        return Maps.newHashMap();
    }

    public HttpRequestBase prepareHttpRequest(URI uri) {
        HttpRequestBase request;
        if( configuration.getRequestMethod().equals(HttpProviderConfiguration.RequestMethod.GET)) {
            request = new HttpGet(uri);
        } else if( configuration.getRequestMethod().equals(HttpProviderConfiguration.RequestMethod.POST)) {
            request = new HttpPost(uri);
        } else {
            // this shouldn't happen because of the default
            request = new HttpGet(uri);
        }

        request.addHeader("content-type", this.configuration.getContentType());

        return request;

    }

    @Override
    public void prepare(Object configurationObject) {

        mapper = StreamsJacksonMapper.getInstance();

        uriBuilder = new URIBuilder()
            .setScheme(this.configuration.getProtocol())
            .setHost(this.configuration.getHostname())
            .setPort(this.configuration.getPort().intValue())
            .setPath(this.configuration.getResourcePath());

        SSLContextBuilder builder = new SSLContextBuilder();
        SSLConnectionSocketFactory sslsf = null;
        try {
            builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
            sslsf = new SSLConnectionSocketFactory(
                    builder.build(), SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn(e.getMessage());
        } catch (KeyManagementException e) {
            LOGGER.warn(e.getMessage());
        } catch (KeyStoreException e) {
            LOGGER.warn(e.getMessage());
        }

        httpclient = HttpClients.custom().setSSLSocketFactory(
                sslsf).build();

        executor = Executors.newSingleThreadExecutor();

    }

    @Override
    public void cleanUp() {

        LOGGER.info("shutting down SimpleHttpProvider");
        this.shutdownAndAwaitTermination(executor);
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

        executor.execute(new Runnable() {
            @Override
            public void run() {

                readCurrent();

                Uninterruptibles.sleepUninterruptibly(5, TimeUnit.SECONDS);

            }
        });
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

        List<ObjectNode> results = execute(uri);

        lock.writeLock().lock();

        for( ObjectNode item : results ) {
            providerQueue.add(newDatum(item));
        }

        LOGGER.debug("Creating new result set for {} items", providerQueue.size());
        current = new StreamsResultSet(providerQueue);

        return current;
    }

    protected List<ObjectNode> execute(URI uri) {

        Preconditions.checkNotNull(uri);

        List<ObjectNode> results = new ArrayList<>();

        HttpRequestBase httpRequest = prepareHttpRequest(uri);

        CloseableHttpResponse response = null;

        String entityString = null;
        try {
            response = httpclient.execute(httpRequest);
            HttpEntity entity = response.getEntity();
            // TODO: handle retry
            if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
                entityString = EntityUtils.toString(entity);
                if( !entityString.equals("{}") && !entityString.equals("[]") ) {
                    JsonNode jsonNode = mapper.readValue(entityString, JsonNode.class);
                    results = parse(jsonNode);
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

    /**
     Override this to change how entity gets converted to objects
     */
    protected List<ObjectNode> parse(JsonNode jsonNode) {

        List<ObjectNode> results = new ArrayList<>();

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

        return results;
    }

    /**
     Override this to change how metadata is derived from object
     */
    protected StreamsDatum newDatum(ObjectNode item) {
        try {
            String id = null;
            if( item.get("id") != null )
                id = item.get("id").asText();
            DateTime timestamp = null;
            if( item.get("timestamp") != null )
                timestamp = new DateTime(item.get("timestamp").asText());
            if( id != null && timestamp != null )
                return new StreamsDatum(item, id, timestamp);
            else if( id != null )
                return new StreamsDatum(item, id);
            else if( timestamp != null )
                return new StreamsDatum(item, null, timestamp);
            else return new StreamsDatum(item);
        } catch( Exception e ) {
            return new StreamsDatum(item);
        }
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

    protected void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(10, TimeUnit.SECONDS))
                    LOGGER.error("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
