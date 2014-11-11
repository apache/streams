package org.apache.streams.config.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.streams.config.ComponentConfiguration;
import org.apache.streams.config.StreamsConfiguration;
import org.apache.streams.config.StreamsConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.Map;
import java.util.Scanner;

/**
 * Created by sblackmon on 10/20/14.
 */
public class StreamsConfiguratorTest {

    private final static ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDetectConfiguration() throws Exception {

        Config config = ConfigFactory.load();

        Config detected = StreamsConfigurator.config;

        StreamsConfiguration defaultPojo = StreamsConfigurator.detectConfiguration();

        assert(defaultPojo != null);

        StreamsConfiguration configuredPojo = StreamsConfigurator.detectConfiguration(StreamsConfigurator.config);

        assert(configuredPojo != null);

        junit.framework.Assert.assertEquals(configuredPojo, defaultPojo);

    }
}
