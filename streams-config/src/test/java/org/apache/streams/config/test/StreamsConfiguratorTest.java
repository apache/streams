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

        StreamsConfiguration configuredPojo = StreamsConfigurator.detectConfiguration(StreamsConfigurator.config);

        assert(configuredPojo != null);

        Assert.assertEquals(configuredPojo, defaultPojo);

    }
}
