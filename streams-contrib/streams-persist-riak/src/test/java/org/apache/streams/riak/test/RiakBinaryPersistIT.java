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
import org.apache.streams.riak.binary.RiakBinaryClient;
import org.apache.streams.riak.binary.RiakBinaryPersistReader;
import org.apache.streams.riak.binary.RiakBinaryPersistWriter;
import org.apache.streams.riak.pojo.RiakConfiguration;

import com.basho.riak.client.api.RiakCommand;
import com.basho.riak.client.api.commands.buckets.ListBuckets;
import com.basho.riak.client.core.NodeStateListener;
import com.basho.riak.client.core.RiakNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.testng.Assert.assertTrue;

/**
 * Integration test for RiakBinaryPersist.
 */
public class RiakBinaryPersistIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(RiakBinaryPersistIT.class);

  private static final ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  private RiakBinaryClient testClient;

  private RiakConfiguration testConfiguration;

  @BeforeClass
  public void prepareTest() throws Exception {

    testConfiguration = new ComponentConfigurator<>(RiakConfiguration.class).detectConfiguration( "RiakBinaryPersistIT");
    testClient = RiakBinaryClient.getInstance(testConfiguration);

    Assert.assertTrue(testClient.client().getRiakCluster().getNodes().size() > 0);

    Thread.sleep(10000);
    
    ListBuckets.Builder builder = new ListBuckets.Builder("default");

    testClient.client().execute(builder.build(), 30, TimeUnit.SECONDS);
  
  }

  @Test
  public void testRiakBinaryPersist() throws Exception {

    RiakBinaryPersistWriter testPersistWriter = new RiakBinaryPersistWriter(testConfiguration);
    testPersistWriter.prepare(testConfiguration);

    InputStream testActivityFolderStream = RiakBinaryPersistIT.class.getClassLoader()
        .getResourceAsStream("activities");
    List<String> files = IOUtils.readLines(testActivityFolderStream, StandardCharsets.UTF_8);

    int count = 0;
    for( String file : files) {
      LOGGER.info("File: " + file );
      InputStream testActivityFileStream = RiakBinaryPersistIT.class.getClassLoader()
          .getResourceAsStream("activities/" + file);
      Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);

      StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
      testPersistWriter.write( datum );
      LOGGER.info("Wrote: " + activity.getVerb() );
      count++;
    }

    testPersistWriter.cleanUp();

    LOGGER.info("Total Written: {}", count );
    Assert.assertEquals(count, 89);

    RiakBinaryPersistReader testPersistReader = new RiakBinaryPersistReader(testConfiguration);
    testPersistReader.prepare(testConfiguration);

    StreamsResultSet readerResultSet = testPersistReader.readAll();
    Assert.assertNotNull(readerResultSet);
    LOGGER.info("Total Read: {}", readerResultSet.size() );
    Assert.assertEquals(readerResultSet.size(), 89);

  }

  @AfterClass
  public void cleanup() throws Exception {

  }

}
