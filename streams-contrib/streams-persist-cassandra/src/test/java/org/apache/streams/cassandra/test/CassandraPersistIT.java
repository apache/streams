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

package org.apache.streams.cassandra.test;

import org.apache.streams.cassandra.CassandraConfiguration;
import org.apache.streams.cassandra.CassandraPersistReader;
import org.apache.streams.cassandra.CassandraPersistWriter;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;

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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Test writing documents
 */
public class CassandraPersistIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(CassandraPersistIT.class);

  private ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  private CassandraConfiguration testConfiguration;

  private int count = 0;

  @BeforeClass
  public void setup() throws Exception {
    Config reference = ConfigFactory.load();
    File conf_file = new File("target/test-classes/CassandraPersistIT.conf");
    assert(conf_file.exists());
    Config testResourceConfig = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));
    Config typesafe = testResourceConfig.withFallback(reference).resolve();
    testConfiguration = new ComponentConfigurator<>(CassandraConfiguration.class).detectConfiguration(typesafe, "cassandra");
  }

  @Test
  public void testCassandraPersist() throws Exception {
    CassandraPersistWriter writer = new CassandraPersistWriter(testConfiguration);

    writer.prepare(null);

    InputStream testActivityFolderStream = CassandraPersistIT.class.getClassLoader()
        .getResourceAsStream("activities");
    List<String> files = IOUtils.readLines(testActivityFolderStream, StandardCharsets.UTF_8);

    for (String file: files) {
      LOGGER.info("File: " + file );
      InputStream testActivityFileStream = CassandraPersistIT.class.getClassLoader()
          .getResourceAsStream("activities/" + file);
      Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
      activity.getAdditionalProperties().remove("$license");
      StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
      writer.write(datum);

      LOGGER.info("Wrote: " + activity.getVerb() );
      count++;
    }

    LOGGER.info("Total Written: {}", count );
    Assert.assertEquals(89, count);

    writer.cleanUp();

    CassandraPersistReader reader = new CassandraPersistReader(testConfiguration);

    reader.prepare(null);

    StreamsResultSet resultSet = reader.readAll();

    LOGGER.info("Total Read: {}", resultSet.size() );
    Assert.assertEquals(resultSet.size(), 89);
  }
}