package org.apache.streams.twitter.provider;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.twitter.TwitterOAuthConfiguration;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterStreamConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterStreamConfigurator.class);

    public static TwitterStreamConfiguration detectConfiguration(Config twitter) {
        Config oauth = StreamsConfigurator.config.getConfig("twitter.oauth");

        TwitterStreamConfiguration twitterStreamConfiguration = new TwitterStreamConfiguration();
        TwitterOAuthConfiguration twitterOAuthConfiguration = new TwitterOAuthConfiguration();
        twitterOAuthConfiguration.setConsumerKey(oauth.getString("consumerKey"));
        twitterOAuthConfiguration.setConsumerSecret(oauth.getString("consumerSecret"));
        twitterOAuthConfiguration.setAccessToken(oauth.getString("accessToken"));
        twitterOAuthConfiguration.setAccessTokenSecret(oauth.getString("accessTokenSecret"));
        twitterStreamConfiguration.setOauth(twitterOAuthConfiguration);

        try {
            twitterStreamConfiguration.setTrack(twitter.getStringList("track"));
        } catch( ConfigException ce ) {}
        try {
            twitterStreamConfiguration.setFollow(twitter.getLongList("follow"));
        } catch( ConfigException ce ) {}

        twitterStreamConfiguration.setFilterLevel(twitter.getString("filter-level"));
        twitterStreamConfiguration.setEndpoint(twitter.getString("endpoint"));

        return twitterStreamConfiguration;
    }

}
