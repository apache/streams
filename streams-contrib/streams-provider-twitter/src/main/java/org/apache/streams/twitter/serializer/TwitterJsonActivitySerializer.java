package org.apache.streams.twitter.serializer;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.AnnotationIntrospector;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector;
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.jackson.StreamsJacksonModule;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.pojo.json.Provider;
import org.apache.streams.twitter.Url;
import org.apache.streams.twitter.pojo.*;
import org.apache.streams.twitter.provider.TwitterEventClassifier;
import org.apache.streams.urls.LinkDetails;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.io.Serializable;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
 * Created by sblackmon on 3/26/14.
 */
public class TwitterJsonActivitySerializer implements ActivitySerializer<String>, Serializable
{

    public TwitterJsonActivitySerializer() {

    }

    TwitterJsonTweetActivitySerializer tweetActivitySerializer = new TwitterJsonTweetActivitySerializer();
    TwitterJsonRetweetActivitySerializer retweetActivitySerializer = new TwitterJsonRetweetActivitySerializer();
    TwitterJsonDeleteActivitySerializer deleteActivitySerializer = new TwitterJsonDeleteActivitySerializer();

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public String serialize(Activity deserialized) throws ActivitySerializerException {
        throw new NotImplementedException();
    }

    @Override
    public Activity deserialize(String serialized) throws ActivitySerializerException {

        Class documentSubType = TwitterEventClassifier.detectClass(serialized);

        Activity activity;
        if( documentSubType == Tweet.class )
            activity = tweetActivitySerializer.deserialize(serialized);
        else if( documentSubType == Retweet.class )
            activity = retweetActivitySerializer.deserialize(serialized);
        else if( documentSubType == Delete.class )
            activity = deleteActivitySerializer.deserialize(serialized);
        else throw new ActivitySerializerException("unrecognized type");

        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        throw new NotImplementedException();
    }

    public static Actor buildActorRetweet(Retweet tweet) {
        return buildActor(tweet.getUser());
    }

    public static Actor buildActorTweet(Tweet tweet) {
        return buildActor(tweet.getUser());
    }

    public static Actor buildActor(User user) {
        Actor actor = new Actor();

        actor.setId(formatId(
                Optional.fromNullable(
                        user.getIdStr())
                        .or(Optional.of(user.getId().toString()))
                        .orNull()
        ));
        actor.setDisplayName(user.getScreenName());
        if (user.getUrl()!=null){
            actor.setUrl(user.getUrl());
        }

        actor.setDisplayName(user.getName());
        actor.setSummary(user.getDescription());

        Map<String, Object> extensions = new HashMap<String, Object>();
        extensions.put("location", user.getLocation());
        extensions.put("posts", user.getStatusesCount());
        extensions.put("favorites", user.getFavouritesCount());
        extensions.put("followers", user.getFollowersCount());

        Map<String, Object> image = new HashMap<String, Object>();
        image.put("url", user.getProfileImageUrlHttps());

        extensions.put("image", image);
        extensions.put("screenName", user.getScreenName());

        actor.setAdditionalProperty("extensions", extensions);

        return actor;
    }

    public static Provider getProvider() {
        Provider provider = new Provider();
        provider.setId("id:providers:twitter");
        return provider;
    }

    public static void addTwitterExtension(Activity activity, ObjectNode event) {
        Map<String, Object> extensions = org.apache.streams.data.util.ActivityUtil.ensureExtensions(activity);
        extensions.put("twitter", event);
    }

    public static String formatId(String... idparts) {
        return Joiner.on(":").join(Lists.asList("id:twitter", idparts));
    }
}
