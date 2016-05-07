package org.apache.streams.plugins.test;

import com.google.common.collect.Lists;
import com.google.common.io.Files;
import junit.framework.TestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.apache.streams.plugins.test.StreamsHiveResourceGeneratorTest.hqlFilter;

/**
 * Tests that streams-plugin-hive running via maven generates hql resources
 */
public class StreamsHiveResourceGeneratorMojoTest extends TestCase {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsHiveResourceGeneratorMojoTest.class);

    protected void setUp() throws Exception
    {
        // required for mojo lookups to work
        super.setUp();
    }


    @Test
    public void testStreamsHiveResourceGeneratorMojo() throws Exception {

        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/streams-plugin-hive" );

        Verifier verifier;

        verifier = new Verifier( testDir.getAbsolutePath() );

        List cliOptions = new ArrayList();
        cliOptions.add( "-N" );
        verifier.executeGoals( Lists.<String>newArrayList(
                "clean",
                "dependency:unpack-dependencies",
                "generate-resources"));

        verifier.verifyErrorFreeLog();

        verifier.resetStreams();

        File testOutput = new File(testDir.getAbsolutePath() + "/target/generated-resources/test-mojo");

        assert( testOutput != null );
        assert( testOutput.exists() == true );
        assert( testOutput.isDirectory() == true );

        Iterable<File> outputIterator = Files.fileTreeTraverser().breadthFirstTraversal(testOutput)
                .filter(hqlFilter);
        Collection<File> outputCollection = Lists.newArrayList(outputIterator);
        assert( outputCollection.size() == 133 );
    }
}