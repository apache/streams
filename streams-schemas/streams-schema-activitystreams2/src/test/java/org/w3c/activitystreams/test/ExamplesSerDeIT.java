/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.w3c.activitystreams.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Tests that activities matching core-ex* can be parsed by apache streams.
 */
public class ExamplesSerDeIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExamplesSerDeIT.class);

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Tests that activities matching core-ex* can be parsed by apache streams.
   *
   * @throws Exception test exception
   */
  @Test
  public void testCoreSerDe() throws Exception {

    InputStream testActivityFolderStream = ExamplesSerDeIT.class.getClassLoader()
        .getResourceAsStream("w3c/activitystreams-master/test");
    List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

    for (String file : files) {
      if ( !file.startsWith(".") && file.contains("core-ex") ) {
        LOGGER.info("File: activitystreams-master/test/" + file);
        String testFileString = new String(Files.readAllBytes(Paths.get("target/test-classes/w3c/activitystreams-master/test/" + file)));
        LOGGER.info("Content: " + testFileString);
        ObjectNode testFileObjectNode = MAPPER.readValue(testFileString, ObjectNode.class);
        LOGGER.info("Object:" + testFileObjectNode);
      }
    }
  }

  /**
   * Tests that activities matching simple* can be parsed by apache streams.
   *
   * @throws Exception test exception
   */
  @Test
  public void testSimpleSerDe() throws Exception {

    InputStream testActivityFolderStream = ExamplesSerDeIT.class.getClassLoader()
        .getResourceAsStream("w3c/activitystreams-master/test");
    List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

    for (String file : files) {
      if ( !file.startsWith(".") && file.contains("simple") ) {
        LOGGER.info("File: activitystreams-master/test/" + file);
        String testFileString = new String(Files.readAllBytes(Paths.get("target/test-classes/w3c/activitystreams-master/test/" + file)));
        LOGGER.info("Content: " + testFileString);
        ObjectNode testFileObjectNode = MAPPER.readValue(testFileString, ObjectNode.class);
        LOGGER.info("Object:" + testFileObjectNode);
      }
    }
  }

  /**
   * Tests that activities matching vocabulary-ex* can be parsed by apache streams.
   *
   * @throws Exception test exception
   */
  @Ignore
  @Test
  public void testVocabularySerDe() throws Exception {

    InputStream testActivityFolderStream = ExamplesSerDeIT.class.getClassLoader()
        .getResourceAsStream("w3c/activitystreams-master/test");
    List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

    for (String file : files) {
      if ( !file.startsWith(".") && file.contains("vocabulary-ex") ) {
        LOGGER.info("File: activitystreams-master/test/" + file);
        String testFileString = new String(Files.readAllBytes(Paths.get("target/test-classes/w3c/activitystreams-master/test/" + file)));
        LOGGER.info("Content: " + testFileString);
        ObjectNode testFileObjectNode = MAPPER.readValue(testFileString, ObjectNode.class);
        LOGGER.info("Object:" + testFileObjectNode);
      }
    }
  }

  /**
   * Tests that activities expect to fail cannot be parsed by apache streams.
   *
   * @throws Exception test exception
   */
  @Ignore
  @Test
  public void testFailSerDe() throws Exception {

    InputStream testActivityFolderStream = ExamplesSerDeIT.class.getClassLoader()
        .getResourceAsStream("w3c/activitystreams-master/test/fail");
    List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

    for (String file : files) {
      if ( !file.startsWith(".") && file.contains("vocabulary-ex") ) {
        LOGGER.info("File: activitystreams-master/test/fail/" + file);
        String testFileString = new String(Files.readAllBytes(Paths.get("target/test-classes/w3c/activitystreams-master/test/" + file)));
        LOGGER.info("Content: " + testFileString);
        ObjectNode testFileObjectNode = MAPPER.readValue(testFileString, ObjectNode.class);
        LOGGER.info("Object:" + testFileObjectNode);
      }
    }
  }
}