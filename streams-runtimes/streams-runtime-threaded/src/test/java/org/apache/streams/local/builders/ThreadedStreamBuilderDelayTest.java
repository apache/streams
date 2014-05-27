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
import org.apache.streams.builders.threaded.ThreadedStreamBuilder;
import org.apache.streams.local.test.processors.PassThroughStaticCounterProcessor;
import org.apache.streams.local.test.processors.SimpleProcessorCounter;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.providers.NumericMessageProviderDelayed;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * These tests ensure that StreamsBuilder works
 */
public class ThreadedStreamBuilderDelayTest {

    @Test
    public void delayedWriterTest() {

        int numDatums = 5;
        StreamBuilder builder = new ThreadedStreamBuilder();
        DatumCounterWriter writer = new DatumCounterWriter(500); // give the DatumCounter a 500ms delay
        SimpleProcessorCounter proc1 = new SimpleProcessorCounter();
        SimpleProcessorCounter proc2 = new SimpleProcessorCounter();

        builder.newReadCurrentStream("prov1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", proc1, 1, "prov1")
                .addStreamsProcessor("proc2", proc2, 1, "prov1")
                .addStreamsPersistWriter("w1", writer, 1, "proc1", "proc2");

        builder.start();

        assertEquals("Number in should equal number out", numDatums, proc1.getMessageCount());
        assertEquals("Number in should equal number out", numDatums, proc2.getMessageCount());
        assertEquals("Number in should equal number out", numDatums * 2, writer.getDatumsCounted());
    }

    @Test
    public void delayedProcessorTest() {
        int numDatums = 5;
        StreamBuilder builder = new ThreadedStreamBuilder();
        DatumCounterWriter writer = new DatumCounterWriter();
        SimpleProcessorCounter proc1 = new SimpleProcessorCounter(500);
        SimpleProcessorCounter proc2 = new SimpleProcessorCounter(250);

        builder.newReadCurrentStream("prov1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", proc1, 1, "prov1")
                .addStreamsProcessor("proc2", proc2, 1, "prov1")
                .addStreamsPersistWriter("w1", writer, 1, "proc1", "proc2");

        builder.start();

        assertEquals("Number in should equal number out", numDatums, proc1.getMessageCount());
        assertEquals("Number in should equal number out", numDatums, proc2.getMessageCount());
        assertEquals("Number in should equal number out", numDatums * 2, writer.getDatumsCounted());
    }


    @Test
    public void delayedProviderTest()  {
        int numDatums = 10;
        StreamBuilder builder = new ThreadedStreamBuilder();

        NumericMessageProviderDelayed provider = new NumericMessageProviderDelayed(numDatums, 100);
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor(250);
        DatumCounterWriter writer = new DatumCounterWriter(125);
        builder.newReadCurrentStream("sp1", provider)
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        assertEquals("Should have same number", numDatums, writer.getDatumsCounted());
    }

    @Test
    public void noDataTest()  {
        int numDatums = 8;
        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor(100);
        DatumCounterWriter writer = new DatumCounterWriter(200);
        builder.newReadCurrentStream("sp1", new NumericMessageProviderDelayed(numDatums, 100))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        assertEquals("Should have same number", numDatums, writer.getDatumsCounted());
    }

    @Test
    public void dualDelayedProcessorsLinearTest() {
        int numDatums = 12;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor proc1 = new PassThroughStaticCounterProcessor(100);
        PassThroughStaticCounterProcessor proc2 = new PassThroughStaticCounterProcessor(200);
        DatumCounterWriter writer = new DatumCounterWriter();

        builder.newReadCurrentStream("sp1", new NumericMessageProviderDelayed(numDatums))
                .addStreamsProcessor("proc1", proc1, 1, "sp1")
                .addStreamsProcessor("proc2", proc2, 1, "proc1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc2");

        builder.start();

        assertEquals("Should have same number", numDatums, writer.getDatumsCounted());
        assertEquals("Should have same number", numDatums, proc1.getMessageCount());
        assertEquals("Should have same number", numDatums, proc2.getMessageCount());
    }

    @Test
    public void dualDelayedProcessorsBranchTest() {
        int numDatums = 10;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor proc1 = new PassThroughStaticCounterProcessor(100);
        PassThroughStaticCounterProcessor proc2 = new PassThroughStaticCounterProcessor(200);
        DatumCounterWriter writer = new DatumCounterWriter();

        builder.newReadCurrentStream("sp1", new NumericMessageProviderDelayed(numDatums))
                .addStreamsProcessor("proc1", proc1, 1, "sp1")
                .addStreamsProcessor("proc2", proc2, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1", "proc2");

        builder.start();

        assertEquals("Should have same number", 2 * numDatums, writer.getDatumsCounted());
        assertEquals("Should have same number", numDatums, proc1.getMessageCount());
        assertEquals("Should have same number", numDatums, proc2.getMessageCount());
    }

    @Test
    public void dualDelayedProviderTest() {
        int numDatums = 4;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor proc1 = new PassThroughStaticCounterProcessor(100);
        DatumCounterWriter writer = new DatumCounterWriter();

        builder.newReadCurrentStream("sp1", new NumericMessageProviderDelayed(numDatums, 300))
                .newReadCurrentStream("sp2", new NumericMessageProviderDelayed(numDatums, 200))
                .addStreamsProcessor("proc1", proc1, 1, "sp1", "sp2")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");

        builder.start();

        assertEquals("Should have same number", 2 * numDatums, writer.getDatumsCounted());
        assertEquals("Should have same number", 2 * numDatums, proc1.getMessageCount());
    }

    @Test
    public void dualDelayedMergedTest() {
        int numDatums = 6;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor proc1 = new PassThroughStaticCounterProcessor(100);
        PassThroughStaticCounterProcessor proc2 = new PassThroughStaticCounterProcessor(150);
        DatumCounterWriter writer = new DatumCounterWriter(150);

        builder.newReadCurrentStream("sp1", new NumericMessageProviderDelayed(numDatums, 300))
                .newReadCurrentStream("sp2", new NumericMessageProviderDelayed(numDatums, 350))
                .addStreamsProcessor("proc1", proc1, 1, "sp1")
                .addStreamsProcessor("proc2", proc2, 1, "sp2")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1", "proc2");

        builder.start();

        assertEquals("Should have same number", 2 * numDatums, writer.getDatumsCounted());
        assertEquals("Should have same number", numDatums, proc1.getMessageCount());
        assertEquals("Should have same number", numDatums, proc2.getMessageCount());
    }
}
