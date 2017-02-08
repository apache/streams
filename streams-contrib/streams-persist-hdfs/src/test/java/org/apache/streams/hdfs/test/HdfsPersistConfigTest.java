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

package org.apache.streams.hdfs.test;

import org.apache.streams.hdfs.HdfsConfiguration;
import org.apache.streams.hdfs.HdfsReaderConfiguration;
import org.apache.streams.hdfs.HdfsWriterConfiguration;
import org.apache.streams.hdfs.WebHdfsPersistReader;
import org.apache.streams.hdfs.WebHdfsPersistWriter;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;

/**
 * Test for checking that strings append to FS paths as expected
 */
public class HdfsPersistConfigTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(HdfsPersistConfigTest.class);

  @Test
  public void getWriterFileUriTest() {
    HdfsWriterConfiguration writerConfiguration = new HdfsWriterConfiguration();
    writerConfiguration.setScheme(HdfsConfiguration.Scheme.FILE);
    writerConfiguration.setPath("path");
    writerConfiguration.setWriterPath("writerPath");
    writerConfiguration.setUser("cloudera");

    WebHdfsPersistWriter webHdfsPersistWriter = new WebHdfsPersistWriter(writerConfiguration);

    String uri = null;
    try {
      uri = webHdfsPersistWriter.getURI().toString();
    } catch (URISyntaxException e) {
      Assert.fail("URI Syntax");
    }
    Assert.assertArrayEquals(uri.toCharArray(), ("file:///").toCharArray());
    webHdfsPersistWriter.prepare(null);
    Assert.assertTrue(webHdfsPersistWriter.isConnected());
  }

  @Test
  public void getWriterHdfsUriTest() {
    HdfsWriterConfiguration writerConfiguration = new HdfsWriterConfiguration();
    writerConfiguration.setScheme(HdfsConfiguration.Scheme.HDFS);
    writerConfiguration.setHost("localhost");
    writerConfiguration.setPort(9000L);
    writerConfiguration.setPath("path");
    writerConfiguration.setWriterPath("writerPath");
    writerConfiguration.setUser("cloudera");

    WebHdfsPersistWriter webHdfsPersistWriter = new WebHdfsPersistWriter(writerConfiguration);

    String uri = null;
    try {
      uri = webHdfsPersistWriter.getURI().toString();
    } catch (URISyntaxException e) {
      Assert.fail("URI Syntax");
    }
    Assert.assertArrayEquals(uri.toCharArray(), ("hdfs://localhost:9000").toCharArray());

  }

  @Test
  public void getWriterWebHdfsUriTest() {
    HdfsWriterConfiguration writerConfiguration = new HdfsWriterConfiguration();
    writerConfiguration.setScheme(HdfsConfiguration.Scheme.WEBHDFS);
    writerConfiguration.setHost("localhost");
    writerConfiguration.setPort(57000L);
    writerConfiguration.setPath("path");
    writerConfiguration.setWriterPath("writerPath");
    writerConfiguration.setUser("cloudera");

    WebHdfsPersistWriter webHdfsPersistWriter = new WebHdfsPersistWriter(writerConfiguration);

    String uri = null;
    try {
      uri = webHdfsPersistWriter.getURI().toString();
    } catch (URISyntaxException e) {
      Assert.fail("URI Syntax");
    }
    Assert.assertArrayEquals(uri.toCharArray(), ("webhdfs://localhost:57000").toCharArray());

  }

  @Test
  public void getReaderFileUriTest() {
    HdfsReaderConfiguration readerConfiguration = new HdfsReaderConfiguration();
    readerConfiguration.setScheme(HdfsConfiguration.Scheme.FILE);
    readerConfiguration.setPath("path");
    readerConfiguration.setReaderPath("readerPath");

    WebHdfsPersistReader webHdfsPersistReader = new WebHdfsPersistReader(readerConfiguration);

    String uri = null;
    try {
      uri = webHdfsPersistReader.getURI().toString();
    } catch (URISyntaxException e) {
      Assert.fail("URI Syntax");
    }
    Assert.assertArrayEquals(uri.toCharArray(), ("file:///").toCharArray());
  }

  @Test
  public void getReaderHdfsUriTest() {
    HdfsReaderConfiguration readerConfiguration = new HdfsReaderConfiguration();
    readerConfiguration.setScheme(HdfsConfiguration.Scheme.HDFS);
    readerConfiguration.setHost("localhost");
    readerConfiguration.setPort(9000L);
    readerConfiguration.setPath("path");
    readerConfiguration.setReaderPath("readerPath");

    WebHdfsPersistReader webHdfsPersistReader = new WebHdfsPersistReader(readerConfiguration);

    String uri = null;
    try {
      uri = webHdfsPersistReader.getURI().toString();
    } catch (URISyntaxException e) {
      Assert.fail("URI Syntax");
    }
    Assert.assertArrayEquals(uri.toCharArray(), ("hdfs://localhost:9000").toCharArray());

  }

  @Test
  public void getReaderWebHdfsUriTest() {
    HdfsReaderConfiguration readerConfiguration = new HdfsReaderConfiguration();
    readerConfiguration.setScheme(HdfsConfiguration.Scheme.WEBHDFS);
    readerConfiguration.setHost("localhost");
    readerConfiguration.setPort(57000L);
    readerConfiguration.setPath("path");
    readerConfiguration.setReaderPath("readerPath");

    WebHdfsPersistReader webHdfsPersistReader = new WebHdfsPersistReader(readerConfiguration);

    String uri = null;
    try {
      uri = webHdfsPersistReader.getURI().toString();
    } catch (URISyntaxException e) {
      Assert.fail("URI Syntax");
    }
    Assert.assertArrayEquals(uri.toCharArray(), ("webhdfs://localhost:57000").toCharArray());

  }

}
