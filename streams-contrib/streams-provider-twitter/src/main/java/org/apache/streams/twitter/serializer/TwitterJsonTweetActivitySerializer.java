package org.apache.streams.twitter.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.data.ActivitySerializer;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.twitter.pojo.Tweet;
import org.apache.streams.twitter.pojo.User;

import java.util.HashMap;
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

    public Activity convert(ObjectNode event) {

        Tweet tweet = null;
        try {
            tweet = mapper.treeToValue(event, Tweet.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Activity activity = new Activity();
        activity.setActor(buildActor(tweet));
        activity.setVerb("post");
        activity.setObject(buildActivityObject(tweet));
        activity.setId(formatId(activity.getVerb(), tweet.getIdStr()));
        activity.setTarget(buildTarget(tweet));
        activity.setPublished(parse(tweet.getCreatedAt()));
        activity.setGenerator(buildGenerator(event));
        activity.setIcon(getIcon(event));
        activity.setProvider(buildProvider(event));
        activity.setTitle("");
        activity.setContent(tweet.getText());
        activity.setUrl(getUrls(event));
        activity.setLinks(getLinks(event));
        addTwitterExtension(activity, event);
        addLocationExtension(activity, tweet);
        return activity;
    }

    public static Actor buildActor(Tweet tweet) {
        Actor actor = new Actor();
        User user = tweet.getUser();
        actor.setId(formatId(user.getIdStr(), tweet.getIdStr()));
        actor.setDisplayName(user.getScreenName());
        actor.setId(user.getIdStr());
        if (user.getUrl()!=null){
            actor.setUrl(user.getUrl());
        }
        return actor;
    }

    public static ActivityObject buildActivityObject(Tweet tweet) {
        ActivityObject actObj = new ActivityObject();
        actObj.setId(formatId(tweet.getIdStr()));
        actObj.setObjectType("tweet");
        return actObj;
    }

    public static ActivityObject buildTarget(Tweet tweet) {
        return null;
    }

    public static void addLocationExtension(Activity activity, Tweet tweet) {
        Map<String, Object> extensions = ensureExtensions(activity);
        Map<String, Object> location = new HashMap<String, Object>();
        location.put("id", formatId(tweet.getIdStr()));
        location.put("coordinates", tweet.getCoordinates());
        extensions.put("location", location);
    }

}
