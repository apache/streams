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
            "target/test-classes/streams-schema-activitystreams/activity.json",
            "target/test-classes/streams-schema-activitystreams/collection.json",
            "target/test-classes/streams-schema-activitystreams/media_link.json",
            "target/test-classes/streams-schema-activitystreams/object.json",
            "target/test-classes/streams-schema-activitystreams/objectTypes",
            "target/test-classes/streams-schema-activitystreams/verbs"
        );
        config.setSourcePaths(sourcePaths);

//        config.setSourceDirectory("target/test-classes/streams-schemas");
        config.setTargetPackage("org.apache.streams.pojo");
        config.setTargetDirectory("target/generated-sources/pojo");

        StreamsPojoSourceGenerator streamsPojoSourceGenerator = new StreamsPojoSourceGenerator(config);
        streamsPojoSourceGenerator.run();

        assert( config.getTargetDirectory() != null );
        assert( config.getTargetDirectory().exists() == true );
        assert( config.getTargetDirectory().isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(config.getTargetDirectory())
                .filter(javaFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() > 133 );

  }
}