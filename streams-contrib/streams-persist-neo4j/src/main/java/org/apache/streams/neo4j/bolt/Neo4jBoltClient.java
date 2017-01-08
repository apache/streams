package org.apache.streams.neo4j.bolt;

import org.apache.streams.neo4j.Neo4jConfiguration;

import com.google.common.base.Preconditions;

import org.apache.commons.lang3.StringUtils;
import org.neo4j.driver.v1.AuthToken;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.MatcherAssert.assertThat;

public class Neo4jBoltClient {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(Neo4jBoltClient.class);

    private Driver client;

    public Neo4jConfiguration config;

    private Neo4jBoltClient(Neo4jConfiguration neo4jConfiguration) {
        this.config = neo4jConfiguration;
        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
            this.client = null;
        }
    }

    private static Map<Neo4jConfiguration, Neo4jBoltClient> INSTANCE_MAP = new ConcurrentHashMap<Neo4jConfiguration, Neo4jBoltClient>();

    public static Neo4jBoltClient getInstance(Neo4jConfiguration neo4jConfiguration) {
        if ( INSTANCE_MAP != null &&
             INSTANCE_MAP.size() > 0 &&
             INSTANCE_MAP.containsKey(neo4jConfiguration)) {
            return INSTANCE_MAP.get(neo4jConfiguration);
        } else {
            Neo4jBoltClient instance = new Neo4jBoltClient(neo4jConfiguration);
            if( instance != null && instance.client != null ) {
                INSTANCE_MAP.put(neo4jConfiguration, instance);
                return instance;
            } else {
                return null;
            }
        }
    }

    public void start() throws Exception {

        Objects.nonNull(config);
        assertThat("config.getScheme().startsWith(\"tcp\")", config.getScheme().startsWith("tcp"));

        LOGGER.info("Neo4jConfiguration.start {}", config);

        AuthToken authToken = null;
        if( StringUtils.isNotBlank(config.getUsername()) && StringUtils.isNotBlank(config.getPassword())) {
            authToken = AuthTokens.basic( config.getUsername(), config.getPassword() );
        }

        if( authToken == null ) {
            client = GraphDatabase.driver("bolt://" + config.getHosts().get(0) + ":" + config.getPort());
        } else {
            client = GraphDatabase.driver("bolt://" + config.getHosts().get(0) + ":" + config.getPort(), authToken);
        }

        Objects.nonNull(client);

    }

    public void stop() throws Exception {
        this.client.session().close();
        this.client = null;
    }

    public Neo4jConfiguration config() {
        return config;
    }

    public Driver client() {
        return client;
    }
}
