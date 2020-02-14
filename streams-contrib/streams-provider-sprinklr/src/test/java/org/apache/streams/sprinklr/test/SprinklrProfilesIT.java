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
import org.apache.streams.sprinklr.Sprinklr;
import org.apache.streams.sprinklr.api.ProfileConversationsRequest;
import org.apache.streams.sprinklr.api.ProfileConversationsResponse;
import org.apache.streams.sprinklr.api.SocialProfileRequest;
import org.apache.streams.sprinklr.api.SocialProfileResponse;
import org.apache.streams.sprinklr.config.SprinklrConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.util.List;

/**
 * Integration tests for all implemented sprinklr.com Profiles endpoints.
 */
public class SprinklrProfilesIT {

  private static final Logger LOGGER = LoggerFactory.getLogger(SprinklrProfilesIT.class);

  private static String configfile = "target/test-classes/SprinklrIT/SprinklrProfilesIT.conf";

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
  public void testGetSocialProfile() throws Exception {
    Sprinklr sprinklr = Sprinklr.getInstance(config);
    String snType = StreamsConfigurator.getConfig()
        .getString("org.apache.streams.sprinklr.config.SprinklrConfiguration.testGetSocialProfile.snType");
    String snUserId = StreamsConfigurator.getConfig()
        .getString("org.apache.streams.sprinklr.config.SprinklrConfiguration.testGetSocialProfile.snUserId");
    SocialProfileRequest request = new SocialProfileRequest()
        .withSnType(snType)
        .withSnUserId(snUserId);
    List<SocialProfileResponse> response = sprinklr.getSocialProfile(request);
    Assert.assertFalse(response.isEmpty());
  }

  @Test
  public void testGetProfileConversations() throws Exception {
    Sprinklr sprinklr = Sprinklr.getInstance(config);
    String snType = StreamsConfigurator.getConfig()
        .getString("org.apache.streams.sprinklr.config.SprinklrConfiguration.testGetProfileConversations.snType");
    String snUserId = StreamsConfigurator.getConfig()
        .getString("org.apache.streams.sprinklr.config.SprinklrConfiguration.testGetProfileConversations.snUserId");
    Long rows = StreamsConfigurator.getConfig()
        .getLong("org.apache.streams.sprinklr.config.SprinklrConfiguration.testGetProfileConversations.rows");
    Long start = StreamsConfigurator.getConfig()
        .getLong("org.apache.streams.sprinklr.config.SprinklrConfiguration.testGetProfileConversations.start");
    ProfileConversationsRequest request = new ProfileConversationsRequest()
        .withSnType(snType)
        .withSnUserId(snUserId)
        .withRows(rows)
        .withStart(start);
    List<ProfileConversationsResponse> response = sprinklr.getProfileConversations(request);
    Assert.assertFalse(response.isEmpty());
  }

  @Test
  public void testGetAllProfileConversations() throws Exception {
    Sprinklr sprinklr = Sprinklr.getInstance(config);
    String snType = StreamsConfigurator.getConfig()
        .getString("org.apache.streams.sprinklr.config.SprinklrConfiguration.testGetAllProfileConversations.snType");
    String snUserId = StreamsConfigurator.getConfig()
        .getString("org.apache.streams.sprinklr.config.SprinklrConfiguration.testGetAllProfileConversations.snUserId");
    Long rows = StreamsConfigurator.getConfig()
        .getLong("org.apache.streams.sprinklr.config.SprinklrConfiguration.testGetAllProfileConversations.rows");
    ProfileConversationsRequest request = new ProfileConversationsRequest()
        .withSnType(snType)
        .withSnUserId(snUserId)
        .withRows(rows);
    List<ProfileConversationsResponse> response = sprinklr.getAllProfileConversations(request);
    Assert.assertFalse(response.isEmpty());
    Assert.assertTrue(response.size() > rows);
  }
}