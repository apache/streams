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
 * Test for
 * @see {@link org.apache.streams.config.StreamsConfigurator}
 */
public class StreamsConfiguratorTest {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testDetectConfiguration() throws Exception {

        Config config = ConfigFactory.load();

        Config detected = StreamsConfigurator.getConfig();

        junit.framework.Assert.assertEquals(config, detected);

        StreamsConfiguration defaultPojo = StreamsConfigurator.detectConfiguration();

        assert(defaultPojo != null);

        StreamsConfiguration configuredPojo = StreamsConfigurator.detectConfiguration(StreamsConfigurator.config);

        assert(configuredPojo != null);

        junit.framework.Assert.assertEquals(configuredPojo, defaultPojo);

    }
}
