package org.apache.streams.plugins.test;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.streams.plugins.StreamsPojoSourceGenerator;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static org.apache.streams.plugins.test.StreamsPojoSourceGeneratorTest.javaFilter;

/**
 * Created by sblackmon on 5/5/16.
 */
public class StreamsPojoSourceGeneratorCLITest {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPojoSourceGeneratorTest.class);

    @Test
    public void testStreamsPojoSourceGeneratorCLI() throws Exception {

        String sourceDirectory = "target/test-classes/streams-schema-activitystreams";
        String targetDirectory = "target/generated-sources/test-cli";

        List<String> argsList = Lists.newArrayList(sourceDirectory, targetDirectory);
        StreamsPojoSourceGenerator.main(argsList.toArray(new String[0]));

        File testOutput = new File(targetDirectory);

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(javaFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() > 133 );
    }
}
