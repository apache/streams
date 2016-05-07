package org.apache.streams.plugins.elasticsearch.test;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Tests that streams-plugin-hive running via maven generates hql resources
 */
public class StreamsElasticsearchResourceGeneratorMojoTest extends TestCase {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsElasticsearchResourceGeneratorMojoTest.class);

    protected void setUp() throws Exception
    {
        // required for mojo lookups to work
        super.setUp();
    }


    @Test
    public void testStreamsElasticsearchResourceGeneratorMojo() throws Exception {

        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/streams-plugin-elasticsearch" );

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

    }
}