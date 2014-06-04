package com.w2olabs.util.elasticsearch.test;

import org.apache.streams.builders.threaded.ThreadedStreamBuilder;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.elasticsearch.ElasticsearchClientManager;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.local.test.providers.NumericMessageProvider;
import org.junit.Test;

import java.util.concurrent.LinkedBlockingQueue;

import static org.junit.Assert.*;

public class PersistWriterTest {

    @Test
    public void testSingleWriterSingleThreadFlushByCount() throws Exception {

        final String clusterName = "testSingleWriterSingleThreadFlushByCount";
        final String index = "index1";
        final String type = "type1";
        final int count = 100;

        ElasticsearchWriterConfiguration config = ElasticSearchHelper.createWriterConfiguration(clusterName, index, type, 10, 1024 * 1024);
        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticsearchPersistWriter esWriter = new ElasticsearchPersistWriter(config, escm);
        StreamsProvider provider = new NumericMessageProvider(1000, count);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));

        builder.newReadCurrentStream("provider", provider);
        builder.addStreamsPersistWriter("es_writer", esWriter, 1, "provider");

        builder.start();

        assertEquals("Should have 100 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index, type));
        assertEquals("Should have 100 items (index)", count, ElasticSearchHelper.countRecordsInIndex(escm, index));

        assertEquals("Writer should report 10 items batchesSent", 10, esWriter.getBatchesSent());
        assertEquals("Writer should report 10 items batchesResponded", 10, esWriter.getBatchesResponded());

        assertEquals("Writer should report 100 items ok", count, esWriter.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter.getTotalOutstanding());

        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }

    @Test
    public void testSingleWriterSingleThreadFlushByBytes() throws Exception {

        // This will produce 1,500 bytes worth of information
        final String clusterName = "testSingleWriterSingleThreadFlushByBytes";
        final String index = "index1";
        final String type = "type1";
        final int count = 100;

        ElasticsearchWriterConfiguration config = ElasticSearchHelper.createWriterConfiguration(clusterName, index, type, 100, 1500 / 20);
        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticsearchPersistWriter esWriter = new ElasticsearchPersistWriter(config, escm);
        StreamsProvider provider = new NumericMessageProvider(1000, count);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));

        builder.newReadCurrentStream("provider", provider);
        builder.addStreamsPersistWriter("es_writer", esWriter, 1, "provider");

        builder.start();

        assertEquals("Should have 100 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index, type));
        assertEquals("Should have 100 items (index)", count, ElasticSearchHelper.countRecordsInIndex(escm, index));

        assertEquals("Writer should report 10 items batchesSent", 20, esWriter.getBatchesSent());
        assertEquals("Writer should report 10 items batchesResponded", 20, esWriter.getBatchesResponded());

        assertEquals("Writer should report 100 items ok", count, esWriter.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter.getTotalOutstanding());

        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }

    @Test
    public void testSingleWriterMultiThreaded() throws Exception {

        final String clusterName = "testSingleWriter";
        final String index = "index1";
        final String type = "type1";
        final int count = 100;

        ElasticsearchWriterConfiguration config = ElasticSearchHelper.createWriterConfiguration(clusterName, index, type, 10, 1024 * 1024);
        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticsearchPersistWriter esWriter = new ElasticsearchPersistWriter(config, escm);
        StreamsProvider provider = new NumericMessageProvider(count);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));

        builder.newReadCurrentStream("provider", provider);
        builder.addStreamsPersistWriter("es_writer", esWriter, 10, "provider");

        builder.start();

        assertEquals("Should have 100 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index, type));
        assertEquals("Should have 100 items (index)", count, ElasticSearchHelper.countRecordsInIndex(escm, index));

        assertEquals("Writer should report 10 items batchesSent", 10, esWriter.getBatchesSent());
        assertEquals("Writer should report 10 items batchesResponded", 10, esWriter.getBatchesResponded());

        assertEquals("Writer should report 100 items ok", count, esWriter.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter.getTotalOutstanding());


        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }

    @Test
    public void testSingleDatumBeyondMinimums() throws Exception {

        final String clusterName = "testSingleWriter";
        final String index = "index1";
        final String type = "type1";
        final int count = 1;

        ElasticsearchWriterConfiguration config = ElasticSearchHelper.createWriterConfiguration(clusterName, "index1", "type1", 10, 1024 * 1024);
        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticsearchPersistWriter esWriter = new ElasticsearchPersistWriter(config, escm);
        StreamsProvider provider = new NumericMessageProvider(count);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));

        builder.newReadCurrentStream("provider", provider);
        builder.addStreamsPersistWriter("es_writer", esWriter, 10, "provider");

        builder.start();

        assertEquals("Should have 1 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index, type));
        assertEquals("Should have 1 items (index)", count, ElasticSearchHelper.countRecordsInIndex(escm, index));

        assertEquals("Writer should report 10 items batchesSent", 1, esWriter.getBatchesSent());
        assertEquals("Writer should report 10 items batchesResponded", 1, esWriter.getBatchesResponded());

        assertEquals("Writer should report 1 items ok", count, esWriter.getTotalOk());
        assertEquals("Writer should report 1 items sent", count, esWriter.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter.getTotalOutstanding());


        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }

    @Test
    public void testSingleESCMsDualIndexDualConfigs() throws Exception {

        final String clusterName = "testDualWriter";
        final String index1 = "index1";
        final String type1 = "type1";
        final String index2 = "index2";
        final String type2 = "type2";
        final int count = 100;

        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticsearchWriterConfiguration config1 = ElasticSearchHelper.createWriterConfiguration(clusterName, index1, type1, 10, 1024 * 1024);
        ElasticsearchWriterConfiguration config2 = ElasticSearchHelper.createWriterConfiguration(clusterName, index2, type2, 5, 1024 * 1024);

        ElasticsearchPersistWriter esWriter1 = new ElasticsearchPersistWriter(config1, escm);
        ElasticsearchPersistWriter esWriter2 = new ElasticsearchPersistWriter(config2, escm);

        StreamsProvider provider = new NumericMessageProvider(count);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(5));

        builder.newReadCurrentStream("provider", provider);
        builder.addStreamsPersistWriter("es_writer_1", esWriter1, 10, "provider");
        builder.addStreamsPersistWriter("es_writer_2", esWriter2, 10, "provider");

        builder.start();

        assertEquals("Should have 100 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index1, type1));
        assertEquals("Should have 100 items (index)", count, ElasticSearchHelper.countRecordsInIndex(escm, index1));


        assertEquals("Writer should report 10 items batchesSent", 10, esWriter1.getBatchesSent());
        assertEquals("Writer should report 10 items batchesResponded", 10, esWriter1.getBatchesResponded());

        assertEquals("Writer should report 100 items ok", count, esWriter1.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter1.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter1.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter1.getTotalOutstanding());

        assertEquals("Should have 100 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index2, type2));
        assertEquals("Should have 100 items (index)", count, ElasticSearchHelper.countRecordsInIndex(escm, index2));

        assertEquals("Writer should report 10 items batchesSent", 20, esWriter2.getBatchesSent());
        assertEquals("Writer should report 10 items batchesResponded", 20, esWriter2.getBatchesResponded());

        assertEquals("Writer should report 100 items ok", count, esWriter2.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter2.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter2.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter2.getTotalOutstanding());

        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }

    @Test
    public void testDualESCMsSameCluster() throws Exception {

        final String clusterName = "testSingleESCMsSingleIndexDualWriters";
        final String index1 = "index1";
        final String type1 = "type1";
        final String index2 = "index2";
        final String type2 = "type2";
        final int count = 100;

        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticsearchWriterConfiguration config1 = ElasticSearchHelper.createWriterConfiguration(clusterName, index1, type1, 10, 1024 * 1024);
        ElasticsearchWriterConfiguration config2 = ElasticSearchHelper.createWriterConfiguration(clusterName, index2, type2, 5, 1024 * 1024);

        ElasticsearchPersistWriter esWriter1 = new ElasticsearchPersistWriter(config1, escm);
        ElasticsearchPersistWriter esWriter2 = new ElasticsearchPersistWriter(config2, escm);

        StreamsProvider provider = new NumericMessageProvider(count);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(5));

        builder.newReadCurrentStream("provider", provider);
        builder.addStreamsPersistWriter("es_writer_1", esWriter1, 10, "provider");
        builder.addStreamsPersistWriter("es_writer_2", esWriter2, 10, "provider");

        builder.start();

        assertEquals("Should have 100 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index1, type1));
        assertEquals("Should have 100 items (index)", count, ElasticSearchHelper.countRecordsInIndex(escm, index1));


        assertEquals("Writer should report 10 items batchesSent", 10, esWriter1.getBatchesSent());
        assertEquals("Writer should report 10 items batchesResponded", 10, esWriter1.getBatchesResponded());

        assertEquals("Writer should report 100 items ok", count, esWriter1.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter1.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter1.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter1.getTotalOutstanding());

        assertEquals("Should have 100 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index2, type2));
        assertEquals("Should have 100 items (index)", count, ElasticSearchHelper.countRecordsInIndex(escm, index2));

        assertEquals("Writer should report 10 items batchesSent", 20, esWriter2.getBatchesSent());
        assertEquals("Writer should report 10 items batchesResponded", 20, esWriter2.getBatchesResponded());

        assertEquals("Writer should report 100 items ok", count, esWriter2.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter2.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter2.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter2.getTotalOutstanding());

        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }

    @Test
    public void testSingleESCMsSingleIndexDualWriters() throws Exception {

        final String clusterName = "testDualESCMsSameCluster";
        final String index1 = "index1";
        final String type1 = "type1";
        final int count = 100;


        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticsearchWriterConfiguration config1 = ElasticSearchHelper.createWriterConfiguration(clusterName, index1, type1, 1, 5 * 1024 * 1024);
        ElasticsearchPersistWriter esWriter1 = new ElasticsearchPersistWriter(config1, escm);

        ElasticsearchWriterConfiguration config2 = ElasticSearchHelper.createWriterConfiguration(clusterName, index1, type1, 1000, 1500 / 10);
        ElasticsearchPersistWriter esWriter2 = new ElasticsearchPersistWriter(config2, escm);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));

        builder.newReadCurrentStream("provider", new NumericMessageProvider(1000, count));
        builder.addStreamsPersistWriter("es_writer_1", esWriter1, 10, "provider");
        builder.addStreamsPersistWriter("es_writer_2", esWriter2, 5, "provider");

        builder.start();

        assertEquals("Should have 100 items (index & type)", count * 2, ElasticSearchHelper.countRecordsInIndex(escm, index1, type1));
        assertEquals("Should have 100 items (index)", count * 2, ElasticSearchHelper.countRecordsInIndex(escm, index1));

        assertEquals("Writer should report 100 items batchesSent", 100, esWriter1.getBatchesSent());
        assertEquals("Writer should report 100 items batchesResponded", 100, esWriter1.getBatchesResponded());

        assertEquals("Writer should report 100 items ok", count, esWriter1.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter1.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter1.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter1.getTotalOutstanding());

        assertEquals("Writer should report 10 items batchesSent", 10, esWriter2.getBatchesSent());
        assertEquals("Writer should report 10 items batchesResponded", 10, esWriter2.getBatchesResponded());

        assertEquals("Writer should report 100 items ok", count, esWriter2.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter2.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter2.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter2.getTotalOutstanding());

        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }

    @Test
    public void testSingleESCMsSingleIndexTwoTypesDualWriters() throws Exception {

        final String clusterName = "testDualESCMsSameCluster";
        final String index1 = "index1";
        final String type1 = "type1";
        final String type2 = "type2";
        final int count = 100;


        ElasticsearchClientManager escm = ElasticSearchHelper.getElasticSearchClientManager(clusterName);

        ElasticsearchWriterConfiguration config1 = ElasticSearchHelper.createWriterConfiguration(clusterName, index1, type1, 1000, 1500 / 15);
        ElasticsearchPersistWriter esWriter1 = new ElasticsearchPersistWriter(config1, escm);

        ElasticsearchWriterConfiguration config2 = ElasticSearchHelper.createWriterConfiguration(clusterName, index1, type2, 1, 1000000000);
        ElasticsearchPersistWriter esWriter2 = new ElasticsearchPersistWriter(config2, escm);

        // Create the builder then execute
        ThreadedStreamBuilder builder = new ThreadedStreamBuilder(new LinkedBlockingQueue<StreamsDatum>(75));

        builder.newReadCurrentStream("provider", new NumericMessageProvider(1000, count));
        builder.addStreamsPersistWriter("es_writer_1", esWriter1, 10, "provider");
        builder.addStreamsPersistWriter("es_writer_2", esWriter2, 5, "provider");

        builder.start();

        assertEquals("Should have 100 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index1, type1));
        assertEquals("Should have 100 items (index & type)", count, ElasticSearchHelper.countRecordsInIndex(escm, index1, type2));
        assertEquals("Should have 100 items (index)", count * 2, ElasticSearchHelper.countRecordsInIndex(escm, index1));

        assertEquals("Writer should report 10 items batchesSent", 15, esWriter1.getBatchesSent());
        assertEquals("Writer should report 10 items batchesResponded", 15, esWriter1.getBatchesResponded());

        assertEquals("Writer should report 100 items ok", count, esWriter1.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter1.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter1.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter1.getTotalOutstanding());

        assertEquals("Writer should report 100 items batchesSent", 100, esWriter2.getBatchesSent());
        assertEquals("Writer should report 100 items batchesResponded", 100, esWriter2.getBatchesResponded());

        assertEquals("Writer should report 100 items ok", count, esWriter2.getTotalOk());
        assertEquals("Writer should report 100 items sent", count, esWriter2.getTotalSent());
        assertEquals("Writer should report 0 items fail", 0, esWriter2.getTotalFailed());
        assertEquals("Writer should report 0 items outstanding", 0, esWriter2.getTotalOutstanding());

        // clean up
        ElasticSearchHelper.destroyElasticSearchClientManager(escm);
    }

}
