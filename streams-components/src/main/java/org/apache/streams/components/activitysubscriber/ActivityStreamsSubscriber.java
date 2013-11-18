package org.apache.streams.components.activitysubscriber;

import org.apache.streams.persistence.model.ActivityStreamsEntry;

import java.util.Date;
import java.util.List;

public interface ActivityStreamsSubscriber {
    void receive(List<ActivityStreamsEntry> activities);
    String getStream() throws Exception;
    Date getLastUpdated();
    void setLastUpdated(Date lastUpdated);
    void reset();
}
