package org.apache.streams.config.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import junit.framework.Assert;
import org.apache.streams.config.ComponentConfiguration;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.junit.Test;

/**
 * Created by sblackmon on 10/20/14.
 */
public class ComponentConfiguratorTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDetectConfiguration() throws Exception {

        Config config = ConfigFactory.load("componentTest");

        ComponentConfigurator<ComponentConfiguration> configurator = new ComponentConfigurator<>(ComponentConfiguration.class);
        
        ComponentConfiguration defaultPojo = configurator.detectConfiguration(config.getConfig("defaultComponent"));

        assert(defaultPojo != null);

        ComponentConfiguration configuredPojo = configurator.detectConfiguration(config.getConfig("configuredComponent"));

        assert(configuredPojo != null);

        Assert.assertEquals(configuredPojo,defaultPojo);

    }
}
