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
package org.apache.streams.local.builders;

import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.builders.threaded.ThreadedStreamBuilder;
import org.apache.streams.local.test.processors.PassThroughStaticCounterProcessor;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.providers.NumericMessageProviderDelayed;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.assertEquals;

/**
 * Tests the parallel ability of streams
 */
public class ThreadedStreamBuilderParallelTest {

    @Test
    public void testParallelProcessors() {
        int numDatums = 20;
        int parallelHint = 20;
        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, parallelHint, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");

        builder.start();

        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
        assertEquals("Correct number of processors created", processor.getMessageCount(), numDatums);
        assertEquals("All should have seen the data", writer.getDatumsCounted(), numDatums);
    }

    @Test
    public void testParallelProcessorsManyDatums() {
        int numDatums = 2000;
        int parallelHint = 20;
        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, parallelHint, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");

        builder.start();

        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
        assertEquals("Correct number of processors created", processor.getMessageCount(), numDatums);
        assertEquals("All should have seen the data", writer.getDatumsCounted(), numDatums);
    }


    @Test
    public void testParallelWritersManyDatums() {
        int numDatums = 2000;
        int parallelHint = 20;
        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, parallelHint, "proc1");

        builder.start();

        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
        assertEquals("Correct number of processors created", processor.getMessageCount(), numDatums);
        assertEquals("All should have seen the data", writer.getDatumsCounted(), numDatums);
    }

    @Test
    public void testParallelProcessorsAndWritersManyDatums() {
        int numDatums = 2000;
        int parallelHint = 20;
        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, parallelHint, "sp1")
                .addStreamsPersistWriter("writer1", writer, parallelHint, "proc1");

        builder.start();

        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
        assertEquals("Correct number of processors created", processor.getMessageCount(), numDatums);
        assertEquals("All should have seen the data", writer.getDatumsCounted(), numDatums);
    }

    @Test
    public void testParallelProcessorsAndWritersSingle() {
        int numDatums = 1;
        int parallelHint = 20;
        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, parallelHint, "sp1")
                .addStreamsPersistWriter("writer1", writer, parallelHint, "proc1");

        builder.start();

        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
        assertEquals("Correct number of processors created", processor.getMessageCount(), numDatums);
        assertEquals("All should have seen the data", writer.getDatumsCounted(), numDatums);
    }

    @Test
    public void testParallelProcessorsAndWritersWithSillySizedQueues() {
        int numDatums = 40;
        int parallelHint = 20;
        StreamBuilder builder = new ThreadedStreamBuilder(new ArrayBlockingQueue<StreamsDatum>(1));
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, parallelHint, "sp1")
                .addStreamsPersistWriter("writer1", writer, parallelHint, "proc1");

        builder.start();

        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
        assertEquals("Correct number of processors created", processor.getMessageCount(), numDatums);
        assertEquals("All should have seen the data", writer.getDatumsCounted(), numDatums);
    }

    @Test
    public void testParallelProcessorsAndWritersSingleWithBigDelays() {
        int numDatums = 1;
        int parallelHint = 20;

        StreamBuilder builder = new ThreadedStreamBuilder();
        NumericMessageProviderDelayed provider = new NumericMessageProviderDelayed(numDatums, 1000);
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor(1000);
        DatumCounterWriter writer = new DatumCounterWriter(1000);
        builder.newReadCurrentStream("sp1", provider)
                .addStreamsProcessor("proc1", processor, parallelHint, "sp1")
                .addStreamsPersistWriter("writer1", writer, parallelHint, "proc1");

        builder.start();

        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
        assertEquals("Correct number of processors created", processor.getMessageCount(), numDatums);
        assertEquals("All should have seen the data", writer.getDatumsCounted(), numDatums);
    }

}
