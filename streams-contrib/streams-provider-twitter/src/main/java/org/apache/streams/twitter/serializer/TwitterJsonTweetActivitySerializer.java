package org.apache.streams.twitter.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang.NotImplementedException;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.jackson.StreamsJacksonMapper;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.twitter.Url;
import org.apache.streams.twitter.pojo.*;
import org.apache.streams.urls.LinkDetails;
import twitter4j.HashtagEntity;

import java.io.IOException;
import java.util.*;

import static org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer.*;
import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
* Created with IntelliJ IDEA.
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public class TwitterJsonTweetActivitySerializer implements ActivitySerializer<String> {

    public TwitterJsonTweetActivitySerializer() {

    }

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

        ObjectMapper mapper = StreamsTwitterMapper.getInstance();
        Tweet tweet = null;
        try {
            tweet = mapper.readValue(serialized, Tweet.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Activity activity = new Activity();

        activity.setActor(buildActorTweet(tweet));
        activity.setVerb("post");
        activity.setId(formatId(activity.getVerb(),
                Optional.fromNullable(
                        tweet.getIdStr())
                        .or(Optional.of(tweet.getId().toString()))
                        .orNull()));
        if(Strings.isNullOrEmpty(activity.getId()))
            throw new ActivitySerializerException("Unable to determine activity id");
        try {
            activity.setPublished(tweet.getCreatedAt());
        } catch( Exception e ) {
            throw new ActivitySerializerException("Unable to determine publishedDate", e);
        }
        activity.setTarget(buildTarget(tweet));
        activity.setProvider(getProvider());
        activity.setTitle("");
        activity.setContent(tweet.getText());
        activity.setUrl("http://twitter.com/" + tweet.getIdStr());
        activity.setLinks(getLinks(tweet));

        addTwitterExtension(activity, mapper.convertValue(tweet, ObjectNode.class));
        addLocationExtension(activity, tweet);
        addTwitterExtensions(activity, tweet);

        return activity;
    }

    public static void addTwitterExtensions(Activity activity, Tweet tweet) {
        Map<String, Object> extensions = ensureExtensions(activity);

        List<String> hashtags = new ArrayList<String>();
        for(Hashtag hashtag : tweet.getEntities().getHashtags()) {
            hashtags.add(hashtag.getText());
        }
        extensions.put("hashtags", hashtags);

        Map<String, Object> likes = new HashMap<String, Object>();
        likes.put("perspectival", tweet.getFavorited());
        likes.put("count", tweet.getAdditionalProperties().get("favorite_count"));

        extensions.put("likes", likes);

        Map<String, Object> rebroadcasts = new HashMap<String, Object>();
        rebroadcasts.put("perspectival", tweet.getRetweeted());
        rebroadcasts.put("count", tweet.getRetweetCount());

        extensions.put("rebroadcasts", rebroadcasts);

        List<Map<String, Object>> userMentions = new ArrayList<Map<String, Object>>();
        Entities entities = tweet.getEntities();

        for(UserMentions user : entities.getUserMentions()) {
            //Map the twitter user object into an actor
            Map<String, Object> actor = new HashMap<String, Object>();
            actor.put("id", "id:twitter:" + user.getIdStr());
            actor.put("displayName", user.getScreenName());

            userMentions.add(actor);
        }

        extensions.put("user_mentions", userMentions);

        List<LinkDetails> urls = new ArrayList<LinkDetails>();
        for(Url url : entities.getUrls()) {
            LinkDetails linkDetails = new LinkDetails();

            linkDetails.setFinalURL(url.getExpandedUrl());
            linkDetails.setNormalizedURL(url.getDisplayUrl());
            linkDetails.setOriginalURL(url.getUrl());

            urls.add(linkDetails);
        }
        extensions.put("urls", urls);

        extensions.put("keywords", tweet.getText());
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        return null;
    }

    public static List<String> getLinks(Tweet tweet) {
        List<String> links = Lists.newArrayList();
        if( tweet.getEntities().getUrls() != null ) {
            for (Url url : tweet.getEntities().getUrls()) {
                links.add(url.getExpandedUrl());
            }
        }
        else
            System.out.println("  0 links");
        return links;
    }

    public static ActivityObject buildTarget(Tweet tweet) {
        return null;
    }

    public static void addLocationExtension(Activity activity, Tweet tweet) {
        Map<String, Object> extensions = ensureExtensions(activity);
        Map<String, Object> location = new HashMap<String, Object>();
        location.put("id", formatId(
                Optional.fromNullable(
                        tweet.getIdStr())
                        .or(Optional.of(tweet.getId().toString()))
                        .orNull()
        ));
        location.put("coordinates", tweet.getCoordinates());
        extensions.put("location", location);
    }

}
