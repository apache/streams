package org.apache.streams.persistence.model.cassandra;

import org.apache.streams.persistence.model.ActivityStreamsEntry;
import org.apache.streams.persistence.model.ActivityStreamsObject;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.Date;

public class CassandraActivityStreamsEntry implements ActivityStreamsEntry, Comparable<CassandraActivityStreamsEntry>{
    private String id;
    private String tags;
    private Date published;
    private String verb;

    @JsonDeserialize(as = CassandraActivityStreamsObject.class)
    private ActivityStreamsObject actor;

    @JsonDeserialize(as = CassandraActivityStreamsObject.class)
    private ActivityStreamsObject object;

    @JsonDeserialize(as = CassandraActivityStreamsObject.class)
    private ActivityStreamsObject provider;

    @JsonDeserialize(as = CassandraActivityStreamsObject.class)
    private ActivityStreamsObject target;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public Date getPublished() {
        return published;
    }

    public void setPublished(Date published) {
        this.published = published;
    }

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public ActivityStreamsObject getActor() {
        return actor;
    }

    public void setActor(ActivityStreamsObject actor) {
        this.actor = actor;
    }

    public ActivityStreamsObject getObject() {
        return object;
    }

    public void setObject(ActivityStreamsObject object) {
        this.object = object;
    }

    public ActivityStreamsObject getProvider() {
        return provider;
    }

    public void setProvider(ActivityStreamsObject provider) {
        this.provider = provider;
    }

    public ActivityStreamsObject getTarget() {
        return target;
    }

    public void setTarget(ActivityStreamsObject target) {
        this.target = target;
    }

    public int compareTo(CassandraActivityStreamsEntry entry){
        return (this.getPublished()).compareTo(entry.getPublished());
    }

}
