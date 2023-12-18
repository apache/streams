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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.networknt.schema.*;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Test validity of documents vs schemas.
 */
public class SchemaValidationTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaValidationTest.class);

  private static final ObjectMapper MAPPER = new ObjectMapper();

  /**
   * Tests that activities matching core-ex* can be parsed by apache streams.
   *
   * @throws Exception Test Exception
   */
  @Test
  public void testValidateToSchema() throws Exception {

    JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);

    InputStream testActivityFolderStream = SchemaValidationTest.class.getClassLoader()
        .getResourceAsStream("activities");
    List<String> files = IOUtils.readLines(testActivityFolderStream, StandardCharsets.UTF_8);

    for (String file : files) {
      if ( !file.startsWith(".") ) {

        LOGGER.debug("Test File: activities/" + file);
        String testFileString = new String(Files.readAllBytes(Paths.get("target/test-classes/activities/" + file)));
        LOGGER.debug("Test Document JSON: " + testFileString);
        JsonNode testNode = MAPPER.readValue(testFileString, ObjectNode.class);
        LOGGER.debug("Test Document Object:" + testNode);
        LOGGER.debug("Test Schema File: " + "target/classes/verbs/" + file);
        String testSchemaString = new String(Files.readAllBytes(Paths.get("target/classes/verbs/" + file)));
        LOGGER.debug("Test Schema JSON: " + testSchemaString);
        JsonNode testSchemaNode = MAPPER.readValue(testFileString, ObjectNode.class);
        LOGGER.debug("Test Schema Object:" + testSchemaNode);
        JsonSchema testSchema = factory.getSchema(testSchemaNode);
        LOGGER.debug("Test Schema:" + testSchema);

        Set<ValidationMessage> errors = testSchema.validate(testNode);
        assertThat(errors.size(), is(0));

      }
    }
  }

}
