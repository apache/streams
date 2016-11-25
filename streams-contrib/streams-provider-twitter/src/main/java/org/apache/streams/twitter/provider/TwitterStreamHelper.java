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

package org.apache.streams.twitter.provider;

import org.apache.streams.core.StreamsDatum;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.twitter.converter.TwitterDocumentClassifier;
import org.apache.streams.util.ComponentUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.twitter.hbc.core.processor.StringDelimitedProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * TwitterStreamHelper helps with hosebird twitter stream.
 */
public class TwitterStreamHelper extends StringDelimitedProcessor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterStreamHelper.class);
  private static final int DEFAULT_POOL_SIZE = 5;

  private static final TwitterDocumentClassifier TWITTER_DOCUMENT_CLASSIFIER = new TwitterDocumentClassifier();

  private final TwitterStreamProvider provider;
  private final ExecutorService service;

  public TwitterStreamHelper(TwitterStreamProvider provider) {
    this(provider, DEFAULT_POOL_SIZE);
  }

  /**
   * TwitterStreamHelper constructor.
   * @param provider TwitterStreamProvider
   * @param poolSize poolSize
   */
  public TwitterStreamHelper(TwitterStreamProvider provider, int poolSize) {
    //We are only going to use the Hosebird processor to manage the extraction of the tweets from the Stream
    super(null);
    service = Executors.newFixedThreadPool(poolSize);
    this.provider = provider;
  }

  @Override
  public boolean process() throws IOException, InterruptedException {
    String msg;
    do {
      msg = this.processNextMessage();
      if (msg == null) {
        Thread.sleep(10);
      }
    }
    while (msg == null);

    //Deserializing to an ObjectNode can take time.  Parallelize the task to improve throughput
    return provider.addDatum(service.submit(new StreamDeserializer(msg)));
  }

  public void cleanUp() {
    ComponentUtils.shutdownExecutor(service, 1, 30);
  }

  protected static class StreamDeserializer implements Callable<List<StreamsDatum>> {

    protected static final ObjectMapper mapper = StreamsJacksonMapper.getInstance();

    protected String item;

    public StreamDeserializer(String item) {
      this.item = item;
    }

    @Override
    public List<StreamsDatum> call() throws Exception {
      if (item != null) {
        Class itemClass = TWITTER_DOCUMENT_CLASSIFIER.detectClasses(item).get(0);
        Object document = mapper.readValue(item, itemClass);
        StreamsDatum rawDatum = new StreamsDatum(document);
        return Lists.newArrayList(rawDatum);
      }
      return new ArrayList<>();
    }
  }
}
