package org.apache.streams.elasticsearch.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang.SerializationUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.ElasticsearchConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchReaderConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.elasticsearch.processor.DatumFromMetadataProcessor;
import org.apache.streams.elasticsearch.processor.DocumentToMetadataProcessor;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by sblackmon on 10/20/14.
 */
@ElasticsearchIntegrationTest.ClusterScope(scope= ElasticsearchIntegrationTest.Scope.TEST, numNodes=1)
public class TestDatumFromMetadataProcessor extends ElasticsearchIntegrationTest {

    private final String TEST_INDEX = "TestDatumFromMetadataProcessor".toLowerCase();

    private ElasticsearchReaderConfiguration testConfiguration;

    @Test
    public void testSerializability() {
        DatumFromMetadataProcessor processor = new DatumFromMetadataProcessor(testConfiguration);

        DatumFromMetadataProcessor clone = (DatumFromMetadataProcessor) SerializationUtils.clone(processor);
    }

    @Before
    public void prepareTest() {

        testConfiguration = new ElasticsearchReaderConfiguration();
        testConfiguration.setHosts(Lists.newArrayList("localhost"));
        testConfiguration.setClusterName(cluster().getClusterName());

        String testJsonString = "{\"dummy\":\"true\"}";

        client().index(client().prepareIndex(TEST_INDEX, "activity", "id").setSource(testJsonString).request()).actionGet(5, TimeUnit.SECONDS);

    }

    @Test
    public void testDatumFromMetadataProcessor() {

        Map<String, Object> metadata = Maps.newHashMap();

        metadata.put("index", TEST_INDEX);
        metadata.put("type", "activity");
        metadata.put("id", "id");

        DatumFromMetadataProcessor processor = new DatumFromMetadataProcessor(testConfiguration);

        StreamsDatum testInput = new StreamsDatum(null);

        testInput.setMetadata(metadata);

        Assert.assertNull(testInput.document);

        processor.prepare(null);

        StreamsDatum testOutput = processor.process(testInput).get(0);

        processor.cleanUp();

        Assert.assertNotNull(testOutput.document);

    }
}
