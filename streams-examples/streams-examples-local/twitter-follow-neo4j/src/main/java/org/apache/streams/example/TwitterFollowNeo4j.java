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
import org.apache.streams.converter.ActivityConverterProcessorConfiguration;
import org.apache.streams.converter.TypeConverterProcessor;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.data.ActivityConverter;
import org.apache.streams.data.DocumentClassifier;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.local.LocalRuntimeConfiguration;
import org.apache.streams.local.builders.LocalStreamBuilder;
import org.apache.streams.neo4j.Neo4jConfiguration;
import org.apache.streams.neo4j.bolt.Neo4jBoltPersistWriter;
import org.apache.streams.twitter.TwitterFollowingConfiguration;
import org.apache.streams.twitter.converter.TwitterDocumentClassifier;
import org.apache.streams.twitter.converter.TwitterFollowActivityConverter;
import org.apache.streams.twitter.provider.TwitterFollowingProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Collects friend and follow connections for a set of twitter users and builds a graph
 * database in neo4j.
 */
public class TwitterFollowNeo4j implements Runnable {

  private final static Logger LOGGER = LoggerFactory.getLogger(TwitterFollowNeo4j.class);

  private TwitterFollowNeo4jConfiguration config;

  public TwitterFollowNeo4j() {
    this(new ComponentConfigurator<>(TwitterFollowNeo4jConfiguration.class).detectConfiguration(StreamsConfigurator.getConfig()));
  }

  public TwitterFollowNeo4j(TwitterFollowNeo4jConfiguration config) {
    this.config = config;
  }

  public void run() {

    TwitterFollowingConfiguration twitterFollowingConfiguration = config.getTwitter();
    TwitterFollowingProvider followingProvider = new TwitterFollowingProvider(twitterFollowingConfiguration);
    TypeConverterProcessor converter = new TypeConverterProcessor(String.class);

    List<DocumentClassifier> classifiers = Stream.of((DocumentClassifier) new TwitterDocumentClassifier()).collect(Collectors.toList());
    List<ActivityConverter> converters = Stream.of((ActivityConverter) new TwitterFollowActivityConverter()).collect(Collectors.toList());
    ActivityConverterProcessorConfiguration activityConverterProcessorConfiguration =
        new ActivityConverterProcessorConfiguration()
            .withClassifiers(classifiers)
            .withConverters(converters);
    ActivityConverterProcessor activity = new ActivityConverterProcessor(activityConverterProcessorConfiguration);

    Neo4jConfiguration neo4jConfiguration = config.getNeo4j();
    Neo4jBoltPersistWriter graphPersistWriter = new Neo4jBoltPersistWriter(neo4jConfiguration);
    graphPersistWriter.prepare(neo4jConfiguration);

    LocalRuntimeConfiguration localRuntimeConfiguration =
        StreamsJacksonMapper.getInstance().convertValue(StreamsConfigurator.detectConfiguration(), LocalRuntimeConfiguration.class);
    StreamBuilder builder = new LocalStreamBuilder(localRuntimeConfiguration);

    builder.newPerpetualStream(TwitterFollowingProvider.class.getCanonicalName(), followingProvider);
    builder.addStreamsProcessor(TypeConverterProcessor.class.getCanonicalName(), converter, 1, TwitterFollowingProvider.class.getCanonicalName());
    builder.addStreamsProcessor(ActivityConverterProcessor.class.getCanonicalName(), activity, 1, TypeConverterProcessor.class.getCanonicalName());
    builder.addStreamsPersistWriter(Neo4jBoltPersistWriter.class.getCanonicalName(), graphPersistWriter, 1, ActivityConverterProcessor.class.getCanonicalName());

    builder.start();
  }

  public static void main(String[] args) {

    LOGGER.info(StreamsConfigurator.getConfig().toString());

    TwitterFollowNeo4j stream = new TwitterFollowNeo4j();

    stream.run();

  }

}