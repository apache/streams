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

package org.apache.streams.instagram.api;

import org.apache.streams.instagram.config.InstagramConfiguration;

import org.apache.commons.codec.binary.Hex;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.HttpRequestWrapper;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

/**
 * Handles request signing to api.instagram.com.
 *
 * @see <a href="https://www.instagram.com/developer/secure-api-requests/">https://www.instagram.com/developer/secure-api-requests/</a>
 */
public class InstagramOAuthRequestSigner implements HttpRequestInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramOAuthRequestSigner.class);

  private static final String oauth_signature_encoding = "UTF-8";
  private static final String oauth_signature_method = "HmacSHA256";

  InstagramConfiguration configuration;

  public InstagramOAuthRequestSigner(InstagramConfiguration configuration) {
    this.configuration = configuration;
  }

  @Override
  public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {

    try {
      HttpRequestWrapper httpRequestWrapper = (HttpRequestWrapper) httpRequest;
      HttpRequestBase httpRequestBase = (HttpRequestBase) httpRequestWrapper.getOriginal();

      String sig_uri = httpRequestBase.getURI().getPath();
      if( sig_uri.startsWith("/"+configuration.getVersion()) ) {
        sig_uri = sig_uri.substring(configuration.getVersion().length()+1);
      }

      String sig = generateSignature(sig_uri);

      URI oauthURI = new URIBuilder(httpRequestBase.getURI())
          .addParameter("access_token", configuration.getOauth().getAccessToken())
          .addParameter("sig", sig)
          .build();

      httpRequestBase.setURI(oauthURI);
      ((HttpRequestWrapper) httpRequest).setURI(oauthURI);

    } catch( Exception ue ) {
      throw new IOException("Exception", ue);
    }

  }

  /**
   * generateSignature.
   * @param uri uri
   * @return String
   */
  public String generateSignature(String uri) {

    Map<String,String> oauthParamMap = new HashMap<>();

    oauthParamMap.put("access_token", configuration.getOauth().getAccessToken());

    Map<String,String> allParamsMap = new HashMap<>(oauthParamMap);

    String request_path = uri;

    if( uri.contains("?")) {
      request_path = uri.substring(0, uri.indexOf('?'));
      String request_param_line = uri.substring(uri.indexOf('?') + 1);
      String[] request_params = URLDecoder.decode(request_param_line).split("&");
      for ( String request_param : request_params ) {
        String key = request_param.substring(0, request_param.indexOf('='));
        String value = request_param.substring(request_param.indexOf('=') + 1, request_param.length());
        allParamsMap.put(key, value);
      }
    }

    String endpoint = request_path;

    // the sig string to sign in the form "endpoint|key1=value1|key2=value2|...."
    String signature_base_string = generateSignatureBaseString(endpoint, allParamsMap);

    String oauth_signature;
    try {
      oauth_signature = computeSignature(signature_base_string, configuration.getOauth().getClientSecret());
    } catch (GeneralSecurityException ex) {
      LOGGER.warn("GeneralSecurityException", ex);
      return null;
    } catch (UnsupportedEncodingException ex) {
      LOGGER.warn("UnsupportedEncodingException", ex);
      return null;
    }

    return oauth_signature;
  }

  /**
   * generateSignatureBaseString.
   * @param endpoint endpoint
   * @param allParamsMap allParamsMap
   * @return String
   */
  public static String generateSignatureBaseString(String endpoint, Map<String, String> allParamsMap) {

    SortedSet<String> sortedKeys = new TreeSet<>(allParamsMap.keySet());

    StringJoiner stringJoiner = new StringJoiner("|");
    stringJoiner.add(endpoint);
    for ( String key : sortedKeys ) {
      stringJoiner.add(key + "=" + allParamsMap.get(key));
    }

    return stringJoiner.toString();
  }

  /**
   * computeSignature.
   * @param signature_base_string Signature Base String
   * @param clientSecret Client Secret
   * @return String
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   * @throws UnsupportedEncodingException
   */
  public static String computeSignature(String signature_base_string, String clientSecret) throws NoSuchAlgorithmException, InvalidKeyException, UnsupportedEncodingException {
    SecretKeySpec keySpec = new SecretKeySpec(clientSecret.getBytes(oauth_signature_encoding), oauth_signature_method);
    Mac mac = Mac.getInstance(oauth_signature_method);
    mac.init(keySpec);
    byte[] result = mac.doFinal(signature_base_string.getBytes(oauth_signature_encoding));
    return Hex.encodeHexString(result);
  }
}
