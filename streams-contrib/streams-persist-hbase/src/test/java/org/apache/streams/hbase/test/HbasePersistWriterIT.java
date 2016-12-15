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
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.hbase.HbaseConfiguration;
import org.apache.streams.hbase.HbasePersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
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
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.testng.Assert.assertTrue;

/**
 * Test HbasePersistWriterIT.
 */
@Test
    (
        groups={"HbasePersistWriterIT"}
    )
public class HbasePersistWriterIT {

  private final static Logger LOGGER = LoggerFactory.getLogger(HbasePersistWriterIT.class);

  private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

  protected HbaseConfiguration testConfiguration;

  @BeforeClass
  public void prepareTest() throws Exception {

    Config reference  = ConfigFactory.load();
    File conf_file = new File("target/test-classes/HbasePersistWriterIT.conf");
    assertTrue(conf_file.exists());
    Config testResourceConfig  = ConfigFactory.parseFileAnySyntax(conf_file, ConfigParseOptions.defaults().setAllowMissing(false));
    Config typesafe  = testResourceConfig.withFallback(reference).resolve();
    testConfiguration = new ComponentConfigurator<>(HbaseConfiguration.class).detectConfiguration(typesafe, "hbase");

  }

  @Test(enabled = false)
  public void testPersistWriter() throws Exception {

    HbasePersistWriter testPersistWriter = new HbasePersistWriter(testConfiguration);
    testPersistWriter.prepare(testConfiguration);

    InputStream testActivityFolderStream = HbasePersistWriterIT.class.getClassLoader()
        .getResourceAsStream("activities");
    List<String> files = IOUtils.readLines(testActivityFolderStream, StandardCharsets.UTF_8);

    for( String file : files) {
      LOGGER.info("File: " + file );
      InputStream testActivityFileStream = HbasePersistWriterIT.class.getClassLoader()
          .getResourceAsStream("activities/" + file);
      Activity activity = MAPPER.readValue(testActivityFileStream, Activity.class);
      StreamsDatum datum = new StreamsDatum(activity, activity.getVerb());
      testPersistWriter.write( datum );
      LOGGER.info("Wrote: " + activity.getVerb() );
    }

    testPersistWriter.cleanUp();

  }

}
