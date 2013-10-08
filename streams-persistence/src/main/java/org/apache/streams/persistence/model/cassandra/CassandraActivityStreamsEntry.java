package org.apache.streams.persistence.model.cassandra;

import org.apache.rave.model.ActivityStreamsObject;
import org.apache.rave.portal.model.impl.ActivityStreamsEntryImpl;
import org.apache.rave.portal.model.impl.ActivityStreamsObjectImpl;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

import java.util.Date;

public class CassandraActivityStreamsEntry extends ActivityStreamsEntryImpl implements Comparable<CassandraActivityStreamsEntry>{

    @JsonDeserialize(as=ActivityStreamsObjectImpl.class)
    private ActivityStreamsObject object;

    @JsonDeserialize(as=ActivityStreamsObjectImpl.class)
    private ActivityStreamsObject target;

    @JsonDeserialize(as=ActivityStreamsObjectImpl.class)
    private ActivityStreamsObject actor;

    @JsonDeserialize(as=ActivityStreamsObjectImpl.class)
    private ActivityStreamsObject provider;

    public int compareTo(CassandraActivityStreamsEntry entry){
        return (this.getPublished()).compareTo(entry.getPublished());
    }
}
