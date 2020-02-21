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

package org.apache.streams.sprinklr.test;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import org.apache.streams.config.ComponentConfigurator;
import org.apache.streams.config.StreamsConfigurator;
import org.apache.streams.sprinklr.api.PartnerAccountsResponse;
import org.apache.streams.sprinklr.config.SprinklrConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.testng.Assert;

import org.apache.streams.sprinklr.Sprinklr;

import java.io.File;

/**
 * Integration tests for all implemented sprinklr.com Bootstrap endpoints.
 */
public class SprinklrBootstrapIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(SprinklrBootstrapIT.class);

  private static String configfile = "target/test-classes/SprinklrIT/SprinklrBootstrapIT.conf";

  private static SprinklrConfiguration config;

  private static Config testsconfig;

  @BeforeClass(alwaysRun = true)
  public void setup() throws Exception {
    File conf = new File(configfile);
    Assert.assertTrue(conf.exists());
    Assert.assertTrue(conf.canRead());
    Assert.assertTrue(conf.isFile());
    Config parsedConfig = ConfigFactory.parseFileAnySyntax(conf);
    StreamsConfigurator.addConfig(parsedConfig);
    config = new ComponentConfigurator<>(SprinklrConfiguration.class).detectConfiguration();
    testsconfig = StreamsConfigurator.getConfig().getConfig("org.apache.streams.sprinklr.config.SprinklrConfiguration");
  }

  @Test
  public void testGetPartnerAccounts() throws Exception {
    Sprinklr sprinklr = Sprinklr.getInstance(config);
    PartnerAccountsResponse response = sprinklr.getPartnerAccounts();
    Assert.assertFalse(response.getPartnerAccounts().isEmpty());
  }
}