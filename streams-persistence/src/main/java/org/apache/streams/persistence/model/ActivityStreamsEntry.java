package org.apache.streams.persistence.model;

import java.util.Date;

public interface ActivityStreamsEntry {
    String getVerb();
    void setVerb(String verb);
    String getId();
    void setId(String id);
    String getTags();
    void setTags(String tags);
    Date getPublished();
    void setPublished(Date published);
    ActivityStreamsObject getActor();
    void setActor(ActivityStreamsObject actor);
    ActivityStreamsObject getTarget();
    void setTarget(ActivityStreamsObject actor);
    ActivityStreamsObject getObject();
    void setObject(ActivityStreamsObject actor);
    ActivityStreamsObject getProvider();
    void setProvider(ActivityStreamsObject actor);

}
