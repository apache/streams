package org.apache.streams.local.builders;

import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.local.test.processors.PassThroughStaticCounterProcessor;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.providers.NumericMessageProviderDelayed;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.junit.Ignore;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.assertEquals;

/**
 * Tests the parallel ability of streams
 */
public class LocalStreamBuilderParallelTest {

    @Test
    public void testParallelProcessors() {
        int numDatums = 20;
        int parallelHint = 20;
        StreamBuilder builder = new LocalStreamBuilder();
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
        StreamBuilder builder = new LocalStreamBuilder();
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
        StreamBuilder builder = new LocalStreamBuilder();
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
        StreamBuilder builder = new LocalStreamBuilder();
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
        StreamBuilder builder = new LocalStreamBuilder();
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
        StreamBuilder builder = new LocalStreamBuilder(new ArrayBlockingQueue<StreamsDatum>(1));
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
        StreamBuilder builder = new LocalStreamBuilder();
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
