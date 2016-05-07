package org.apache.streams.plugins.test;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.streams.plugins.StreamsPojoGenerationConfig;
import org.apache.streams.plugins.StreamsPojoSourceGenerator;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileFilter;
import java.util.Collection;
import java.util.List;

/**
 * Test that Activity beans are compatible with the example activities in the spec.
 */
public class StreamsPojoSourceGeneratorTest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPojoSourceGeneratorTest.class);

    public static final Predicate<File> javaFilter = new Predicate<File>() {
        @Override
        public boolean apply(@Nullable File file) {
            if( file.getName().endsWith(".java") )
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
    public void testStreamsPojoSourceGenerator() throws Exception {

        StreamsPojoGenerationConfig config = new StreamsPojoGenerationConfig();

        List<String> sourcePaths = Lists.newArrayList(
            "target/test-classes/streams-schemas/activity.json",
            "target/test-classes/streams-schemas/collection.json",
            "target/test-classes/streams-schemas/media_link.json",
            "target/test-classes/streams-schemas/object.json",
            "target/test-classes/streams-schemas/objectTypes",
            "target/test-classes/streams-schemas/verbs"
        );
        config.setSourcePaths(sourcePaths);

//        config.setSourceDirectory("target/test-classes/streams-schemas");
        config.setTargetPackage("org.apache.streams.pojo.test");
        config.setTargetDirectory("target/generated-sources/test");

        StreamsPojoSourceGenerator streamsPojoSourceGenerator = new StreamsPojoSourceGenerator(config);
        Thread thread = new Thread(streamsPojoSourceGenerator);
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            LOGGER.error("InterruptedException", e);
        } catch (Exception e) {
            LOGGER.error("Exception", e);
        }

        File testOutput = new File( "target/generated-sources/test");

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(javaFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() > 133 );

//        assert( testOutput.listFiles(javaFilter).length == 11 );
//        assert( new File(testOutput + "/traits").exists() == true );
//        assert( new File(testOutput + "/traits").isDirectory() == true );
//        assert( new File(testOutput + "/traits").listFiles(scalaFilter) != null );
//        assert( new File(testOutput + "/traits").listFiles(scalaFilter).length == 4 );
//        assert( new File(testOutput + "/objectTypes").exists() == true );
//        assert( new File(testOutput + "/objectTypes").isDirectory() == true );
//        assert( new File(testOutput + "/objectTypes").listFiles(scalaFilter) != null );
//        assert( new File(testOutput + "/objectTypes").listFiles(scalaFilter).length == 43 );
//        assert( new File(testO`utput + "/verbs").exists() == true );
//        assert( new File(testOutput + "/verbs").isDirectory() == true );
//        assert( new File(testOutput + "/verbs").listFiles(scalaFilter) != null );
//        assert( new File(testOutput + "/verbs").listFiles(scalaFilter).length == 89 );
    }
}