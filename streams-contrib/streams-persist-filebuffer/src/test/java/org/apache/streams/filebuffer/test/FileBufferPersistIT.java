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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.apache.streams.console.ConsolePersistReader;
import org.apache.streams.console.ConsolePersistWriter;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.filebuffer.FileBufferConfiguration;
import org.apache.streams.filebuffer.FileBufferPersistReader;
import org.apache.streams.filebuffer.FileBufferPersistWriter;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import static org.mockito.Mockito.mock;

import java.io.File;
import java.util.Map;

import static org.mockito.Mockito.mock;

/**
 * Tests {@link org.apache.streams.filebuffer.FileBufferPersistWriter }
 * Tests {@link org.apache.streams.filebuffer.FileBufferPersistReader }
 */
public class FileBufferPersistIT {

    private FileBufferConfiguration testConfiguration;

    ConsolePersistReader reader = Mockito.mock(ConsolePersistReader.class);
    ConsolePersistWriter writer = Mockito.mock(ConsolePersistWriter.class);

    StreamsDatum testDatum1 = new StreamsDatum("{\"datum\":1}");
    StreamsDatum testDatum2 = new StreamsDatum("{\"datum\":2}");
    StreamsDatum testDatum3 = new StreamsDatum("{\"datum\":3}");

    @Before
    public void prepareTest() {

        testConfiguration = new FileBufferConfiguration();
        testConfiguration.setPath("target/FilePersistIT.txt");

        File file = new File( testConfiguration.getPath());
        if( file.exists() )
            file.delete();

        PowerMockito.when(reader.readCurrent())
                .thenReturn(
                        new StreamsResultSet(Queues.newConcurrentLinkedQueue(
                                Lists.newArrayList(testDatum1, testDatum2, testDatum3)))
                ).thenReturn(null);
    }

    @Test
    public void testPersistStream() {

        assert(testConfiguration != null);

        Map<String, Object> streamConfig = Maps.newHashMap();
        streamConfig.put(LocalStreamBuilder.TIMEOUT_KEY, 1000);

        StreamBuilder builder = new LocalStreamBuilder(1, streamConfig);

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

        builder = new LocalStreamBuilder(1, streamConfig);
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

    @After
    public void shutdownTest() {

    }

}
