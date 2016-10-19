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

package org.apache.streams.mongo.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.mongo.MongoConfiguration;
import org.apache.streams.mongo.MongoPersistReader;
import org.apache.streams.mongo.MongoPersistWriter;
import org.apache.streams.pojo.json.Activity;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test writing documents
 */
public class MongoPersistIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(MongoPersistIT.class);

    ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    MongoConfiguration testConfiguration;

    int count = 0;

    @Before
    public void setup() throws Exception {

        Config reference  = ConfigFactory.load();
        File conf_file = new File("target/test-classes/MongoPersistIT.conf");
        assert(conf_file.exists());
        Config testResourceConfig  = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));
        Config typesafe  = testResourceConfig.withFallback(reference).resolve();
        testConfiguration = new ComponentConfigurator<>(MongoConfiguration.class).detectConfiguration(typesafe, "mongo");

    }

    @Test
    public void testMongoPersist() throws Exception {

        MongoPersistWriter writer = new MongoPersistWriter(testConfiguration);

        writer.prepare(null);

        InputStream testActivityFolderStream = MongoPersistIT.class.getClassLoader()
                .getResourceAsStream("activities");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        for( String file : files) {
            LOGGER.info("File: " + file );
            InputStream testActivityFileStream = MongoPersistIT.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
            activity.getAdditionalProperties().remove("$license");
            StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
            writer.write( datum );
            LOGGER.info("Wrote: " + activity.getVerb() );
            count++;
        }

        LOGGER.info("Total Written: {}", count );

        assertEquals( 89, count );

        writer.cleanUp();

        MongoPersistReader reader = new MongoPersistReader(testConfiguration);

        reader.prepare(null);

        StreamsResultSet resultSet = reader.readAll();

        LOGGER.info("Total Read: {}", resultSet.size() );

        assertEquals( 89, resultSet.size() );

    }
}
