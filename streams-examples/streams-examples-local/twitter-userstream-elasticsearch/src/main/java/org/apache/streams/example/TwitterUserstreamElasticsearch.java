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
import org.apache.streams.core.StreamsDatum;
import org.apache.streams.core.StreamsProcessor;
import org.apache.streams.elasticsearch.ElasticsearchPersistDeleter;
import org.apache.streams.elasticsearch.ElasticsearchPersistWriter;
import org.apache.streams.elasticsearch.ElasticsearchWriterConfiguration;
import org.apache.streams.filters.VerbDefinitionDropFilter;
import org.apache.streams.filters.VerbDefinitionKeepFilter;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.local.LocalRuntimeConfiguration;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.twitter.config.TwitterStreamConfiguration;
import org.apache.streams.twitter.provider.TwitterStreamProvider;
import org.apache.streams.verbs.ObjectCombination;
import org.apache.streams.verbs.VerbDefinition;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsInstanceOf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Example stream that populates elasticsearch with activities from twitter userstream in real-time
 */
public class TwitterUserstreamElasticsearch implements Runnable {

  public final static String STREAMS_ID = "TwitterUserstreamElasticsearch";

  private final static Logger LOGGER = LoggerFactory.getLogger(TwitterUserstreamElasticsearch.class);

  /* this pattern will match any/only deletes */
  private static VerbDefinition deleteVerbDefinition =
      new VerbDefinition()
          .withValue("delete")
          .withObjects(Stream.of(new ObjectCombination()).collect(Collectors.toList()));

  private TwitterUserstreamElasticsearchConfiguration config;

  public TwitterUserstreamElasticsearch() {
    this(new StreamsConfigurator<>(TwitterUserstreamElasticsearchConfiguration.class).detectCustomConfiguration());
  }

  public TwitterUserstreamElasticsearch(TwitterUserstreamElasticsearchConfiguration config) {
    this.config = config;
  }

  public static void main(String[] args)
  {
    LOGGER.info(StreamsConfigurator.getConfig().toString());

    TwitterUserstreamElasticsearch userstream = new TwitterUserstreamElasticsearch();

    new Thread(userstream).start();

  }

  @Override
  public void run() {

    TwitterStreamConfiguration twitterStreamConfiguration = config.getTwitter();
    ElasticsearchWriterConfiguration elasticsearchWriterConfiguration = config.getElasticsearch();

    TwitterStreamProvider stream = new TwitterStreamProvider(twitterStreamConfiguration);
    ActivityConverterProcessor converter = new ActivityConverterProcessor();
    VerbDefinitionDropFilter noDeletesProcessor = new VerbDefinitionDropFilter(Stream.of(deleteVerbDefinition).collect(Collectors.toSet()));
    ElasticsearchPersistWriter writer = new ElasticsearchPersistWriter(elasticsearchWriterConfiguration);
    VerbDefinitionKeepFilter deleteOnlyProcessor = new VerbDefinitionKeepFilter(Stream.of(deleteVerbDefinition).collect(Collectors.toSet()));
    SetDeleteIdProcessor setDeleteIdProcessor = new SetDeleteIdProcessor();
    ElasticsearchPersistDeleter deleter = new ElasticsearchPersistDeleter(elasticsearchWriterConfiguration);

    StreamBuilder builder = new LocalStreamBuilder();

    builder.newPerpetualStream(TwitterStreamProvider.class.getCanonicalName(), stream);
    builder.addStreamsProcessor(ActivityConverterProcessor.class.getCanonicalName(), converter, 2, TwitterStreamProvider.class.getCanonicalName());
    builder.addStreamsProcessor(VerbDefinitionDropFilter.class.getCanonicalName(), noDeletesProcessor, 1, ActivityConverterProcessor.class.getCanonicalName());
    builder.addStreamsPersistWriter(ElasticsearchPersistWriter.class.getCanonicalName(), writer, 1, VerbDefinitionDropFilter.class.getCanonicalName());
    builder.addStreamsProcessor(VerbDefinitionKeepFilter.class.getCanonicalName(), deleteOnlyProcessor, 1, ActivityConverterProcessor.class.getCanonicalName());
    builder.addStreamsProcessor(SetDeleteIdProcessor.class.getCanonicalName(), setDeleteIdProcessor, 1, VerbDefinitionKeepFilter.class.getCanonicalName());
    builder.addStreamsPersistWriter(ElasticsearchPersistDeleter.class.getCanonicalName(), deleter, 1, SetDeleteIdProcessor.class.getCanonicalName());

    builder.start();

  }

  protected class SetDeleteIdProcessor implements StreamsProcessor {

    public String getId() {
      return "TwitterUserstreamElasticsearch.SetDeleteIdProcessor";
    }

    @Override
    public List<StreamsDatum> process(StreamsDatum entry) {

      MatcherAssert.assertThat(entry.getDocument(), IsInstanceOf.instanceOf(Activity.class));
      String id = entry.getId();
      // replace delete with post in id
      // ensure ElasticsearchPersistDeleter will remove original post if present
      id = StringUtils.replace(id, "delete", "post");
      entry.setId(id);

      return Stream.of(entry).collect(Collectors.toList());
    }

    @Override
    public void prepare(Object configurationObject) {


    }

    @Override
    public void cleanUp() {

    }
  }

}
