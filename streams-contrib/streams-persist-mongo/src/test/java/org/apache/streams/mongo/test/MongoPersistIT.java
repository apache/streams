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

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.mongo.MongoConfiguration;
import org.apache.streams.mongo.MongoPersistReader;
import org.apache.streams.mongo.MongoPersistWriter;
import org.apache.streams.pojo.json.Activity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Test writing documents
 */
public class MongoPersistIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoPersistIT.class);

  private ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  private MongoConfiguration testConfiguration;

  private int count = 0;

  @BeforeClass
  public void setup() throws Exception {

    testConfiguration = new ComponentConfigurator<>(MongoConfiguration.class).detectConfiguration();

  }

  @Test
  public void testMongoPersist() throws Exception {

    MongoPersistWriter writer = new MongoPersistWriter(testConfiguration);

    writer.prepare(testConfiguration);

    Path testdataDir = Paths.get("target/dependency/activitystreams-testdata");
    List<Path> testdataPaths = Files.list(testdataDir).collect(Collectors.toList());
    for( Path docPath : testdataPaths ) {
      LOGGER.info("File: " + docPath );
      FileInputStream testActivityFileStream = new FileInputStream(docPath.toFile());
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
