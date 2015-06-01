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

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.elasticsearch.action.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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

        if(streamsDatum == null || streamsDatum.getDocument() == null)
            return;

        LOGGER.debug("Update Document: {}", streamsDatum.getDocument());

        Map<String, Object> metadata = streamsDatum.getMetadata();

        LOGGER.debug("Update Metadata: {}", metadata);

        String index = ElasticsearchMetadataUtil.getIndex(metadata, config);
        String type = ElasticsearchMetadataUtil.getType(metadata, config);
        String id = ElasticsearchMetadataUtil.getId(streamsDatum);
        String parent = ElasticsearchMetadataUtil.getParent(streamsDatum);

        try {

            String docAsJson = docAsJson(streamsDatum.getDocument());

            LOGGER.debug("Attempt Update: ({},{},{},{}) {}", index, type, id, parent, docAsJson);

            update(index, type, id, parent, docAsJson);

        } catch (Throwable e) {
            LOGGER.warn("Unable to Update Document in ElasticSearch: {}", e.getMessage());
        }
    }

    public void update(String indexName, String type, String id, String parent, String json) {
        UpdateRequest updateRequest;

        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(json);

        // They didn't specify an ID, so we will create one for them.
        updateRequest = new UpdateRequest()
                .index(indexName)
                .type(type)
                .id(id)
                .doc(json);

        if(!Strings.isNullOrEmpty(parent)) {
            updateRequest = updateRequest.parent(parent);
            updateRequest = updateRequest.routing(parent);
        }

        // add fields
        //updateRequest.docAsUpsert(true);

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
