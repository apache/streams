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
import org.apache.streams.local.builder.LocalStreamBuilder;
import org.apache.streams.local.test.processors.PassThroughStaticCounterProcessor;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.providers.NumericMessageProviderDelayed;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;

/**
 * Basic Tests for the LocalStreamBuilder.
 *
 * Checks to see, under various permutations that if
 * 5 enter, 5 get processed, and 5 exit.
 */
public class LocalStreamBuilderTest {

    ByteArrayOutputStream out;

    @Before
    public void setSystemOut() {
        out = new ByteArrayOutputStream();
        System.setOut(new PrintStream(out));
    }

    @Test
    public void testStreamIdValidations() {

        StreamBuilder builder = new LocalStreamBuilder();
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

    public void testBasicLinearStream1()  {
        int numDatums = 1;


        StreamBuilder builder = new LocalStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        assertEquals("Should have same number", numDatums, writer.getDatumsCounted());
    }

    @Test
    public void testBasicLinearStreamFinite()  {
        int numDatums = 1;
        StreamBuilder builder = new LocalStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProviderDelayed(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        assertEquals("Should have same number", numDatums, writer.getDatumsCounted());
    }

    @Test
    public void testBasicLinearStreamFiniteMany()  {
        int numDatums = 100;
        StreamBuilder builder = new LocalStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProviderDelayed(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        assertEquals("Should have same number", numDatums, writer.getDatumsCounted());
    }


    @Test
    public void testBasicLinearStream2()  {
        int numDatums = 100;

        StreamBuilder builder = new LocalStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");

        builder.start();

        assertEquals("Datums in should match datums out", numDatums, writer.getDatumsCounted());
    }

    @Test
    public void testBasicMergeStream() {
        int numDatums1 = 1;
        int numDatums2 = 100;

        PassThroughStaticCounterProcessor processor1 = new PassThroughStaticCounterProcessor();
        PassThroughStaticCounterProcessor processor2 = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();

        String ident_provider_one = "provider_1";
        String ident_provider_two = "provider_2";

        String ident_processor_one = "processor_1";
        String ident_processor_two = "processor_2";

        String ident_writer = "writer";

        StreamBuilder builder = new LocalStreamBuilder();
        builder.newReadCurrentStream(ident_provider_one, new NumericMessageProvider(numDatums1))
                .newReadCurrentStream(ident_provider_two, new NumericMessageProvider(numDatums2))
                .addStreamsProcessor(ident_processor_one, processor1, 1, ident_provider_one)
                .addStreamsProcessor(ident_processor_two, processor2, 1, ident_provider_two)
                .addStreamsPersistWriter(ident_writer, writer, 1, ident_processor_one, ident_processor_two);

        builder.start();

        assertEquals("Processor 1 should have processed 1 item", numDatums1, processor1.getMessageCount());
        assertEquals("Processor 2 should have processed 100 item", numDatums2, processor2.getMessageCount());
        assertEquals("number in should equal number out", numDatums1 + numDatums2, writer.getDatumsCounted());
    }

    @Test
    public void testBasicBranch() {
        int numDatums = 100;
        StreamBuilder builder = new LocalStreamBuilder();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("prov1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", new PassThroughStaticCounterProcessor(), 1, "prov1")
                .addStreamsProcessor("proc2", new PassThroughStaticCounterProcessor(), 1, "prov1")
                .addStreamsPersistWriter("w1", writer, 1, "proc1", "proc2");

        builder.start();

        assertEquals("Number in should equal number out", numDatums * 2, writer.getDatumsCounted());
    }


}
