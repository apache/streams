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
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.apache.streams.components.http.HttpConfigurator;
import org.apache.streams.components.http.HttpProcessorConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.data.util.ExtensionUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;
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
public class SimpleHTTPGetProcessor implements StreamsProcessor {

    private final static String STREAMS_ID = "SimpleHTTPGetProcessor";

    // from root config id
    private final static String EXTENSION = "account_type";

    private final static Logger LOGGER = LoggerFactory.getLogger(SimpleHTTPGetProcessor.class);

    protected ObjectMapper mapper;

    protected URIBuilder uriBuilder;

    protected CloseableHttpClient httpclient;

    protected HttpProcessorConfiguration configuration;

    protected String authHeader;
//
//    // authorized only
//    //private PeoplePatternConfiguration peoplePatternConfiguration = null;
//    //private String authHeader;
//
    public SimpleHTTPGetProcessor() {
        this(HttpConfigurator.detectProcessorConfiguration(StreamsConfigurator.config.getConfig("http")));
    }

    public SimpleHTTPGetProcessor(HttpProcessorConfiguration processorConfiguration) {
        LOGGER.info("creating SimpleHTTPGetProcessor");
        LOGGER.info(processorConfiguration.toString());
        this.configuration = processorConfiguration;
    }


    /**
     Override this to store a result other than exact json representation of response
     */
    protected ObjectNode prepareExtensionFragment(String entityString) {

        try {
            return mapper.readValue(entityString, ObjectNode.class);
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
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
            LOGGER.warn(e.getMessage());
            return null;
        } catch (IOException e) {
            LOGGER.warn(e.getMessage());
            return null;
        }

    }
        /**
         Override this to place result in non-standard location on document
         */
    protected ObjectNode getEntityToExtend(ObjectNode rootDocument) {

        if( this.configuration.getEntity().equals(HttpProcessorConfiguration.Entity.ACTIVITY))
            return rootDocument;
        else
            return (ObjectNode) rootDocument.get(this.configuration.getEntity().toString());

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
            LOGGER.error("URI error {}", uriBuilder.toString());
            return result;
        }

        HttpGet httpget = prepareHttpGet(uri);

        CloseableHttpResponse response = null;

        String entityString = null;
        try {
            response = httpclient.execute(httpget);
            HttpEntity entity = response.getEntity();
            // TODO: handle retry
            if (response.getStatusLine().getStatusCode() == 200 && entity != null) {
                entityString = EntityUtils.toString(entity);
            }
        } catch (IOException e) {
            LOGGER.error("IO error:\n{}\n{}\n{}", uri.toString(), response, e.getMessage());
            return result;
        } finally {
            try {
                response.close();
            } catch (IOException e) {}
            try {
                httpclient.close();
            } catch (IOException e) {}
        }

        if( entityString == null )
            return result;

        LOGGER.debug(entityString);

        ObjectNode extensionFragment = prepareExtensionFragment(entityString);

        ObjectNode extensionEntity = getEntityToExtend(rootDocument);

        ExtensionUtil.ensureExtensions(extensionEntity);

        ExtensionUtil.addExtension(extensionEntity, this.configuration.getExtension(), extensionFragment);

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


    public HttpGet prepareHttpGet(URI uri) {
        HttpGet httpget = new HttpGet(uri);
        httpget.addHeader("content-type", this.configuration.getContentType());
        if( !Strings.isNullOrEmpty(authHeader))
            httpget.addHeader("Authorization", String.format("Basic %s", authHeader));
        return httpget;
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
        LOGGER.info("shutting down SimpleHTTPGetProcessor");
    }
}
