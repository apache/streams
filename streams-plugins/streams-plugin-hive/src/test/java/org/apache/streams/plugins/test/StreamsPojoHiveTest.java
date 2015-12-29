package org.apache.streams.plugins.test;

import org.apache.streams.plugins.StreamsPojoHive;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;

/**
 * Test that Activity beans are compatible with the example activities in the spec.
 */
public class StreamsPojoHiveTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPojoHiveTest.class);

    /**
     * Tests that all example activities can be loaded into Activity beans
     *
     * @throws Exception
     */
    @Test
    public void testStreamsPojoHive() throws Exception {
        StreamsPojoHive streamsPojoHive = new StreamsPojoHive();
        streamsPojoHive.main(new String[0]);

        File testOutput = new File( "./target/generated-sources/hive/org/apache/streams/hive");
        FileFilter hqlFilter = new FileFilter() {
            @Override
            public boolean accept(File pathname) {
                if( pathname.getName().endsWith(".hql") )
                    return true;
                return false;
            }
        };

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );
        assert( testOutput.listFiles(hqlFilter).length == 11 );
//        assert( new File(testOutput + "/traits").exists() == true );
//        assert( new File(testOutput + "/traits").isDirectory() == true );
//        assert( new File(testOutput + "/traits").listFiles(scalaFilter) != null );
//        assert( new File(testOutput + "/traits").listFiles(scalaFilter).length == 4 );
//        assert( new File(testOutput + "/objectTypes").exists() == true );
//        assert( new File(testOutput + "/objectTypes").isDirectory() == true );
//        assert( new File(testOutput + "/objectTypes").listFiles(scalaFilter) != null );
//        assert( new File(testOutput + "/objectTypes").listFiles(scalaFilter).length == 43 );
//        assert( new File(testOutput + "/verbs").exists() == true );
//        assert( new File(testOutput + "/verbs").isDirectory() == true );
//        assert( new File(testOutput + "/verbs").listFiles(scalaFilter) != null );
//        assert( new File(testOutput + "/verbs").listFiles(scalaFilter).length == 89 );
    }
}