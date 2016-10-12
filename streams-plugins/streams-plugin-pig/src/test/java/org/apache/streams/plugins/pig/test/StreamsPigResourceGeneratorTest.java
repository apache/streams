package org.apache.streams.plugins.pig.test;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.streams.plugins.pig.StreamsPigGenerationConfig;
import org.apache.streams.plugins.pig.StreamsPigResourceGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.Iterator;

import static org.apache.streams.util.schema.FileUtil.dropSourcePathPrefix;

/**
 * Test that Activity beans are compatible with the example activities in the spec.
 */
public class StreamsPigResourceGeneratorTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPigResourceGeneratorTest.class);

    public static final Predicate<File> pigFilter = new Predicate<File>() {
        @Override
        public boolean apply(@Nullable File file) {
            if( file.getName().endsWith(".pig") )
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
    public void StreamsPigResourceGenerator() throws Exception {

        StreamsPigGenerationConfig config = new StreamsPigGenerationConfig();

        String sourceDirectory = "target/test-classes/streams-schema-activitystreams";

        config.setSourceDirectory(sourceDirectory);

        config.setTargetDirectory("target/generated-resources/pig");

        config.setExclusions(Sets.newHashSet("attachments"));

        config.setMaxDepth(2);

        StreamsPigResourceGenerator streamsPigResourceGenerator = new StreamsPigResourceGenerator(config);
        streamsPigResourceGenerator.run();

        File testOutput = config.getTargetDirectory();

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(pigFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() == 133 );

        String expectedDirectory = "target/test-classes/expected";
        File testExpected = new File( expectedDirectory );

        Iterable<File> expectedIterator = Files.fileTreeTraverser().breadthFirstTraversal(testExpected)
                .filter(pigFilter);
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
    }
}