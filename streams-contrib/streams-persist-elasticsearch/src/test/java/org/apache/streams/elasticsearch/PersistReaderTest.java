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


import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.local.test.processors.ObjectTypeEnforcerProcessor;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.apache.streams.threaded.builders.ThreadedStreamBuilder;
import org.elasticsearch.common.unit.TimeValue;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PersistReaderTest {

    final String clusterName = UUID.randomUUID().toString();
    final String index = "index1";
    final String type = "type1";
    final int count = 10000;
    ElasticsearchClientManager escm;

    /**
     * Write the data that is to be read later in the tests.
     * @throws Exception
     */
    @Before
    public void writeTheData() throws Exception {

        this.escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);;

        ElasticsearchWriterConfiguration configWrite = ElasticSearchHelper.createWriterConfiguration(clusterName, index, type, 1000, 5 * 1024 * 1024);
        configWrite.setBulk(true);
        ElasticsearchPersistWriter esWriter = new ElasticsearchPersistWriter(configWrite, escm);

        StreamsProvider provider = new NumericMessageProvider(1000, count);

        // Create the builder then execute
        new ThreadedStreamBuilder()
                    .newReadCurrentStream("provider", provider)
                    .addStreamsPersistWriter("es_writer", esWriter, "provider")
                    .start();

        assertEquals("Should have 10,000 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index, type));

        assertEquals("Writer should report 100 items ok", count, esWriter.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter.getTotalOutstanding());
    }

    @Test
    public void countTotalRecords() throws Exception {
        assertEquals(this.count, ElasticSearchHelper.countRecordsInIndex(this.escm, this.index, this.type));
    }

    /**
     * This test passes... // TODO: Re-Write ElasticSearchPersistReader
     * @throws Exception
     */
    @Test
    public void testReadDataSingleItemPerBatch() throws Exception {
        DatumCounterWriter writerCounter = new DatumCounterWriter();
        ObjectTypeEnforcerProcessor typeEnforcer = new ObjectTypeEnforcerProcessor(ObjectNode.class);

        ElasticsearchReaderConfiguration configRead = ElasticSearchHelper.createReadConfiguration(clusterName,
                new ArrayList<String>() {{ add(index); }},
                new ArrayList<String>() {{ add(type); }});

        configRead.setBatchSize(1l);
        configRead.setSize(100l);

        ElasticsearchPersistReader elasticsearchPersistReader = new ElasticsearchPersistReader(configRead, escm);

        // Create the builder then execute
        new ThreadedStreamBuilder()
                    .newPerpetualStream("es_reader", elasticsearchPersistReader)
                    .addStreamsProcessor("ObjectTypeEnforcerProcessor", typeEnforcer, 1, "es_reader")
                    .addStreamsPersistWriter("datum_writer", writerCounter, 1, "ObjectTypeEnforcerProcessor")
                    .start();

        assertEquals(100, writerCounter.getDatumsCounted());
        assertFalse(typeEnforcer.getIncorrectClassPresent());
    }


    /**
     * This test passes... // TODO: Re-Write ElasticSearchPersistReader
     * @throws Exception
     */
    @Test
    public void testReaderTimeoutFailSafely() throws Exception {
        DatumCounterWriter writerCounter = new DatumCounterWriter();
        ObjectTypeEnforcerProcessor typeEnforcer = new ObjectTypeEnforcerProcessor(ObjectNode.class);

        ElasticsearchReaderConfiguration configRead = ElasticSearchHelper.createReadConfiguration(clusterName,
                new ArrayList<String>() {{ add(index); }},
                new ArrayList<String>() {{ add(type); }});

        configRead.setBatchSize(1l);
        configRead.setScrollTimeout("1s");

        ElasticsearchPersistReader elasticsearchPersistReader = new ElasticsearchPersistReader(configRead, escm);


        // Create the builder then execute
        new ThreadedStreamBuilder(new ArrayBlockingQueue<StreamsDatum>(1))
                .newPerpetualStream("es_reader", elasticsearchPersistReader)
                .addStreamsProcessor("proc", new StreamsProcessor() {
                    @Override
                    public List<StreamsDatum> process(final StreamsDatum entry) {
                        long start = new Date().getTime();
                        while(new Date().getTime() - start < (10 * 1000)) {
                            try {
                                Thread.sleep(1);
                            } catch (InterruptedException e) {
                                // do nothing
                            }
                        }
                        return new ArrayList<StreamsDatum>() {{ add(entry); }};
                    }

                    @Override public void prepare(Object configurationObject) { }

                    @Override public void cleanUp() { }
                }, "es_reader")
                .addStreamsProcessor("ObjectTypeEnforcerProcessor", typeEnforcer, 1, "proc")
                .addStreamsPersistWriter("datum_writer", writerCounter, 1, "ObjectTypeEnforcerProcessor")
                .start();

        assertTrue("Should not have read that many items", writerCounter.getDatumsCounted() < 100);
        assertFalse("Invalid type coming from ES Reader", typeEnforcer.getIncorrectClassPresent());
    }


    /**
     * This test passes... // TODO: Re-Write ElasticSearchPersistReader
     * @throws Exception
     */
    @Test
    public void testReadData() throws Exception {
        DatumCounterWriter writerCounter = new DatumCounterWriter();
        ObjectTypeEnforcerProcessor typeEnforcer = new ObjectTypeEnforcerProcessor(ObjectNode.class);

        ElasticsearchReaderConfiguration configRead = ElasticSearchHelper.createReadConfiguration(clusterName,
                new ArrayList<String>() {{ add(index); }},
                new ArrayList<String>() {{ add(type); }});

        // Create the builder then execute
        new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75))
            .newPerpetualStream("es_reader", new ElasticsearchPersistReader(configRead, escm))
            .addStreamsProcessor("ObjectTypeEnforcerProcessor", typeEnforcer, 1, "es_reader")
            .addStreamsPersistWriter("datum_writer", writerCounter, 1, "ObjectTypeEnforcerProcessor")
            .start();

        assertEquals("Writer should read 10,000 items", count, writerCounter.getDatumsCounted());
        assertFalse("Invalid type coming from ES Reader", typeEnforcer.getIncorrectClassPresent());
    }

    @After
    public void destroyESCM() {
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }


}
