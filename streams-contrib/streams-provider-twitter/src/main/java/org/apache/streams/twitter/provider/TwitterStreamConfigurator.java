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

package org.apache.streams.twitter.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.twitter.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by sblackmon on 12/10/13.
 */
public class TwitterStreamConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterStreamConfigurator.class);
    private final static ObjectMapper mapper = new ObjectMapper();


    public static TwitterConfiguration detectTwitterConfiguration(Config config) {
        TwitterConfiguration twitterConfiguration = new TwitterConfiguration();

        try {
            Config basicauth = StreamsConfigurator.config.getConfig("twitter.basicauth");
            TwitterBasicAuthConfiguration twitterBasicAuthConfiguration = new TwitterBasicAuthConfiguration();
            twitterBasicAuthConfiguration.setUsername(basicauth.getString("username"));
            twitterBasicAuthConfiguration.setPassword(basicauth.getString("password"));
            twitterConfiguration.setBasicauth(twitterBasicAuthConfiguration);
        } catch( ConfigException ce ) {}

        try {
            Config oauth = StreamsConfigurator.config.getConfig("twitter.oauth");
            TwitterOAuthConfiguration twitterOAuthConfiguration = new TwitterOAuthConfiguration();
            twitterOAuthConfiguration.setConsumerKey(oauth.getString("consumerKey"));
            twitterOAuthConfiguration.setConsumerSecret(oauth.getString("consumerSecret"));
            twitterOAuthConfiguration.setAccessToken(oauth.getString("accessToken"));
            twitterOAuthConfiguration.setAccessTokenSecret(oauth.getString("accessTokenSecret"));
            twitterConfiguration.setOauth(twitterOAuthConfiguration);
        } catch( ConfigException ce ) {}

        twitterConfiguration.setEndpoint(config.getString("endpoint"));

        return twitterConfiguration;
    }

    public static TwitterStreamConfiguration detectConfiguration(Config config) {

        TwitterStreamConfiguration twitterStreamConfiguration = mapper.convertValue(detectTwitterConfiguration(config), TwitterStreamConfiguration.class);

        try {
            twitterStreamConfiguration.setTrack(config.getStringList("track"));
        } catch( ConfigException ce ) {}

        try {
            // create the array
            List<Long> follows = Lists.newArrayList();
            // add the ids of the people we want to 'follow'
            for(Integer id : config.getIntList("follow"))
                follows.add((long)id);
            // set the array
            twitterStreamConfiguration.setFollow(follows);

        } catch( ConfigException ce ) {}

        twitterStreamConfiguration.setFilterLevel(config.getString("filter-level"));
        twitterStreamConfiguration.setWith(config.getString("with"));
        twitterStreamConfiguration.setReplies(config.getString("replies"));
        twitterStreamConfiguration.setJsonStoreEnabled("true");
        twitterStreamConfiguration.setIncludeEntities("true");

        return twitterStreamConfiguration;
    }

    public static TwitterUserInformationConfiguration detectTwitterUserInformationConfiguration(Config config) {
        return mapper.convertValue(detectTwitterConfiguration(config), TwitterUserInformationConfiguration.class);
    }

}
