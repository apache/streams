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
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;
import org.elasticsearch.action.delete.DeleteRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class ElasticsearchPersistDeleter extends ElasticsearchPersistWriter implements StreamsPersistWriter {

    private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPersistDeleter.class);

    public ElasticsearchPersistDeleter() {
        super();
    }

    public ElasticsearchPersistDeleter(ElasticsearchWriterConfiguration config) {
        super(config);
    }

    @Override
    public void write(StreamsDatum streamsDatum) {

        if(streamsDatum == null || streamsDatum.getDocument() == null)
            return;

        LOGGER.debug("Delete Document: {}", streamsDatum.getDocument());

        Map<String, Object> metadata = streamsDatum.getMetadata();

        LOGGER.debug("Delete Metadata: {}", metadata);

        String index = getIndex(metadata, config);
        String type = getType(metadata, config);
        String id = getId(streamsDatum);

        try {
            delete(index, type, id);
        } catch (Throwable e) {
            LOGGER.warn("Unable to Delete Document from ElasticSearch: {}", e.getMessage());
        }
    }

    public void delete(String index, String type, String id) {
        DeleteRequest deleteRequest;

        Preconditions.checkNotNull(index);
        Preconditions.checkNotNull(id);
        Preconditions.checkNotNull(type);

        // They didn't specify an ID, so we will create one for them.
        deleteRequest = new DeleteRequest()
                .index(index)
                .type(type)
                .id(id);

        add(deleteRequest);

    }

    public void add(DeleteRequest request) {

        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(request.index());

        // If our queue is larger than our flush threshold, then we should flush the queue.
        synchronized (this) {
            checkIndexImplications(request.index());

            bulkRequest.add(request);

            currentBatchItems.incrementAndGet();

            checkForFlush();
        }

    }

}
