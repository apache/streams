package org.apache.streams.twitter.api;

import org.apache.streams.twitter.TwitterConfiguration;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.provider.TwitterProviderUtil;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.juneau.BeanContext;
import org.apache.juneau.BeanSession;
import org.apache.juneau.PropertyStore;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.json.JsonSerializer;
import org.apache.juneau.parser.ParseException;
import org.apache.juneau.plaintext.PlainTextSerializer;
import org.apache.juneau.rest.client.HttpMethod;
import org.apache.juneau.rest.client.RestCall;
import org.apache.juneau.rest.client.RestCallException;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.rest.client.RestClientBuilder;
import org.apache.juneau.serializer.Serializer;
import org.apache.juneau.serializer.SerializerSession;
import org.apache.juneau.serializer.WriterSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.hamcrest.MatcherAssert.assertThat;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sblackmon on 3/19/17.
 */
public class Twitter implements Statuses {

  private static final Logger LOGGER = LoggerFactory.getLogger(Twitter.class);

  private static Map<TwitterConfiguration, Twitter> INSTANCE_MAP = new ConcurrentHashMap<>();

  private TwitterConfiguration configuration;

  private String rootUrl;

  private CloseableHttpClient httpclient;

  RestClient restClient;

  private Twitter(TwitterConfiguration configuration) {
    this.configuration = configuration;
    this.httpclient = HttpClients.createDefault();
    this.rootUrl = TwitterProviderUtil.baseUrl(configuration);
    this.restClient = new RestClientBuilder()
          .httpClient(httpclient, true)
          .serializer(PlainTextSerializer.class)
          .parser(JsonParser.class)
          .rootUrl(rootUrl)
          .build();
  }

  public static Twitter getInstance(TwitterConfiguration configuration) {
    if (INSTANCE_MAP.containsKey(configuration) && INSTANCE_MAP.get(configuration) != null) {
      return INSTANCE_MAP.get(configuration);
    } else {
      INSTANCE_MAP.put(configuration, new Twitter(configuration));
      return INSTANCE_MAP.get(configuration);
    }
  }

  @Override
  public List<Tweet> userTimeline(StatusesUserTimelineRequest parameters) {
    assertThat(
        "user_id or screen_name must be set",
        (
            Objects.nonNull(parameters.getUserId())
                || Objects.nonNull(parameters.getScreenName())
        )
    );
    String id = ObjectUtils.firstNonNull(parameters.getUserId(), parameters.getScreenName());
    // there may be other parameters too
    try {
      RestCall restCall = restClient.doGet("/statuses/lookup?id=" + id);
      List<Tweet> result = restCall.getResponse(List.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
      // what kind of exception?
    } catch (ParseException e) {
      LOGGER.warn("ParseException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    }
    return null;
  }

  @Override
  public List<Tweet> lookup(StatusesLookupRequest parameters) {
    String ids = StringUtils.join(parameters.getId(), ',');
    // there may be other parameters too
    try {
      RestCall restCall = restClient.doGet("/statuses/lookup?id=" + ids);
      List<Tweet> result = restCall.getResponse(List.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
      // what kind of exception?
    } catch (ParseException e) {
      LOGGER.warn("ParseException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    }
    return null;
  }

  @Override
  public Tweet show(StatusesShowRequest parameters) {
    String id = parameters.getId().toString();
    // there may be other parameters too
    try {
      RestCall restCall = restClient.doGet("/statuses/show/" + id);
      Tweet result = restCall.getResponse(Tweet.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
      // what kind of exception?
    } catch (ParseException e) {
      LOGGER.warn("ParseException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    }
    return null;
  }
}
