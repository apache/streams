package org.apache.streams.elasticsearch.test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import org.apache.commons.lang.SerializationUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.ElasticsearchReaderConfiguration;
import org.apache.streams.elasticsearch.processor.DocumentToMetadataProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

/**
 * Created by sblackmon on 10/20/14.
 */
public class TestDocumentToMetadataProcessor {

    private static ObjectMapper MAPPER = StreamsJacksonMapper.getInstance();

    @Before
    public void prepareTest() {

    }

    @Test
    public void testSerializability() {
        DocumentToMetadataProcessor processor = new DocumentToMetadataProcessor();

        DocumentToMetadataProcessor clone = (DocumentToMetadataProcessor) SerializationUtils.clone(processor);
    }

    @Test
    public void testDocumentToMetadataProcessor() {

        ObjectNode document = MAPPER.createObjectNode()
                .put("a", "a")
                .put("b", "b")
                .put("c", 6);

        DocumentToMetadataProcessor processor = new DocumentToMetadataProcessor();

        StreamsDatum testInput = new StreamsDatum(document);

        Assert.assertNotNull(testInput.document);
        Assert.assertNotNull(testInput.metadata);
        Assert.assertEquals(testInput.metadata.size(), 0);

        processor.prepare(null);

        StreamsDatum testOutput = processor.process(testInput).get(0);

        processor.cleanUp();

        Assert.assertNotNull(testOutput.metadata);
        Assert.assertEquals(testInput.metadata.size(), 3);

    }
}
