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

package org.apache.streams.moreover;

import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.pojo.extensions.ExtensionUtil;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Provider;

import com.moreover.api.Article;
import com.moreover.api.Author;
import com.moreover.api.AuthorPublishingPlatform;
import com.moreover.api.Feed;
import com.moreover.api.Source;
import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.LANGUAGE_EXTENSION;
import static org.apache.streams.data.util.ActivityUtil.LOCATION_EXTENSION;
import static org.apache.streams.data.util.ActivityUtil.LOCATION_EXTENSION_COUNTRY;
import static org.apache.streams.data.util.ActivityUtil.getObjectId;

/**
 * Provides utilities for Moreover data.
 */
public class MoreoverUtils {

  private MoreoverUtils() {
  }

  public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  /**
   * convert article into Activity.
   * @param article article
   * @return Activity
   */
  public static Activity convert(Article article) {
    Activity activity = new Activity();
    Source source = article.getSource();
    activity.setActor(convert(article.getAuthor(), source.getName()));
    activity.setProvider(convert(source));
    activity.setTarget(convertTarget(source));
    activity.setObject(convertObject(article));
    activity.setPublished(DateTime.parse(article.getPublishedDate()));
    activity.setContent(article.getContent());
    activity.setTitle(article.getTitle());
    activity.setVerb("posted");
    fixActivityId(activity);
    addLocationExtension(activity, source);
    addLanguageExtension(activity, article);
    activity.setLinks(convertLinks(article));
    return activity;
  }

  /**
   * convert Source to Provider.
   * @param source Source
   * @return Provider
   */
  public static Provider convert(Source source) {
    Provider provider = new Provider();
    Feed feed = source.getFeed();
    String display = getProviderId(feed);
    provider.setId(ActivityUtil.getProviderId(display.trim().toLowerCase().replace(" ", "_")));
    provider.setDisplayName(display);
    provider.setUrl(feed.getUrl());
    return provider;
  }

  /**
   * convert Author and platformName to Actor.
   * @param author Author
   * @param platformName platformName
   * @return $.actor
   */
  public static ActivityObject convert(Author author, String platformName) {
    ActivityObject actor = new ActivityObject();
    AuthorPublishingPlatform platform = author.getPublishingPlatform();
    String userId = platform.getUserId();
    if (userId != null) {
      actor.setId(ActivityUtil.getPersonId(getProviderId(platformName), userId));
    }
    actor.setDisplayName(author.getName());
    actor.setUrl(author.getHomeUrl());
    actor.setSummary(author.getDescription());
    actor.setAdditionalProperty("email", author.getEmail());
    return actor;
  }

  private static void fixActivityId(Activity activity) {
    if (activity.getId() != null && activity.getId().matches("\\{[a-z]*\\}")) {
      activity.setId(null);
    }
  }

  private static List convertLinks(Article article) {
    List<String> list = new LinkedList<>();
    Article.OutboundUrls outboundUrls = article.getOutboundUrls();
    if (outboundUrls != null) {
      for (String url : outboundUrls.getOutboundUrl()) {
        list.add(url);
      }
    }
    return list;
  }

  /**
   * convertTarget.
   * @param source source
   * @return ActivityObject $.target
   */
  public static ActivityObject convertTarget(Source source) {
    ActivityObject object = new ActivityObject();
    object.setUrl(source.getHomeUrl());
    object.setDisplayName(source.getName());
    return object;
  }

  /**
   * convertObject.
   * @param article article
   * @return ActivityObject $.object
   */
  public static ActivityObject convertObject(Article article) {
    ActivityObject object = new ActivityObject();
    object.setContent(article.getContent());
    object.setSummary(article.getTitle());
    object.setUrl(article.getOriginalUrl());
    object.setObjectType(article.getDataFormat());
    String type = article.getDataFormat().equals("text") ? "article" : article.getDataFormat();
    object.setId(getObjectId(getProviderId(article.getSource().getFeed()), type, article.getId()));
    object.setPublished(DateTime.parse(article.getPublishedDate()));
    return object;
  }

  /**
   * addLocationExtension.
   * @param activity Activity
   * @param source Source
   */
  public static void addLocationExtension(Activity activity, Source source) {
    Map<String, Object> extensions = ExtensionUtil.getInstance().ensureExtensions(activity);
    String country = source.getLocation().getCountryCode() == null
        ? source.getLocation().getCountry()
        : source.getLocation().getCountryCode();
    if (country != null) {
      Map<String, Object> location = new HashMap<>();
      location.put(LOCATION_EXTENSION_COUNTRY, country);
      extensions.put(LOCATION_EXTENSION, location);
    }
  }

  /**
   * addLanguageExtension.
   * @param activity Activity
   * @param article Article
   */
  public static void addLanguageExtension(Activity activity, Article article) {
    Map<String, Object> extensions = ExtensionUtil.getInstance().ensureExtensions(activity);
    String language = article.getLanguage();
    if (language != null) {
      extensions.put(LANGUAGE_EXTENSION, language);
    }
  }

  private static String getProviderId(Feed feed) {
    return getProviderId(feed.getPublishingPlatform() == null ? feed.getMediaType() : feed.getPublishingPlatform());
  }

  private static String getProviderId(String feed) {
    return feed.toLowerCase().replace(" ", "_").trim();
  }
}
