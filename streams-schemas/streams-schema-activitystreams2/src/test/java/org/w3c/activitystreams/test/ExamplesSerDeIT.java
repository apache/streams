package org.w3c.activitystreams.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class ExamplesSerDeIT {

    private final static Logger LOGGER = LoggerFactory.getLogger(ExamplesSerDeIT.class);

    private final static ObjectMapper MAPPER = new ObjectMapper();

    /**
     * Tests that activities matching core-ex* can be parsed by apache streams
     *
     * @throws Exception
     */
    @Test
    public void testCoreSerDe() throws Exception {

        InputStream testActivityFolderStream = ExamplesSerDeIT.class.getClassLoader()
                .getResourceAsStream("w3c/activitystreams-master/test");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        for (String file : files) {
            if( !file.startsWith(".") && file.contains("core-ex") ) {
                LOGGER.info("File: activitystreams-master/test/" + file);
                String testFileString = new String(Files.readAllBytes(Paths.get("target/test-classes/w3c/activitystreams-master/test/" + file)));
                LOGGER.info("Content: " + testFileString);
                ObjectNode testFileObjectNode = MAPPER.readValue(testFileString, ObjectNode.class);
                LOGGER.info("Object:" + testFileObjectNode);
            }
        }
    }

    /**
     * Tests that activities matching simple* can be parsed by apache streams
     *
     * @throws Exception
     */
    @Test
    public void testSimpleSerDe() throws Exception {

        InputStream testActivityFolderStream = ExamplesSerDeIT.class.getClassLoader()
                .getResourceAsStream("w3c/activitystreams-master/test");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        for (String file : files) {
            if( !file.startsWith(".") && file.contains("simple") ) {
                LOGGER.info("File: activitystreams-master/test/" + file);
                String testFileString = new String(Files.readAllBytes(Paths.get("target/test-classes/w3c/activitystreams-master/test/" + file)));
                LOGGER.info("Content: " + testFileString);
                ObjectNode testFileObjectNode = MAPPER.readValue(testFileString, ObjectNode.class);
                LOGGER.info("Object:" + testFileObjectNode);
            }
        }
    }

    /**
     * Tests that activities matching vocabulary-ex* can be parsed by apache streams
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testVocabularySerDe() throws Exception {

        InputStream testActivityFolderStream = ExamplesSerDeIT.class.getClassLoader()
                .getResourceAsStream("w3c/activitystreams-master/test");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        for (String file : files) {
            if( !file.startsWith(".") && file.contains("vocabulary-ex") ) {
                LOGGER.info("File: activitystreams-master/test/" + file);
                String testFileString = new String(Files.readAllBytes(Paths.get("target/test-classes/w3c/activitystreams-master/test/" + file)));
                LOGGER.info("Content: " + testFileString);
                ObjectNode testFileObjectNode = MAPPER.readValue(testFileString, ObjectNode.class);
                LOGGER.info("Object:" + testFileObjectNode);
            }
        }
    }

    /**
     * Tests that activities expect to fail cannot be parsed by apache streams
     *
     * @throws Exception
     */
    @Ignore
    @Test
    public void testFailSerDe() throws Exception {

        InputStream testActivityFolderStream = ExamplesSerDeIT.class.getClassLoader()
                .getResourceAsStream("w3c/activitystreams-master/test/fail");
        List<String> files = IOUtils.readLines(testActivityFolderStream, Charsets.UTF_8);

        for (String file : files) {
            if( !file.startsWith(".") && file.contains("vocabulary-ex") ) {
                LOGGER.info("File: activitystreams-master/test/fail/" + file);
                String testFileString = new String(Files.readAllBytes(Paths.get("target/test-classes/w3c/activitystreams-master/test/" + file)));
                LOGGER.info("Content: " + testFileString);
                ObjectNode testFileObjectNode = MAPPER.readValue(testFileString, ObjectNode.class);
                LOGGER.info("Object:" + testFileObjectNode);
            }
        }
    }
}