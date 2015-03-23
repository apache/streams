package org.apache.streams.data.moreover;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.moreover.api.Article;
import com.moreover.api.Author;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.data.util.ActivityUtil;
import org.apache.streams.data.util.RFC3339Utils;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Provider;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

/**
 * Converts Article Information into Activities
 */
public class MoreoverArticleSerializer implements ActivitySerializer<Article> {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoreoverArticleSerializer.class);

    private static final String ID_PREFIX = "id:moreover:post:";
    private static final String ACTOR_ID_PREFIX = "id:moreover:";
    private static final String MOREOVER_ID = "id:providers:moreover";
    private static final String MOREOVER_DISPLAY = "Moreover";

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public Article serialize(Activity deserialized) throws ActivitySerializerException {
        throw new NotImplementedException();
    }

    @Override
    public Activity deserialize(Article article) throws ActivitySerializerException {
        Activity activity = new Activity();
        activity.setId(ID_PREFIX+article.getId());
        activity.setActor(createActor(article));
        activity.setContent(article.getContent());
        activity.setTitle(article.getTitle());
        activity.setUrl(article.getUrl());
        activity.setVerb("POST");
        activity.setLinks(createLinks(article));
        activity.setProvider(createProvider(article));
        try {
            activity.setPublished(RFC3339Utils.parseToUTC(article.getPublishedDate()));
        } catch (Throwable t) {
            LOGGER.warn("Unable to parse date : {}", article.getPublishedDate());
            try {
                activity.setPublished(RFC3339Utils.parseToUTC(article.getHarvestDate()));
            } catch (Throwable t2) {
                LOGGER.warn("Unable to parse havest date, {} . Setting published to NOW.", article.getHarvestDate());
                activity.setPublished(DateTime.now().withZone(DateTimeZone.UTC));
            }
        }
        Map<String, Object> extensions = Maps.newHashMap();
        extensions.put("moreover", article);
        activity.setAdditionalProperty("extensions", extensions);
        return activity;
    }

    private Provider createProvider(Article article) {
        Provider provider = new Provider();
        provider.setId(MOREOVER_ID);
        provider.setDisplayName(MOREOVER_DISPLAY);
        if(article.getSource() != null) {
            provider.setAdditionalProperty("domain", article.getSource());
        }
        return provider;
    }

    private List<String> createLinks(Article article) {
        List<String> links = Lists.newLinkedList();
        if(article.getOutboundUrls() != null) {
            links.addAll(article.getOutboundUrls().getOutboundUrl());
        }
        return links;
    }

    private Actor createActor(Article article) {
        Author author = article.getAuthor();
        Actor actor = new Actor();
        actor.setDisplayName(author.getName());
        actor.setId(ACTOR_ID_PREFIX+author.getName());
        actor.setSummary(author.getDescription());
        actor.setUrl(author.getHomeUrl());
        return actor;
    }

    @Override
    public List<Activity> deserializeAll(List<Article> serializedList) {
        List<Activity> activities = Lists.newLinkedList();
        for(Article article : serializedList) {
            try {
                activities.add(deserialize(article));
            } catch (ActivitySerializerException ase) {
                LOGGER.warn("Unable to convert Article to Activity : {}", article.getId());
            }
        }
        return activities;
    }
}
