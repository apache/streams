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

package org.apache.streams.example;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.hdfs.WebHdfsPersistReader;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.local.LocalRuntimeConfiguration;
import org.apache.streams.local.builders.LocalStreamBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copies documents from new-line delimited json on dfs to an elasticsearch index.
 */
public class HdfsElasticsearch implements Runnable {

  public final static String STREAMS_ID = "HdfsElasticsearch";

  private final static Logger LOGGER = LoggerFactory.getLogger(HdfsElasticsearch.class);

  HdfsElasticsearchConfiguration config;

  public HdfsElasticsearch() {
    this(new StreamsConfigurator<>(HdfsElasticsearchConfiguration.class).detectCustomConfiguration());
  }

  public HdfsElasticsearch(HdfsElasticsearchConfiguration reindex) {
    this.config = reindex;
  }

  public static void main(String[] args)
  {
    LOGGER.info(StreamsConfigurator.getConfig().toString());
    HdfsElasticsearch restore = new HdfsElasticsearch();
    new Thread(restore).start();
  }

  @Override
  public void run() {

    WebHdfsPersistReader webHdfsPersistReader = new WebHdfsPersistReader(config.getSource());
    ElasticsearchPersistWriter elasticsearchPersistWriter = new ElasticsearchPersistWriter(config.getDestination());

    StreamBuilder builder = new LocalStreamBuilder(config);

    builder.newPerpetualStream(WebHdfsPersistReader.class.getCanonicalName(), webHdfsPersistReader);
    builder.addStreamsPersistWriter(ElasticsearchPersistWriter.class.getCanonicalName(), elasticsearchPersistWriter, 1, WebHdfsPersistReader.class.getCanonicalName());
    builder.start();
  }
}
