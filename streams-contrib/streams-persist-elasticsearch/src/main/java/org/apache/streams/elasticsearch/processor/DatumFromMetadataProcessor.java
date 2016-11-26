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

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.elasticsearch.ElasticsearchClientManager;
import org.apache.streams.elasticsearch.ElasticsearchMetadataUtil;
import org.apache.streams.elasticsearch.ElasticsearchReaderConfiguration;

import com.typesafe.config.Config;
import org.elasticsearch.action.get.GetRequestBuilder;
import org.elasticsearch.action.get.GetResponse;
import org.joda.time.DateTime;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Uses index and type in metadata to populate current document into datums.
 */
public class DatumFromMetadataProcessor implements StreamsProcessor, Serializable {

  private static final String STREAMS_ID = "DatumFromMetadataProcessor";

  private ElasticsearchClientManager elasticsearchClientManager;
  private ElasticsearchReaderConfiguration config;

  public DatumFromMetadataProcessor() {
    this.config = new ComponentConfigurator<>(ElasticsearchReaderConfiguration.class)
        .detectConfiguration(StreamsConfigurator.getConfig().getConfig("elasticsearch"));
  }

  public DatumFromMetadataProcessor(Config config) {
    this.config = new ComponentConfigurator<>(ElasticsearchReaderConfiguration.class)
        .detectConfiguration(StreamsConfigurator.getConfig().getConfig("elasticsearch"));
  }

  public DatumFromMetadataProcessor(ElasticsearchReaderConfiguration config) {
    this.config = config;
  }

  @Override
  public String getId() {
    return STREAMS_ID;
  }

  @Override
  public List<StreamsDatum> process(StreamsDatum entry) {
    List<StreamsDatum> result = new ArrayList<>();

    if (entry == null || entry.getMetadata() == null) {
      return result;
    }

    Map<String, Object> metadata = entry.getMetadata();

    String index = ElasticsearchMetadataUtil.getIndex(metadata, config);
    String type = ElasticsearchMetadataUtil.getType(metadata, config);
    String id = ElasticsearchMetadataUtil.getId(entry);

    GetRequestBuilder getRequestBuilder = elasticsearchClientManager.getClient().prepareGet(index, type, id);
    getRequestBuilder.setFields("*", "_timestamp");
    getRequestBuilder.setFetchSource(true);
    GetResponse getResponse = getRequestBuilder.get();

    if ( getResponse == null || !getResponse.isExists() || getResponse.isSourceEmpty() ) {
      return result;
    }

    entry.setDocument(getResponse.getSource());
    if ( getResponse.getField("_timestamp") != null) {
      DateTime timestamp = new DateTime(((Long) getResponse.getField("_timestamp").getValue()).longValue());
      entry.setTimestamp(timestamp);
    }

    result.add(entry);

    return result;
  }

  @Override
  public void prepare(Object configurationObject) {
    this.elasticsearchClientManager = new ElasticsearchClientManager(config);

  }

  @Override
  public void cleanUp() {
    this.elasticsearchClientManager.getClient().close();
  }
}
