package org.apache.streams.elasticsearch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
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
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

//import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;

public class ElasticsearchPersistUpdater extends ElasticsearchPersistWriter implements StreamsPersistWriter, Flushable, Closeable {
    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPersistUpdater.class);
    private final static NumberFormat MEGABYTE_FORMAT = new DecimalFormat("#.##");
    private final static NumberFormat NUMBER_FORMAT = new DecimalFormat("###,###,###,###");

    protected ElasticsearchClientManager manager;
    protected Client client;
    private String parentID = null;
    protected BulkRequestBuilder bulkRequest;
    private OutputStreamWriter currentWriter = null;

    protected String index = null;
    protected String type = null;
    private int batchSize = 50;
    private int totalRecordsWritten = 0;
    private boolean veryLargeBulk = false;  // by default this setting is set to false

    private final static Long DEFAULT_BULK_FLUSH_THRESHOLD = 5l * 1024l * 1024l;
    private static final long WAITING_DOCS_LIMIT = 10000;

    public volatile long flushThresholdSizeInBytes = DEFAULT_BULK_FLUSH_THRESHOLD;

    private volatile int totalSent = 0;
    private volatile int totalSeconds = 0;
    private volatile int totalOk = 0;
    private volatile int totalFailed = 0;
    private volatile int totalBatchCount = 0;
    private volatile long totalSizeInBytes = 0;

    private volatile long batchSizeInBytes = 0;
    private volatile int batchItemsSent = 0;

    public void setIndex(String index) {
        this.index = index;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setVeryLargeBulk(boolean veryLargeBulk) {
        this.veryLargeBulk = veryLargeBulk;
    }

    private final List<String> affectedIndexes = new ArrayList<String>();

    public int getTotalOutstanding() {
        return this.totalSent - (this.totalFailed + this.totalOk);
    }

    public long getFlushThresholdSizeInBytes() {
        return flushThresholdSizeInBytes;
    }

    public int getTotalSent() {
        return totalSent;
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    public int getTotalOk() {
        return totalOk;
    }

    public int getTotalFailed() {
        return totalFailed;
    }

    public int getTotalBatchCount() {
        return totalBatchCount;
    }

    public long getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    public long getBatchSizeInBytes() {
        return batchSizeInBytes;
    }

    public int getBatchItemsSent() {
        return batchItemsSent;
    }

    public List<String> getAffectedIndexes() {
        return this.affectedIndexes;
    }

    public void setFlushThresholdSizeInBytes(long sizeInBytes) {
        this.flushThresholdSizeInBytes = sizeInBytes;
    }

    Thread task;

    protected volatile Queue<StreamsDatum> persistQueue;

    private ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    private ElasticsearchConfiguration config;

    public ElasticsearchPersistUpdater() {
        Config config = StreamsConfigurator.config.getConfig("elasticsearch");
        this.config = ElasticsearchConfigurator.detectConfiguration(config);
    }

    public ElasticsearchPersistUpdater(ElasticsearchConfiguration config) {
        this.config = config;
    }

    private static final int BYTES_IN_MB = 1024 * 1024;
    private static final int BYTES_BEFORE_FLUSH = 5 * BYTES_IN_MB;
    private volatile int totalByteCount = 0;
    private volatile int byteCount = 0;

    public boolean isConnected() {
        return (client != null);
    }

    @Override
    public void write(StreamsDatum streamsDatum) {

        Preconditions.checkNotNull(streamsDatum);
        Preconditions.checkNotNull(streamsDatum.getDocument());
        Preconditions.checkNotNull(streamsDatum.getMetadata());
        Preconditions.checkNotNull(streamsDatum.getMetadata().get("id"));

        String id;
        String json;
        try {

            json = mapper.writeValueAsString(streamsDatum.getDocument());

            id = (String) streamsDatum.getMetadata().get("id");

            add(index, type, id, json);

        } catch (JsonProcessingException e) {
            LOGGER.warn("{} {}", e.getLocation(), e.getMessage());

        }
    }

    public void start() {

        manager = new ElasticsearchClientManager(config);
        client = manager.getClient();

        LOGGER.info(client.toString());
    }

    public void cleanUp() {

        try {
            flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        close();
    }

    @Override
    public void close() {
        try {
            // before they close, check to ensure that
            this.flush();

            int count = 0;
            // We are going to give it 5 minutes.
            while (this.getTotalOutstanding() > 0 && count++ < 20 * 60 * 5)
                Thread.sleep(50);

            if (this.getTotalOutstanding() > 0) {
                LOGGER.error("We never cleared our buffer");
            }


            for (String indexName : this.getAffectedIndexes()) {
                createIndexIfMissing(indexName);

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

            LOGGER.info("Closed: Wrote[{} of {}] Failed[{}]", this.getTotalOk(), this.getTotalSent(), this.getTotalFailed());

        } catch (Exception e) {
            // this line of code should be logically unreachable.
            LOGGER.warn("This is unexpected: {}", e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void flush() throws IOException {
        flushInternal();
    }

    public void flushInternal() {
        synchronized (this) {
            // we do not have a working bulk request, we can just exit here.
            if (this.bulkRequest == null || batchItemsSent == 0)
                return;

            // call the flush command.
            flush(this.bulkRequest, batchItemsSent, batchSizeInBytes);

            // null the flush request, this will be created in the 'add' function below
            this.bulkRequest = null;

            // record the proper statistics, and add it to our totals.
            this.totalSizeInBytes += this.batchSizeInBytes;
            this.totalSent += batchItemsSent;

            // reset the current batch statistics
            this.batchSizeInBytes = 0;
            this.batchItemsSent = 0;

            try {
                int count = 0;
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
                    while (this.getTotalOutstanding() > WAITING_DOCS_LIMIT && count++ < 500)
                        Thread.sleep(10);

                    if (this.getTotalOutstanding() > WAITING_DOCS_LIMIT)
                        LOGGER.warn("Even after back-off there are {} items still in queue.", this.getTotalOutstanding());
                }
            } catch (Exception e) {
                LOGGER.info("We were broken from our loop: {}", e.getMessage());
            }
        }
    }

    private void flush(final BulkRequestBuilder bulkRequest, final Integer thisSent, final Long thisSizeInBytes) {
        bulkRequest.execute().addListener(new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                if (bulkItemResponses.hasFailures())
                    LOGGER.warn("Bulk Uploading had totalFailed: " + bulkItemResponses.buildFailureMessage());

                long thisFailed = 0;
                long thisOk = 0;
                long thisMillis = bulkItemResponses.getTookInMillis();

                // keep track of the number of totalFailed and items that we have totalOk.
                for (BulkItemResponse resp : bulkItemResponses.getItems()) {
                    if (resp.isFailed())
                        thisFailed++;
                    else
                        thisOk++;
                }

                totalOk += thisOk;
                totalFailed += thisFailed;
                totalSeconds += (thisMillis / 1000);

                if (thisSent != (thisOk + thisFailed))
                    LOGGER.error("We sent more items than this");

                LOGGER.debug("Batch[{}mb {} items with {} failures in {}ms] - Total[{}mb {} items with {} failures in {}seconds] {} outstanding]",
                        MEGABYTE_FORMAT.format((double) thisSizeInBytes / (double) (1024 * 1024)), NUMBER_FORMAT.format(thisOk), NUMBER_FORMAT.format(thisFailed), NUMBER_FORMAT.format(thisMillis),
                        MEGABYTE_FORMAT.format((double) totalSizeInBytes / (double) (1024 * 1024)), NUMBER_FORMAT.format(totalOk), NUMBER_FORMAT.format(totalFailed), NUMBER_FORMAT.format(totalSeconds), NUMBER_FORMAT.format(getTotalOutstanding()));
            }

            @Override
            public void onFailure(Throwable e) {
                LOGGER.error("Error bulk loading: {}", e.getMessage());
                e.printStackTrace();
            }
        });

        this.notify();
    }

    public void add(String indexName, String type, String id, String json) {
        UpdateRequest updateRequest;

        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(json);

        // They didn't specify an ID, so we will create one for them.
        updateRequest = new UpdateRequest()
                .index(indexName)
                .type(type)
                .id(id)
                .doc(json);

        add(updateRequest);

    }

    public void add(UpdateRequest updateRequest) {
        Preconditions.checkNotNull(updateRequest);
        synchronized (this) {
            checkAndCreateBulkRequest();
            checkIndexImplications(updateRequest.index());
            bulkRequest.add(updateRequest);
            try {
                Optional<Integer> size = Objects.firstNonNull(
                        Optional.fromNullable(updateRequest.doc().source().length()),
                        Optional.fromNullable(updateRequest.script().length()));
                trackItemAndBytesWritten(size.get().longValue());
            } catch (NullPointerException x) {
                trackItemAndBytesWritten(1000);
            }
        }
    }

    private void trackItemAndBytesWritten(long sizeInBytes) {
        batchItemsSent++;
        batchSizeInBytes += sizeInBytes;

        // If our queue is larger than our flush threashold, then we should flush the queue.
        if (batchSizeInBytes > flushThresholdSizeInBytes)
            flushInternal();
    }

    private void checkAndCreateBulkRequest() {
        // Synchronize to ensure that we don't lose any records
        synchronized (this) {
            if (bulkRequest == null)
                bulkRequest = this.manager.getClient().prepareBulk();
        }
    }

    private void checkIndexImplications(String indexName) {

        // check to see if we have seen this index before.
        if (this.affectedIndexes.contains(indexName))
            return;

        // we haven't log this index.
        this.affectedIndexes.add(indexName);

        // Check to see if we are in 'veryLargeBulk' mode
        // if we aren't, exit early
        if (!this.veryLargeBulk)
            return;


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

    public void createIndexIfMissing(String indexName) {
        if (!this.manager.getClient()
                .admin()
                .indices()
                .exists(new IndicesExistsRequest(indexName))
                .actionGet()
                .isExists()) {
            // It does not exist... So we are going to need to create the index.
            // we are going to assume that the 'templates' that we have loaded into
            // elasticsearch are sufficient to ensure the index is being created properly.
            CreateIndexResponse response = this.manager.getClient().admin().indices().create(new CreateIndexRequest(indexName)).actionGet();

            if (response.isAcknowledged()) {
                LOGGER.info("Index {} did not exist. The index was automatically created from the stored ElasticSearch Templates.", indexName);
            } else {
                LOGGER.error("Index {} did not exist. While attempting to create the index from stored ElasticSearch Templates we were unable to get an acknowledgement.", indexName);
                LOGGER.error("Error Message: {}", response.toString());
                throw new RuntimeException("Unable to create index " + indexName);
            }
        }
    }

    public void add(String indexName, String type, Map<String, Object> toImport) {
        for (String id : toImport.keySet())
            add(indexName, type, id, (String) toImport.get(id));
    }

    private void checkThenAddBatch(String index, String type, Map<String, String> workingBatch) {
        Set<String> invalidIDs = checkIds(workingBatch.keySet(), index, type);

        for (String toAddId : workingBatch.keySet())
            if (!invalidIDs.contains(toAddId))
                add(index, type, toAddId, workingBatch.get(toAddId));

        LOGGER.info("Adding Batch: {} -> {}", workingBatch.size(), invalidIDs.size());
    }


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

    @Override
    public void prepare(Object configurationObject) {
        start();
    }

}
