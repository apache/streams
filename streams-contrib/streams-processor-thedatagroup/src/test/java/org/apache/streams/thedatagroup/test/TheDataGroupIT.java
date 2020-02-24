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

package org.apache.streams.thedatagroup.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigRenderOptions;
import org.apache.juneau.json.JsonParser;
import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.thedatagroup.SyncAppend;
import org.apache.streams.thedatagroup.TheDataGroup;
import org.apache.streams.thedatagroup.api.AppendRequest;
import org.apache.streams.thedatagroup.api.DemographicsAppendResponse;
import org.apache.streams.thedatagroup.config.TheDataGroupConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;

import static java.util.Objects.nonNull;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Integration Tests for all implemented thedatagroup.com endpoints.
 */
public class TheDataGroupIT {

    private static final Logger LOGGER = LoggerFactory.getLogger(TheDataGroupIT.class);

    private static String configfile = "target/test-classes/TheDataGroupIT/TheDataGroupIT.conf";

    private static TheDataGroupConfiguration config;

    private static Config testsconfig;

    @BeforeClass(alwaysRun = true)
    public void setup() throws Exception {
        File conf = new File(configfile);
        Assert.assertTrue (conf.exists());
        Assert.assertTrue (conf.canRead());
        Assert.assertTrue (conf.isFile());
        StreamsConfigurator.addConfig(ConfigFactory.parseFileAnySyntax(conf));
        config = new ComponentConfigurator<>(TheDataGroupConfiguration.class).detectConfiguration();
        testsconfig = StreamsConfigurator.getConfig().getConfig("org.apache.streams.thedatagroup.test.TheDataGroupIT");
    }

    @Test
    public void testDemographicsAppend() throws Exception {
        SyncAppend syncAppend = TheDataGroup.getInstance(config);
        Config requestConfig = StreamsConfigurator.getConfig().getConfig("org.apache.streams.thedatagroup.test.TheDataGroupIT.testAppendDemographics");
        AppendRequest req = JsonParser.DEFAULT.parse(requestConfig.root().render(ConfigRenderOptions.concise()), AppendRequest.class);
        DemographicsAppendResponse response = syncAppend.appendDemographics(req);
        nonNull(response);
    }

}