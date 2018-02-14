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

package org.apache.streams.filebuffer.test;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.filebuffer.FileBufferConfiguration;
import org.apache.streams.filebuffer.FileBufferPersistReader;
import org.apache.streams.filebuffer.FileBufferPersistWriter;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;

/**
 * Tests @link:{ org.apache.streams.filebuffer.FileBufferWriter }
 * Tests @link:{ org.apache.streams.filebuffer.FileBufferReader }
 */
public class TestFileBufferPersist {

  private FileBufferConfiguration testConfiguration;

  @Test
  public void testPersistWriterString() {

    testConfiguration = new FileBufferConfiguration();
    testConfiguration.setBuffer("target/TestFilePersist.txt");

    File file = new File( testConfiguration.getBuffer());
    if( file.exists() ) {
      file.delete();
    }

    FileBufferPersistWriter testPersistWriter = new FileBufferPersistWriter(testConfiguration);
    testPersistWriter.prepare(testConfiguration);

    String testJsonString = "{\"dummy\":\"true\"}";

    testPersistWriter.write(new StreamsDatum(testJsonString, "test"));

    testPersistWriter.cleanUp();

    FileBufferPersistReader testPersistReader = new FileBufferPersistReader(testConfiguration);
    try {
      testPersistReader.prepare(testConfiguration);
    } catch( Throwable e ) {
      e.printStackTrace();
      Assert.fail();
    }

    StreamsResultSet testResult = testPersistReader.readCurrent();

    testPersistReader.cleanUp();

    assertEquals(1, testResult.size());

  }

}
