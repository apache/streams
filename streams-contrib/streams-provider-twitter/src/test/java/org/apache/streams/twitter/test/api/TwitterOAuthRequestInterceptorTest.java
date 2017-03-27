package org.apache.streams.twitter.test.api;

import org.apache.streams.twitter.TwitterOAuthConfiguration;
import org.apache.streams.twitter.api.TwitterOAuthRequestInterceptor;
import org.apache.streams.twitter.test.utils.TwitterActivityConvertersTest;

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

import static org.apache.streams.twitter.api.TwitterOAuthRequestInterceptor.encode;
import static org.junit.Assert.assertEquals;

/**
 * Created by sblackmon on 3/26/17.
 */
public class TwitterOAuthRequestInterceptorTest {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterActivityConvertersTest.class);

  @Test
  public void testEncode() {
    assertEquals( "Ladies%20%2B%20Gentlemen", encode("Ladies + Gentlemen"));
    assertEquals( "An%20encoded%20string%21", encode("An encoded string!"));
    assertEquals( "Dogs%2C%20Cats%20%26%20Mice", encode("Dogs, Cats & Mice"));
    assertEquals( "%E2%98%83", encode("☃"));
  }

  @Test
  public void testGenerateSignatureBaseString() {
    Map<String,String> testParamMap = new HashMap<>();
    testParamMap.put("status", encode("Hello Ladies + Gentlemen, a signed OAuth request!"));
    testParamMap.put("include_entities", "true");
    testParamMap.put("oauth_consumer_key", "xvz1evFS4wEEPTGEFPHBog");
    testParamMap.put("oauth_nonce", "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg");
    testParamMap.put("oauth_signature_method", "HMAC-SHA1");
    testParamMap.put("oauth_timestamp", "1318622958");
    testParamMap.put("oauth_token", "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb");
    testParamMap.put("oauth_version", "1.0");
    String signature_parameter_string = TwitterOAuthRequestInterceptor.generateSignatureParameterString(testParamMap);
    String signature_base_string = TwitterOAuthRequestInterceptor.generateSignatureBaseString("POST", "https://api.twitter.com/1/statuses/update.json", signature_parameter_string);
    String expected = "POST&https%3A%2F%2Fapi.twitter.com%2F1%2Fstatuses%2Fupdate.json&include_entities%3Dtrue%26oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1318622958%26oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26oauth_version%3D1.0%26status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521";
    assertEquals(expected, signature_base_string);
  }

  /**
   * @see <a href=https://dev.twitter.com/oauth/overview/creating-signatures">https://dev.twitter.com/oauth/overview/creating-signatures</a>
   */
  @Test
  public void testGenerateSignatureParameterString() {
    Map<String,String> testParamMap = new HashMap<>();
    testParamMap.put("status", encode("Hello Ladies + Gentlemen, a signed OAuth request!"));
    testParamMap.put("include_entities", "true");
    testParamMap.put("oauth_consumer_key", "xvz1evFS4wEEPTGEFPHBog");
    testParamMap.put("oauth_nonce", "kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg");
    testParamMap.put("oauth_signature_method", "HMAC-SHA1");
    testParamMap.put("oauth_timestamp", "1318622958");
    testParamMap.put("oauth_token", "370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb");
    testParamMap.put("oauth_version", "1.0");
    String signature_parameter_string = TwitterOAuthRequestInterceptor.generateSignatureParameterString(testParamMap);
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
    String authorization_header_string = TwitterOAuthRequestInterceptor.generateAuthorizationHeaderString(oauthParamMap);
    assertEquals(expected, authorization_header_string);
  }

  @Test
  public void testComputeSignature() throws Exception {
    String signature_base_string = "POST&https%3A%2F%2Fapi.twitter.com%2F1%2Fstatuses%2Fupdate.json&include_entities%3Dtrue%26oauth_consumer_key%3Dxvz1evFS4wEEPTGEFPHBog%26oauth_nonce%3DkYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg%26oauth_signature_method%3DHMAC-SHA1%26oauth_timestamp%3D1318622958%26oauth_token%3D370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb%26oauth_version%3D1.0%26status%3DHello%2520Ladies%2520%252B%2520Gentlemen%252C%2520a%2520signed%2520OAuth%2520request%2521";
    String signing_key = "kAcSOqF21Fu85e7zjz7ZN2U4ZRhfV3WpwPAoE3Z7kBw&LswwdoUaIvS8ltyTt5jkRh4J50vUPVVHtR2YPi5kE";
    String expected = "tnnArxj06cWHq44gCs1OSKk/jLY=";
    String oauth_signature = TwitterOAuthRequestInterceptor.computeSignature(signature_base_string, signing_key);
    assertEquals(expected, oauth_signature);
  }

  @Test
  public void testProcess() throws Exception {
    URI testURI = new URIBuilder()
        .setPath("/1/statuses/update.json")
        .setParameter("include_entities", "true")
        .build();
    HttpPost testRequest = new HttpPost(testURI);
    testRequest.setEntity(new StringEntity("status="+encode("Hello Ladies + Gentlemen, a signed OAuth request!")));
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
    String expected = "OAuth oauth_consumer_key=\"xvz1evFS4wEEPTGEFPHBog\", oauth_nonce=\"kYjzVBB8Y0ZFabxSWbWovY3uYSQ2pTgmZeNu2VS4cg\", oauth_signature=\"tnnArxj06cWHq44gCs1OSKk%2FjLY%3D\", oauth_signature_method=\"HMAC-SHA1\", oauth_timestamp=\"1318622958\", oauth_token=\"370773112-GmHxMAgYyLbNEtIKZeRNFsMKPR9EyMZeS9weJAEb\", oauth_version=\"1.0\"";
    assertEquals(1, wrapper.getHeaders("Authorization").length);
    assertEquals(expected, wrapper.getFirstHeader("Authorization").getValue());
  }

}
