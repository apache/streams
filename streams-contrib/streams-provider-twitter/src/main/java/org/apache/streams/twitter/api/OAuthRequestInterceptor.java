package org.apache.streams.twitter.api;

import org.apache.streams.twitter.TwitterOAuthConfiguration;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.BASE64Encoder;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.util.Calendar;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by sblackmon on 3/25/17.
 */
public class OAuthRequestInterceptor implements HttpRequestInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(OAuthRequestInterceptor.class);

  private static final String get_or_post = "GET";
  private static final String oauth_signature_method = "HMAC-SHA1";
  private static final BASE64Encoder base64Encoder = new BASE64Encoder();

  TwitterOAuthConfiguration oAuthConfiguration;

  public OAuthRequestInterceptor(TwitterOAuthConfiguration oAuthConfiguration) {
    this.oAuthConfiguration = oAuthConfiguration;
  }

  @Override
  public void process(HttpRequest httpRequest, HttpContext httpContext) throws HttpException, IOException {

    String uuid_string = UUID.randomUUID().toString();
    uuid_string = uuid_string.replaceAll("-", "");
    String oauth_nonce = uuid_string; // any relatively random alphanumeric string will work here

    // get the timestamp
    Calendar tempcal = Calendar.getInstance();
    long ts = tempcal.getTimeInMillis();// get current time in milliseconds
    String oauth_timestamp = (new Long(ts/1000)).toString(); // then divide by 1000 to get seconds

    String parameter_string = new StringBuilder()
        .append("oauth_consumer_key=" + oAuthConfiguration.getConsumerKey())
        .append("&")
        .append("oauth_nonce=" + oauth_nonce)
        .append("&")
        .append("oauth_signature_method=" + oauth_signature_method)
        .append("&")
        .append("oauth_timestamp=" + oauth_timestamp)
        .append("&")
        .append("oauth_token=" + encode(oAuthConfiguration.getAccessToken()))
        .append("&")
        .append("oauth_version=1.0")
        .toString();

    String request_url = httpRequest.getRequestLine().getUri();
    String twitter_endpoint = request_url;
    String signature_base_string = new StringBuilder()
      .append(get_or_post)
      .append("&")
      .append(encode(twitter_endpoint))
      .append("&")
      .append(encode(parameter_string))
      .toString();

    String oauth_signature;
    try {
      oauth_signature = computeSignature(signature_base_string, oAuthConfiguration.getConsumerSecret() + "&" + encode(oAuthConfiguration.getAccessTokenSecret()));
    } catch (GeneralSecurityException e) {
      LOGGER.warn("GeneralSecurityException", e);
      return;
    }

    String authorization_header_string = new StringBuilder()
      .append("OAuth ")
      .append("oauth_consumer_key=\"" + oAuthConfiguration.getConsumerKey() + "\"")
      .append(", ")
      .append("oauth_nonce=\"" + oauth_nonce + "\"")
      .append(", ")
      .append("oauth_signature=\"" + oauth_signature + "\"")
      .append(", ")
      .append("oauth_signature_method=\"HMAC-SHA1\"")
      .append(", ")
      .append("oauth_timestamp=\"" + oauth_timestamp + "\"")
      .append(", ")
      .append("oauth_token=\"" + encode(oAuthConfiguration.getAccessToken()) + "\"")
      .append(", ")
      .append("oauth_version=\"1.0\"")
      .toString();

    httpRequest.setHeader("Authorization", authorization_header_string);

  }

  public String encode(String value)
  {
    String encoded = null;
    try {
      encoded = URLEncoder.encode(value, "UTF-8");
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

  private static String computeSignature(String baseString, String keyString) throws GeneralSecurityException, UnsupportedEncodingException
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
