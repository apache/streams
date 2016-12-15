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

package org.apache.streams.hbase.test;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.hbase.HbaseConfiguration;
import org.apache.streams.hbase.HbasePersistReader;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.Uninterruptibles;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigParseOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.concurrent.TimeUnit;

/**
 * Test HbasePersistReader.
 */
@Test
    (
        groups={"HbasePersistReaderIT"},
        dependsOnGroups={"HbasePersistWriterIT"}
    )
public class HbasePersistReaderIT {

  private final static Logger LOGGER = LoggerFactory.getLogger(HbasePersistReaderIT.class);

  private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  protected HbaseConfiguration testConfiguration;

  @BeforeClass
  public void prepareTest() throws Exception {

    Config reference  = ConfigFactory.load();
    File conf_file = new File("target/test-classes/HbasePersistReaderIT.conf");
    assert(conf_file.exists());
    Config testResourceConfig  = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));
    Config typesafe  = testResourceConfig.withFallback(reference).resolve();
    testConfiguration = new ComponentConfigurator<>(HbaseConfiguration.class).detectConfiguration(typesafe, "hbase");

  }

  @Test(enabled = false)
  public void testPersistReader() throws Exception {

    HbasePersistReader testPersistReader = new HbasePersistReader(testConfiguration);
    testPersistReader.prepare(testConfiguration);
    testPersistReader.startStream();

    Uninterruptibles.sleepUninterruptibly(10, TimeUnit.SECONDS);

    testPersistReader.cleanUp();

    while( testPersistReader.isRunning() ) {
      Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
    }

    StreamsResultSet resultSet = testPersistReader.readCurrent();

    assert( resultSet.size() > 0 );

  }

}
