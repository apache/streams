package org.apache.streams.cassandra.model;

import org.apache.rave.model.ActivityStreamsObject;
import org.apache.rave.portal.model.impl.ActivityStreamsEntryImpl;
import org.apache.rave.portal.model.impl.ActivityStreamsObjectImpl;
import org.codehaus.jackson.map.annotate.JsonDeserialize;

public class CassandraActivityStreamsEntry extends ActivityStreamsEntryImpl{
    @JsonDeserialize(as=ActivityStreamsObjectImpl.class)
    private ActivityStreamsObject object;

    @JsonDeserialize(as=ActivityStreamsObjectImpl.class)
    private ActivityStreamsObject target;

    @JsonDeserialize(as=ActivityStreamsObjectImpl.class)
    private ActivityStreamsObject actor;

    @JsonDeserialize(as=ActivityStreamsObjectImpl.class)
    private ActivityStreamsObject provider;
}
