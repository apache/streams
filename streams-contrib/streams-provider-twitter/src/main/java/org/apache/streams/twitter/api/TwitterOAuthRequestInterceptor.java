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

package org.apache.streams.twitter.api;

import org.apache.streams.twitter.TwitterOAuthConfiguration;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.entity.StringEntity;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Handles request signing to api.twitter.com
 */
public class TwitterOAuthRequestInterceptor implements HttpRequestInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterOAuthRequestInterceptor.class);

  private static final String oauth_signature_encoding = "UTF-8";
  private static final String oauth_signature_method = "HMAC-SHA1";
  private static final String oauth_version = "1.0";

  private static final BASE64Encoder base64Encoder = new BASE64Encoder();

  TwitterOAuthConfiguration oAuthConfiguration;

  public TwitterOAuthRequestInterceptor(TwitterOAuthConfiguration oAuthConfiguration) {
    this.oAuthConfiguration = oAuthConfiguration;
  }

  @Override
  public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {

    String oauth_nonce = generateNonce();

    String oauth_timestamp = generateTimestamp();

    Map<String,String> oauthParamMap = new HashMap<>();
    oauthParamMap.put("oauth_consumer_key", oAuthConfiguration.getConsumerKey());
    oauthParamMap.put("oauth_nonce", oauth_nonce);
    oauthParamMap.put("oauth_signature_method", oauth_signature_method);
    oauthParamMap.put("oauth_timestamp", oauth_timestamp);
    oauthParamMap.put("oauth_token", oAuthConfiguration.getAccessToken());
    oauthParamMap.put("oauth_version", oauth_version);

    String request_host = ((HttpRequestWrapper)httpRequest).getTarget().toString().replace(":443","");
    String request_path = httpRequest.getRequestLine().getUri().substring(0, httpRequest.getRequestLine().getUri().indexOf('?'));
    String request_param_line = httpRequest.getRequestLine().getUri().substring(httpRequest.getRequestLine().getUri().indexOf('?')+1);
    String[] request_params = URLDecoder.decode(request_param_line).split("&");

    Map<String,String> allParamsMap = new HashMap<>(oauthParamMap);

    for( String request_param : request_params ) {
      String key = request_param.substring(0, request_param.indexOf('='));
      String value = request_param.substring(request_param.indexOf('=')+1, request_param.length());
      allParamsMap.put(key, value);
    }

    String[] body_params;
    if( ((HttpRequestWrapper) httpRequest).getOriginal() instanceof HttpPost) {
      String body = EntityUtils.toString(((HttpPost)((HttpRequestWrapper) httpRequest).getOriginal()).getEntity());
      body_params = body.split(",");
      for( String body_param : body_params ) {
        String key = body_param.substring(0, body_param.indexOf('='));
        String value = body_param.substring(body_param.indexOf('=')+1, body_param.length());
        allParamsMap.put(key, value);
      }
    }

    allParamsMap = encodeMap(allParamsMap);

    String signature_parameter_string = generateSignatureParameterString(allParamsMap);

    String signature_base_string = generateSignatureBaseString(((HttpRequestWrapper) httpRequest).getMethod(), request_host+request_path, signature_parameter_string);

    String signing_key = encode(oAuthConfiguration.getConsumerSecret()) + "&" + encode(oAuthConfiguration.getAccessTokenSecret());

    String oauth_signature;
    try {
      oauth_signature = computeSignature(signature_base_string, signing_key);
    } catch (GeneralSecurityException e) {
      LOGGER.warn("GeneralSecurityException", e);
      return;
    }

    oauthParamMap.put("oauth_signature", oauth_signature);

    String authorization_header_string = generateAuthorizationHeaderString(oauthParamMap);

    httpRequest.setHeader("Authorization", authorization_header_string);

  }

  public String generateTimestamp() {
    Calendar tempcal = Calendar.getInstance();
    long ts = tempcal.getTimeInMillis();// get current time in milliseconds
    String oauth_timestamp = (new Long(ts/1000)).toString();
    return oauth_timestamp;
  }

  public String generateNonce() {
    String uuid_string = UUID.randomUUID().toString();
    uuid_string = uuid_string.replaceAll("-", "");
    String oauth_nonce = base64Encoder.encode(uuid_string.getBytes());
    return oauth_nonce;
  }

  public static Map<String, String> encodeMap(Map<String, String> map) {
    Map<String,String> newMap = new HashMap<>();
    for( String key : map.keySet() ) {
      String value = map.get(key);
      newMap.put(encode(key), encode(value));
    }
    return newMap;
  }


  public static String generateAuthorizationHeaderString(Map<String,String> oauthParamMap) {
    SortedSet<String> sortedKeys = new TreeSet<>(oauthParamMap.keySet());

    StringJoiner stringJoiner = new StringJoiner(", ");
    for( String key : sortedKeys ) {
      stringJoiner.add(encode(key)+"="+"\""+encode(oauthParamMap.get(key))+"\"");
    }

    String authorization_header_string = new StringBuilder()
        .append("OAuth ")
        .append(stringJoiner.toString())
        .toString();
    return authorization_header_string;
  }

  public static String generateSignatureBaseString(String method, String request_url, String signature_parameter_string) {
    String signature_base_string = new StringBuilder()
        .append(method)
        .append("&")
        .append(encode(request_url))
        .append("&")
        .append(encode(signature_parameter_string))
        .toString();
    return signature_base_string;
  }

  public static String generateSignatureParameterString(Map<String, String> allParamsMap) {

    SortedSet<String> sortedKeys = new TreeSet<>(allParamsMap.keySet());

    StringJoiner stringJoiner = new StringJoiner("&");
    for( String key : sortedKeys ) {
      stringJoiner.add(key+"="+allParamsMap.get(key));
    }

    return stringJoiner.toString();
  }

  public static String encode(String value)
  {
    String encoded = null;
    try {
      encoded = URLEncoder.encode(value, oauth_signature_encoding);
    } catch (UnsupportedEncodingException ignore) {
    }
    StringBuilder buf = new StringBuilder(encoded.length());
    char focus;
    for (int i = 0; i < encoded.length(); i++) {
      focus = encoded.charAt(i);
      if (focus == '*') {
        buf.append("%2A");
      } else if (focus == '+') {
        buf.append("%20");
      } else if (focus == '%' && (i + 1) < encoded.length()
          && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
        buf.append('~');
        i += 2;
      } else {
        buf.append(focus);
      }
    }
    return buf.toString();
  }

  public static String computeSignature(String baseString, String keyString) throws GeneralSecurityException, UnsupportedEncodingException
  {
    SecretKey secretKey = null;

    byte[] keyBytes = keyString.getBytes();
    secretKey = new SecretKeySpec(keyBytes, "HmacSHA1");

    Mac mac = Mac.getInstance("HmacSHA1");
    mac.init(secretKey);

    byte[] text = baseString.getBytes();

    return new String(base64Encoder.encode(mac.doFinal(text))).trim();
  }
}
