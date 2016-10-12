package org.apache.streams.plugins.test;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.streams.plugins.hive.StreamsHiveResourceGenerator;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.List;

import static org.apache.streams.plugins.test.StreamsHiveResourceGeneratorTest.hqlFilter;

/**
 * Created by sblackmon on 5/5/16.
 */
public class StreamsHiveResourceGeneratorCLITest {

    @Test
    public void testStreamsHiveResourceGeneratorCLI() throws Exception {

        String sourceDirectory = "target/test-classes/streams-schema-activitystreams";
        String targetDirectory = "target/generated-resources/hive-cli";

        List<String> argsList = Lists.newArrayList(sourceDirectory, targetDirectory);
        StreamsHiveResourceGenerator.main(argsList.toArray(new String[0]));

        File testOutput = new File(targetDirectory);

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(hqlFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() == 133 );
    }
}