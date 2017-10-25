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

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.StringJoiner;
import java.util.TreeSet;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by sblackmon on 6/15/17.
 */
public class TwitterSecurity {

  private static final Logger LOGGER = LoggerFactory.getLogger(TwitterOAuthRequestInterceptor.class);

  public static final String oauth_signature_encoding = "UTF-8";
  public static final String oauth_version = "1.0";

  public static final String oauth_signature_method_param = "HMAC-SHA1";
  public static final String oauth_signature_method = "HmacSHA1";
  public static final String webhook_signature_method = "HmacSHA256";

  private static final Base64 base64 = new Base64();

  public String encode(String value)
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

  public String generateSignatureParameterString(Map<String, String> allParamsMap) {

    SortedSet<String> sortedKeys = new TreeSet<>(allParamsMap.keySet());

    StringJoiner stringJoiner = new StringJoiner("&");
    for( String key : sortedKeys ) {
      stringJoiner.add(key+"="+allParamsMap.get(key));
    }

    return stringJoiner.toString();
  }

  public String generateSignatureBaseString(String method, String request_url, String signature_parameter_string) {
    String signature_base_string = new StringBuilder()
        .append(method)
        .append("&")
        .append(encode(request_url))
        .append("&")
        .append(encode(signature_parameter_string))
        .toString();
    return signature_base_string;
  }

  public String generateAuthorizationHeaderString(Map<String,String> oauthParamMap) {
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

  public Map<String, String> encodeMap(Map<String, String> map) {
    Map<String,String> newMap = new HashMap<>();
    for( String key : map.keySet() ) {
      String value = map.get(key);
      newMap.put(encode(key), encode(value));
    }
    return newMap;
  }

  public byte[] computeSignatureBytes(String baseString, String keyString, String signature_method) throws GeneralSecurityException, UnsupportedEncodingException
  {
    SecretKey secretKey = null;

    byte[] keyBytes = keyString.getBytes();
    secretKey = new SecretKeySpec(keyBytes, signature_method);

    Mac mac = Mac.getInstance(signature_method);
    mac.init(secretKey);

    byte[] text = baseString.getBytes();

    return mac.doFinal(text);
  }

  public String computeAndEncodeSignature(String baseString, String keyString, String signature_method) throws GeneralSecurityException, UnsupportedEncodingException {

    return new String(base64.encode(computeSignatureBytes(baseString, keyString, signature_method))).trim();

  }


}
