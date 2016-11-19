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

package org.apache.streams.json.test;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.json.JsonPathExtractor;

import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * Test for extracting json fields and
 * objects from datums using JsonPath syntax.
 */
public class JsonPathExtractorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(JsonPathExtractorTest.class);

  private String testJson;

  @Before
  public void initialize() {
    try {
      testJson = FileUtils.readFileToString(new File("src/test/resources/books.json"));
    } catch (IOException ex) {
      ex.printStackTrace();
      Assert.fail();
    }
  }

  @Test
  public void test1()
  {
    JsonPathExtractor extractor = new JsonPathExtractor();
    extractor.prepare("$.store.book[*].author");
    List<StreamsDatum> result = extractor.process(new StreamsDatum(testJson));
    assertThat(result.size(), is(2));
    assertTrue(result.get(0).getDocument() instanceof String);
    assertTrue(result.get(1).getDocument() instanceof String);
  }

  @Test
  public void test2()
  {
    JsonPathExtractor extractor = new JsonPathExtractor();
    extractor.prepare("$.store.book[?(@.category == 'reference')]");
    List<StreamsDatum> result = extractor.process(new StreamsDatum(testJson));
    assertThat(result.size(), is(1));
    assertTrue(result.get(0).getDocument() instanceof ObjectNode);
  }

  @Test
  public void test3()
  {
    JsonPathExtractor extractor = new JsonPathExtractor();
    extractor.prepare("$.store.book[?(@.price > 10)]");
    List<StreamsDatum> result = extractor.process(new StreamsDatum(testJson));
    assertThat(result.size(), is(1));
    assertTrue(result.get(0).getDocument() instanceof ObjectNode);
  }

  @Test
  public void test4()
  {
    JsonPathExtractor extractor = new JsonPathExtractor();
    extractor.prepare("$.store.book[?(@.isbn)]");
    List<StreamsDatum> result = extractor.process(new StreamsDatum(testJson));
    assertThat(result.size(), is(1));
    assertTrue(result.get(0).getDocument() instanceof ObjectNode);
  }

}
