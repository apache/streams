package org.apache.streams.local.builders;

import org.apache.streams.builders.threaded.ThreadedStreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.local.test.providers.PreDefinedProvider;
import org.apache.streams.local.test.writer.DatumCollectorWriter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

import static org.junit.Assert.*;

public class ThreadedStreamBuilderDatumTest {

    @Test
    public void testDatums() {
        List<StreamsDatum> providerDatums = new ArrayList<StreamsDatum>();
        providerDatums.add(new StreamsDatum(1, "1"));
        providerDatums.add(new StreamsDatum(2, "2"));
        providerDatums.add(new StreamsDatum(3, "3"));
        PreDefinedProvider provider = new PreDefinedProvider(providerDatums);

        DatumCollectorWriter writer = new DatumCollectorWriter();

        new ThreadedStreamBuilder(new ArrayBlockingQueue<StreamsDatum>(10))
                .newReadCurrentStream("provider", provider)
                .addStreamsPersistWriter("writer", writer, 1, "provider")
                .start();

        assertEquals("Datum 1 document is correct", 1, writer.getDatums().get(0).getDocument());
        assertEquals("Datum 1 document is correct", 2, writer.getDatums().get(1).getDocument());
        assertEquals("Datum 1 document is correct", 3, writer.getDatums().get(2).getDocument());

        assertEquals("Datum 1 id is correct", "1", writer.getDatums().get(0).getId());
        assertEquals("Datum 1 id is correct", "2", writer.getDatums().get(1).getId());
        assertEquals("Datum 1 id is correct", "3", writer.getDatums().get(2).getId());

    }

    @Test
    public void testDatumsWithSerialization() {
        List<StreamsDatum> providerDatums = new ArrayList<StreamsDatum>();
        providerDatums.add(new StreamsDatum(new Object(), "1"));
        providerDatums.add(new StreamsDatum(new Object(), "2"));
        providerDatums.add(new StreamsDatum(new Object(), "3"));
        PreDefinedProvider provider = new PreDefinedProvider(providerDatums);

        DatumCollectorWriter writer = new DatumCollectorWriter();

        new ThreadedStreamBuilder(new ArrayBlockingQueue<StreamsDatum>(10))
                .newReadCurrentStream("provider", provider)
                .addStreamsPersistWriter("writer", writer, 1, "provider")
                .start();

        assertEquals("Datum 1 id is correct", "1", writer.getDatums().get(0).getId());
        assertEquals("Datum 1 id is correct", "2", writer.getDatums().get(1).getId());
        assertEquals("Datum 1 id is correct", "3", writer.getDatums().get(2).getId());

    }

}
