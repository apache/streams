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

package org.apache.streams.twitter.test.api;

import org.apache.streams.twitter.api.TwitterOAuthRequestInterceptor;
import org.apache.streams.twitter.api.TwitterSecurity;
import org.apache.streams.twitter.config.TwitterOAuthConfiguration;

import org.apache.http.HttpHost;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpCoreContext;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Unit Tests for twitter request signing.
 */
public class TwitterSecurityTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterSecurityTest.class);

  private TwitterSecurity security = new TwitterSecurity();

  @Test
  public void testEncode() {
    assertEquals( "Ladies%20%2B%20Gentlemen", security.encode("Ladies + Gentlemen"));
    assertEquals( "An%20encoded%20string%21", security.encode("An encoded string!"));
    assertEquals( "Dogs%2C%20Cats%20%26%20Mice", security.encode("Dogs, Cats & Mice"));
    assertEquals( "%E2%98%83", security.encode("☃"));
  }

  @Test
  public void testGenerateSignatureBaseString() {
    Map<String,String> testParamMap = new HashMap<>();
    testParamMap.put("status", security.encode("Hello Ladies + Gentlemen, a signed OAuth request!"));
    testParamMap.put("include_entities", "true");
    testParamMap.put("oauth_consumer_key", "xvz1evFS4wEEPTGEFPHBog");
    testParamMap.put("oauth_nonce", "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg");
    testParamMap.put("oauth_signature_method", "HMAC-SHA1");
    testParamMap.put("oauth_timestamp", "1318622958");
    testParamMap.put("oauth_token", "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb");
    testParamMap.put("oauth_version", "1.0");
    String signature_parameter_string = security.generateSignatureParameterString(testParamMap);
    String signature_base_string = security.generateSignatureBaseString("POST", "https://api.twitter.com/1/statuses/update.json", signature_parameter_string);
    String expected = "POST&https%3A%2F%2Fapi.twitter.com%2F1%2Fstatuses%2Fupdate.json&include_entities%3Dtrue%26oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1318622958%26oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26oauth_version%3D1.0%26status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521";
    assertEquals(expected, signature_base_string);
  }

  /**
   * @see <a href=https://dev.twitter.com/oauth/overview/creating-signatures">https://dev.twitter.com/oauth/overview/creating-signatures</a>
   */
  @Test
  public void testGenerateSignatureParameterString() {
    Map<String,String> testParamMap = new HashMap<>();
    testParamMap.put("status", security.encode("Hello Ladies + Gentlemen, a signed OAuth request!"));
    testParamMap.put("include_entities", "true");
    testParamMap.put("oauth_consumer_key", "xvz1evFS4wEEPTGEFPHBog");
    testParamMap.put("oauth_nonce", "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg");
    testParamMap.put("oauth_signature_method", "HMAC-SHA1");
    testParamMap.put("oauth_timestamp", "1318622958");
    testParamMap.put("oauth_token", "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb");
    testParamMap.put("oauth_version", "1.0");
    String signature_parameter_string = security.generateSignatureParameterString(testParamMap);
    String expected = "include_entities=true&oauth_consumer_key=xvz1evFS4wEEPTGEFPHBog&oauth_nonce=kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg&oauth_signature_method=HMAC-SHA1&oauth_timestamp=1318622958&oauth_token=370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb&oauth_version=1.0&status=Hello%20Ladies%20%2B%20Gentlemen%2C%20a%20signed%20OAuth%20request%21";
    assertEquals(expected, signature_parameter_string);
  }

  @Test
  public void testGenerateAuthorizationHeaderString() {
    Map<String,String> oauthParamMap = new HashMap<>();
    oauthParamMap.put("oauth_consumer_key", "xvz1evFS4wEEPTGEFPHBog");
    oauthParamMap.put("oauth_nonce", "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg");
    oauthParamMap.put("oauth_signature", "tnnArxj06cWHq44gCs1OSKk/jLY=");
    oauthParamMap.put("oauth_signature_method", "HMAC-SHA1");
    oauthParamMap.put("oauth_timestamp", "1318622958");
    oauthParamMap.put("oauth_token", "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb");
    oauthParamMap.put("oauth_version", "1.0");
    String expected = "OAuth oauth_consumer_key=\"xvz1evFS4wEEPTGEFPHBog\", oauth_nonce=\"kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg\", oauth_signature=\"tnnArxj06cWHq44gCs1OSKk%2FjLY%3D\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1318622958\", oauth_token=\"370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb\", oauth_version=\"1.0\"";
    String authorization_header_string = security.generateAuthorizationHeaderString(oauthParamMap);
    assertEquals(expected, authorization_header_string);
  }

  @Test
  public void testComputeOauthSignature() throws Exception {
    String signature_base_string = "POST&https%3A%2F%2Fapi.twitter.com%2F1%2Fstatuses%2Fupdate.json&include_entities%3Dtrue%26oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1318622958%26oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26oauth_version%3D1.0%26status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521";
    String signing_key = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw&LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE";
    String expected = "tnnArxj06cWHq44gCs1OSKk/jLY=";
    String oauth_signature = security.computeAndEncodeSignature(signature_base_string, signing_key, TwitterSecurity.oauth_signature_method);
    assertEquals(expected, oauth_signature);
  }

  @Test
  public void testComputeWebhookSignature() throws Exception {
    // https://tools.ietf.org/html/rfc4231
    String signature_base_string = "decacec0-51f5-11e7-98f5-7556589ff0ff";
    String signing_key = "uQ0gukfFwOZTQmTDRrZ35rwEe7D9WLXNYuZkCX147v8fYikK7u";
    String expected = "gPUsBnJwEylNWuiIpmwKeZKmh3tjm3sq1CXeGe/GD4M=";
    String webhook_signature = security.computeAndEncodeSignature(signature_base_string, signing_key, TwitterSecurity.webhook_signature_method);
    assertEquals(expected, webhook_signature);
  }

  @Test
  public void testProcess() throws Exception {
    URI testURI = new URIBuilder()
        .setPath("/1/statuses/update.json")
        .setParameter("include_entities", "true")
        .build();
    HttpPost testRequest = new HttpPost(testURI);
    testRequest.setEntity(new StringEntity("status="+security.encode("Hello Ladies + Gentlemen, a signed OAuth request!")));
    HttpHost host = new HttpHost("api.twitter.com", -1, "https");
    HttpRequestWrapper wrapper = HttpRequestWrapper.wrap(testRequest, host);
    TwitterOAuthConfiguration testOauthConfiguration = new TwitterOAuthConfiguration()
        .withConsumerKey("xvz1evFS4wEEPTGEFPHBog")
        .withConsumerSecret("kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw")
        .withAccessToken("370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb")
        .withAccessTokenSecret("LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE");
    TwitterOAuthRequestInterceptor interceptor = Mockito.spy(new TwitterOAuthRequestInterceptor(testOauthConfiguration));
    Mockito.when(interceptor.generateNonce()).thenReturn("kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg");
    Mockito.when(interceptor.generateTimestamp()).thenReturn("1318622958");
    interceptor.process(wrapper, new HttpCoreContext());
    assertEquals(1, wrapper.getHeaders("Authorization").length);
    String actual = wrapper.getFirstHeader("Authorization").getValue();
    String expected = "OAuth oauth_consumer_key=\"xvz1evFS4wEEPTGEFPHBog\", oauth_nonce=\"kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg\", oauth_signature=\"tnnArxj06cWHq44gCs1OSKk%2FjLY%3D\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1318622958\", oauth_token=\"370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb\", oauth_version=\"1.0\"";
    assertEquals(expected, actual);
  }

}
