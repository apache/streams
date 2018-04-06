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

package org.apache.streams.riak.test;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.riak.http.RiakHttpClient;
import org.apache.streams.riak.http.RiakHttpPersistReader;
import org.apache.streams.riak.http.RiakHttpPersistWriter;
import org.apache.streams.riak.pojo.RiakConfiguration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static org.testng.Assert.assertTrue;

/**
 * Integration test for RiakHttpPersist.
 */
public class RiakHttpPersistIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(RiakHttpPersistIT.class);

  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  private RiakHttpClient testClient;

  private RiakConfiguration testConfiguration;

  @BeforeClass
  public void prepareTest() throws IOException {

    testConfiguration = new ComponentConfigurator<>(RiakConfiguration.class).detectConfiguration( "RiakHttpPersistIT");
    testClient = RiakHttpClient.getInstance(testConfiguration);

  }

  @Test
  public void testRiakHttpPersist() throws Exception {

    RiakHttpPersistWriter testPersistWriter = new RiakHttpPersistWriter(testConfiguration);
    testPersistWriter.prepare(testConfiguration);

    int count = 0;
    Path testdataDir = Paths.get("target/dependency/activitystreams-testdata");
    List<Path> testdataPaths = Files.list(testdataDir).collect(Collectors.toList());
    for( Path docPath : testdataPaths ) {
      LOGGER.info("File: " + docPath );
      FileInputStream testActivityFileStream = new FileInputStream(docPath.toFile());
      Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
      StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
      testPersistWriter.write( datum );
      LOGGER.info("Wrote: " + activity.getVerb() );
      count++;
    }

    testPersistWriter.cleanUp();

    LOGGER.info("Total Written: {}", count );
    Assert.assertEquals(count, 89);

    RiakHttpPersistReader testPersistReader = new RiakHttpPersistReader(testConfiguration);
    testPersistReader.prepare(testConfiguration);

    StreamsResultSet readerResultSet = testPersistReader.readAll();
    LOGGER.info("Total Read: {}", readerResultSet.size() );
    Assert.assertEquals(readerResultSet.size(), 89);

  }

}
