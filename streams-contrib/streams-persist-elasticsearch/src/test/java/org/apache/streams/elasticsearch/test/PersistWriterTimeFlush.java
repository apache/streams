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
import org.apache.streams.local.test.providers.NumericMessageProviderDelayed;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;

public class PersistWriterTimeFlush {
    @Test
    public void testSingleWriterSingleThreadFlushByTimer() throws Exception {

        final String clusterName = UUID.randomUUID().toString();
        final String index = "index1";
        final String type = "type1";
        final int count = 5;

        ElasticsearchWriterConfiguration config = ElasticSearchHelper.createWriterConfiguration(clusterName, index, type, Integer.MAX_VALUE, Integer.MAX_VALUE, 50);
        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticsearchPersistWriter esWriter = new ElasticsearchPersistWriter(config, escm);
        StreamsProvider provider = new NumericMessageProviderDelayed(count, 2000, 1);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));

        builder.newReadCurrentStream("provider", provider);
        builder.addStreamsPersistWriter("es_writer", esWriter, 1, "provider");

        builder.start();

        assertEquals("Should have 5 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index, type));
        assertEquals("Should have 5 items (index)", count, ElasticSearchHelper.countRecordsInIndex(escm, index));

        assertEquals("5 batchesSent", count, esWriter.getBatchesSent());
        assertEquals("5 batchesResponded", count, esWriter.getBatchesResponded());

        assertEquals("Writer should report 5 items ok", count, esWriter.getTotalOk());
        assertEquals("Writer should report 5 items sent", count, esWriter.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter.getTotalOutstanding());

        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }
}
