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

import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Handles request signing to api.twitter.com
 */
public class TwitterOAuthRequestInterceptor implements HttpRequestInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterOAuthRequestInterceptor.class);

  private static final Base64 base64 = new Base64();

  TwitterOAuthConfiguration oAuthConfiguration;

  public TwitterSecurity security = new TwitterSecurity();

  public TwitterOAuthRequestInterceptor(TwitterOAuthConfiguration oAuthConfiguration) {
    this.oAuthConfiguration = oAuthConfiguration;
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
    String oauth_nonce = base64.encode(uuid_string.getBytes()).toString();
    return oauth_nonce;
  }

  @Override
  public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {

    String oauth_nonce = generateNonce();

    String oauth_timestamp = generateTimestamp();

    Map<String,String> oauthParamMap = new HashMap<>();
    oauthParamMap.put("oauth_consumer_key", oAuthConfiguration.getConsumerKey());
    oauthParamMap.put("oauth_nonce", oauth_nonce);
    oauthParamMap.put("oauth_signature_method", security.oauth_signature_method_param);
    oauthParamMap.put("oauth_timestamp", oauth_timestamp);
    oauthParamMap.put("oauth_token", oAuthConfiguration.getAccessToken());
    oauthParamMap.put("oauth_version", security.oauth_version);

    String request_host = ((HttpRequestWrapper)httpRequest).getTarget().toString().replace(":443","");
    String request_path = httpRequest.getRequestLine().getUri();

    Map<String,String> allParamsMap = new HashMap<>(oauthParamMap);

    if( request_path.contains("?")) {
      request_path = request_path.substring(0, httpRequest.getRequestLine().getUri().indexOf('?'));
      String request_param_line = httpRequest.getRequestLine().getUri().substring(httpRequest.getRequestLine().getUri().indexOf('?')+1);
      String[] request_params = URLDecoder.decode(request_param_line).split("&");

      for( String request_param : request_params ) {
        String key = request_param.substring(0, request_param.indexOf('='));
        String value = request_param.substring(request_param.indexOf('=')+1, request_param.length());
        allParamsMap.put(key, value);
      }
    }

    if( ((HttpRequestWrapper) httpRequest).getOriginal() instanceof HttpPost) {
      HttpEntity entity = ((HttpEntityEnclosingRequest)((HttpRequestWrapper) httpRequest).getOriginal()).getEntity();
      if( entity != null ) {
        String body = EntityUtils.toString(entity);
        String[] body_params = body.split(",");
        for (String body_param : body_params) {
          body_param = URLDecoder.decode(body_param);
          String key = body_param.substring(0, body_param.indexOf('='));
          String value = body_param.substring(body_param.indexOf('=') + 1, body_param.length());
          allParamsMap.put(key, value);
        }
      }
    }

    allParamsMap = security.encodeMap(allParamsMap);

//    if( httpRequest instanceof HttpEntityEnclosingRequest ) {
//      HttpEntity entity = ((HttpEntityEnclosingRequest) httpRequest).getEntity();
//      if( entity != null ) {
//        httpRequest.setHeader("Content-Length", Integer.toString(EntityUtils.toString(entity).length()));
//      }
//    }

    String signature_parameter_string = security.generateSignatureParameterString(allParamsMap);

    String signature_base_string = security.generateSignatureBaseString(((HttpRequestWrapper) httpRequest).getMethod(), request_host+request_path, signature_parameter_string);

    String signing_key = security.encode(oAuthConfiguration.getConsumerSecret()) + "&" + security.encode(oAuthConfiguration.getAccessTokenSecret());

    String oauth_signature;
    try {
      oauth_signature = security.computeAndEncodeSignature(signature_base_string, signing_key, security.oauth_signature_method);
    } catch (GeneralSecurityException e) {
      LOGGER.warn("GeneralSecurityException", e);
      return;
    }

    oauthParamMap.put("oauth_signature",oauth_signature);

    String authorization_header_string = security.generateAuthorizationHeaderString(oauthParamMap);

    httpRequest.setHeader("Authorization", authorization_header_string);

  }

}
