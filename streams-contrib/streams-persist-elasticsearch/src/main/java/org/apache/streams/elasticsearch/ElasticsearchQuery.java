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

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.script.Script;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.sort.SortBuilders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Helper for building, querying, and paging an elasticsearch query.
 */
public class ElasticsearchQuery implements Iterable<SearchHit>, Iterator<SearchHit>, Serializable {

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchQuery.class);
  private static final int SCROLL_POSITION_NOT_INITIALIZED = -3;

  private ElasticsearchClientManager elasticsearchClientManager;
  private ElasticsearchReaderConfiguration config;
  private List<String> indexes = new ArrayList<>();
  private List<String> types = new ArrayList<>();
  private int limit = 1000 * 1000 * 1000; // we are going to set the default limit very high to 1bil
  private int batchSize = 100;
  private String scrollTimeout = "5m";
  private org.elasticsearch.index.query.QueryBuilder queryBuilder;
  private SearchRequestBuilder search;
  private SearchResponse scrollResp;
  private int scrollPositionInScroll = SCROLL_POSITION_NOT_INITIALIZED;
  private SearchHit next = null;
  private long totalHits = 0;
  private long totalRead = 0;

  private StreamsJacksonMapper mapper = StreamsJacksonMapper.getInstance();

  /**
   * ElasticsearchQuery constructor - resolves ElasticsearchReaderConfiguration from JVM 'elasticsearch'.
   */
  public ElasticsearchQuery() {
    this(new ComponentConfigurator<>(ElasticsearchReaderConfiguration.class)
        .detectConfiguration(StreamsConfigurator.getConfig().getConfig("elasticsearch")));
  }

  /**
   * ElasticsearchQuery constructor - uses provided ElasticsearchReaderConfiguration.
   */
  public ElasticsearchQuery(ElasticsearchReaderConfiguration config) {
    this.config = config;
    this.elasticsearchClientManager = new ElasticsearchClientManager(config);
    this.indexes.addAll(config.getIndexes());
    this.types.addAll(config.getTypes());
    this.scrollTimeout = config.getScrollTimeout();
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

  /**
   * execute ElasticsearchQuery.
   * @param obj deprecated
   */
  public void execute(Object obj) {

    // If we haven't already set up the search, then set up the search.
    if (search == null) {

      search = elasticsearchClientManager.getClient()
          .prepareSearch(indexes.toArray(new String[0]))
          .setSearchType(SearchType.SCAN)
          .setExplain(true)
          .addField("*")
          .setFetchSource(true)
          .setSize(batchSize)
          .setScroll(scrollTimeout)
          .addField("_timestamp");

      LOGGER.debug("Search source: " + search.toString());

      String searchJson;
      if ( config.getSearch() != null ) {
        LOGGER.debug("Have config in Reader: " + config.getSearch().toString());

        try {
          searchJson = mapper.writeValueAsString(config.getSearch());
          LOGGER.debug("Extra source: " + searchJson);
          search = search.setExtraSource(searchJson);

        } catch (JsonProcessingException ex) {
          LOGGER.warn("Could not apply _search supplied by config", ex.getMessage());
        }


      }

      LOGGER.debug("Final Search: " + search.internalBuilder().toString());

      if (this.queryBuilder != null) {
        search = search.setQuery(this.queryBuilder);
      }

      // If the types are null, then don't specify a type
      if (this.types != null && this.types.size() > 0) {
        search = search.setTypes(types.toArray(new String[0]));
      }

      // TODO: Replace when all clusters are upgraded past 0.90.4 so we can implement a RANDOM scroll.
      boolean random = false;
      if (random) {
        search = search.addSort(SortBuilders.scriptSort(new Script("random()"), "number"));
      }
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

  /**
   * shift to next page of scroll.
   */
  public void calcNext() {
    try {
      // We have exhausted our scroll create another scroll.
      if (scrollPositionInScroll == SCROLL_POSITION_NOT_INITIALIZED || scrollPositionInScroll >= scrollResp.getHits().getHits().length) {
        // reset the scroll position
        scrollPositionInScroll = 0;

        // get the next hits of the scroll
        scrollResp = elasticsearchClientManager.getClient()
            .prepareSearchScroll(scrollResp.getScrollId())
            .setScroll(scrollTimeout)
            .execute()
            .actionGet();

        this.totalHits = scrollResp.getHits().getTotalHits();
      }

      // If this scroll has 0 items then we set the scroll position to -1
      // letting the iterator know that we are done.
      if (scrollResp.getHits().getTotalHits() == 0 || scrollResp.getHits().getHits().length == 0) {
        scrollPositionInScroll = -1;
      } else {
        // get the next record
        next = scrollResp.getHits().getAt(scrollPositionInScroll);

        // Increment our counters
        scrollPositionInScroll += 1;
        totalRead += 1;
      }
    } catch (Exception ex) {
      LOGGER.error("Unexpected scrolling error: {}", ex.getMessage());
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
