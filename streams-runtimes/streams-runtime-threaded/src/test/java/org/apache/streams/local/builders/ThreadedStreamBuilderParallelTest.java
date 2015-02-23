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

import org.apache.streams.threaded.builders.ThreadedStreamBuilder;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.local.test.processors.PassThroughStaticCounterProcessor;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.providers.NumericMessageProviderDelayed;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }


    @Test
    public void testParallelProcessorSingleThread() {
        int numDatums = 20;
        StreamBuilder builder = new ThreadedStreamBuilder(new ArrayBlockingQueue<StreamsDatum>(1));
        PassThroughStaticCounterProcessor proc1 = new PassThroughStaticCounterProcessor();
        PassThroughStaticCounterProcessor proc2 = new PassThroughStaticCounterProcessor();
        PassThroughStaticCounterProcessor proc3 = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", proc1, 1, "sp1")
                .addStreamsProcessor("proc2", proc2, 1, "sp1")
                .addStreamsProcessor("proc3", proc3, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc3");

        builder.start();

        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
        assertEquals("Correct number of processors created", proc1.getMessageCount(), numDatums);
        assertEquals("Correct number of processors created", proc2.getMessageCount(), numDatums);
        assertEquals("Correct number of processors created", proc3.getMessageCount(), numDatums);
        assertEquals("All should have seen the data", writer.getDatumsCounted(), numDatums);
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
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
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }


    @Test
    public void testParallelWritersManyDatums() {
        int numDatums = 2000;

        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        new ThreadedStreamBuilder().newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, "proc1")
                .start();

        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
        assertEquals("Correct number of processors created", processor.getMessageCount(), numDatums);
        assertEquals("All should have seen the data", writer.getDatumsCounted(), numDatums);
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }


    /**
     * Run 100 streams at the same time, each with 1,000 datums, twice.
     */
    @Test
    public void streamStressTest() {

        int numConcurrentStreams = 100;

        final List<AtomicBoolean> runningList = Collections.synchronizedList(new ArrayList<AtomicBoolean>());
        final List<AtomicBoolean> failureMarker = Collections.synchronizedList(new ArrayList<AtomicBoolean>());

        for(int i = 0; i < numConcurrentStreams; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AtomicBoolean done = new AtomicBoolean(false);
                    runningList.add(done);
                    try {
                        int numDatums = 1000;

                        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
                        DatumCounterWriter writer = new DatumCounterWriter();
                        new ThreadedStreamBuilder().newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                                .addStreamsProcessor("proc1", processor, 1, "sp1")
                                .addStreamsPersistWriter("writer1", writer, "proc1")
                                .start();

                        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
                        assertEquals("Correct number of processors created", processor.getMessageCount(), numDatums);
                        assertEquals("All should have seen the data", writer.getDatumsCounted(), numDatums);
                        assertTrue("cleanup called", writer.wasCleanupCalled());
                        assertTrue("cleanup called", writer.wasPrepeareCalled());

                        processor = new PassThroughStaticCounterProcessor();
                        writer = new DatumCounterWriter();
                        new ThreadedStreamBuilder().newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                                .addStreamsProcessor("proc1", processor, 1, "sp1")
                                .addStreamsPersistWriter("writer1", writer, "proc1")
                                .start();

                        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
                        assertEquals("Correct number of processors created", processor.getMessageCount(), numDatums);
                        assertEquals("All should have seen the data", writer.getDatumsCounted(), numDatums);
                        assertTrue("cleanup called", writer.wasCleanupCalled());
                        assertTrue("cleanup called", writer.wasPrepeareCalled());

                    } catch (Throwable e) {
                        failureMarker.add(new AtomicBoolean(true));
                    } finally {
                        done.set(true);
                    }
                }
            }).start();
        }

        while(runningList.size() < numConcurrentStreams) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        boolean shouldStop = false;
        while(!shouldStop || runningList.size() < numConcurrentStreams) {
            shouldStop = true;
            for(AtomicBoolean b : runningList) {
                shouldStop = b != null && b.get() && shouldStop;
            }
        }

        // check to see if anything bubbled up.
        for(AtomicBoolean failure : failureMarker) {
            if (failure.get()) {
                fail("this failed...");
            }
        }
    }

    @Test
    public void testParallelProcessorsAndWritersManyDatums() throws Throwable {
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
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
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
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
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
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }

    @Test
    public void testParallelProcessorsAndWritersSingleWithBigDelays() {
        int numDatums = 1;

        NumericMessageProviderDelayed provider = new NumericMessageProviderDelayed(numDatums, 200);
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor(200);
        DatumCounterWriter writer = new DatumCounterWriter(200);

        new ThreadedStreamBuilder().newReadCurrentStream("sp1", provider)
                .addStreamsProcessor("proc1", processor, "sp1")
                .addStreamsPersistWriter("writer1", writer, "proc1")
                .start();

        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
        assertEquals("Correct number of processors created", processor.getMessageCount(), numDatums);
        assertEquals("All should have seen the data", writer.getDatumsCounted(), numDatums);
        assertTrue("cleanup called", writer.wasCleanupCalled());
        assertTrue("cleanup called", writer.wasPrepeareCalled());
    }
}