package com.w2olabs.util.elasticsearch.test;

import org.apache.streams.builders.threaded.ThreadedStreamBuilder;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.elasticsearch.*;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.apache.streams.local.test.writer.DatumCounterWriter;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.assertEquals;

public class PersistReaderTest {

    /**
     * This test passes... // TODO: Re-Write ElasticSearchPersistReader
     * @throws Exception
     */
    @Test
    public void testWriteThenRead() throws Exception {

        final String clusterName = "testWriteThenRead";
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
