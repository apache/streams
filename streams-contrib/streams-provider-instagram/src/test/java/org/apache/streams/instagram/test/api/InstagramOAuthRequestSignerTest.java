/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
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

package org.apache.streams.instagram.test.api;

import org.apache.streams.instagram.api.InstagramOAuthRequestSigner;
import org.apache.streams.instagram.config.InstagramConfiguration;
import org.apache.streams.instagram.config.InstagramOAuthConfiguration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Unit Test request signing logic.
 */
public class InstagramOAuthRequestSignerTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramOAuthRequestSignerTest.class);

  private static final InstagramOAuthConfiguration testOauthConfig = new InstagramOAuthConfiguration()
      .withClientSecret("6dc1787668c64c939929c17683d7cb74")
      .withAccessToken("fb2e77d.47a0479900504cb3ab4a1f626d174d2d");

  private static final InstagramConfiguration testConfig = new InstagramConfiguration()
      .withProtocol("https")
      .withHost("api.instagram.com")
      .withVersion("v1")
      .withOauth(testOauthConfig);

  private static final InstagramOAuthRequestSigner signer =
      new InstagramOAuthRequestSigner(testConfig);

  @Test
  public void testExample1() throws Exception {
    String endpoint = "/users/self";
    Map<String,String> testParamMap = new HashMap<>();
    testParamMap.put("access_token", "fb2e77d.47a0479900504cb3ab4a1f626d174d2d");
    String signature_parameter_string = signer.generateSignatureBaseString(endpoint, testParamMap);
    String expected_parameter_string = endpoint + "|access_token=fb2e77d.47a0479900504cb3ab4a1f626d174d2d";
    assertEquals(expected_parameter_string, signature_parameter_string);
    String signature = signer.computeSignature(signature_parameter_string, testOauthConfig.getClientSecret());
    String expected_signature = "cbf5a1f41db44412506cb6563a3218b50f45a710c7a8a65a3e9b18315bb338bf";
    assertEquals(expected_signature, signature);
  }

  @Test
  public void testExample2() throws Exception {
    String endpoint = "/media/657988443280050001_25025320";
    Map<String,String> testParamMap = new HashMap<>();
    testParamMap.put("access_token", "fb2e77d.47a0479900504cb3ab4a1f626d174d2d");
    testParamMap.put("count", "10");
    String signature_parameter_string = signer.generateSignatureBaseString(endpoint, testParamMap);
    String expected_parameter_string = endpoint + "|access_token=fb2e77d.47a0479900504cb3ab4a1f626d174d2d|count=10";
    assertEquals(expected_parameter_string, signature_parameter_string);
    String signature = signer.computeSignature(signature_parameter_string, testOauthConfig.getClientSecret());
    String expected_signature = "260634b241a6cfef5e4644c205fb30246ff637591142781b86e2075faf1b163a";
    assertEquals(expected_signature, signature);
  }

}
