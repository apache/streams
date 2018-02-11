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

package org.apache.streams.config.test;

import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigValueFactory;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test for {@link org.apache.streams.config.StreamsConfigurator}
 */
public class StreamsConfiguratorTest {

  @Test
  public void testDetectConfiguration() throws Exception {

    Config config = ConfigFactory.load();

    Config detected = StreamsConfigurator.getConfig();

    Assert.assertEquals(config, detected);

    StreamsConfiguration defaultPojo = StreamsConfigurator.detectConfiguration();

    assert(defaultPojo != null);

    StreamsConfiguration configuredPojo = StreamsConfigurator.detectConfiguration(StreamsConfigurator.getConfig());

    assert(configuredPojo != null);

    Assert.assertEquals(configuredPojo, defaultPojo);

  }

  @Test
  public void testReference() throws Exception {

    Config defaultConfig = StreamsConfigurator.getConfig();

    assert( defaultConfig.getLong("org.apache.streams.config.StreamsConfiguration.batchFrequencyMs") > 0);
    assert( defaultConfig.getLong("org.apache.streams.config.StreamsConfiguration.batchSize") > 0);
    assert( defaultConfig.getLong("org.apache.streams.config.StreamsConfiguration.parallelism") > 0);
    assert( defaultConfig.getLong("org.apache.streams.config.StreamsConfiguration.providerTimeoutMs") > 0);
    assert( defaultConfig.getLong("org.apache.streams.config.StreamsConfiguration.queueSize") > 0);

    StreamsConfiguration defaultPojo = StreamsConfigurator.detectConfiguration();

    assert(defaultPojo != null);

    assert( defaultPojo.getBatchFrequencyMs() > 0);
    assert( defaultPojo.getBatchSize() > 0);
    assert( defaultPojo.getParallelism() > 0);
    assert( defaultPojo.getProviderTimeoutMs() > 0);
    assert( defaultPojo.getQueueSize() > 0);

  }

  @Test
  public void testOverride() throws Exception {

    Config overrides = ConfigFactory.empty().withValue("parallelism", ConfigValueFactory.fromAnyRef(100l));

    StreamsConfigurator.addConfig(overrides, "org.apache.streams.config.StreamsConfiguration");

    Config withOverride = StreamsConfigurator.getConfig();

    assert( withOverride.getLong("org.apache.streams.config.StreamsConfiguration.parallelism") == 100);

    StreamsConfiguration defaultPojo = StreamsConfigurator.detectConfiguration();

    assert( defaultPojo != null);

    assert( defaultPojo.getParallelism() == 100);

  }

  @Test
  public void testResolve() throws Exception {

    Config overrides = ConfigFactory.parseResourcesAnySyntax("testResolve.conf");

    StreamsConfigurator.addConfig(overrides);

    Config withOverride = StreamsConfigurator.getConfig();

    assert( withOverride.getString("message") != null);

    assert( withOverride.getConfig("evenmore").getString("message") != null);

    assert( withOverride.getString("samemessage") != null);

  }
}
