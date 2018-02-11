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

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.console.ConsolePersistReader;
import org.apache.streams.console.ConsolePersistWriter;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.filebuffer.FileBufferConfiguration;
import org.apache.streams.filebuffer.FileBufferPersistReader;
import org.apache.streams.filebuffer.FileBufferPersistWriter;
import org.apache.streams.local.builders.LocalStreamBuilder;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Tests {@link org.apache.streams.filebuffer.FileBufferPersistWriter }
 * Tests {@link org.apache.streams.filebuffer.FileBufferPersistReader }
 */
public class FileBufferPersistIT {

  private FileBufferConfiguration testConfiguration;

  private ConsolePersistReader reader = Mockito.mock(ConsolePersistReader.class);
  private ConsolePersistWriter writer = Mockito.mock(ConsolePersistWriter.class);

  private StreamsDatum testDatum1 = new StreamsDatum("{\"datum\":1}");
  private StreamsDatum testDatum2 = new StreamsDatum("{\"datum\":2}");
  private StreamsDatum testDatum3 = new StreamsDatum("{\"datum\":3}");

  @BeforeClass
  public void prepareTestPersistStream() {

    Config testConfig = ConfigFactory.load("FileBufferPersistIT.conf");

    StreamsConfigurator.addConfig(testConfig);

    testConfiguration = new ComponentConfigurator<>(FileBufferConfiguration.class).detectConfiguration();

    File file = new File( testConfiguration.getPath());
    if( file.exists() )
      file.delete();

    PowerMockito.when(reader.readCurrent())
      .thenReturn(
        new StreamsResultSet(new ConcurrentLinkedQueue<>(
          Arrays.asList(testDatum1, testDatum2, testDatum3)))
      ).thenReturn(null);
  }

  @Test
  public void testPersistStream() {

    assert(testConfiguration != null);

    StreamBuilder builder = new LocalStreamBuilder();

    FileBufferPersistWriter fileWriter = new FileBufferPersistWriter(testConfiguration);
    FileBufferPersistReader fileReader = new FileBufferPersistReader(testConfiguration);

    builder.newReadCurrentStream("stdin", reader);
    builder.addStreamsPersistWriter("writer", fileWriter, 1, "stdin");
    builder.start();

    try {
      Thread.sleep(1000);
    } catch (InterruptedException ie) {
      //Handle exception
    }

    builder.stop();

    builder = new LocalStreamBuilder();

    builder.newReadCurrentStream("reader", fileReader);
    builder.addStreamsPersistWriter("stdout", writer, 1, "reader");
    builder.start();

    try {
      Thread.sleep(1000);
    } catch (InterruptedException ie) {
      //Handle exception
    }

    builder.stop();

    Mockito.verify(writer).write(testDatum1);
    Mockito.verify(writer).write(testDatum2);
    Mockito.verify(writer).write(testDatum3);

  }

}
