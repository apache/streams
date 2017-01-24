package org.apache.streams.riak.http;

import org.apache.streams.riak.pojo.RiakConfiguration;

import org.apache.http.client.HttpClient;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class RiakHttpClient {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(RiakHttpClient.class);

    public RiakConfiguration config;

    protected CloseableHttpClient client;
    protected URI baseURI;

    private RiakHttpClient(RiakConfiguration config) {
        this.config = config;
        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
            this.client = null;
        }
    }

    private static Map<RiakConfiguration, RiakHttpClient> INSTANCE_MAP = new ConcurrentHashMap<>();

    public static RiakHttpClient getInstance(RiakConfiguration riakConfiguration) {
        if ( INSTANCE_MAP != null
             && INSTANCE_MAP.size() > 0
             && INSTANCE_MAP.containsKey(riakConfiguration)) {
            return INSTANCE_MAP.get(riakConfiguration);
        } else {
            RiakHttpClient instance = new RiakHttpClient(riakConfiguration);
            if( instance != null && instance.client != null ) {
                INSTANCE_MAP.put(riakConfiguration, instance);
                return instance;
            } else {
                return null;
            }
        }
    }

    public void start() throws Exception {
        Objects.nonNull(config);
        assert(config.getScheme().startsWith("http"));
        URIBuilder uriBuilder = new URIBuilder();
        uriBuilder.setScheme(config.getScheme());
        uriBuilder.setHost(config.getHosts().get(0));
        uriBuilder.setPort(config.getPort().intValue());
        baseURI = uriBuilder.build();
        client = HttpClients.createDefault();
    }

    public void stop() {
        try {
            client.close();
        } catch( Exception e) {
            LOGGER.error( "Exception", e );
        } finally {
            client = null;
        }
    }

    public RiakConfiguration config() {
        return config;
    }

    public HttpClient client() {
        return client;
    }

}
