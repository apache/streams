package org.apache.streams.twitter.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.apache.streams.data.util.ActivityUtil.ensureExtensions;

/**
* Created with IntelliJ IDEA.
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public class TwitterJsonTweetActivitySerializer extends TwitterJsonEventActivitySerializer implements ActivitySerializer<String> {

    public TwitterJsonTweetActivitySerializer() {

    }

    public Activity convert(ObjectNode event) throws ActivitySerializerException {

        Tweet tweet = null;
        try {
            tweet = mapper.treeToValue(event, Tweet.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        System.out.println("10");

        Activity activity = new Activity();

        System.out.println("11");

        activity.setActor(buildActor(tweet));
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
        activity.setGenerator(buildGenerator(event));
        activity.setIcon(getIcon(event));
        activity.setProvider(buildProvider(event));
        activity.setTitle("");
        activity.setContent(tweet.getText());
        activity.setUrl(getUrls(event));
        activity.setLinks(getLinks(tweet));

        System.out.println("12");

        addTwitterExtension(activity, event);
        addLocationExtension(activity, tweet);
        return activity;
    }

    public static Actor buildActor(Tweet tweet) {
        Actor actor = new Actor();
        User user = tweet.getUser();
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

    public static List<Object> getLinks(Tweet tweet) {
        List<Object> links = Lists.newArrayList();
        if( tweet.getEntities().getUrls() != null )
            for( Url url : tweet.getEntities().getUrls() ) {
                links.add(url.getExpandedUrl());
            }
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
