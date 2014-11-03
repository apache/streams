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

package org.apache.streams.elasticsearch.processor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.elasticsearch.ElasticsearchClientManager;
import org.apache.streams.elasticsearch.ElasticsearchConfiguration;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.exists.indices.IndicesExistsRequest;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequestBuilder;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.percolate.PercolateRequestBuilder;
import org.elasticsearch.action.percolate.PercolateResponse;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.QueryStringQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

/**
 * References:
 * Some helpful references to help
 * Purpose              URL
 * -------------        ----------------------------------------------------------------
 * [Status Codes]       http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 * [Test Cases]         http://greenbytes.de/tech/tc/httpredirects/
 * [t.co behavior]      https://dev.twitter.com/docs/tco-redirection-behavior
 */

public class PercolateTagProcessor implements StreamsProcessor {

    public static final String STREAMS_ID = "PercolateTagProcessor";
    private final static Logger LOGGER = LoggerFactory.getLogger(PercolateTagProcessor.class);
    private final static String DEFAULT_PERCOLATE_FIELD = "_all";

    private ObjectMapper mapper;

    protected Queue<StreamsDatum> inQueue;
    protected Queue<StreamsDatum> outQueue;

    public String TAGS_EXTENSION = "tags";

    private ElasticsearchWriterConfiguration config;
    private ElasticsearchClientManager manager;
    private BulkRequestBuilder bulkBuilder;
    protected String usePercolateField;

    public PercolateTagProcessor(ElasticsearchWriterConfiguration config) {
        this(config, DEFAULT_PERCOLATE_FIELD);
    }

    public PercolateTagProcessor(ElasticsearchWriterConfiguration config, String defaultPercolateField) {
        this.config = config;
        this.usePercolateField = defaultPercolateField;
    }

    public ElasticsearchClientManager getManager() {
        return manager;
    }

    public void setManager(ElasticsearchClientManager manager) {
        this.manager = manager;
    }

    public ElasticsearchConfiguration getConfig() {
        return config;
    }

    public void setConfig(ElasticsearchWriterConfiguration config) {
        this.config = config;
    }

