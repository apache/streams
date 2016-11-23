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

package org.apache.streams.dropwizard;

import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.core.StreamBuilder;
import org.apache.streams.core.StreamsProvider;
import org.apache.streams.local.LocalRuntimeConfiguration;
import org.apache.streams.local.builders.LocalStreamBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * StreamDropwizardBuilder is currently a light wrapper around LocalStreamBuilder.
 *
 * <p/>
 * It's a seperate class because they will almost certainly deviate going forward
 */
public class StreamDropwizardBuilder extends LocalStreamBuilder implements StreamBuilder {

  public StreamDropwizardBuilder() {
    super();
  }

  public StreamDropwizardBuilder(StreamsConfiguration streamConfig) {
    super(new ObjectMapper().convertValue(streamConfig, LocalRuntimeConfiguration.class));
  }

  public StreamDropwizardBuilder(Map<String, Object> streamConfig) {
    super(streamConfig);
  }

  public StreamDropwizardBuilder(int maxQueueCapacity) {
    super(maxQueueCapacity);
  }

  public StreamDropwizardBuilder(int maxQueueCapacity, Map<String, Object> streamConfig) {
    super(maxQueueCapacity, streamConfig);
  }

  @Override
  public StreamBuilder newPerpetualStream(String streamId, StreamsProvider provider) {
    return super.newPerpetualStream(streamId, provider);
  }

}
