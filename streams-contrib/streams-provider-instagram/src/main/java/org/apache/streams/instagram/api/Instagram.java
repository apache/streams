package org.apache.streams.instagram.api;

import org.apache.streams.instagram.config.InstagramConfiguration;
import org.apache.streams.instagram.pojo.UserRecentMediaRequest;
import org.apache.streams.instagram.provider.InstagramProviderUtil;
import org.apache.streams.jackson.StreamsJacksonMapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.juneau.json.JsonParser;
import org.apache.juneau.rest.client.RestCall;
import org.apache.juneau.rest.client.RestCallException;
import org.apache.juneau.rest.client.RestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sblackmon on 4/11/17.
 */
public class Instagram implements Media, Users {

  private static final Logger LOGGER = LoggerFactory.getLogger(Instagram.class);

  private static Map<InstagramConfiguration, Instagram> INSTANCE_MAP = new ConcurrentHashMap<>();

  private InstagramConfiguration configuration;

  private ObjectMapper mapper;

  private String rootUrl;

  private CloseableHttpClient httpclient;

  private InstagramOAuthRequestSigner oauthSigner;

  RestClient restClient;

  private Instagram(InstagramConfiguration configuration) throws InstantiationException {
    this.configuration = configuration;
    this.rootUrl = InstagramProviderUtil.baseUrl(configuration);
    this.oauthSigner = new InstagramOAuthRequestSigner(configuration.getOauth());
    this.httpclient = HttpClientBuilder.create()
        .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectionRequestTimeout(5000)
            .setConnectTimeout(5000)
            .setSocketTimeout(5000)
            .setCookieSpec("easy")
            .build()
        )
        .setMaxConnPerRoute(20)
        .setMaxConnTotal(100)
        .build();

    this.restClient = new RestClient()
        .setHttpClient(httpclient)
        .setParser(JsonParser.class)
        .setRootUrl(rootUrl);

