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
package org.apache.streams.elasticsearch;


import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.apache.streams.threaded.builders.ThreadedStreamBuilder;
import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;

public class PersistReaderTest {

    /**
     * This test passes... // TODO: Re-Write ElasticSearchPersistReader
     * @throws Exception
     */
    @Test
    public void testWriteThenRead() throws Exception {

        final String clusterName = UUID.randomUUID().toString();
        final String index = "index1";
        final String type = "type1";
        final int count = 10000;

        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticsearchWriterConfiguration configWrite = ElasticSearchHelper.createWriterConfiguration(clusterName, index, type, 10, 1024 * 1024);
        ElasticsearchPersistWriter esWriter = new ElasticsearchPersistWriter(configWrite, escm);

        StreamsProvider provider = new NumericMessageProvider(1000, count);

        // Create the builder then execute
        StreamBuilder builderWrite = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));

        builderWrite.newReadCurrentStream("provider", provider);
        builderWrite.addStreamsPersistWriter("es_writer", esWriter, 1, "provider");

        builderWrite.start();

        assertEquals("Should have 10,000 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index, type));

        assertEquals("Writer should report 100 items ok", count, esWriter.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter.getTotalOutstanding());


        // Create the builder then execute
        StreamBuilder builderRead = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));
        ElasticsearchReaderConfiguration configRead = ElasticSearchHelper.createReadConfiguration(clusterName,
                new ArrayList<String>() {{ add(index); }},
                new ArrayList<String>() {{ add(type); }});

        ElasticsearchPersistReader elasticsearchPersistReader = new ElasticsearchPersistReader(configRead, escm);
        DatumCounterWriter writerCounter = new DatumCounterWriter();

        builderRead.newPerpetualStream("es_reader", elasticsearchPersistReader);
        builderRead.addStreamsPersistWriter("datum_writer", writerCounter, 1, "es_reader");

        builderRead.start();

        assertEquals("Writer should read 10,000 items", count, writerCounter.getDatumsCounted());

        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }


}
