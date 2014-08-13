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
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.index.query.IdsQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
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

public class ElasticsearchPersistUpdater extends ElasticsearchPersistWriter implements StreamsPersistWriter {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPersistUpdater.class);

    public ElasticsearchPersistUpdater() {
        super();
    }

    public ElasticsearchPersistUpdater(ElasticsearchWriterConfiguration config) {
        super(config);
    }

    @Override
    public void write(StreamsDatum streamsDatum) {

        Preconditions.checkNotNull(streamsDatum);
        Preconditions.checkNotNull(streamsDatum.getDocument());
        Preconditions.checkNotNull(streamsDatum.getMetadata());
        Preconditions.checkNotNull(streamsDatum.getMetadata().get("id"));

        String index;
        String type;
        String id;
        String json;
        try {

            json = OBJECT_MAPPER.writeValueAsString(streamsDatum.getDocument());

            index = Optional.fromNullable(
                    (String) streamsDatum.getMetadata().get("index"))
                    .or(config.getIndex());
            type = Optional.fromNullable(
                    (String) streamsDatum.getMetadata().get("type"))
                    .or(config.getType());
            id = (String) streamsDatum.getMetadata().get("id");

            update(index, type, id, json);

        } catch (JsonProcessingException e) {
            LOGGER.warn("{} {}", e.getLocation(), e.getMessage());

        }
    }

    public void update(String indexName, String type, String id, String json) {
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

    public void add(UpdateRequest request) {

        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(request.index());

        // If our queue is larger than our flush threshold, then we should flush the queue.
        synchronized (this) {
            checkIndexImplications(request.index());

            bulkRequest.add(request);

            currentBatchBytes.addAndGet(request.doc().source().length());
            currentBatchItems.incrementAndGet();

            checkForFlush();
        }

    }

}
