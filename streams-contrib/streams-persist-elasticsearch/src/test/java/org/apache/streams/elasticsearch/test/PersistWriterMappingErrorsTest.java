/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.streams.elasticsearch.test;


import org.apache.streams.builders.threaded.ThreadedStreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.elasticsearch.ElasticsearchClientManager;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.local.test.providers.ShapeShifterProvider;
import org.elasticsearch.action.admin.cluster.state.ClusterStateResponse;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.cluster.metadata.MappingMetaData;
import org.elasticsearch.common.hppc.cursors.ObjectCursor;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PersistWriterMappingErrorsTest {


    private static final String ES_MAPPING = "{\"type1\":{\"properties\":{\"number\":{\"type\":\"integer\"}}}}";

    @Test
    public void testSingleWriterSingleThreadShapeShifter() throws Exception {

        final String clusterName = UUID.randomUUID().toString();
        final String index = "index1";
        final String type = "type1";
        final int count = 100;

        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticSearchHelper.createIndexWithMapping(escm, index, type, ES_MAPPING);

        ElasticsearchWriterConfiguration config = ElasticSearchHelper.createWriterConfiguration(clusterName, index, type, 10, 1024 * 1024 * 10);

        ElasticsearchPersistWriter esWriter = new ElasticsearchPersistWriter(config, escm);

        // Shift the shape of the object to 'clash' the mapping every 10 units
        StreamsProvider provider = new ShapeShifterProvider(1000, count, 10);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));

        builder.newReadCurrentStream("provider", provider);
        builder.addStreamsPersistWriter("es_writer", esWriter, 1, "provider");

        builder.start();

        // 1/2 of these should fail
        assertEquals("Should have 50 items (index & type)", count / 2, ElasticSearchHelper.countRecordsInIndex(escm, index, type));
        assertEquals("Should have 50 items (index)", count / 2, ElasticSearchHelper.countRecordsInIndex(escm, index));

        assertEquals("10 batchesSent", 10, esWriter.getBatchesSent());
        assertEquals("10 batchesResponded", 10, esWriter.getBatchesResponded());

        // It will, however, report the item as a failure, so, we should have 50 passes, and 50 fails
        assertEquals("100 items sent", count, esWriter.getTotalSent());
        assertEquals("50 items ok", count / 2, esWriter.getTotalOk());
        assertEquals("50 items fail", count / 2, esWriter.getTotalFailed());
        assertEquals("0 items outstanding", 0, esWriter.getTotalOutstanding());

        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }

    @Test
    public void testSingleWriterMultiThreadShapeShifter() throws Exception {

        final String clusterName = UUID.randomUUID().toString();
        final String index = "index1";
        final String type = "type1";
        final int count = 100;

        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticSearchHelper.createIndexWithMapping(escm, index, type, ES_MAPPING);

        ElasticsearchWriterConfiguration config = ElasticSearchHelper.createWriterConfiguration(clusterName, index, type, 10, 1024 * 1024 * 10);

        ElasticsearchPersistWriter esWriter = new ElasticsearchPersistWriter(config, escm);

        // Shift the shape of the object to 'clash' the mapping every 4 units
        StreamsProvider provider = new ShapeShifterProvider(1000, count, 4);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));

        builder.newReadCurrentStream("provider", provider);
        builder.addStreamsPersistWriter("es_writer", esWriter, 5, "provider");

        builder.start();

        ClusterStateResponse clusterStateResponse = escm.getClient()
                .admin()
                .cluster()
                .prepareState()
                .execute()
                .actionGet();

        // 1/2 of these should fail
        assertEquals("Should have 52 items (index & type)", 52, ElasticSearchHelper.countRecordsInIndex(escm, index, type));
        assertEquals("Should have 52 items (index)", 52, ElasticSearchHelper.countRecordsInIndex(escm, index));

        assertEquals("10 batchesSent", 10, esWriter.getBatchesSent());
        assertEquals("10 batchesResponded", 10, esWriter.getBatchesResponded());

        // It will, however, report the item as a failure, so, we should have 50 passes, and 50 fails
        assertEquals("100 items sent", count, esWriter.getTotalSent());
        assertEquals("52 items ok", 52, esWriter.getTotalOk());
        assertEquals("48 items fail", 48, esWriter.getTotalFailed());
        assertEquals("0 items outstanding", 0, esWriter.getTotalOutstanding());

        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }


    @Test
    public void testSingleWriterSingleThreadShapeShifterThousandsOnePerBatch() throws Exception {

        final String clusterName = UUID.randomUUID().toString();
        final String index = "index1";
        final String type = "type1";
        final int count = 5000;

        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticSearchHelper.createIndexWithMapping(escm, index, type, ES_MAPPING);

        ElasticsearchWriterConfiguration config = ElasticSearchHelper.createWriterConfiguration(clusterName, index, type, 1, 1024 * 1024 * 10);

        ElasticsearchPersistWriter esWriter = new ElasticsearchPersistWriter(config, escm);

        // Shift the shape of the object to 'clash' the mapping every 4 units
        StreamsProvider provider = new ShapeShifterProvider(1000, count, 2);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));

        builder.newReadCurrentStream("provider", provider);
        builder.addStreamsPersistWriter("es_writer", esWriter, 1, "provider");

        builder.start();

        // 1/2 of these should fail
        assertEquals("Should have 52 items (index & type)", count / 2, ElasticSearchHelper.countRecordsInIndex(escm, index, type));
        assertEquals("Should have 52 items (index)", count / 2, ElasticSearchHelper.countRecordsInIndex(escm, index));

        assertEquals("10 batchesSent", count, esWriter.getBatchesSent());
        assertEquals("10 batchesResponded", count, esWriter.getBatchesResponded());

        // It will, however, report the item as a failure, so, we should have 50 passes, and 50 fails
        assertEquals("5,000 items sent", count, esWriter.getTotalSent());
        assertEquals("2,500 items ok", count / 2, esWriter.getTotalOk());
        assertEquals("2,500 items fail", count / 2, esWriter.getTotalFailed());
        assertEquals("0 items outstanding", 0, esWriter.getTotalOutstanding());

        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }


    @Test
    public void testSingleWriterMultiThreadShapeShifterThousandsOnePerBatch() throws Exception {

        final String clusterName = UUID.randomUUID().toString();
        final String index = "index1";
        final String type = "type1";
        final int count = 5000;

        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticSearchHelper.createIndexWithMapping(escm, index, type, ES_MAPPING);

        ElasticsearchWriterConfiguration config = ElasticSearchHelper.createWriterConfiguration(clusterName, index, type, 1, 1024 * 1024 * 10);

        ElasticsearchPersistWriter esWriter = new ElasticsearchPersistWriter(config, escm);

        // Shift the shape of the object to 'clash' the mapping every 4 units
        StreamsProvider provider = new ShapeShifterProvider(1000, count, 2);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));

        builder.newReadCurrentStream("provider", provider);
        builder.addStreamsPersistWriter("es_writer", esWriter, 5, "provider");

        builder.start();

        // 1/2 of these should fail
        assertEquals("Should have 52 items (index & type)", count / 2, ElasticSearchHelper.countRecordsInIndex(escm, index, type));
        assertEquals("Should have 52 items (index)", count / 2, ElasticSearchHelper.countRecordsInIndex(escm, index));

        assertEquals("10 batchesSent", count, esWriter.getBatchesSent());
        assertEquals("10 batchesResponded", count, esWriter.getBatchesResponded());

        // It will, however, report the item as a failure, so, we should have 50 passes, and 50 fails
        assertEquals("5,000 items sent", count, esWriter.getTotalSent());
        assertEquals("2,500 items ok", count / 2, esWriter.getTotalOk());
        assertEquals("2,500 items fail", count / 2, esWriter.getTotalFailed());
        assertEquals("0 items outstanding", 0, esWriter.getTotalOutstanding());

        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }

}
