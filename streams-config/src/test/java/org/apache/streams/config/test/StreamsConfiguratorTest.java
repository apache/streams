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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Test for {@link org.apache.streams.config.StreamsConfigurator}
 */
public class StreamsConfiguratorTest {

  /**
   * Test that basic stream properties are resolved from reference.conf
   *
   * @throws Exception
   */
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

  /**
   * Test that basic stream properties can be overridden by adding a partial config at the
   * appropriate path.
   *
   * @throws Exception
   */
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

  /**
   * Test that custom config file includes and internal references work as expected.
   *
   * @throws Exception
   */
  @Test
  public void testResolve() throws Exception {

    Config overrides = ConfigFactory.parseResourcesAnySyntax("testResolve.conf");

    StreamsConfigurator.addConfig(overrides);

    Config withOverride = StreamsConfigurator.getConfig();

    assert( withOverride.getString("message") != null);

    assert( withOverride.getConfig("evenmore").getString("message") != null);

    assert( withOverride.getString("samemessage") != null);

  }

  /**
   * Test that a class which uses a configuration class with StreamsConfiguration
   * as an ancestor can use a typed StreamsConfigurator and detectCustomConfiguration
   * to populate its fields using ComponentConfigurators automatically.
   *
   * @throws Exception
   */
  @Test
  public void testDetectCustomStreamsConfiguration() throws Exception {

    Config overrides = ConfigFactory.parseResourcesAnySyntax("custom.conf");

    StreamsConfigurator.addConfig(overrides);

    StreamsConfigurator<StreamsConfigurationForTesting> configurator = new StreamsConfigurator<>(StreamsConfigurationForTesting.class);

    StreamsConfigurationForTesting customPojo = configurator.detectCustomConfiguration();

    verifyCustomStreamsConfiguration(customPojo);

  }

  /**
   * Test that a class which uses a configuration class with StreamsConfiguration
   * as an ancestor can use a typed StreamsConfigurator and detectCustomConfiguration
   * to populate its fields using ComponentConfigurators automatically, using a
   * a custom Configuration.
   *
   * @throws Exception
   */
  @Test
  public void testDetectCustomStreamsConfigurationProvideConfig() throws Exception {

    Config base = StreamsConfigurator.getConfig();

    Config overrides = ConfigFactory.parseResourcesAnySyntax("custom.conf");

    Config combined = overrides.withFallback(base);

    StreamsConfigurator<StreamsConfigurationForTesting> configurator = new StreamsConfigurator<>(StreamsConfigurationForTesting.class);

    StreamsConfigurationForTesting customPojo = configurator.detectCustomConfiguration(combined);

    verifyCustomStreamsConfiguration(customPojo);

  }

  /**
   * Test that a class which uses a configuration class with StreamsConfiguration
   * as an ancestor can use a typed StreamsConfigurator and detectCustomConfiguration
   * to populate its fields using ComponentConfigurators automatically, sourced using
   * a provided child path.
   *
   * @throws Exception
   */
  @Test
  public void testDetectCustomStreamsConfigurationProvidePath() throws Exception {

    String testPath = "childPath";

    Config base = StreamsConfigurator.getConfig();

    Config overrides = ConfigFactory.parseResourcesAnySyntax("customChild.conf");

    Config combined = overrides.withFallback(base);

    StreamsConfigurator<StreamsConfigurationForTesting> configurator = new StreamsConfigurator<>(StreamsConfigurationForTesting.class);

    StreamsConfigurationForTesting customPojo = configurator.detectCustomConfiguration(combined, testPath);

    verifyCustomStreamsConfiguration(customPojo);
  }

  /**
   * Test that a class which uses a configuration class with StreamsConfiguration
   * as an ancestor can use a typed StreamsConfigurator and detectCustomConfiguration
   * to populate its fields using ComponentConfigurators automatically, sourced using a
   * a custom Configuration and a child path.
   *
   * @throws Exception
   */
  @Test
  public void testDetectCustomStreamsConfigurationProvideConfigAndPath() throws Exception {

    String testPath = "testPath";

    Config base = StreamsConfigurator.getConfig().atPath(testPath);

    Config overrides = ConfigFactory.parseResourcesAnySyntax("custom.conf").atPath(testPath);

    Config combined = overrides.withFallback(base);

    StreamsConfigurator<StreamsConfigurationForTesting> configurator = new StreamsConfigurator<>(StreamsConfigurationForTesting.class);

    StreamsConfigurationForTesting customPojo = configurator.detectCustomConfiguration(combined, testPath);

    verifyCustomStreamsConfiguration(customPojo);

  }

  private void verifyCustomStreamsConfiguration(StreamsConfigurationForTesting customPojo) {
    Assert.assertThat(customPojo, is(notNullValue()));

    Assert.assertThat(customPojo.getParallelism(), is(notNullValue()));
    Assert.assertThat(customPojo.getParallelism(), is(equalTo(1l)));

    Assert.assertThat(customPojo.getComponentOne().getInClasses(), is(notNullValue()));
    Assert.assertThat(customPojo.getComponentOne().getInClasses().size(), is(greaterThan(0)));
    Assert.assertThat(customPojo.getComponentOne().getInClasses().get(0), equalTo("java.lang.Integer"));
    Assert.assertThat(customPojo.getComponentOne().getOutClasses(), is(notNullValue()));
    Assert.assertThat(customPojo.getComponentOne().getOutClasses().size(), is(greaterThan(0)));
    Assert.assertThat(customPojo.getComponentOne().getOutClasses().get(0), equalTo("java.lang.String"));

    Assert.assertThat(customPojo.getComponentTwo().getInClasses(), is(notNullValue()));
    Assert.assertThat(customPojo.getComponentTwo().getInClasses().size(), is(greaterThan(0)));
    Assert.assertThat(customPojo.getComponentTwo().getInClasses().get(0), equalTo("java.lang.Float"));
    Assert.assertThat(customPojo.getComponentTwo().getOutClasses(), is(notNullValue()));
    Assert.assertThat(customPojo.getComponentTwo().getOutClasses().size(), is(greaterThan(0)));
    Assert.assertThat(customPojo.getComponentTwo().getOutClasses().get(0), equalTo("java.lang.Double"));
  }

}
