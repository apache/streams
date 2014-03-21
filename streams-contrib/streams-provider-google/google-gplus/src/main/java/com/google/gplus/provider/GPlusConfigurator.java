package com.google.gplus.provider;

import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.google.gplus.GPlusConfiguration;
import org.apache.streams.google.gplus.GPlusOAuthConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class GPlusConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(GPlusConfigurator.class);

    public static GPlusConfiguration detectConfiguration(Config config) {
        Config oauth = StreamsConfigurator.config.getConfig("gplus.oauth");

        GPlusConfiguration gplusConfiguration = new GPlusConfiguration();

        gplusConfiguration.setProtocol(config.getString("protocol"));
        gplusConfiguration.setHost(config.getString("host"));
        gplusConfiguration.setPort(config.getLong("port"));
        gplusConfiguration.setVersion(config.getString("version"));
        GPlusOAuthConfiguration gPlusOAuthConfiguration = new GPlusOAuthConfiguration();
        gPlusOAuthConfiguration.setConsumerKey(oauth.getString("consumerKey"));
        gPlusOAuthConfiguration.setConsumerSecret(oauth.getString("consumerSecret"));
        gPlusOAuthConfiguration.setAccessToken(oauth.getString("accessToken"));
        gPlusOAuthConfiguration.setAccessTokenSecret(oauth.getString("accessTokenSecret"));
        gplusConfiguration.setOauth(gPlusOAuthConfiguration);

        return gplusConfiguration;
    }

}
