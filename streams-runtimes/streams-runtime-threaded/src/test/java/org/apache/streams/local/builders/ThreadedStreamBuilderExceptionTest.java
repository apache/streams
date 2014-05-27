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
import org.apache.streams.local.test.processors.PassThroughStaticCounterExceptionProcessor;
import org.apache.streams.local.test.processors.PassThroughStaticCounterProcessor;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.providers.NumericMessageThrowExceptionProvider;
import org.apache.streams.local.test.writer.DatumCounterExceptionWriter;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by rdouglas on 5/16/14.
 */
public class ThreadedStreamBuilderExceptionTest {

    @Test
    public void testProviderSingleException() {
        int numDatums = 1;
        int numErrors = 1;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageThrowExceptionProvider(numDatums, numErrors))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();

        assertEquals("Should have same number", 0, writer.getDatumsCounted());
    }

    @Test
    public void testProviderMultipleException() {
        int numDatums = 100;
        int numErrors = 3;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageThrowExceptionProvider(numDatums, numErrors))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();

        assertEquals("Should have same number", 0, writer.getDatumsCounted());
    }

    @Test
    public void testProcessorSingleException() {
        int numDatums = 1;
        int numErrors = 1;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterExceptionProcessor(0, numErrors);
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();

        assertEquals("Should have same number", 0, writer.getDatumsCounted());
    }

    /**
     * This test is cool, one of the providers throws an exception upfront
     * rendering it completely useless. Another provider in the stream, however,
     * is good.
     *
     * Testing for:
     * - The bad provider doesn't affect the stream.
     * - The good provider continues working
     * - Even through there is 1 processing error, stream continues
     * - counts should match up at the end
     */
    @Test
    public void testStreamsOneProviderFailsOtherWorks() {
        int numDatumsGood = 10;
        int numDatumsBad = 10;
        int numErrors = 1;

        StreamBuilder builder = new ThreadedStreamBuilder();
        NumericMessageProvider providerGood = new NumericMessageProvider(numDatumsGood);
        NumericMessageProvider providerBad = new NumericMessageThrowExceptionProvider(numDatumsBad, 10);
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterExceptionProcessor(0, numErrors);
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1_bad", providerBad)
                .newReadCurrentStream("sp1_good", providerGood)
                .addStreamsProcessor("proc1", processor, 1, "sp1_good", "sp1_bad")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();

        assertEquals("Should have same number", numDatumsGood - numErrors, writer.getDatumsCounted());
    }

    @Test
    public void testProcessorMultipleException() {
        int numDatums = 100;
        int numErrors = 3;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterExceptionProcessor(0, numErrors);
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();

        assertEquals("Should have same number", numDatums - numErrors, writer.getDatumsCounted());
    }

    @Test
    public void testProcessorChainMultipleException() {
        int numDatums = 100;
        int numErrors = 3;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor1 = new PassThroughStaticCounterExceptionProcessor(0, numErrors);
        PassThroughStaticCounterProcessor processor2 = new PassThroughStaticCounterExceptionProcessor(0, numErrors);

        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor1, 1, "sp1")
                .addStreamsProcessor("proc2", processor2, 1, "proc1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc2");
        builder.start();

        assertEquals("Should have same number", numDatums - (2 * numErrors), writer.getDatumsCounted());
    }

    @Test
    public void testProcessorMergeMultipleException() {
        int numDatums = 100;
        int numErrors = 3;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor1 = new PassThroughStaticCounterExceptionProcessor(0, numErrors);
        PassThroughStaticCounterProcessor processor2 = new PassThroughStaticCounterExceptionProcessor(0, numErrors);

        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor1, 1, "sp1")
                .addStreamsProcessor("proc2", processor2, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1", "proc2");
        builder.start();

        assertEquals("Should have same number", 2 * numDatums - (2 * numErrors), writer.getDatumsCounted());
    }

    @Test
    public void testSingleWriterException() {
        int numDatums = 100;
        int numErrors = 1;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor1 = new PassThroughStaticCounterProcessor(0);

        DatumCounterWriter writer = new DatumCounterExceptionWriter(numErrors);
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor1, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();

        assertEquals("Should have same number", numDatums - numErrors, writer.getDatumsCounted());
    }

    @Test
    public void testMultipleWriterException() {
        int numDatums = 100;
        int numErrors = 3;

        StreamBuilder builder = new ThreadedStreamBuilder();
        PassThroughStaticCounterProcessor processor1 = new PassThroughStaticCounterProcessor(0);

        DatumCounterWriter writer = new DatumCounterExceptionWriter(numErrors);
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor1, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();

        assertEquals("Should have same number", numDatums - numErrors, writer.getDatumsCounted());
    }
}
