package org.apache.streams.instagram.api;

import org.apache.streams.instagram.config.InstagramOAuthConfiguration;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
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
public class InstagramOAuthRequestSigner {

  private static final Logger LOGGER = LoggerFactory.getLogger(InstagramOAuthRequestSigner.class);

  private static final String oauth_signature_encoding = "UTF-8";
  private static final String oauth_signature_method = "HmacSHA256";

  InstagramOAuthConfiguration oAuthConfiguration;

  public InstagramOAuthRequestSigner(InstagramOAuthConfiguration oauth) {
    this.oAuthConfiguration = oauth;
  }

  /**
   * generateSignature.
   * @param uri uri
   * @return String
   */
  public String generateSignature(String uri) {

    String request_path = uri.substring(0, uri.indexOf('?'));
    String request_param_line = uri.substring(uri.indexOf('?') + 1);
    String[] request_params = URLDecoder.decode(request_param_line).split("&");

    Map<String,String> oauthParamMap = new HashMap<>();
    oauthParamMap.put("access_token", oAuthConfiguration.getAccessToken());

    Map<String,String> allParamsMap = new HashMap<>(oauthParamMap);

    for ( String request_param : request_params ) {
      String key = request_param.substring(0, request_param.indexOf('='));
      String value = request_param.substring(request_param.indexOf('=') + 1, request_param.length());
      allParamsMap.put(key, value);
    }

    String endpoint = request_path;

    // the sig string to sign in the form "endpoint|key1=value1|key2=value2|...."
    String signature_base_string = generateSignatureBaseString(endpoint, allParamsMap);

    String oauth_signature;
    try {
      oauth_signature = computeSignature(signature_base_string, oAuthConfiguration.getClientSecret());
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
