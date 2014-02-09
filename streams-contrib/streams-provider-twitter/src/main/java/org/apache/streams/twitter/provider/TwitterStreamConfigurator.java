package org.apache.streams.twitter.provider;

import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.twitter.TwitterOAuthConfiguration;
import org.apache.streams.twitter.TwitterStreamConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterStreamConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterStreamConfigurator.class);

    public static TwitterStreamConfiguration detectConfiguration(Config twitter) {
        Config oauth = StreamsConfigurator.config.getConfig("twitter.oauth");

        TwitterStreamConfiguration twitterStreamConfiguration = new TwitterStreamConfiguration();
        twitterStreamConfiguration.setProtocol(twitter.getString("protocol"));
        twitterStreamConfiguration.setHost(twitter.getString("host"));
        twitterStreamConfiguration.setPort(twitter.getLong("port"));
        twitterStreamConfiguration.setVersion(twitter.getString("version"));
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
            List<Long> follows = Lists.newArrayList();
            for( Integer id : twitter.getIntList("follow"))
                follows.add(new Long(id));
            twitterStreamConfiguration.setFollow(follows);
        } catch( ConfigException ce ) {}

        twitterStreamConfiguration.setFilterLevel(twitter.getString("filter-level"));
        twitterStreamConfiguration.setEndpoint(twitter.getString("endpoint"));
        twitterStreamConfiguration.setJsonStoreEnabled("true");
        twitterStreamConfiguration.setIncludeEntities("true");

        return twitterStreamConfiguration;
    }

}
