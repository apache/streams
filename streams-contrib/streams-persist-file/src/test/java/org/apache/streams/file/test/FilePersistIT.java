package org.apache.streams.file.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;
import org.apache.streams.console.ConsolePersistReader;
import org.apache.streams.console.ConsolePersistWriter;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.file.FileConfiguration;
import org.apache.streams.file.FilePersistReader;
import org.apache.streams.file.FilePersistWriter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.internal.util.reflection.Whitebox;
import org.powermock.api.mockito.PowerMockito;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;

/**
 * Created by sblackmon on 10/20/14.
 */
public class FilePersistIT {

    private FileConfiguration testConfiguration;

    ConsolePersistReader reader = Mockito.mock(ConsolePersistReader.class);
    ConsolePersistWriter writer = Mockito.mock(ConsolePersistWriter.class);

    StreamsDatum testDatum1 = new StreamsDatum("{\"datum\":1}");
    StreamsDatum testDatum2 = new StreamsDatum("{\"datum\":2}");
    StreamsDatum testDatum3 = new StreamsDatum("{\"datum\":3}");

    @Before
    public void prepareTest() {

        testConfiguration = new FileConfiguration();
        //testConfiguration.setFile("./test-queue.txt");

        File file = new File( testConfiguration.getFile());
        if( file.exists() )
            file.delete();

        PowerMockito.when(reader.readCurrent())
                .thenReturn(
                        new StreamsResultSet(Queues.newConcurrentLinkedQueue(
                                Lists.newArrayList(testDatum1, testDatum2, testDatum3)))
                ).thenReturn(null);
    }

    @Test
    public void testPersistStream() {

        assert(testConfiguration != null);

        Map<String, Object> streamConfig = Maps.newHashMap();
        streamConfig.put(LocalStreamBuilder.TIMEOUT_KEY, 1000);

        StreamBuilder builder = new LocalStreamBuilder(1, streamConfig);

        FilePersistWriter fileWriter = new FilePersistWriter(testConfiguration);
        FilePersistReader fileReader = new FilePersistReader(testConfiguration);

        builder.newReadCurrentStream("stdin", reader);
        builder.addStreamsPersistWriter("writer", fileWriter, 1, "stdin");
        builder.newReadCurrentStream("reader", fileReader);
        builder.addStreamsPersistWriter("stdout", writer, 1, "reader");

        builder.start();

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            //Handle exception
        }

        builder.stop();

        Mockito.verify(writer).write(testDatum1);
        Mockito.verify(writer).write(testDatum2);
        Mockito.verify(writer).write(testDatum3);

    }

    @After
    public void shutdownTest() {

    }

}
