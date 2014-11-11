package org.apache.streams.file.test;

import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsResultSet;
import org.apache.streams.file.FileConfiguration;
import org.apache.streams.file.FilePersistReader;
import org.apache.streams.file.FilePersistWriter;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * Created by sblackmon on 10/20/14.
 */
public class TestFilePersist {

    private FileConfiguration testConfiguration;

    @Test
    public void testPersistWriterString() {

        testConfiguration = new FileConfiguration();
        //testConfiguration.setFile("./test-queue.txt");

        File file = new File( testConfiguration.getFile());
        if( file.exists() )
            file.delete();

        FilePersistWriter testPersistWriter = new FilePersistWriter(testConfiguration);
        testPersistWriter.prepare(testConfiguration);

        String testJsonString = "{\"dummy\":\"true\"}";

        testPersistWriter.write(new StreamsDatum(testJsonString, "test"));

        testPersistWriter.cleanUp();

        FilePersistReader testPersistReader = new FilePersistReader(testConfiguration);
        try {
            testPersistReader.prepare(testConfiguration);
        } catch( Throwable e ) {
            e.printStackTrace();
            Assert.fail();
        }

        StreamsResultSet testResult = testPersistReader.readCurrent();

        testPersistReader.cleanUp();

        assert(testResult.size() == 1);

    }

}
