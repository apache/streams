package org.apache.streams.plugins.test;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.streams.plugins.hbase.StreamsHbaseResourceGenerator;
import org.junit.Test;

import javax.annotation.Nullable;
import java.io.File;
import java.util.Collection;
import java.util.List;

import static org.apache.streams.plugins.test.StreamsHbaseResourceGeneratorTest.txtFilter;

/**
 * Created by sblackmon on 5/5/16.
 */
public class StreamsHbaseResourceGeneratorCLITest {

    @Test
    public void testStreamsHiveResourceGeneratorCLI() throws Exception {

        String sourceDirectory = "target/test-classes/streams-schemas";
        String targetDirectory = "target/generated-resources/hbase-cli";

        List<String> argsList = Lists.newArrayList(sourceDirectory, targetDirectory);
        StreamsHbaseResourceGenerator.main(argsList.toArray(new String[0]));

        File testOutput = new File(targetDirectory);

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(txtFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() == 133 );
    }
}