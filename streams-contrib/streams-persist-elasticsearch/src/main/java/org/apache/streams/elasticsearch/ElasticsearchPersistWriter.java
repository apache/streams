package org.apache.streams.elasticsearch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.typesafe.config.Config;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ElasticsearchPersistWriter implements StreamsPersistWriter, Flushable, Closeable, DatumStatusCountable {
    public static final String STREAMS_ID = "ElasticsearchPersistWriter";

    public volatile long flushThresholdSizeInBytes = DEFAULT_BULK_FLUSH_THRESHOLD;

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPersistWriter.class);
    private static final NumberFormat MEGABYTE_FORMAT = new DecimalFormat("#.##");
    private static final NumberFormat NUMBER_FORMAT = new DecimalFormat("###,###,###,###");
    private static final Long DEFAULT_BULK_FLUSH_THRESHOLD = 5l * 1024l * 1024l;
    private static final long WAITING_DOCS_LIMIT = 10000;
    private static final int BYTES_IN_MB = 1024 * 1024;
    private static final int BYTES_BEFORE_FLUSH = 5 * BYTES_IN_MB;
    private static final long DEFAULT_MAX_WAIT = 10000;
    private static final int DEFAULT_BATCH_SIZE = 100;

    private final List<String> affectedIndexes = new ArrayList<String>();
    private final ScheduledExecutorService backgroundFlushTask = Executors.newSingleThreadScheduledExecutor();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    private ObjectMapper mapper = new StreamsJacksonMapper();
    private ElasticsearchClientManager manager;
    private ElasticsearchWriterConfiguration config;
    private Client client;
    private String parentID = null;
    private BulkRequestBuilder bulkRequest;
    private OutputStreamWriter currentWriter = null;
    private int batchSize;
    private long maxTimeBetweenFlushMs;
    private boolean veryLargeBulk = false;  // by default this setting is set to false

    protected Thread task;

    protected volatile Queue<StreamsDatum> persistQueue;

    private volatile int currentItems = 0;
    private volatile int totalSent = 0;
    private volatile int totalSeconds = 0;
    private volatile int totalAttempted = 0;
    private volatile int totalOk = 0;
    private volatile int totalFailed = 0;
    private volatile int totalBatchCount = 0;
    private volatile int totalRecordsWritten = 0;
    private volatile long totalSizeInBytes = 0;
    private volatile long batchSizeInBytes = 0;
    private volatile int batchItemsSent = 0;
    private volatile int totalByteCount = 0;
    private volatile int byteCount = 0;
    private volatile AtomicLong lastWrite = new AtomicLong(System.currentTimeMillis());

    public ElasticsearchPersistWriter() {
        Config config = StreamsConfigurator.config.getConfig("elasticsearch");
        this.config = ElasticsearchConfigurator.detectWriterConfiguration(config);
    }

    public ElasticsearchPersistWriter(ElasticsearchWriterConfiguration config) {
        this.config = config;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setVeryLargeBulk(boolean veryLargeBulk) {
        this.veryLargeBulk = veryLargeBulk;
    }

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

    public long getMaxTimeBetweenFlushMs() {
        return maxTimeBetweenFlushMs;
    }

    public void setMaxTimeBetweenFlushMs(long maxTimeBetweenFlushMs) {
        this.maxTimeBetweenFlushMs = maxTimeBetweenFlushMs;
    }

    public boolean isConnected() {
        return (client != null);
    }

    @Override
    public void write(StreamsDatum streamsDatum) {

        String json;
        try {
            String id = streamsDatum.getId();
            if (streamsDatum.getDocument() instanceof String)
                json = streamsDatum.getDocument().toString();
            else {
                json = mapper.writeValueAsString(streamsDatum.getDocument());
            }

            add(config.getIndex(), config.getType(), id, json);

        } catch (Exception e) {
            LOGGER.warn("{} {}", e.getMessage());
            e.printStackTrace();
        }
    }

    public void cleanUp() {

        try {
            flush();
            backgroundFlushTask.shutdownNow();
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

    @Override
    public DatumStatusCounter getDatumStatusCounter() {
        DatumStatusCounter counters = new DatumStatusCounter();
        counters.incrementAttempt(this.batchItemsSent);
        counters.incrementStatus(DatumStatus.SUCCESS, this.totalOk);
        counters.incrementStatus(DatumStatus.FAIL, this.totalFailed);
        return counters;
    }

    public void start() {
        backgroundFlushTask.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                LOGGER.debug("Checking to see if data needs to be flushed");
                long time = System.currentTimeMillis() - lastWrite.get();
                if (time > maxTimeBetweenFlushMs && batchItemsSent > 0) {
                    LOGGER.debug("Background Flush task determined {} are waiting to be flushed.  It has been {} since the last write to ES", batchItemsSent, time);
                    flushInternal();
                }
            }
        }, 0, maxTimeBetweenFlushMs * 2, TimeUnit.MILLISECONDS);
        manager = new ElasticsearchClientManager(config);
        client = manager.getClient();

        LOGGER.info(client.toString());
    }

    public void flushInternal() {
        lock.writeLock().lock();
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
        } finally {
            lock.writeLock().unlock();
        }

    }

    public void add(String indexName, String type, String json) {
        add(indexName, type, null, json);
    }

    public void add(String indexName, String type, String id, String json) {
        IndexRequest indexRequest;

        // They didn't specify an ID, so we will create one for them.
        if (id == null)
            indexRequest = new IndexRequest(indexName, type);
        else
            indexRequest = new IndexRequest(indexName, type, id);

        indexRequest.source(json);

        // If there is a parentID that is associated with this bulk, then we are
        // going to have to parse the raw JSON and attempt to dereference
        // what the parent document should be
        if (parentID != null) {
            try {
                // The JSONObject constructor can throw an exception, it is called
                // out explicitly here so we can catch it.
                indexRequest = indexRequest.parent(new JSONObject(json).getString(parentID));
            } catch (JSONException e) {
                LOGGER.warn("Malformed JSON, cannot grab parentID: {}@{}[{}]: {}", id, indexName, type, e.getMessage());
                totalFailed++;
            }
        }
        add(indexRequest);
    }

    public void add(UpdateRequest updateRequest) {
        Preconditions.checkNotNull(updateRequest);
        lock.writeLock().lock();
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
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void add(IndexRequest indexRequest) {
        lock.writeLock().lock();
        checkAndCreateBulkRequest();
        checkIndexImplications(indexRequest.index());
        bulkRequest.add(indexRequest);
        try {
            trackItemAndBytesWritten(indexRequest.source().length());
        } catch (NullPointerException x) {
            LOGGER.warn("NPE adding/sizing indexrequest");
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void trackItemAndBytesWritten(long sizeInBytes)
    {
        currentItems++;
        batchItemsSent++;
        batchSizeInBytes += sizeInBytes;

        // If our queue is larger than our flush threashold, then we should flush the queue.
        if( (batchSizeInBytes > flushThresholdSizeInBytes) ||
                (currentItems >= batchSize) ) {
            flushInternal();
            this.currentItems = 0;
        }
    }

    private void checkIndexImplications(String indexName)
    {

        // check to see if we have seen this index before.
        if(this.affectedIndexes.contains(indexName))
            return;

        // we haven't log this index.
        this.affectedIndexes.add(indexName);

        // Check to see if we are in 'veryLargeBulk' mode
        // if we aren't, exit early
        if(!this.veryLargeBulk)
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
        mapper = StreamsJacksonMapper.getInstance();
        veryLargeBulk = config.getBulk() == null ? Boolean.FALSE : config.getBulk();
        batchSize = config.getBatchSize() == null ? DEFAULT_BATCH_SIZE : (int)(config.getBatchSize().longValue());
        maxTimeBetweenFlushMs = config.getMaxTimeBetweenFlushMs() == null ? DEFAULT_MAX_WAIT : config.getMaxTimeBetweenFlushMs().longValue();
        start();
    }
    
    /**
     * This method is to ONLY be called by flushInternal otherwise the counts will be off.
     * @param bulkRequest
     * @param thisSent
     * @param thisSizeInBytes
     */
    private void flush(final BulkRequestBuilder bulkRequest, final Integer thisSent, final Long thisSizeInBytes) {
        final Object messenger = new Object();
        LOGGER.debug("Attempting to write {} items to ES", thisSent);
        bulkRequest.execute().addListener(new ActionListener<BulkResponse>() {
            @Override
            public void onResponse(BulkResponse bulkItemResponses) {
                lastWrite.set(System.currentTimeMillis());

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

                totalAttempted += thisSent;
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
    }



    private void checkAndCreateBulkRequest() {
        // Synchronize to ensure that we don't lose any records
        lock.writeLock().lock();
        try {
            if (bulkRequest == null)
                bulkRequest = this.manager.getClient().prepareBulk();
        } finally {
            lock.writeLock().unlock();
        }
    }

}

