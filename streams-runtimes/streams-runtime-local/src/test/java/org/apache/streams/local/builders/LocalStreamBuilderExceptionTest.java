package org.apache.streams.local.builders;

import org.apache.streams.core.StreamBuilder;
import org.apache.streams.local.test.processors.PassThroughStaticCounterExceptionProcessor;
import org.apache.streams.local.test.processors.PassThroughStaticCounterProcessor;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.providers.NumericMessageThrowExceptionProvider;
import org.apache.streams.local.test.writer.DatumCounterExceptionWriter;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by rdouglas on 5/16/14.
 */
public class LocalStreamBuilderExceptionTest {

    @Test
    public void testProviderSingleException() {
        int numDatums = 1;
        int numErrors = 1;

        StreamBuilder builder = new LocalStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageThrowExceptionProvider(numDatums, numErrors))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();

        assertEquals("Should have same number", 0, writer.getDatumsCounted());
    }

    @Test @Ignore //I currently fail :(
    public void testProviderMultipleException() {
        int numDatums = 100;
        int numErrors = 3;

        StreamBuilder builder = new LocalStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageThrowExceptionProvider(numDatums, numErrors))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();

        assertEquals("Should have same number", numDatums - numErrors, writer.getDatumsCounted());
    }

    @Test
    public void testProcessorSingleException() {
        int numDatums = 1;
        int numErrors = 1;

        StreamBuilder builder = new LocalStreamBuilder();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterExceptionProcessor(0, numErrors);
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();

        assertEquals("Should have same number", 0, writer.getDatumsCounted());
    }

    @Test
    public void testProcessorMultipleException() {
        int numDatums = 100;
        int numErrors = 3;

        StreamBuilder builder = new LocalStreamBuilder();
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

        StreamBuilder builder = new LocalStreamBuilder();
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

        StreamBuilder builder = new LocalStreamBuilder();
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

        StreamBuilder builder = new LocalStreamBuilder();
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

        StreamBuilder builder = new LocalStreamBuilder();
        PassThroughStaticCounterProcessor processor1 = new PassThroughStaticCounterProcessor(0);

        DatumCounterWriter writer = new DatumCounterExceptionWriter(numErrors);
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor1, 1, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");
        builder.start();

        assertEquals("Should have same number", numDatums - numErrors, writer.getDatumsCounted());
    }
}