    public Queue<StreamsDatum> getProcessorOutputQueue() {
        return outQueue;
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

        List<StreamsDatum> result = Lists.newArrayList();

        String json;
        ObjectNode node;
        // first check for valid json
        if (entry.getDocument() instanceof String) {
            json = (String) entry.getDocument();
            try {
                node = (ObjectNode) mapper.readTree(json);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        } else if (entry.getDocument() instanceof ObjectNode) {
            node = (ObjectNode) entry.getDocument();
            try {
                json = mapper.writeValueAsString(node);
            } catch (JsonProcessingException e) {
                LOGGER.warn("Invalid datum: ", node);
                return null;
            }
        } else {
            LOGGER.warn("Incompatible document type: ", entry.getDocument().getClass());
            return null;
        }

        StringBuilder percolateRequestJson = new StringBuilder();
        percolateRequestJson.append("{ \"doc\": ");
        percolateRequestJson.append(json);
        //percolateRequestJson.append("{ \"content\" : \"crazy good shit\" }");
        percolateRequestJson.append("}");

        PercolateRequestBuilder request;
        PercolateResponse response;

        try {
            LOGGER.trace("Percolate request json: {}", percolateRequestJson.toString());
            request = manager.getClient().preparePercolate().setIndices(config.getIndex()).setDocumentType(config.getType()).setSource(percolateRequestJson.toString());
            LOGGER.trace("Percolate request: {}", mapper.writeValueAsString(request.request()));
            response = request.execute().actionGet();
            LOGGER.trace("Percolate response: {} matches", response.getMatches().length);
        } catch (Exception e) {
            LOGGER.warn("Percolate exception: {}", e.getMessage());
            return null;
        }

        ArrayNode tagArray = JsonNodeFactory.instance.arrayNode();

        Iterator<PercolateResponse.Match> matchIterator = response.iterator();
        while(matchIterator.hasNext()) {
            tagArray.add(matchIterator.next().getId().string());
        }

        LOGGER.trace("Percolate matches: {}", tagArray);

        Activity activity = mapper.convertValue(node, Activity.class);

        appendMatches(tagArray, activity);

        entry.setDocument(activity);

        result.add(entry);

        return result;

    }

    protected void appendMatches(ArrayNode tagArray, Activity activity) {
        Map<String, Object> extensions = ActivityUtil.ensureExtensions(activity);

        extensions.put(TAGS_EXTENSION, tagArray);

        activity.setAdditionalProperty(ActivityUtil.EXTENSION_PROPERTY, extensions);
    }

    @Override
    public void prepare(Object o) {

        Preconditions.checkNotNull(config);
        Preconditions.checkNotNull(config.getTags());
        Preconditions.checkArgument(config.getTags().getAdditionalProperties().size() > 0);

        // consider using mapping to figure out what fields are included in _all
        //manager.getClient().admin().indices().prepareGetMappings(config.getIndex()).get().getMappings().get(config.getType()).;

        mapper = StreamsJacksonMapper.getInstance();
        manager = new ElasticsearchClientManager(config);
        bulkBuilder = manager.getClient().prepareBulk();
        createIndexIfMissing(config.getIndex());
        if( config.getReplaceTags() == true ) {
            deleteOldQueries(config.getIndex());
        }
        for (String tag : config.getTags().getAdditionalProperties().keySet()) {
            String query = (String) config.getTags().getAdditionalProperties().get(tag);
            PercolateQueryBuilder queryBuilder = new PercolateQueryBuilder(tag, query, this.usePercolateField);
            addPercolateRule(queryBuilder, config.getIndex());
        }
        if (writePercolateRules() == true)
            LOGGER.info("wrote " + bulkBuilder.numberOfActions() + " tags to " + config.getIndex() + " _percolator");
        else
            LOGGER.error("FAILED writing " + bulkBuilder.numberOfActions() + " tags to " + config.getIndex() + " _percolator");

    }

    @Override
    public void cleanUp() {
        if( config.getCleanupTags() == true )
            deleteOldQueries(config.getIndex());
        manager.getClient().close();
    }

    public int numOfPercolateRules() {
        return this.bulkBuilder.numberOfActions();
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

    public void addPercolateRule(PercolateQueryBuilder builder, String index) {
        this.bulkBuilder.add(manager.getClient().prepareIndex(index, ".percolator", builder.getId())
                .setSource(builder.getSource()));
    }

    /**
     *
     * @return returns true if all rules were addded. False indicates one or more rules have failed.
     */
    public boolean writePercolateRules() {
        if(this.numOfPercolateRules() < 0) {
            throw new RuntimeException("No Rules Have been added!");
        }
        BulkResponse response = this.bulkBuilder.execute().actionGet();
        for(BulkItemResponse r : response.getItems()) {
            if(r.isFailed()) {
                System.out.println(r.getId()+"\t"+r.getFailureMessage());
            }
        }
        return !response.hasFailures();
    }

    /**
     *
     * @param ids
     * @param index
     * @return  Returns true if all of the old tags were removed. False indicates one or more tags were not removed.
     */
    public boolean removeOldTags(Set<String> ids, String index) {
        if(ids.size() == 0) {
            return false;
        }
        BulkRequestBuilder bulk = manager.getClient().prepareBulk();
        for(String id : ids) {
            bulk.add(manager.getClient().prepareDelete("_percolator", index, id));
        }
        return !bulk.execute().actionGet().hasFailures();
    }

    public Set<String> getActivePercolateTags(String index) {
        Set<String> tags = new HashSet<String>();
        SearchRequestBuilder searchBuilder = manager.getClient().prepareSearch("*").setIndices(index).setTypes(".percolator").setSize(1000);
        SearchResponse response = searchBuilder.setQuery(QueryBuilders.matchAllQuery()).execute().actionGet();
        SearchHits hits = response.getHits();
        for(SearchHit hit : hits.getHits()) {
            tags.add(hit.id());
        }
        return tags;
    }

    /**
     *
     * @param index
     * @return
     */
    public boolean deleteOldQueries(String index) {
        Set<String> tags = getActivePercolateTags(index);
        if(tags.size() == 0) {
            LOGGER.warn("No active tags were found in _percolator for index : {}", index);
            return false;
        }
        LOGGER.info("Deleting {} tags.", tags.size());
        BulkRequestBuilder bulk = manager.getClient().prepareBulk();
        for(String tag : tags) {
            bulk.add(manager.getClient().prepareDelete().setType(".percolator").setIndex(index).setId(tag));
        }
        BulkResponse response =bulk.execute().actionGet();
        return !response.hasFailures();
    }

    public static class PercolateQueryBuilder {

        private QueryStringQueryBuilder queryBuilder;
        private String id;

        public PercolateQueryBuilder(String id, String query, String defaultPercolateField) {
            this.id = id;
            this.queryBuilder = QueryBuilders.queryString(query);
            this.queryBuilder.defaultField(defaultPercolateField);
        }

        public String getId() {
            return this.id;
        }

        public String getSource() {
            return "{ \n\"query\" : "+this.queryBuilder.toString()+"\n}";
        }

    }

    public enum FilterLevel {
        MUST, SHOULD, MUST_NOT
    }
}