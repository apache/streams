package org.apache.streams.elasticsearch.test;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang.SerializationUtils;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.elasticsearch.ElasticsearchConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchReaderConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.test.ElasticsearchIntegrationTest;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by sblackmon on 10/20/14.
 */
@ElasticsearchIntegrationTest.ClusterScope(scope= ElasticsearchIntegrationTest.Scope.TEST, numNodes=1)
public class TestElasticsearchPersistWriter extends ElasticsearchIntegrationTest {

    private final String TEST_INDEX = "TestElasticsearchPersistWriter".toLowerCase();

    private ElasticsearchWriterConfiguration testConfiguration;

    public void prepareTest() {

        testConfiguration = new ElasticsearchWriterConfiguration();
        testConfiguration.setHosts(Lists.newArrayList("localhost"));
        testConfiguration.setClusterName(cluster().getClusterName());

    }

   @Test
    public void testPersistWriterString() {

        ElasticsearchWriterConfiguration testConfiguration = new ElasticsearchWriterConfiguration();
        testConfiguration.setHosts(Lists.newArrayList("localhost"));
        testConfiguration.setClusterName(cluster().getClusterName());
        testConfiguration.setBatchSize(1l);
        testConfiguration.setIndex(TEST_INDEX);
        testConfiguration.setType("string");
        ElasticsearchPersistWriter testPersistWriter = new ElasticsearchPersistWriter(testConfiguration);
        testPersistWriter.prepare(null);

        String testJsonString = "{\"dummy\":\"true\"}";

        assert(!indexExists(TEST_INDEX));

        testPersistWriter.write(new StreamsDatum(testJsonString, "test"));

        testPersistWriter.cleanUp();

        flushAndRefresh();

        assert(indexExists(TEST_INDEX));

        long count = client().count(client().prepareCount().request()).actionGet().getCount();

        assert(count > 0);

    }
}
