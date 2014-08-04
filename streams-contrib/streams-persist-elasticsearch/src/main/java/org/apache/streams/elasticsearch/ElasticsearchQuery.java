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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.Lists;
import com.google.common.base.Objects;
import com.typesafe.config.Config;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.FilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ElasticsearchQuery implements Iterable<SearchHit>, Iterator<SearchHit>, Serializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchQuery.class);
    private static final int SCROLL_POSITION_NOT_INITIALIZED = -3;
    private static final Integer DEFAULT_BATCH_SIZE = 500;
    private static final String DEFAULT_SCROLL_TIMEOUT = "5m";

    private ElasticsearchClientManager elasticsearchClientManager;
    private ElasticsearchReaderConfiguration config;
    private List<String> indexes = Lists.newArrayList();
    private List<String> types = Lists.newArrayList();
    private String[] withfields;
    private String[] withoutfields;
    private DateTime startDate;
    private DateTime endDate;
    private int limit = 1000 * 1000 * 1000; // we are going to set the default limit very high to 1bil
    private boolean random = false;
    private int batchSize = 100;
    private String scrollTimeout = null;
    private org.elasticsearch.index.query.QueryBuilder queryBuilder;
    private org.elasticsearch.index.query.FilterBuilder filterBuilder;// These are private to help us manage the scroll
    private SearchRequestBuilder search;
    private SearchResponse scrollResp;
    private int scrollPositionInScroll = SCROLL_POSITION_NOT_INITIALIZED;
    private SearchHit next = null;
    private long totalHits = 0;
    private long totalRead = 0;

    private StreamsJacksonMapper mapper = StreamsJacksonMapper.getInstance();

    public ElasticsearchQuery() {
        Config config = StreamsConfigurator.config.getConfig("elasticsearch");
        this.config = ElasticsearchConfigurator.detectReaderConfiguration(config);
    }

    public ElasticsearchQuery(ElasticsearchReaderConfiguration config) {
        this.config = config;
        this.elasticsearchClientManager = new ElasticsearchClientManager(config);
        this.indexes.addAll(config.getIndexes());
        this.types.addAll(config.getTypes());
    }

    public long getHitCount() {
        return this.search == null ? 0 : this.totalHits;
    }

    public long getReadCount() {
        return this.totalRead;
    }

    public double getReadPercent() {
        return (double) this.getReadCount() / (double) this.getHitCount();
    }

    public long getRemainingCount() {
        return this.totalRead - this.totalHits;
    }

    public void setBatchSize(int batchSize) {
        this.batchSize = batchSize;
    }

    public void setScrollTimeout(String scrollTimeout) {
        this.scrollTimeout = scrollTimeout;
    }

    public void setQueryBuilder(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    public void setFilterBuilder(FilterBuilder filterBuilder) {
        this.filterBuilder = filterBuilder;
    }

    public void execute(Object o) {

        // If we haven't already set up the search, then set up the search.
        if (search == null) {

            search = elasticsearchClientManager.getClient()
                    .prepareSearch(indexes.toArray(new String[0]))
                    .setSearchType(SearchType.SCAN)
                    .setExplain(true)
                    .addField("*")
                    .setFetchSource(true)
                    .setSize(Objects.firstNonNull(batchSize, DEFAULT_BATCH_SIZE).intValue())
                    .setScroll(Objects.firstNonNull(scrollTimeout, DEFAULT_SCROLL_TIMEOUT))
                    .addField("_timestamp");

            String searchJson;
            if( config.getSearch() != null ) {
                LOGGER.debug("Have config in Reader: " + config.getSearch().toString());

                try {
                    searchJson = mapper.writeValueAsString(config.getSearch());
                    LOGGER.debug("Setting source: " + searchJson);
                    search = search.setExtraSource(searchJson);

                } catch (JsonProcessingException e) {
                    LOGGER.warn("Could not apply _search supplied by config", e.getMessage());
                }

                LOGGER.debug("Search Source is now " + search.toString());

            }


            if (this.queryBuilder != null)
                search = search.setQuery(this.queryBuilder);

            // If the types are null, then don't specify a type
            if (this.types != null && this.types.size() > 0)
                search = search.setTypes(types.toArray(new String[0]));

            // TODO: Replace when all clusters are upgraded past 0.90.4 so we can implement a RANDOM scroll.
            if (this.random)
                search = search.addSort(SortBuilders.scriptSort("random()", "number"));
        }

        // We don't have a scroll, we need to create a scroll
        if (scrollResp == null) {
            scrollResp = search.execute().actionGet();
            LOGGER.trace(search.toString());
        }
    }

    //Iterable methods
    @Override
    public Iterator<SearchHit> iterator() {
        return this;
    }

    //Iterator methods
    @Override
    public SearchHit next() {
        return this.next;
    }

    @Override
    public boolean hasNext() {
        calcNext();
        return hasRecords();
    }

    public void calcNext() {
        try {
            // We have exhausted our scroll create another scroll.
            if (scrollPositionInScroll == SCROLL_POSITION_NOT_INITIALIZED || scrollPositionInScroll >= scrollResp.getHits().getHits().length) {
                // reset the scroll position
                scrollPositionInScroll = 0;

                // get the next hits of the scroll
                scrollResp = elasticsearchClientManager.getClient()
                        .prepareSearchScroll(scrollResp.getScrollId())
                        .setScroll(Objects.firstNonNull(scrollTimeout, DEFAULT_SCROLL_TIMEOUT))
                        .execute()
                        .actionGet();

                this.totalHits = scrollResp.getHits().getTotalHits();
            }

            // If this scroll has 0 items then we set the scroll position to -1
            // letting the iterator know that we are done.
            if (scrollResp.getHits().getTotalHits() == 0 || scrollResp.getHits().getHits().length == 0)
                scrollPositionInScroll = -1;
            else {
                // get the next record
                next = scrollResp.getHits().getAt(scrollPositionInScroll);

                // Increment our counters
                scrollPositionInScroll += 1;
                totalRead += 1;
            }
        } catch (Exception e) {
            LOGGER.error("Unexpected scrolling error: {}", e.getMessage());
            scrollPositionInScroll = -1;
            next = null;
        }
    }

    public void remove() {
    }

    public void cleanUp() {
    }

    protected boolean isCompleted() {
        return totalRead >= this.limit && hasRecords();
    }

    protected boolean hasRecords() {
        return scrollPositionInScroll != -1 && (!(this.totalRead > this.limit));
    }

}
