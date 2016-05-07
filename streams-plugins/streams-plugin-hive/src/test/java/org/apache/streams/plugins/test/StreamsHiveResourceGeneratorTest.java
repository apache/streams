package org.apache.streams.plugins.test;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.streams.plugins.hive.StreamsHiveGenerationConfig;
import org.apache.streams.plugins.hive.StreamsHiveResourceGenerator;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import static org.apache.streams.schema.FileUtil.dropSourcePathPrefix;

/**
 * Test that Activity beans are compatible with the example activities in the spec.
 */
@Ignore
public class StreamsHiveResourceGeneratorTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsHiveResourceGeneratorTest.class);

    public static final Predicate<File> hqlFilter = new Predicate<File>() {
        @Override
        public boolean apply(@Nullable File file) {
            if( file.getName().endsWith(".hql") )
                return true;
            else return false;
        }
    };

    /**
     * Tests that all example activities can be loaded into Activity beans
     *
     * @throws Exception
     */
    @Test
    public void StreamsHiveResourceGenerator() throws Exception {

        StreamsHiveGenerationConfig config = new StreamsHiveGenerationConfig();

        String sourceDirectory = "target/test-classes/streams-schemas";

        config.setSourceDirectory(sourceDirectory);

        config.setTargetDirectory("target/generated-sources/test");

        config.setExclusions(Sets.newHashSet("attachments"));

        config.setMaxDepth(2);

        StreamsHiveResourceGenerator streamsHiveResourceGenerator = new StreamsHiveResourceGenerator(config);
        Thread thread = new Thread(streamsHiveResourceGenerator);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException", e);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }

        File testOutput = config.getTargetDirectory();

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(hqlFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() == 133 );

        String expectedDirectory = "target/test-classes/expected";
        File testExpected = new File( expectedDirectory );

        Iterable<File> expectedIterator = Files.fileTreeTraverser().breadthFirstTraversal(testExpected)
                .filter(hqlFilter);
        Collection<File> expectedCollection = Lists.newArrayList(expectedIterator);

        int fails = 0;

        Iterator<File> iterator = expectedCollection.iterator();
        while( iterator.hasNext() ) {
            File objectExpected = iterator.next();
            String expectedEnd = dropSourcePathPrefix(objectExpected.getAbsolutePath(),  expectedDirectory);
            File objectActual = new File(config.getTargetDirectory() + "/" + expectedEnd);
            LOGGER.info("Comparing: {} and {}", objectExpected.getAbsolutePath(), objectActual.getAbsolutePath());
            assert( objectActual.exists());
            if( FileUtils.contentEquals(objectActual, objectExpected) == true ) {
                LOGGER.info("Exact Match!");
            } else {
                LOGGER.info("No Match!");
                fails++;
            }
        }
        if( fails > 0 ) {
            LOGGER.info("Fails: {}", fails);
            Assert.fail();
        }


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