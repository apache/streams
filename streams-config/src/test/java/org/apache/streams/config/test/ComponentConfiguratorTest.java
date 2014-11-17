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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import junit.framework.Assert;
import org.apache.streams.config.ComponentConfiguration;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
* Test for
* @see {@link org.apache.streams.config.ComponentConfigurator}
*/
@RunWith(PowerMockRunner.class)
@PrepareForTest(StreamsConfigurator.class)
public class ComponentConfiguratorTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDetectDefaults() throws Exception {

        Config config = ConfigFactory.load("componentTest");

        ComponentConfigurator<ComponentConfiguration> configurator = new ComponentConfigurator<>(ComponentConfiguration.class);
        
        ComponentConfiguration defaultPojo = configurator.detectConfiguration(config.getConfig("defaultComponent"));

        assert(defaultPojo != null);

        ComponentConfiguration configuredPojo = configurator.detectConfiguration(config.getConfig("configuredComponent"));

        assert(configuredPojo != null);

        Assert.assertEquals(configuredPojo,defaultPojo);

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
}