package org.apache.streams.plugins.cassandra.test;

import com.google.common.base.Charsets;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.streams.plugins.cassandra.StreamsCassandraGenerationConfig;
import org.apache.streams.plugins.cassandra.StreamsCassandraResourceGenerator;
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
import java.util.List;

import static org.apache.streams.schema.FileUtil.dropSourcePathPrefix;

/**
 * Test that cassandra resources are generated.
 */
public class StreamsCassandraResourceGeneratorTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsCassandraResourceGeneratorTest.class);

    public static final Predicate<File> cqlFilter = new Predicate<File>() {
        @Override
        public boolean apply(@Nullable File file) {
            if( file.getName().endsWith(".cql") )
                return true;
            else return false;
        }
    };

    /**
     * Test that cassandra resources are generated
     *
     * @throws Exception
     */
    @Test
    public void StreamsCassandraResourceGenerator() throws Exception {

        StreamsCassandraGenerationConfig config = new StreamsCassandraGenerationConfig();

        String sourceDirectory = "target/test-classes/streams-schemas";

        config.setSourceDirectory(sourceDirectory);

        config.setTargetDirectory("target/generated-sources/test");

        config.setExclusions(Sets.newHashSet("attachments"));

        config.setMaxDepth(2);

        StreamsCassandraResourceGenerator streamsCassandraResourceGenerator = new StreamsCassandraResourceGenerator(config);
        Thread thread = new Thread(streamsCassandraResourceGenerator);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException", e);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }

        File testOutput = new File( "./target/generated-sources/test");

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(cqlFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() == 1 );

        Path path = Paths.get("./target/generated-sources/test/types.cql");

        String typesCqlBytes = new String(
                java.nio.file.Files.readAllBytes(path));

        assert( StringUtils.countMatches(typesCqlBytes, "CREATE TYPE") == 133 );

        assert( !typesCqlBytes.contains("IDK"));


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