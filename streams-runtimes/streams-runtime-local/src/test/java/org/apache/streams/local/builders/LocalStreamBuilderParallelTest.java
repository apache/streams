package org.apache.streams.local.builders;

import org.apache.streams.core.StreamBuilder;
import org.apache.streams.local.test.processors.PassThroughStaticCounterProcessor;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests the parallel ability of streams
 */
public class LocalStreamBuilderParallelTest {

    @Test
    public void testParallelProcessors() {
        int numDatums = 1000;
        int parallelHint = 20;
        StreamBuilder builder = new LocalStreamBuilder();
        PassThroughStaticCounterProcessor.clear();
        PassThroughStaticCounterProcessor processor = new PassThroughStaticCounterProcessor();
        DatumCounterWriter writer = new DatumCounterWriter();
        builder.newReadCurrentStream("sp1", new NumericMessageProvider(numDatums))
                .addStreamsProcessor("proc1", processor, parallelHint, "sp1")
                .addStreamsPersistWriter("writer1", writer, 1, "proc1");

        builder.start();

        assertEquals("number of items in should equal number of items out", writer.getDatumsCounted(), numDatums);
        assertEquals("Correct number of processors created", PassThroughStaticCounterProcessor.CLAIMED_NUMBERS.size(), parallelHint);
        assertEquals("All should have seen the data", PassThroughStaticCounterProcessor.SEEN_DATA.size(), parallelHint);
    }

}
