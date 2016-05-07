package org.apache.streams.plugins.elasticsearch.test;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.streams.plugins.elasticsearch.StreamsElasticsearchGenerationConfig;
import org.apache.streams.plugins.elasticsearch.StreamsElasticsearchResourceGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Iterator;

import static org.apache.streams.schema.FileUtil.dropSourcePathPrefix;

/**
 * Test that Elasticsearch resources are generated.
 */
public class StreamsElasticsearchResourceGeneratorTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsElasticsearchResourceGeneratorTest.class);

    /**
     * Test that Elasticsearch resources are generated
     *
     * @throws Exception
     */
    @Test
    public void StreamsElasticsearchResourceGenerator() throws Exception {

        StreamsElasticsearchGenerationConfig config = new StreamsElasticsearchGenerationConfig();

        String sourceDirectory = "target/test-classes/streams-schemas";

        config.setSourceDirectory(sourceDirectory);

        config.setTargetDirectory("target/generated-sources/test");

        config.setExclusions(Sets.newHashSet("attachments"));

        config.setMaxDepth(2);

        StreamsElasticsearchResourceGenerator streamsElasticsearchResourceGenerator = new StreamsElasticsearchResourceGenerator(config);
        Thread thread = new Thread(streamsElasticsearchResourceGenerator);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException", e);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }

        File testOutput = new File( "./target/generated-sources/test");
        Predicate<File> jsonFilter = new Predicate<File>() {
            @Override
            public boolean apply(@Nullable File file) {
                if( file.getName().endsWith(".json") )
                    return true;
                else return false;
            }
        };

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(jsonFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() == 133 );

        String expectedDirectory = "target/test-classes/expected";
        File testExpected = new File( expectedDirectory );

        Iterable<File> expectedIterator = Files.fileTreeTraverser().breadthFirstTraversal(testExpected)
                .filter(jsonFilter);
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

//        String expectedDirectory = "target/test-classes/expected";
//        File testExpected = new File( expectedDirectory );
//
//        Iterable<File> expectedIterator = Files.fileTreeTraverser().breadthFirstTraversal(testExpected)
//                .filter(cqlFilter);
//        Collection<File> expectedCollection = Lists.newArrayList(expectedIterator);
//
//        int fails = 0;
//
//        Iterator<File> iterator = expectedCollection.iterator();
//        while( iterator.hasNext() ) {
//            File objectExpected = iterator.next();
//            String expectedEnd = dropSourcePathPrefix(objectExpected.getAbsolutePath(),  expectedDirectory);
//            File objectActual = new File(config.getTargetDirectory() + "/" + expectedEnd);
//            LOGGER.info("Comparing: {} and {}", objectExpected.getAbsolutePath(), objectActual.getAbsolutePath());
//            assert( objectActual.exists());
//            if( FileUtils.contentEquals(objectActual, objectExpected) == true ) {
//                LOGGER.info("Exact Match!");
//            } else {
//                LOGGER.info("No Match!");
//                fails++;
//            }
//        }
//        if( fails > 0 ) {
//            LOGGER.info("Fails: {}", fails);
//            Assert.fail();
//        }

    }
}