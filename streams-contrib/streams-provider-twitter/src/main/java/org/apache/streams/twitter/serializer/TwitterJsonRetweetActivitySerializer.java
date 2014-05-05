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
import org.apache.streams.twitter.pojo.Retweet;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;

import java.io.IOException;
import java.util.HashMap;
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
public class TwitterJsonRetweetActivitySerializer implements ActivitySerializer<String> {

    public TwitterJsonRetweetActivitySerializer() {

    }

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
        activity.setActor(buildActor(retweet));
        activity.setVerb("share");
        if( retweet.getRetweetedStatus() != null )
            activity.setObject(buildActivityObject(retweet.getRetweetedStatus()));
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
        addTwitterExtension(activity, mapper.convertValue(retweet, ObjectNode.class));
        addLocationExtension(activity, retweet);
        return activity;
    }

    @Override
    public List<Activity> deserializeAll(List<String> serializedList) {
        return null;
    }

    public static Actor buildActor(Retweet retweet) {
        Actor actor = new Actor();
        User user = retweet.getUser();
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
        return actor;
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
        location.put("coordinates", retweet.getCoordinates());
        extensions.put("location", location);
    }

}