    this.mapper = StreamsJacksonMapper.getInstance();
  }

  public static Instagram getInstance(InstagramConfiguration configuration) throws InstantiationException {
    if (INSTANCE_MAP.containsKey(configuration) && INSTANCE_MAP.get(configuration) != null) {
      return INSTANCE_MAP.get(configuration);
    } else {
      Instagram instagram = new Instagram(configuration);
      INSTANCE_MAP.put(configuration, instagram);
      return INSTANCE_MAP.get(configuration);
    }
  }


  @Override
  public UserInfoResponse self() {
    try {
      //  TODO: juneau-6.3.x-incubating
//      Users restUsers = restClient.getRemoteableProxy("/users", Users.class);
//      UserInfoResponse result = restUsers.lookupUser(parameters);
//      return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/users/self/");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .setRetryable(configuration.getRetryMax().intValue(), configuration.getRetrySleepMs().intValue(), new InstagramRetryHandler())
            .getResponseAsString();
        UserInfoResponse result = mapper.readValue(restResponseEntity, UserInfoResponse.class);
        return result;
      } catch (RestCallException e) {
        LOGGER.warn("RestCallException", e);
      }
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    } catch (Exception e) {
      LOGGER.warn("Exception", e);
    }
    return null;
  }

  @Override
  public UserInfoResponse lookupUser(String user_id) {
    try {
      //  TODO: juneau-6.3.x-incubating
//      Users restUsers = restClient.getRemoteableProxy("/users", Users.class);
//      UserInfoResponse result = restUsers.lookupUser(parameters);
//      return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/users/"+user_id);
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .setRetryable(configuration.getRetryMax().intValue(), configuration.getRetrySleepMs().intValue(), new InstagramRetryHandler())
            .getResponseAsString();
        UserInfoResponse result = mapper.readValue(restResponseEntity, UserInfoResponse.class);
        return result;
      } catch (RestCallException e) {
        LOGGER.warn("RestCallException", e);
      }
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    } catch (Exception e) {
      LOGGER.warn("Exception", e);
    }
    return null;
  }

  @Override
  public RecentMediaResponse selfMediaRecent(SelfRecentMediaRequest parameters) {
    try {
      //  TODO: juneau-6.3.x-incubating
//      Users restUsers = restClient.getRemoteableProxy("/users", Users.class);
//      RecentMediaResponse result = restUsers.selfMediaRecent(parameters);
//      return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/users/self/media/recent");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      if( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if( Objects.nonNull(parameters.getMaxId()) && StringUtils.isNotBlank(parameters.getMaxId().toString())) {
        uriBuilder.addParameter("max_id", parameters.getMaxId().toString());
      }
      if( Objects.nonNull(parameters.getMinId()) && StringUtils.isNotBlank(parameters.getMinId().toString())) {
        uriBuilder.addParameter("min", parameters.getMinId().toString());
      }
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .setRetryable(configuration.getRetryMax().intValue(), configuration.getRetrySleepMs().intValue(), new InstagramRetryHandler())
            .getResponseAsString();
        RecentMediaResponse result = mapper.readValue(restResponseEntity, RecentMediaResponse.class);
        return result;
      } catch (RestCallException e) {
        LOGGER.warn("RestCallException", e);
      }
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    } catch (Exception e) {
      LOGGER.warn("Exception", e);
    }
    return null;
  }

  @Override
  public RecentMediaResponse userMediaRecent(UserRecentMediaRequest parameters) {
    try {
    //  TODO: juneau-6.3.x-incubating
//      Users restUsers = restClient.getRemoteableProxy("/users", Users.class);
//      RecentMediaResponse result = restUsers.userMediaRecent(parameters);
//      return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/users/"+parameters.getUserId()+"/media/recent");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      if( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if( Objects.nonNull(parameters.getMaxId()) && StringUtils.isNotBlank(parameters.getMaxId().toString())) {
        uriBuilder.addParameter("max_id", parameters.getMaxId().toString());
      }
      if( Objects.nonNull(parameters.getMinId()) && StringUtils.isNotBlank(parameters.getMinId().toString())) {
        uriBuilder.addParameter("min", parameters.getMinId().toString());
      }
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .setRetryable(1, 1000, new InstagramRetryHandler())
            .getResponseAsString();
        RecentMediaResponse result = mapper.readValue(restResponseEntity, RecentMediaResponse.class);
        return result;
      } catch (RestCallException e) {
        LOGGER.warn("RestCallException", e);
      }
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    } catch (Exception e) {
      LOGGER.warn("Exception", e);
    }
    return null;
  }

  @Override
  public RecentMediaResponse selfMediaLiked(SelfLikedMediaRequest parameters) {
    try {
      //  TODO: juneau-6.3.x-incubating
//      Users restUsers = restClient.getRemoteableProxy("/users", Users.class);
//      RecentMediaResponse result = restUsers.selfMediaLiked(parameters);
//      return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/users/self/media/liked");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      if( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if( Objects.nonNull(parameters.getMaxLikeId()) && StringUtils.isNotBlank(parameters.getMaxLikeId().toString())) {
        uriBuilder.addParameter("max_like_id", parameters.getMaxLikeId().toString());
      }
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .setRetryable(configuration.getRetryMax().intValue(), configuration.getRetrySleepMs().intValue(), new InstagramRetryHandler())
            .getResponseAsString();
        RecentMediaResponse result = mapper.readValue(restResponseEntity, RecentMediaResponse.class);
        return result;
      } catch (RestCallException e) {
        LOGGER.warn("RestCallException", e);
      }
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    } catch (Exception e) {
      LOGGER.warn("Exception", e);
    }
    return null;
  }

  @Override
  public SearchUsersResponse searchUser(SearchUsersRequest parameters) {
    try {
      //  TODO: juneau-6.3.x-incubating
//      Users restUsers = restClient.getRemoteableProxy("/users", Users.class);
//      SearchUsersResponse result = restUsers.searchUser(parameters);
//      return result;
      URIBuilder uriBuilder = new URIBuilder()
          .setPath("/users/search");
      uriBuilder.addParameter("access_token", configuration.getOauth().getAccessToken());
      if( Objects.nonNull(parameters.getCount()) && StringUtils.isNotBlank(parameters.getCount().toString())) {
        uriBuilder.addParameter("count", parameters.getCount().toString());
      }
      if( Objects.nonNull(parameters.getQ()) && StringUtils.isNotBlank(parameters.getQ().toString())) {
        uriBuilder.addParameter("q", parameters.getQ().toString());
      }
      String sig = oauthSigner.generateSignature(uriBuilder.build().toString());
      uriBuilder.addParameter("sig", sig);
      RestCall restCall = restClient.doGet(uriBuilder.build().toString());
      try {
        String restResponseEntity = restCall
            .setRetryable(configuration.getRetryMax().intValue(), configuration.getRetrySleepMs().intValue(), new InstagramRetryHandler())
            .getResponseAsString();
        SearchUsersResponse result = mapper.readValue(restResponseEntity, SearchUsersResponse.class);
        return result;
      } catch (RestCallException e) {
        LOGGER.warn("RestCallException", e);
      }
    } catch (IOException e) {
      LOGGER.warn("IOException", e);
    } catch (URISyntaxException e) {
      LOGGER.warn("URISyntaxException", e);
    } catch (Exception e) {
      LOGGER.warn("Exception", e);
    }
    return null;
  }

  @Override
  public UsersInfoResponse comments(String media_id) {
    return null;
  }

  @Override
  public UsersInfoResponse likes(String media_id) {
    return null;
  }

  @Override
  public MediaResponse lookupMedia(String media_id) {
    return null;
  }

  @Override
  public MediaResponse shortcode(String shortcode) {
    return null;
  }

  @Override
  public SearchMediaResponse searchMedia(String shortcode) {
    return null;
  }
}
