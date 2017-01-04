package org.apache.streams.neo4j.http;

import org.apache.streams.neo4j.Neo4jConfiguration;

import com.google.common.base.Preconditions;

import org.apache.http.client.HttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Neo4jHttpClient {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(Neo4jHttpClient.class);

    public Neo4jConfiguration config;

    private HttpClient client;

    private Neo4jHttpClient(Neo4jConfiguration neo4jConfiguration) {
        this.config = neo4jConfiguration;
        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
            this.client = null;
        }
    }

    private static Map<Neo4jConfiguration, Neo4jHttpClient> INSTANCE_MAP = new ConcurrentHashMap<Neo4jConfiguration, Neo4jHttpClient>();

    public static Neo4jHttpClient getInstance(Neo4jConfiguration neo4jConfiguration) {
        if (    INSTANCE_MAP != null &&
                INSTANCE_MAP.size() > 0 &&
                INSTANCE_MAP.containsKey(neo4jConfiguration)
                )
            return INSTANCE_MAP.get(neo4jConfiguration);
        else {
            Neo4jHttpClient instance = new Neo4jHttpClient(neo4jConfiguration);
            if( instance != null && instance.client != null ) {
                INSTANCE_MAP.put(neo4jConfiguration, instance);
                return instance;
            } else {
                return null;
            }
        }
    }

    public void start() throws Exception {

        Preconditions.checkNotNull(config);
        Preconditions.checkArgument(
                config.getScheme().startsWith("http")
        );

        LOGGER.info("Neo4jConfiguration.start {}", config);

        Preconditions.checkNotNull(client);

//        try (Session session = driver.session()) {
//            session.run()
//        } catch( Exception e ) {
//            LOGGER.warn("Exception: {}", e);
//            return Optional.absent();
//        }


    }

    public void stop() throws Exception {
        this.client = null;
    }

    public Neo4jConfiguration config() {
        return config;
    }

    public HttpClient client() {
        return client;
    }
}
