package org.apache.streams.mongo;

/*
 * #%L
 * streams-persist-mongo
 * %%
 * Copyright (C) 2013 - 2014 Apache Streams Project
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

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
