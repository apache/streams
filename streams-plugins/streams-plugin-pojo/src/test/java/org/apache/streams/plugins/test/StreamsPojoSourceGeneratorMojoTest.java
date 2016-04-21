package org.apache.streams.plugins.test;

import com.google.common.collect.Lists;
import junit.framework.TestCase;
import org.apache.maven.it.Verifier;
import org.apache.maven.it.util.ResourceExtractor;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.streams.plugins.StreamsPojoGenerationConfig;
import org.apache.streams.plugins.StreamsPojoSourceGenerator;
import org.apache.streams.plugins.StreamsPojoSourceGeneratorMojo;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Test that Activity beans are compatible with the example activities in the spec.
 */
public class StreamsPojoSourceGeneratorMojoTest extends TestCase {

    private final static Logger LOGGER = LoggerFactory.getLogger(StreamsPojoSourceGeneratorMojoTest.class);

    protected void setUp() throws Exception
    {
        // required for mojo lookups to work
        super.setUp();
    }

    /**
     * Tests that streams-plugin-pojo running via maven can convert activity schemas into pojos
     * which then compile.
     *
     * @throws Exception
     */
    @Test
    public void testStreamsPojoSourceGeneratorMojo() throws Exception {

        File testDir = ResourceExtractor.simpleExtractResources( getClass(), "/streams-plugin-pojo" );

        Verifier verifier;

        verifier = new Verifier( testDir.getAbsolutePath() );

        List cliOptions = new ArrayList();
        cliOptions.add( "-N" );
        verifier.executeGoals( Lists.<String>newArrayList(
                "clean",
                "dependency:unpack-dependencies",
                "generate-sources",
                "compile"));

        verifier.verifyErrorFreeLog();

        verifier.resetStreams();
//
//        File pom = getTestFile(  );
//        assertNotNull( pom );
//        assertTrue( pom.exists() );
//
//        StreamsPojoSourceGeneratorMojo myMojo = (StreamsPojoSourceGeneratorMojo)
//                lookupMojo( "pojo", pom);
//        assertNotNull( myMojo );
//        myMojo.execute();
//
//        File testOutput = new File( "target/generated-sources/test-mojo");
//        FileFilter javaFilter = new FileFilter() {
//            @Override
//            public boolean accept(File pathname) {
//            if( pathname.getName().endsWith(".java") )
//                return true;
//            return false;
//            }
//        };
//
//        assert( testOutput != null );
//        assert( testOutput.exists() == true );
//        assert( testOutput.isDirectory() == true );
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