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

import org.apache.streams.config.ComponentConfiguration;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.core.IsEqual.equalTo;

/**
 * Test for
 * @see ComponentConfigurator
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(StreamsConfigurator.class)
public class ComponentConfiguratorTest {

  private static final ObjectMapper mapper = new ObjectMapper();

  @Test
  public void testDetectTestDefaults() throws Exception {

    Config testConfig = ConfigFactory.load("componentTest");

    StreamsConfigurator.addConfig(testConfig);

    ComponentConfigurator<ComponentConfiguration> configurator = new ComponentConfigurator<>(ComponentConfiguration.class);
    ComponentConfiguration defaultPojo = configurator.detectConfiguration();

    assert(defaultPojo != null);

    ComponentConfiguration configuredPojo = configurator.detectConfiguration(testConfig.getConfig("configuredComponent"));

    assert(configuredPojo != null);

  }

  @Test
  public void testDetectConfigurationConfig() throws Exception {

    Config config = ConfigFactory.load("componentTest").getConfig("configuredComponent");

    ComponentConfigurator<ComponentConfiguration> configurator = new ComponentConfigurator<>(ComponentConfiguration.class);

    ComponentConfiguration testPojo = mapper.readValue(config.root().render(ConfigRenderOptions.concise()), ComponentConfiguration.class);

    assert(testPojo != null);

    ComponentConfiguration configuredPojo = configurator.detectConfiguration(config);

    assert(configuredPojo != null);

    Assert.assertEquals(configuredPojo,testPojo);

  }

  @Test
  public void testDetectConfigurationString() throws Exception {

    Config config = ConfigFactory.load("componentTest");

    PowerMockito.mockStatic(StreamsConfigurator.class);

    PowerMockito.when(StreamsConfigurator.getConfig())
      .thenReturn(config);

    ComponentConfigurator<ComponentConfiguration> configurator = new ComponentConfigurator<>(ComponentConfiguration.class);

    ComponentConfiguration testPojo = mapper.readValue(config.root().get("configuredComponent").render(ConfigRenderOptions.concise()), ComponentConfiguration.class);

    assert(testPojo != null);

    ComponentConfiguration configuredPojo = configurator.detectConfiguration("configuredComponent");

    assert(configuredPojo != null);

    Assert.assertEquals(configuredPojo,testPojo);
  }

  @Test
  public void testDetectConfigurationConfigString() throws Exception {

    Config config = ConfigFactory.load("componentTest");

    ComponentConfigurator<ComponentConfiguration> configurator = new ComponentConfigurator<>(ComponentConfiguration.class);

    ComponentConfiguration testPojo = mapper.readValue(config.root().get("configuredComponent").render(ConfigRenderOptions.concise()), ComponentConfiguration.class);

    assert(testPojo != null);

    ComponentConfiguration configuredPojo = configurator.detectConfiguration(config, "configuredComponent");

    assert(configuredPojo != null);

    Assert.assertEquals(configuredPojo,testPojo);
  }

  @Test
  public void testDetectConfigurationCompoundPath() throws Exception {

    Config testConfig = ConfigFactory.parseResourcesAnySyntax("packagePath.conf");

    StreamsConfigurator.setConfig(testConfig);

    ComponentConfigurator<ComponentConfiguration> configurator = new ComponentConfigurator<>(ComponentConfiguration.class);

    ComponentConfiguration configuredPojo = configurator.detectConfiguration("org.apache.streams.config");

    Assert.assertThat(configuredPojo, is(notNullValue()));

    Assert.assertThat(configuredPojo.getInClasses(), is(notNullValue()));
    Assert.assertThat(configuredPojo.getInClasses().size(), is(greaterThan(0)));

    Assert.assertThat(configuredPojo.getOutClasses(), is(notNullValue()));
    Assert.assertThat(configuredPojo.getOutClasses().size(), is(greaterThan(0)));

  }

  @Test
  public void testDetectConfigurationSimpleClassName() throws Exception {

    Config testConfig = ConfigFactory.parseResourcesAnySyntax("simpleClassName.conf");

    StreamsConfigurator.setConfig(testConfig);

    ComponentConfigurator<ComponentConfiguration> configurator = new ComponentConfigurator<>(ComponentConfiguration.class);

    ComponentConfiguration configuredPojo = configurator.detectConfiguration();

    Assert.assertThat(configuredPojo, is(notNullValue()));

    Assert.assertThat(configuredPojo.getInClasses(), is(notNullValue()));
    Assert.assertThat(configuredPojo.getInClasses().size(), is(greaterThan(0)));
    Assert.assertThat(configuredPojo.getInClasses().get(0), equalTo("java.lang.Object"));

    Assert.assertThat(configuredPojo.getOutClasses(), is(notNullValue()));
    Assert.assertThat(configuredPojo.getOutClasses().size(), is(greaterThan(0)));
    Assert.assertThat(configuredPojo.getOutClasses().get(0), equalTo("java.lang.Object"));

  }

  @Test
  public void testDetectConfigurationCanonicalClassName() throws Exception {

    Config testConfig = ConfigFactory.parseResourcesAnySyntax("canonicalClassName.conf");

    StreamsConfigurator.setConfig(testConfig);

    ComponentConfigurator<ComponentConfiguration> configurator = new ComponentConfigurator<>(ComponentConfiguration.class);

    ComponentConfiguration configuredPojo = configurator.detectConfiguration();

    Assert.assertThat(configuredPojo, is(notNullValue()));

    Assert.assertThat(configuredPojo.getInClasses(), is(notNullValue()));
    Assert.assertThat(configuredPojo.getInClasses().size(), is(greaterThan(0)));
    Assert.assertThat(configuredPojo.getInClasses().get(0), equalTo("java.lang.Object"));

    Assert.assertThat(configuredPojo.getOutClasses(), is(notNullValue()));
    Assert.assertThat(configuredPojo.getOutClasses().size(), is(greaterThan(0)));
    Assert.assertThat(configuredPojo.getOutClasses().get(0), equalTo("java.lang.Object"));

  }

  @Test
  public void testDetectConfigurationClassHierarchy() throws Exception {

    Config testConfig = ConfigFactory.parseResourcesAnySyntax("classHierarchy.conf");

    StreamsConfigurator.setConfig(testConfig);

    ComponentConfigurator<ComponentConfigurationForTestingNumberTwo> configurator = new ComponentConfigurator(ComponentConfigurationForTestingNumberTwo.class);

    ComponentConfiguration configuredPojo = configurator.detectConfiguration();

    Assert.assertThat(configuredPojo, is(notNullValue()));

    Assert.assertThat(configuredPojo.getInClasses(), is(notNullValue()));
    Assert.assertThat(configuredPojo.getInClasses().size(), is(greaterThan(0)));
    Assert.assertThat(configuredPojo.getInClasses().get(0), equalTo("java.lang.Integer"));

    Assert.assertThat(configuredPojo.getOutClasses(), is(notNullValue()));
    Assert.assertThat(configuredPojo.getOutClasses().size(), is(greaterThan(0)));
    Assert.assertThat(configuredPojo.getOutClasses().get(0), equalTo("java.lang.Float"));

  }

  @Test
  public void testDetectConfigurationPackageHierarchy() throws Exception {

    Config testConfig = ConfigFactory.parseResourcesAnySyntax("packageHierarchy.conf");

    StreamsConfigurator.setConfig(testConfig);

    ComponentConfigurator<ComponentConfiguration> configurator = new ComponentConfigurator(ComponentConfiguration.class);

    ComponentConfiguration configuredPojo = configurator.detectConfiguration();

    Assert.assertThat(configuredPojo, is(notNullValue()));

    Assert.assertThat(configuredPojo.getInClasses(), is(notNullValue()));
    Assert.assertThat(configuredPojo.getInClasses().size(), is(greaterThan(0)));
    Assert.assertThat(configuredPojo.getInClasses().get(0), equalTo("java.lang.Integer"));

    Assert.assertThat(configuredPojo.getOutClasses(), is(notNullValue()));
    Assert.assertThat(configuredPojo.getOutClasses().size(), is(greaterThan(0)));
    Assert.assertThat(configuredPojo.getOutClasses().get(0), equalTo("java.lang.Float"));

  }

}