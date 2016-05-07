package org.apache.streams.plugins.cassandra.test;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.apache.commons.lang3.StringUtils;
import org.apache.streams.plugins.cassandra.StreamsCassandraResourceGenerator;
import org.junit.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import static org.apache.streams.plugins.cassandra.test.StreamsCassandraResourceGeneratorTest.cqlFilter;

/**
 * Created by sblackmon on 5/5/16.
 */
public class StreamsCassandraResourceGeneratorCLITest {

    @Test
    public void testStreamsHiveResourceGeneratorCLI() throws Exception {

        String sourceDirectory = "target/test-classes/streams-schemas";
        String targetDirectory = "target/generated-resources/cassandra-cli";

        List<String> argsList = Lists.newArrayList(sourceDirectory, targetDirectory);
        StreamsCassandraResourceGenerator.main(argsList.toArray(new String[0]));

        File testOutput = new File( targetDirectory );

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(cqlFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() == 1 );

        Path path = Paths.get("types.cql");

        String typesCqlBytes = new String(
                java.nio.file.Files.readAllBytes(path));

        assert( StringUtils.countMatches(typesCqlBytes, "CREATE TYPE") == 133 );

    }
}