package org.apache.streams.test.component.tests;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.test.component.FileReaderProvider;
import org.apache.streams.test.component.StringToDocumentConverter;
import org.junit.Ignore;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by rebanks on 2/28/14.
 */
public class TestFileReaderProvider {


    @Test
    public void testFileReaderProviderFileName() {
        String fileName = "/TestFile.txt";
        FileReaderProvider provider = new FileReaderProvider(fileName, new StringToDocumentConverter());
        provider.prepare(null);
        StreamsResultSet resultSet = provider.readCurrent();
        int count = 0;
        for(StreamsDatum datum : resultSet) {
            ++count;
        }
        assertEquals(4, count);
        provider.cleanUp();
    }






}
