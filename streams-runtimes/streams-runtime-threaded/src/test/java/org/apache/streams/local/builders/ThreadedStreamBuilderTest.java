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
import org.apache.streams.threaded.builders.ThreadedStreamBuilder;
import org.apache.streams.local.test.processors.PassThroughStaticCounterProcessor;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.providers.NumericMessageProviderDelayed;
import org.apache.streams.local.test.providers.ShapeShifterProvider;
import org.apache.streams.local.test.writer.DatumCounterWriter;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 * Basic Tests for the LocalStreamBuilder.
 *
 * Checks to see, under various permutations that if
 * 5 enter, 5 get processed, and 5 exit.
 */
public class ThreadedStreamBuilderTest {

    ByteArrayOutputStream out;

    @Before
    public void setSystemOut() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
    }

    @Test
    public void testStreamIdValidations() {

        StreamBuilder builder = new ThreadedStreamBuilder();
        builder.newReadCurrentStream("id", new NumericMessageProvider(1));

        try {
            builder.newReadCurrentStream("id", new NumericMessageProvider(1));
            fail("Should have had a runtime exception");
        } catch (RuntimeException e) {
            // noOperation
        }

        builder.addStreamsProcessor("1", new PassThroughStaticCounterProcessor(), 1, "id");
        try {
            builder.addStreamsProcessor("2", new PassThroughStaticCounterProcessor(), 1, "id", "id2");
            fail("Should have had a runtime exception");
        } catch (RuntimeException e) {
            // no Operation
        }
    }

    @Test
    public void testBasicLinearStream1()  {
        int numDatums = 1;


        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        assertEquals("Should have same number", numDatums, writer.getDatumsCounted());
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }

    @Test
    public void testBasicLinearStreamFinite()  {
        int numDatums = 1;
        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProviderDelayed(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        assertEquals("Should have same number", numDatums, writer.getDatumsCounted());
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }

    @Test
    public void testBasicLinearStreamFiniteMany()  {
        int numDatums = 100;
        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProviderDelayed(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        assertEquals("Should have same number", numDatums, writer.getDatumsCounted());
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }


    @Test
    public void testBasicLinearStream2()  {
        int numDatums = 100;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");

        builder.start();

        assertEquals("Datums in should match datums out", numDatums, writer.getDatumsCounted());
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }

    @Test
    public void testBasicMergeStream() {
        int numDatums1 = 1;
        int numDatums2 = 100;

        PassThroughStaticCounterProcessor processor1 = new PassThroughStaticCounterProcessor();
        PassThroughStaticCounterProcessor processor2 = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();

        String ident_writer = "writer";

        StreamBuilder builder = new ThreadedStreamBuilder();

        builder.newReadCurrentStream("provider_1", new NumericMessageProvider(numDatums1))
                .newReadCurrentStream("provider_2", new NumericMessageProvider(numDatums2))

                .addStreamsProcessor("processor_1", processor1, 1, "provider_1")
                .addStreamsProcessor("processor_2", processor2, 1, "provider_2")

                .addStreamsPersistWriter(ident_writer, writer, 1, "processor_1", "processor_2");

        builder.start();

        assertEquals("Processor 1 should have processed 1 items", numDatums1, processor1.getMessageCount());
        assertEquals("Processor 2 should have processed 100 items", numDatums2, processor2.getMessageCount());
        assertEquals("number in should equal number out", numDatums1 + numDatums2, writer.getDatumsCounted());

        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }


    @Test
    public void testFiveStreamsAtOnce() {

        final List<AtomicBoolean> runningList = new ArrayList<AtomicBoolean>();

        for(int i = 0; i < 5; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AtomicBoolean done = new AtomicBoolean(false);
                    runningList.add(done);
                    testBasicMergeStream();
                    done.set(true);
                }
            }).start();
        }


        while(runningList.size() < 5) {
            try {
                Thread.yield();
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        boolean shouldStop = false;
        while(!shouldStop) {

            try {
                Thread.yield();
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            shouldStop = true;
            for(AtomicBoolean b : runningList)
                shouldStop = b != null && b.get() && shouldStop;

        }
    }

    @Test
    public void testBasicBranch() {
        int numDatums = 100;
        StreamBuilder builder = new ThreadedStreamBuilder();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("prov1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", new PassThroughStaticCounterProcessor(), 1, "prov1")
                .addStreamsProcessor("proc2", new PassThroughStaticCounterProcessor(), 1, "prov1")
                .addStreamsPersistWriter("w1", writer, 1, "proc1", "proc2");

        builder.start();

        assertEquals("Number in should equal number out", numDatums * 2, writer.getDatumsCounted());
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }

    @Test
    public void testBasicBranchShapeShifter() {
        int numDatums = 100;
        StreamBuilder builder = new ThreadedStreamBuilder();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("prov1", new ShapeShifterProvider(numDatums, 3))
                .addStreamsProcessor("proc1", new PassThroughStaticCounterProcessor(), 1, "prov1")
                .addStreamsProcessor("proc2", new PassThroughStaticCounterProcessor(), 1, "prov1")
                .addStreamsPersistWriter("w1", writer, 1, "proc1", "proc2");

        builder.start();

        assertEquals("Number in should equal number out", numDatums * 2, writer.getDatumsCounted());
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }




}
