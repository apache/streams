package org.apache.streams.twitter.serializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.streams.pojo.json.Activity;
import org.apache.streams.pojo.json.ActivityObject;
import org.apache.streams.pojo.json.Actor;
import org.apache.streams.twitter.pojo.Delete;
import org.apache.streams.twitter.pojo.Tweet;

import java.io.Serializable;

/**
* Created with IntelliJ IDEA.
* User: mdelaet
* Date: 9/30/13
* Time: 9:24 AM
* To change this template use File | Settings | File Templates.
*/
public class TwitterJsonDeleteActivitySerializer extends TwitterJsonEventActivitySerializer {

    public Activity convert(ObjectNode event) {

        Delete delete = null;
        try {
            delete = mapper.treeToValue(event, Delete.class);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Activity activity = new Activity();
        activity.setActor(buildActor(delete));
        activity.setVerb("delete");
        activity.setObject(buildActivityObject(delete));
        activity.setId(formatId(activity.getVerb(), delete.getDelete().getStatus().getIdStr()));
        activity.setProvider(buildProvider(event));
        addTwitterExtension(activity, event);
        return activity;
    }

    public Actor buildActor(Delete delete) {
        Actor actor = new Actor();
        actor.setId(formatId(delete.getDelete().getStatus().getUserIdStr()));
        return actor;
    }

    public ActivityObject buildActivityObject(Delete delete) {
        ActivityObject actObj = new ActivityObject();
        actObj.setId(formatId(delete.getDelete().getStatus().getIdStr()));
        actObj.setObjectType("tweet");
        return actObj;
    }

    public ActivityObject buildTarget(Tweet tweet) {
        return null;
    }

}
