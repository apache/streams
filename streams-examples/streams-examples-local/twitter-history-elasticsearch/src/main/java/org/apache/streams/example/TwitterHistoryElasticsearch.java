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
import org.apache.streams.converter.ActivityConverterProcessor;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.local.LocalRuntimeConfiguration;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.twitter.provider.TwitterTimelineProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Retrieves as many posts from a known list of users as twitter API allows.
 *
 * Converts them to activities, and writes them in activity format to Elasticsearch.
 */
public class TwitterHistoryElasticsearch implements Runnable {

  public final static String STREAMS_ID = "TwitterHistoryElasticsearch";

  private final static Logger LOGGER = LoggerFactory.getLogger(TwitterHistoryElasticsearch.class);

  private static final ObjectMapper mapper = new ObjectMapper();

  TwitterHistoryElasticsearchConfiguration config;

  public TwitterHistoryElasticsearch() {
    this(new ComponentConfigurator<>(TwitterHistoryElasticsearchConfiguration.class).detectConfiguration());
  }

  public TwitterHistoryElasticsearch(TwitterHistoryElasticsearchConfiguration config) {
    this.config = config;
  }

  public static void main(String[] args)
  {
    LOGGER.info(StreamsConfigurator.getConfig().toString());

    TwitterHistoryElasticsearch history = new TwitterHistoryElasticsearch();

    new Thread(history).start();

  }


  public void run() {

    TwitterTimelineProvider provider = new TwitterTimelineProvider(config.getTwitter());
    ActivityConverterProcessor converter = new ActivityConverterProcessor();
    ElasticsearchPersistWriter writer = new ElasticsearchPersistWriter(config.getElasticsearch());

    LocalRuntimeConfiguration localRuntimeConfiguration =
        StreamsJacksonMapper.getInstance().convertValue(StreamsConfigurator.detectConfiguration(), LocalRuntimeConfiguration.class);
    StreamBuilder builder = new LocalStreamBuilder(localRuntimeConfiguration);

    builder.newPerpetualStream(TwitterTimelineProvider.class.getCanonicalName(), provider);
    builder.addStreamsProcessor(ActivityConverterProcessor.class.getCanonicalName(), converter, 2, TwitterTimelineProvider.class.getCanonicalName());
    builder.addStreamsPersistWriter(ElasticsearchPersistWriter.class.getCanonicalName(), writer, 1, ActivityConverterProcessor.class.getCanonicalName());
    builder.start();
  }
}
