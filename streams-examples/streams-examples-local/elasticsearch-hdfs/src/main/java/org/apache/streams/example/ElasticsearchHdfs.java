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
import org.apache.streams.elasticsearch.ElasticsearchPersistReader;
import org.apache.streams.hdfs.WebHdfsPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.local.LocalRuntimeConfiguration;
import org.apache.streams.local.builders.LocalStreamBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copies documents from an elasticsearch index to new-line delimited json on dfs.
 */
public class ElasticsearchHdfs implements Runnable {

  public final static String STREAMS_ID = "ElasticsearchHdfs";

  private final static Logger LOGGER = LoggerFactory.getLogger(ElasticsearchHdfs.class);

  ElasticsearchHdfsConfiguration config;

  public ElasticsearchHdfs() {
    this(new StreamsConfigurator<>(ElasticsearchHdfsConfiguration.class).detectCustomConfiguration());
  }

  public ElasticsearchHdfs(ElasticsearchHdfsConfiguration reindex) {
    this.config = reindex;
  }

  public static void main(String[] args)
  {
    LOGGER.info(StreamsConfigurator.getConfig().toString());
    ElasticsearchHdfs backup = new ElasticsearchHdfs();
    new Thread(backup).start();
  }

  @Override
  public void run() {

    ElasticsearchPersistReader elasticsearchPersistReader = new ElasticsearchPersistReader(config.getSource());
    WebHdfsPersistWriter hdfsPersistWriter = new WebHdfsPersistWriter(config.getDestination());

    StreamBuilder builder = new LocalStreamBuilder(config);

    builder.newPerpetualStream(ElasticsearchPersistReader.class.getCanonicalName(), elasticsearchPersistReader);
    builder.addStreamsPersistWriter(WebHdfsPersistWriter.class.getCanonicalName(), hdfsPersistWriter, 1, ElasticsearchPersistReader.class.getCanonicalName());
    builder.start();
  }
}
