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

package org.apache.streams.data.util;

import com.moreover.api.*;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Provider;
import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import static org.apache.streams.data.util.ActivityUtil.*;

/**
 * Provides utilities for Moroever data
 */
public class MoreoverUtils {
    private MoreoverUtils() {
    }

    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

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

    private static void fixActivityId(Activity activity) {
        if (activity.getId() != null && activity.getId().matches("\\{[a-z]*\\}")) {
            activity.setId(null);
        }
    }

    private static List convertLinks(Article article) {
        List list = new LinkedList();
        Article.OutboundUrls outboundUrls = article.getOutboundUrls();
        if (outboundUrls != null) {
            for (String url : outboundUrls.getOutboundUrl()) {
                list.add(url);
            }
        }
        return list;
    }

    public static ActivityObject convertTarget(Source source) {
        ActivityObject object = new ActivityObject();
        object.setUrl(source.getHomeUrl());
        object.setDisplayName(source.getName());
        return object;
    }

    public static ActivityObject convertObject(Article article) {
        ActivityObject object = new ActivityObject();
        object.setContent(article.getContent());
        object.setSummary(article.getTitle());
        object.setUrl(article.getOriginalUrl());
        object.setObjectType(article.getDataFormat());
        String type = article.getDataFormat().equals("text") ? "article" : article.getDataFormat();
        object.setId(getObjectId(getProviderID(article.getSource().getFeed()), type, article.getId()));
        object.setPublished(DateTime.parse(article.getPublishedDate()));
        return object;
    }

    public static Provider convert(Source source) {
        Provider provider = new Provider();
        Feed feed = source.getFeed();
        String display = getProviderID(feed);
        provider.setId(getProviderId(display.trim().toLowerCase().replace(" ", "_")));
        provider.setDisplayName(display);
        provider.setUrl(feed.getUrl());
        return provider;
    }

    public static Actor convert(Author author, String platformName) {
        Actor actor = new Actor();
        AuthorPublishingPlatform platform = author.getPublishingPlatform();
        String userId = platform.getUserId();
        if (userId != null) actor.setId(getPersonId(getProviderID(platformName), userId));
        actor.setDisplayName(author.getName());
        actor.setUrl(author.getHomeUrl());
        actor.setSummary(author.getDescription());
        actor.setAdditionalProperty("email", author.getEmail());
        return actor;
    }

    public static void addLocationExtension(Activity activity, Source value) {
        Map<String, Object> extensions = ensureExtensions(activity);
        String country = value.getLocation().getCountryCode() == null ? value.getLocation().getCountry() : value.getLocation().getCountryCode();
        if (country != null) {
            Map<String, Object> location = new HashMap<String, Object>();
            location.put(LOCATION_EXTENSION_COUNTRY, country);
            extensions.put(LOCATION_EXTENSION, location);
        }
    }

    public static void addLanguageExtension(Activity activity, Article value) {
        Map<String, Object> extensions = ensureExtensions(activity);
        String language = value.getLanguage();
        if (language != null) {
            extensions.put(LANGUAGE_EXTENSION, language);
        }
    }

    public static Date parse(String str) {
        DateFormat fmt = new SimpleDateFormat(DATE_FORMAT);
        try {
            return fmt.parse(str);
        } catch (ParseException e) {
            throw new IllegalArgumentException("Invalid date format", e);
        }
    }

    private static String getProviderID(Feed feed) {
        return getProviderID(feed.getPublishingPlatform() == null ? feed.getMediaType() : feed.getPublishingPlatform());
    }

    private static String getProviderID(String feed) {
        return feed.toLowerCase().replace(" ", "_").trim();
    }
}
