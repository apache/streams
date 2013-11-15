package org.apache.streams.persistence.model.mongo;

import com.mongodb.BasicDBObject;
import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.ActivityStreamsObject;
import java.util.*;


public class MongoActivityStreamsEntry extends BasicDBObject implements ActivityStreamsEntry {

    public String getId() {
        return (String)get("id");
    }

    public void setId(String id) {
        put("id",id);
    }

    public String getTags() {
        return (String)get("tags");
    }

    public void setTags(String tags) {
        put("tags",tags);
    }

    public Date getPublished() {
        return (Date)get("published");
    }

    public void setPublished(Date published) {
        put("published",published);
    }

    public String getVerb() {
        return (String)get("verb");
    }

    public void setVerb(String verb) {
        put("verb",verb);
    }

    public ActivityStreamsObject getActor() {
        return (ActivityStreamsObject)get("actor");
    }

    public void setActor(ActivityStreamsObject actor) {
        put("actor",actor);
    }

    public ActivityStreamsObject getObject() {
        return (ActivityStreamsObject)get("object");
    }

    public void setObject(ActivityStreamsObject object) {
        put("object",object);
    }

    public ActivityStreamsObject getProvider() {
        return (ActivityStreamsObject)get("provider");
    }

    public void setProvider(ActivityStreamsObject provider) {
        put("provider",provider);
    }

    public ActivityStreamsObject getTarget() {
        return (ActivityStreamsObject)get("target");
    }

    public void setTarget(ActivityStreamsObject target) {
        put("target",target);
    }
}
