/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.*;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexRequestBuilder;
import org.elasticsearch.common.joda.time.DateTime;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ElasticsearchPersistWriter implements StreamsPersistWriter, DatumStatusCountable {

    public static final String STREAMS_ID = ElasticsearchPersistWriter.class.getCanonicalName();

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPersistWriter.class);
    private static final NumberFormat MEGABYTE_FORMAT = new DecimalFormat("#.##");
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("###,###,###,###");
    private static final Long DEFAULT_BULK_FLUSH_THRESHOLD = 5l * 1024l * 1024l;
    private static final long WAITING_DOCS_LIMIT = 10000;
    private static final long DEFAULT_MAX_WAIT = 10000;
    private static final int DEFAULT_BATCH_SIZE = 100;

    private static final ObjectMapper OBJECT_MAPPER = StreamsJacksonMapper.getInstance();

    private final List<String> affectedIndexes = new ArrayList<String>();

    private final ElasticsearchClientManager manager;
    private final ElasticsearchWriterConfiguration config;

    private BulkRequestBuilder bulkRequest;

    private long flushThresholdsRecords = DEFAULT_BATCH_SIZE;
    private long flushThresholdBytes = DEFAULT_BULK_FLUSH_THRESHOLD;
    private long flushThresholdTime = DEFAULT_MAX_WAIT;
    private boolean veryLargeBulk = false;  // by default this setting is set to false

    private final AtomicInteger batchesSent = new AtomicInteger(0);
    private final AtomicInteger batchesResponded = new AtomicInteger(0);

    private final AtomicLong currentBatchItems = new AtomicLong(0);
    private final AtomicLong currentBatchBytes = new AtomicLong(0);

    private final AtomicLong totalSent = new AtomicLong(0);
    private final AtomicLong totalSeconds = new AtomicLong(0);
    private final AtomicLong totalOk = new AtomicLong(0);
    private final AtomicLong totalFailed = new AtomicLong(0);
    private final AtomicLong totalSizeInBytes = new AtomicLong(0);

    public ElasticsearchPersistWriter() {
        this(ElasticsearchConfigurator.detectWriterConfiguration(StreamsConfigurator.config.getConfig("elasticsearch")));
    }

    public ElasticsearchPersistWriter(ElasticsearchWriterConfiguration config) {
        this(config, new ElasticsearchClientManager(config));
    }

    public ElasticsearchPersistWriter(ElasticsearchWriterConfiguration config, ElasticsearchClientManager manager) {
        this.config = config;
        this.manager = manager;
        this.bulkRequest = this.manager.getClient().prepareBulk();
    }

    public long getBatchesSent()                            { return this.batchesSent.get(); }
    public long getBatchesResponded()                       { return batchesResponded.get(); }


    public long getFlushThresholdsRecords()                 { return this.flushThresholdsRecords; }
    public long getFlushThresholdBytes()                    { return this.flushThresholdBytes; }
    public long getFlushThreasholdMaxTime()                 { return this.flushThresholdTime; }

    public void setFlushThresholdRecords(long val)          { this.flushThresholdsRecords = val; }
    public void setFlushThresholdBytes(long val)            { this.flushThresholdBytes = val; }
    public void setFlushThreasholdMaxTime(long val)         { this.flushThresholdTime = val; }
    public void setVeryLargeBulk(boolean veryLargeBulk)     { this.veryLargeBulk = veryLargeBulk; }


    public long getTotalOutstanding()                       { return this.totalSent.get() - (this.totalFailed.get() + this.totalOk.get()); }
    public long getTotalSent()                              { return this.totalSent.get(); }
    public long getTotalOk()                                { return this.totalOk.get(); }
    public long getTotalFailed()                            { return this.totalFailed.get(); }
    public long getTotalSizeInBytes()                       { return this.totalSizeInBytes.get(); }
    public long getTotalSeconds()                           { return this.totalSeconds.get(); }
    public List<String> getAffectedIndexes()                { return this.affectedIndexes; }

    public boolean isConnected()                            { return (this.manager.getClient() != null); }

    @Override
    public void write(StreamsDatum streamsDatum) {
        if(streamsDatum == null || streamsDatum.getDocument() == null)
            return;

        checkForBackOff();

        try {
            add(config.getIndex(), config.getType(), streamsDatum.getId(),
                    streamsDatum.getTimestamp() == null ? Long.toString(DateTime.now().getMillis()) : Long.toString(streamsDatum.getTimestamp().getMillis()),
                    (streamsDatum.getDocument() instanceof String) ? streamsDatum.getDocument().toString() : OBJECT_MAPPER.writeValueAsString(streamsDatum.getDocument()));
        } catch (Throwable e) {
            LOGGER.warn("Unable to Write Datum to ElasticSearch: {}", e.getMessage());
        }
    }

    public void cleanUp() {
        try {

            // before they close, check to ensure that
            flushInternal();

            // We are going to give it 5 minutes.
            int count = 0;
            if(this.batchesResponded.get() != this.batchesSent.get()) {
                while (this.batchesResponded.get() != this.batchesSent.get() && count++ < 20 * 60 * 5) {
                    Thread.sleep(50);
                }

                if (this.batchesResponded.get() != this.batchesSent.get())
                    LOGGER.error("We never cleared our buffer: Sent[{}] Outstanding[{}]", this.batchesSent.get(), this.batchesResponded.get());
            }

            refreshIndexes();

            LOGGER.debug("Closed ElasticSearch Writer: Ok[{}] Failed[{}] Orphaned[{}]", this.totalOk.get(), this.totalFailed.get(), this.getTotalOutstanding());

        } catch (Throwable e) {
            // this line of code should be logically unreachable.
            LOGGER.warn("This is unexpected: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    private void refreshIndexes() {
        for (String indexName : this.affectedIndexes) {

            if (this.veryLargeBulk) {
                LOGGER.debug("Resetting our Refresh Interval: {}", indexName);
                // They are in 'very large bulk' mode and the process is finished. We now want to turn the
                // refreshing back on.
                UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest(indexName);
                updateSettingsRequest.settings(ImmutableSettings.settingsBuilder().put("refresh_interval", "5s"));

                // submit to ElasticSearch
                this.manager.getClient()
                        .admin()
                        .indices()
                        .updateSettings(updateSettingsRequest)
                        .actionGet();
            }

            checkIndexImplications(indexName);

            LOGGER.debug("Refreshing ElasticSearch index: {}", indexName);
            this.manager.getClient()
                    .admin()
                    .indices()
                    .prepareRefresh(indexName)
                    .execute()
                    .actionGet();
        }
    }

    @Override
    public DatumStatusCounter getDatumStatusCounter() {
        DatumStatusCounter counters = new DatumStatusCounter();
        counters.incrementStatus(DatumStatus.SUCCESS, (int)this.totalOk.get());
        counters.incrementStatus(DatumStatus.FAIL, (int)this.totalFailed.get());
        return counters;
    }

    private synchronized void flushInternal() {
        // we do not have a working bulk request, we can just exit here.
        if (this.bulkRequest == null || this.currentBatchItems.get() == 0)
            return;

        // call the flush command.
        flush(this.bulkRequest, this.currentBatchItems.get(), this.currentBatchBytes.get());

        // reset the current batch statistics
        this.currentBatchItems.set(0);
        this.currentBatchBytes.set(0);

        // reset our bulk request builder
        this.bulkRequest = this.manager.getClient().prepareBulk();
    }

    private void checkForBackOff() {
        try {
            if (this.getTotalOutstanding() > WAITING_DOCS_LIMIT) {
                /****************************************************************************
                 * Author:
                 * Smashew
                 *
                 * Date:
                 * 2013-10-20
                 *
                 * Note:
                 * With the information that we have on hand. We need to develop a heuristic
                 * that will determine when the cluster is having a problem indexing records
                 * by telling it to pause and wait for it to catch back up. A
                 *
                 * There is an impact to us, the caller, whenever this happens as well. Items
                 * that are not yet fully indexed by the server sit in a queue, on the client
                 * that can cause the heap to overflow. This has been seen when re-indexing
                 * large amounts of data to a small cluster. The "deletes" + "indexes" can
                 * cause the server to have many 'outstandingItems" in queue. Running this
                 * software with large amounts of data, on a small cluster, can re-create
                 * this problem.
                 *
                 * DO NOT DELETE THESE LINES
                 ****************************************************************************/

                // wait for the flush to catch up. We are going to cap this at
                int count = 0;
                while (this.getTotalOutstanding() > WAITING_DOCS_LIMIT && count++ < 500)
                    Thread.sleep(10);

                if (this.getTotalOutstanding() > WAITING_DOCS_LIMIT)
                    LOGGER.warn("Even after back-off there are {} items still in queue.", this.getTotalOutstanding());
            }
        } catch (Exception e) {
            LOGGER.warn("We were broken from our loop: {}", e.getMessage());
        }
    }

    public void add(String indexName, String type, String id, String ts, String json) {

        // make sure that these are not null
        Preconditions.checkNotNull(indexName);
        Preconditions.checkNotNull(type);
        Preconditions.checkNotNull(json);

        IndexRequestBuilder indexRequestBuilder = manager.getClient().prepareIndex(indexName, type).setSource(json);

        // / They didn't specify an ID, so we will create one for them.
        if(id != null)
            indexRequestBuilder.setId(id);

        if(ts != null)
            indexRequestBuilder.setTimestamp(ts);

        add(indexRequestBuilder.request());
    }

    /**
     *  This function is trashed... needs to be fixed.
     *
    private synchronized void add(UpdateRequest request) {
        Preconditions.checkNotNull(request);
        checkAndCreateBulkRequest();

        checkIndexImplications(request.index());

        bulkRequest.add(request);
        try {
            Optional<Integer> size = Objects.firstNonNull(
                    Optional.fromNullable(request.doc().source().length()),
                    Optional.fromNullable(request.script().length()));
            trackItemAndBytesWritten(size.get().longValue());
        } catch (NullPointerException x) {
            trackItemAndBytesWritten(1000);
        }
    }
    */

    protected void add(IndexRequest request) {

        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(request.index());

        // If our queue is larger than our flush threshold, then we should flush the queue.
        synchronized (this) {
            checkIndexImplications(request.index());

            bulkRequest.add(request);

            this.currentBatchBytes.addAndGet(request.source().length());
            this.currentBatchItems.incrementAndGet();

            if ((this.currentBatchBytes.get() >= this.flushThresholdBytes) || (this.currentBatchItems.get() >= this.flushThresholdsRecords)) {
                // Flush the internal writer
                flushInternal();
            }
        }
    }

    private void checkIndexImplications(String indexName) {
        // this will be common if we have already verified the index.
        if (this.affectedIndexes.contains(indexName))
            return;

        // We need this to be safe across all writers that are currently being executed
        synchronized (ElasticsearchPersistWriter.class) {

            // create the index if it is missing
            createIndexIfMissing(indexName);

            // we haven't log this index.
            this.affectedIndexes.add(indexName);

            // Check to see if we are in 'veryLargeBulk' mode
            // if we aren't, exit early
            if (this.veryLargeBulk) {

                // They are in 'very large bulk' mode we want to turn off refreshing the index.
                // Create a request then add the setting to tell it to stop refreshing the interval
                UpdateSettingsRequest updateSettingsRequest = new UpdateSettingsRequest(indexName);
                updateSettingsRequest.settings(ImmutableSettings.settingsBuilder().put("refresh_interval", -1));

                // submit to ElasticSearch
                this.manager.getClient()
                        .admin()
                        .indices()
                        .updateSettings(updateSettingsRequest)
                        .actionGet();
            }
        }
    }

    public void createIndexIfMissing(String indexName) {
        // Synchronize this on a static class level
        if (!this.manager.getClient()
                .admin()
                .indices()
                .exists(new IndicesExistsRequest(indexName))
                .actionGet()
                .isExists())
        {
            // It does not exist... So we are going to need to create the index.
            // we are going to assume that the 'templates' that we have loaded into
            // elasticsearch are sufficient to ensure the index is being created properly.
            CreateIndexResponse response = this.manager.getClient().admin().indices().create(new CreateIndexRequest(indexName)).actionGet();

            if (response.isAcknowledged()) {
                LOGGER.info("Index Created: {}", indexName);
            } else {
                LOGGER.error("Index {} did not exist. While attempting to create the index from stored ElasticSearch Templates we were unable to get an acknowledgement.", indexName);
                LOGGER.error("Error Message: {}", response.toString());
                throw new RuntimeException("Unable to create index " + indexName);
            }
        }
    }

    /**
     *
    private Set<String> checkIds(Set<String> input, String index, String type) {

        IdsQueryBuilder idsFilterBuilder = new IdsQueryBuilder();

        for (String s : input)
            idsFilterBuilder.addIds(s);

        SearchRequestBuilder searchRequestBuilder = this.manager.getClient()
                .prepareSearch(index)
                .setTypes(type)
                .setQuery(idsFilterBuilder)
                .addField("_id")
                .setSize(input.size());

        SearchHits hits = searchRequestBuilder.execute()
                .actionGet()
                .getHits();

        Set<String> toReturn = new HashSet<String>();

        for (SearchHit hit : hits) {
            toReturn.add(hit.getId());
        }

        return toReturn;
    }
    */

    public void prepare(Object configurationObject) {
        this.veryLargeBulk = config.getBulk() == null ?
                Boolean.FALSE :
                config.getBulk();

        this.flushThresholdsRecords = config.getBatchSize() == null ?
                DEFAULT_BATCH_SIZE :
                (int)(config.getBatchSize().longValue());

        this.flushThresholdTime = config.getMaxTimeBetweenFlushMs() == null ?
                DEFAULT_MAX_WAIT :
                config.getMaxTimeBetweenFlushMs();

        this.flushThresholdBytes = config.getBatchBytes() == null ?
                DEFAULT_BULK_FLUSH_THRESHOLD :
                config.getBatchBytes();
    }

    private void flush(final BulkRequestBuilder bulkRequest, final Long sent, final Long sizeInBytes) {
        LOGGER.debug("Writing to ElasticSearch: Items[{}] Size[{} mb]", sent, MEGABYTE_FORMAT.format(sizeInBytes / (double) (1024 * 1024)));

        // record the proper statistics, and add it to our totals.
        this.totalSent.addAndGet(sent);
        this.batchesSent.incrementAndGet();

        try {
            bulkRequest.execute().addListener(new ActionListener<BulkResponse>() {
                public void onResponse(BulkResponse bulkItemResponses) {
                    batchesResponded.incrementAndGet();
                    updateTotals(bulkItemResponses, sent, sizeInBytes);
                }

                public void onFailure(Throwable throwable) {
                    batchesResponded.incrementAndGet();
                    throwable.printStackTrace();
                }
            });
        }
        catch(Throwable e) {
            LOGGER.error("There was an error sending the batch: {}", e.getMessage());
        }
    }

    private void updateTotals(final BulkResponse bulkItemResponses, final Long sent, final Long sizeInBytes) {
        long failed = 0;
        long passed = 0;
        long millis = bulkItemResponses.getTookInMillis();

        // keep track of the number of totalFailed and items that we have totalOk.
        for (BulkItemResponse resp : bulkItemResponses.getItems()) {
            if (resp == null || resp.isFailed())
                failed++;
            else
                passed++;
        }

        if (failed > 0)
            LOGGER.warn("Bulk Uploading had {} failures of {}", failed, sent);

        this.totalOk.addAndGet(passed);
        this.totalFailed.addAndGet(failed);
        this.totalSeconds.addAndGet(millis / 1000);
        this.totalSizeInBytes.addAndGet(sizeInBytes);

        if (sent != (passed + failed))
            LOGGER.error("Count MisMatch: Sent[{}] Passed[{}] Failed[{}]", sent, passed, failed);

        LOGGER.debug("Batch[{}mb {} items with {} failures in {}ms] - Total[{}mb {} items with {} failures in {}seconds] {} outstanding]",
                MEGABYTE_FORMAT.format(sizeInBytes / (double) (1024 * 1024)), NUMBER_FORMAT.format(passed), NUMBER_FORMAT.format(failed), NUMBER_FORMAT.format(millis),
                MEGABYTE_FORMAT.format((double) totalSizeInBytes.get() / (double) (1024 * 1024)), NUMBER_FORMAT.format(totalOk), NUMBER_FORMAT.format(totalFailed), NUMBER_FORMAT.format(totalSeconds), NUMBER_FORMAT.format(getTotalOutstanding()));
    }
}
