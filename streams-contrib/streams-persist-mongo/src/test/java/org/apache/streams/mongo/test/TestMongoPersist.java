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
import com.github.fakemongo.Fongo;
import com.mongodb.DB;
import com.mongodb.DBAddress;
import com.mongodb.MongoClient;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.mongo.MongoConfiguration;
import org.apache.streams.mongo.MongoPersistReader;
import org.apache.streams.mongo.MongoPersistWriter;
import org.apache.streams.pojo.json.Activity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;

/**
 * Test copying documents between two indexes on same cluster
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({MongoPersistReader.class, MongoPersistWriter.class, MongoClient.class, DB.class})
public class TestMongoPersist {

    private final static Logger LOGGER = LoggerFactory.getLogger(TestMongoPersist.class);

    ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    MongoClient mockClient;

    Fongo fongo;
    DB mockDB;

    int count = 0;

    @Before
    public void setup() {
        fongo = new Fongo("testmongo");
        mockDB = fongo.getDB("test");

        this.mockClient = PowerMockito.mock(MongoClient.class);

        PowerMockito.when(mockClient.getDB(Mockito.anyString()))
                .thenReturn(mockDB);

        try {
            PowerMockito.whenNew(MongoClient.class).withAnyArguments().thenReturn(mockClient);
        } catch (Exception e) {}

    }

    @Test
    public void testMongoPersist() throws Exception {

        MongoConfiguration mongoConfiguration = new MongoConfiguration().withHost("localhost").withDb("test").withPort(37017l).withCollection("activities");

        MongoPersistWriter writer = new MongoPersistWriter(mongoConfiguration);

        writer.prepare(null);

        InputStream testActivityFolderStream = TestMongoPersist.class.getClassLoader()
                .getResourceAsStream("activities");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        for( String file : files) {
            LOGGER.info("File: " + file );
            InputStream testActivityFileStream = TestMongoPersist.class.getClassLoader()
                    .getResourceAsStream("activities/" + file);
            Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
            activity.getAdditionalProperties().remove("$license");
            StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
            writer.write( datum );
            LOGGER.info("Wrote: " + activity.getVerb() );
            count++;
        }

        writer.cleanUp();

        MongoPersistReader reader = new MongoPersistReader(mongoConfiguration);

        reader.prepare(null);

        StreamsResultSet resultSet = reader.readAll();

        assert( resultSet.size() == count);

    }
}
