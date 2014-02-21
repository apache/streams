package org.apache.streams.mongo;

import com.google.common.base.Objects;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by sblackmon on 12/10/13.
 */
public class MongoConfigurator {

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoConfigurator.class);

    public static MongoConfiguration detectConfiguration(Config mongo) {

        MongoConfiguration mongoConfiguration = new MongoConfiguration();

        mongoConfiguration.setHost(mongo.getString("host"));
        mongoConfiguration.setPort(new Long(mongo.getInt("port")));
        mongoConfiguration.setDb(mongo.getString("db"));
        mongoConfiguration.setCollection(mongo.getString("collection"));

        if( mongo.hasPath("user"))
            mongoConfiguration.setUser(mongo.getString("user"));
        if( mongo.hasPath("password"))
            mongoConfiguration.setPassword(mongo.getString("password"));
        return mongoConfiguration;
    }

}
