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

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsPersistWriter;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.update.UpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;

/**
 * ElasticsearchPersistUpdater updates documents to elasticsearch.
 */
public class ElasticsearchPersistUpdater extends ElasticsearchPersistWriter implements StreamsPersistWriter {

  private static final String STREAMS_ID = ElasticsearchPersistUpdater.class.getCanonicalName();

  private static final Logger LOGGER = LoggerFactory.getLogger(ElasticsearchPersistUpdater.class);

  public ElasticsearchPersistUpdater() {
    super();
  }

  public ElasticsearchPersistUpdater(ElasticsearchWriterConfiguration config) {
    super(config);
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public void write(StreamsDatum streamsDatum) {

    if (streamsDatum == null || streamsDatum.getDocument() == null) {
      return;
    }

    LOGGER.debug("Update Document: {}", streamsDatum.getDocument());

    Map<String, Object> metadata = streamsDatum.getMetadata();

    LOGGER.debug("Update Metadata: {}", metadata);

    String index = ElasticsearchMetadataUtil.getIndex(metadata, config);
    String type = ElasticsearchMetadataUtil.getType(metadata, config);
    String id = ElasticsearchMetadataUtil.getId(streamsDatum);
    String parent = ElasticsearchMetadataUtil.getParent(streamsDatum);
    String routing = ElasticsearchMetadataUtil.getRouting(streamsDatum);

    try {

      String docAsJson = docAsJson(streamsDatum.getDocument());

      LOGGER.debug("Attempt Update: ({},{},{},{},{}) {}", index, type, id, parent, routing, docAsJson);

      update(index, type, id, parent, routing, docAsJson);

    } catch (Throwable ex) {
      LOGGER.warn("Unable to Update Document in ElasticSearch: {}", ex.getMessage());
    }
  }

  /**
   * Prepare and en-queue.
   * @see org.elasticsearch.action.update.UpdateRequest
   * @param indexName indexName
   * @param type type
   * @param id id
   * @param parent parent
   * @param routing routing
   * @param json json
   */
  public void update(String indexName, String type, String id, String parent, String routing, String json) {
    UpdateRequest updateRequest;

    Objects.requireNonNull(id);
    Objects.requireNonNull(json);

    // They didn't specify an ID, so we will create one for them.
    updateRequest = new UpdateRequest()
        .index(indexName)
        .type(type)
        .id(id)
        .doc(json);

    if (StringUtils.isNotBlank(parent)) {
      updateRequest = updateRequest.parent(parent);
    }

    if (StringUtils.isNotBlank(routing)) {
      updateRequest = updateRequest.routing(routing);
    }

    // add fields
    //updateRequest.docAsUpsert(true);

    add(updateRequest);

  }

  /**
   * Enqueue UpdateRequest.
   * @param request request
   */
  public void add(UpdateRequest request) {

    Objects.requireNonNull(request);
    Objects.requireNonNull(request.index());

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
