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

package org.apache.streams.cli.test;

import org.apache.streams.cli.RdfFreemarkerCli;

import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Tests for {$link: org.apache.streams.cli.RdfFreemarkerCli}
 */
public class TestRdfFreemarkerCli {

  @Test
  public void testRdfFreemarkerCli() throws Exception {
    String[] testArgs = {
      ".", //baseDir
      "src/main/resources/default.fmpp", //settingsFile
      "src/test/resources", //sourceRoot
      "src/test/resources", //dataRoot
      "target/test-classes", //outputRoot
      "http://streams.apache.org/streams-cli", //namespace
      "testPerson" //id
    };
    RdfFreemarkerCli rdfFreemarkerCli = new RdfFreemarkerCli(testArgs);
    Boolean success = rdfFreemarkerCli.call();
    assert(success);
    assert(Files.exists(Paths.get("target/test-classes/test.ttl")));
  }
}
