package org.apache.streams.core.builders;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import org.apache.streams.core.test.processors.PassthroughDatumCounterProcessor;
import org.apache.streams.core.test.providers.NumericMessageProvider;
import org.apache.streams.core.test.writer.DatumCounterWriter;
import org.apache.streams.core.test.writer.SystemOutWriter;
import org.junit.Before;
import org.junit.Test;

import java.io.*;
import java.util.HashSet;
import java.util.Scanner;

/**
 * Basic Tests for the LocalStreamBuilder.
 *
 * Test are performed by redirecting system out and counting the number of lines that the SystemOutWriter prints
 * to System.out.  The SystemOutWriter also prints one line when cleanUp() is called, so this is why it tests for
 * the numDatums +1.
 *
 *
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
        Exception exp = null;
        try {
            builder.newReadCurrentStream("id", new NumericMessageProvider(1));
        } catch (RuntimeException e) {
            exp = e;
        }
        assertNotNull(exp);
        exp = null;
        builder.addStreamsProcessor("1", new PassthroughDatumCounterProcessor(), 1, "id");
        try {
            builder.addStreamsProcessor("2", new PassthroughDatumCounterProcessor(), 1, "id", "id2");
        } catch (RuntimeException e) {
            exp = e;
        }
        assertNotNull(exp);
    }

    @Test
    public void testBasicLinearStream1()  {
        int numDatums = 1;
        StreamBuilder builder = new LocalStreamBuilder();
        PassthroughDatumCounterProcessor processor = new PassthroughDatumCounterProcessor();
        SystemOutWriter writer = new SystemOutWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        int count = 0;
        Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray()));
        while(scanner.hasNextLine()) {
            ++count;
            scanner.nextLine();
        }
        assertEquals(numDatums+1, count);
    }

    @Test
    public void testBasicLinearStream2()  {
        int numDatums = 1000;
        StreamBuilder builder = new LocalStreamBuilder();
        PassthroughDatumCounterProcessor processor = new PassthroughDatumCounterProcessor();
        SystemOutWriter writer = new SystemOutWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        int count = 0;
        Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray()));
        while(scanner.hasNextLine()) {
            ++count;
            scanner.nextLine();
        }
        assertEquals(numDatums+1, count);
    }

    @Test
    public void testParallelLinearStream1() {
        int numDatums = 10000;
        int parallelHint = 40;
        PassthroughDatumCounterProcessor.sawData = new HashSet<Integer>();
        PassthroughDatumCounterProcessor.claimedNumber = new HashSet<Integer>();
        StreamBuilder builder = new LocalStreamBuilder();
        PassthroughDatumCounterProcessor processor = new PassthroughDatumCounterProcessor();
        SystemOutWriter writer = new SystemOutWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, parallelHint, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();
        int count = 0;
        Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray()));
        while(scanner.hasNextLine()) {
            ++count;
            scanner.nextLine();
        }
        assertEquals(numDatums+1, count); //+1 is to make sure cleanup is called on the writer
        assertEquals(parallelHint, PassthroughDatumCounterProcessor.claimedNumber.size()); //test 40 were initialized
        assertTrue(PassthroughDatumCounterProcessor.sawData.size() > 1 && PassthroughDatumCounterProcessor.sawData.size() <= parallelHint); //test more than one processor got data
    }

    @Test
    public void testBasicMergeStream() {
        int numDatums1 = 1;
        int numDatums2 = 1000;
        PassthroughDatumCounterProcessor processor1 = new PassthroughDatumCounterProcessor();
        PassthroughDatumCounterProcessor processor2 = new PassthroughDatumCounterProcessor();
        SystemOutWriter writer = new SystemOutWriter();
        StreamBuilder builder = new LocalStreamBuilder();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums1))
                .newReadCurrentStream("sp2", new NumericMessageProvider(numDatums2))
                .addStreamsProcessor("proc1", processor1, 1, "sp1")
                .addStreamsProcessor("proc2", processor2, 1, "sp2")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1", "proc2");
        builder.start();
        int count = 0;
        Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray()));
        while(scanner.hasNextLine()) {
            ++count;
            scanner.nextLine();
        }
        assertEquals(numDatums1+numDatums2+1, count);
    }

    @Test
    public void testBasicBranch() {
        int numDatums = 1000;
        StreamBuilder builder = new LocalStreamBuilder();
        builder.newReadCurrentStream("prov1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", new PassthroughDatumCounterProcessor(), 1, "prov1")
                .addStreamsProcessor("proc2", new PassthroughDatumCounterProcessor(), 1, "prov1")
                .addStreamsPersistWriter("w1", new SystemOutWriter(), 1, "proc1", "proc2");
        builder.start();
        int count = 0;
        Scanner scanner = new Scanner(new ByteArrayInputStream(out.toByteArray()));
        while(scanner.hasNextLine()) {
            ++count;
            scanner.nextLine();
        }
        assertEquals((numDatums*2)+1, count);
    }


}
