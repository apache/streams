package org.apache.streams.twitter.api;

import org.apache.streams.twitter.TwitterConfiguration;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;
import org.apache.streams.twitter.provider.TwitterProviderUtil;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.parser.ParseException;
import org.apache.juneau.plaintext.PlainTextSerializer;
import org.apache.juneau.rest.client.RestCall;
import org.apache.juneau.rest.client.RestCallException;
import org.apache.juneau.rest.client.RestClient;
import org.apache.juneau.rest.client.RestClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sblackmon on 3/19/17.
 */
public class Twitter implements Followers, Friends, Statuses, Users {

  private static final Logger LOGGER = LoggerFactory.getLogger(Twitter.class);

  private static Map<TwitterConfiguration, Twitter> INSTANCE_MAP = new ConcurrentHashMap<>();

  private TwitterConfiguration configuration;

  private String rootUrl;

  private CloseableHttpClient httpclient;

  private HttpRequestInterceptor oauthInterceptor;

  RestClient restClient;

  private Twitter(TwitterConfiguration configuration) {
    this.configuration = configuration;
    this.rootUrl = TwitterProviderUtil.baseUrl(configuration);
    this.oauthInterceptor = new TwitterOAuthRequestInterceptor(configuration.getOauth());
    this.httpclient = HttpClientBuilder.create()
        .addInterceptorFirst(oauthInterceptor).build();
    this.restClient = new RestClientBuilder()
        .httpClient(httpclient, true)
        .parser(JsonParser.class)
        .rootUrl(rootUrl)
        .retryable(
            configuration.getRetryMax().intValue(),
            configuration.getRetrySleepMs(),
            new TwitterRetryHandler())
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
    try {
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/statuses/user_timeline.json")
          .addParameter("user_id", parameters.getUserId().toString())
          .addParameter("screen_name", parameters.getScreenName())
          .addParameter("since_id", parameters.getSinceId().toString())
          .addParameter("count", parameters.getCount().toString())
          .addParameter("max_id", parameters.getMaxId().toString())
          .addParameter("trim_user", parameters.getTrimUser().toString())
          .addParameter("exclude_replies", parameters.getExcludeReplies().toString())
          .addParameter("contributor_details", parameters.getContributorDetails().toString())
          .addParameter("include_rts", parameters.getIncludeRts().toString());
      RestCall restCall = restClient.doGet(uriBuilder.build());

      List<Tweet> result = restCall.getResponse(LinkedList.class, Tweet.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
      // what kind of exception?
    } catch (ParseException e) {
      LOGGER.warn("ParseException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return new ArrayList<>();
  }

  @Override
  public List<Tweet> lookup(StatusesLookupRequest parameters) {
    String ids = StringUtils.join(parameters.getId(), ',');
    try {
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/statuses/lookup.json")
          .addParameter("id", ids)
          .addParameter("trim_user", parameters.getTrimUser().toString())
          .addParameter("include_entities", parameters.getIncludeEntities().toString())
          .addParameter("map", parameters.getMap().toString());
      RestCall restCall = restClient.doGet(uriBuilder.build());
      List<Tweet> result = restCall.getResponse(LinkedList.class, Tweet.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
      // what kind of exception?
    } catch (ParseException e) {
      LOGGER.warn("ParseException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return new ArrayList<>();
  }

  @Override
  public Tweet show(StatusesShowRequest parameters) {
    try {
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/statuses/show.json")
          .addParameter("id", parameters.getId().toString())
          .addParameter("trim_user", parameters.getTrimUser().toString())
          .addParameter("include_entities", parameters.getIncludeEntities().toString())
          .addParameter("include_my_retweet", parameters.getIncludeMyRetweet().toString());
      RestCall restCall = restClient.doGet(uriBuilder.build());
      Tweet result = restCall.getResponse(Tweet.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
      // what kind of exception?
    } catch (ParseException e) {
      LOGGER.warn("ParseException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return null;
  }

  @Override
  public FriendsIdsResponse ids(FriendsIdsRequest parameters) {
    try {
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/friends/ids.json")
          .addParameter("id", parameters.getId().toString())
          .addParameter("screen_name", parameters.getScreenName())
          .addParameter("curser", parameters.getCurser().toString())
          .addParameter("count", parameters.getCount().toString())
          .addParameter("stringify_ids", parameters.getStringifyIds().toString());
      RestCall restCall = restClient.doGet(uriBuilder.build());
      FriendsIdsResponse result = restCall.getResponse(FriendsIdsResponse.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
      // what kind of exception?
    } catch (ParseException e) {
      LOGGER.warn("ParseException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return null;
  }

  @Override
  public FriendsListResponse list(FriendsListRequest parameters) {
    try {
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/friends/list.json")
          .addParameter("id", parameters.getId().toString())
          .addParameter("screen_name", parameters.getScreenName())
          .addParameter("curser", parameters.getCurser().toString())
          .addParameter("count", parameters.getCount().toString())
          .addParameter("skip_status", parameters.getSkipStatus().toString())
          .addParameter("include_user_entities", parameters.getIncludeUserEntities().toString());
      RestCall restCall = restClient.doGet(uriBuilder.build());
      FriendsListResponse result = restCall.getResponse(FriendsListResponse.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
      // what kind of exception?
    } catch (ParseException e) {
      LOGGER.warn("ParseException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return null;
  }

  @Override
  public FollowersIdsResponse ids(FollowersIdsRequest parameters) {
    try {
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/followers/ids.json")
          .addParameter("id", parameters.getId().toString())
          .addParameter("screen_name", parameters.getScreenName())
          .addParameter("curser", parameters.getCurser().toString())
          .addParameter("count", parameters.getCount().toString())
          .addParameter("stringify_ids", parameters.getStringifyIds().toString());
      RestCall restCall = restClient.doGet(uriBuilder.build());
      FollowersIdsResponse result = restCall.getResponse(FollowersIdsResponse.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
      // what kind of exception?
    } catch (ParseException e) {
      LOGGER.warn("ParseException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return null;
  }

  @Override
  public FollowersListResponse list(FollowersListRequest parameters) {
    try {
      URIBuilder uriBuilder =
          new URIBuilder()
              .setPath("/followers/list.json")
              .addParameter("id", parameters.getId().toString())
              .addParameter("screen_name", parameters.getScreenName())
              .addParameter("curser", parameters.getCurser().toString())
              .addParameter("count", parameters.getCount().toString())
              .addParameter("skip_status", parameters.getSkipStatus().toString())
              .addParameter("include_user_entities", parameters.getIncludeUserEntities().toString());
      RestCall restCall = restClient.doGet(uriBuilder.build());
      FollowersListResponse result = restCall.getResponse(FollowersListResponse.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
      // what kind of exception?
    } catch (ParseException e) {
      LOGGER.warn("ParseException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return null;
  }

  @Override
  public List<User> lookup(UsersLookupRequest parameters) {
    String user_ids = StringUtils.join(parameters.getUserId(), ',');
    String screen_names = StringUtils.join(parameters.getScreenName(), ',');
    try {
      URIBuilder uriBuilder =
          new URIBuilder()
              .setPath("/users/lookup.json");
      if( Objects.nonNull(user_ids) && StringUtils.isNotBlank(user_ids)) {
        uriBuilder.addParameter("user_id", user_ids);
      }
      if( Objects.nonNull(screen_names) && StringUtils.isNotBlank(screen_names)) {
        uriBuilder.addParameter("screen_name", screen_names);
      }
      if( Objects.nonNull(parameters.getIncludeEntities()) && StringUtils.isNotBlank(parameters.getIncludeEntities().toString())) {
        uriBuilder.addParameter("include_entities", parameters.getIncludeEntities().toString());
      }
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      List<User> result = restCall.getResponse(LinkedList.class, User.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
      // what kind of exception?
    } catch (ParseException e) {
      LOGGER.warn("ParseException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return new ArrayList<>();
  }

  @Override
  public User show(UsersShowRequest parameters) {
    try {
      URIBuilder uriBuilder =
          new URIBuilder()
              .setPath("/users/show.json")
              .addParameter("user_id", parameters.getUserId().toString())
              .addParameter("screen_name", parameters.getScreenName())
              .addParameter("include_entities", parameters.getIncludeEntities().toString());
      RestCall restCall = restClient.doGet(uriBuilder.build());
      User result = restCall.getResponse(User.class);
      return result;
    } catch (RestCallException e) {
      LOGGER.warn("RestCallException", e);
      // what kind of exception?
    } catch (ParseException e) {
      LOGGER.warn("ParseException", e);
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    }
    return null;
  }

}
