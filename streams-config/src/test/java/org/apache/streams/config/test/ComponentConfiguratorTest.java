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
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

/**
 * Created by sblackmon on 10/20/14.
 */
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

        StreamsConfigurator mockStreamsConfigurator = Mockito.mock(StreamsConfigurator.class);

        PowerMockito.when(mockStreamsConfigurator.getConfig())
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