package org.apache.streams.plugins.pig.test;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.streams.plugins.pig.StreamsPigResourceGenerator;
import org.junit.Test;

import java.io.File;
import java.util.Collection;
import java.util.List;

import static org.apache.streams.plugins.pig.test.StreamsPigResourceGeneratorTest.pigFilter;

/**
 * Created by sblackmon on 5/5/16.
 */
public class StreamsPigResourceGeneratorCLITest {

    @Test
    public void testStreamsPigResourceGeneratorCLI() throws Exception {

        String sourceDirectory = "target/test-classes/streams-schemas";
        String targetDirectory = "target/generated-resources/hive-cli";

        List<String> argsList = Lists.newArrayList(sourceDirectory, targetDirectory);
        StreamsPigResourceGenerator.main(argsList.toArray(new String[0]));

        File testOutput = new File(targetDirectory);

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(pigFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() == 133 );
    }
}