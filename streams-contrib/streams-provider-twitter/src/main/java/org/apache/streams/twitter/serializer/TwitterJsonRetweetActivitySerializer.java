package org.apache.streams.twitter.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.exceptions.ActivitySerializerException;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.twitter.Url;
import org.apache.streams.twitter.pojo.*;
import org.apache.streams.urls.LinkDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static org.apache.streams.twitter.serializer.TwitterJsonActivitySerializer.*;
import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
* Created with IntelliJ IDEA.
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public class TwitterJsonRetweetActivitySerializer implements ActivitySerializer<String>, Serializable {

    public TwitterJsonRetweetActivitySerializer() {

    }

    private final static Logger LOGGER = LoggerFactory.getLogger(TwitterJsonRetweetActivitySerializer.class);

    @Override
    public String serializationFormat() {
        return null;
    }

    @Override
    public String serialize(Activity deserialized) throws ActivitySerializerException {
        return null;
    }

    @Override
    public Activity deserialize(String event) throws ActivitySerializerException {

        ObjectMapper mapper = StreamsTwitterMapper.getInstance();
        Retweet retweet = null;
        try {
            retweet = mapper.readValue(event, Retweet.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Activity activity = new Activity();
        activity.setActor(buildActorRetweet(retweet));
        activity.setVerb("share");
        Tweet retweetStatus = retweet.getRetweetedStatus();

        if( retweet.getRetweetedStatus() != null )
            activity.setObject(buildActivityObject(retweetStatus));
        activity.setId(TwitterJsonActivitySerializer.formatId(activity.getVerb(),
                Optional.fromNullable(
                        retweet.getIdStr())
                        .or(Optional.of(retweet.getId().toString()))
                        .orNull()
        ));
        if(Strings.isNullOrEmpty(activity.getId()))
            throw new ActivitySerializerException("Unable to determine activity id");
        try {
            activity.setPublished(retweet.getCreatedAt());
        } catch( Exception e ) {
            throw new ActivitySerializerException("Unable to determine publishedDate", e);
        }
        //activity.setGenerator(buildGenerator(mapper));
        //activity.setIcon(getIcon(event));
        activity.setProvider(TwitterJsonActivitySerializer.getProvider());
        activity.setTitle("");
        try {
            activity.setContent(retweet.getRetweetedStatus().getText());
        } catch( Exception e ) {
            throw new ActivitySerializerException("Unable to determine content", e);
        }
        activity.setUrl("http://twitter.com/" + retweet.getUser().getIdStr() + "/status/" + retweet.getIdStr());
        activity.setLinks(TwitterJsonTweetActivitySerializer.getLinks(retweet.getRetweetedStatus()));

        addTwitterExtensions(activity, retweetStatus);
        addTwitterExtension(activity, mapper.convertValue(retweet, ObjectNode.class));
        addLocationExtension(activity, retweet);

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

    public static ActivityObject buildActivityObject(Tweet tweet) {
        ActivityObject actObj = new ActivityObject();
        String id =  Optional.fromNullable(
                tweet.getIdStr())
                .or(Optional.of(tweet.getId().toString()))
                .orNull();
        if( id != null )
            actObj.setId(id);
        actObj.setObjectType("tweet");
        return actObj;
    }

    public static void addLocationExtension(Activity activity, Retweet retweet) {
        Map<String, Object> extensions = ensureExtensions(activity);
        Map<String, Object> location = new HashMap<String, Object>();
        location.put("id", TwitterJsonActivitySerializer.formatId(retweet.getIdStr()));
        location.put("coordinates", retweet.getCoordinates());
        extensions.put("location", location);
    }

}
