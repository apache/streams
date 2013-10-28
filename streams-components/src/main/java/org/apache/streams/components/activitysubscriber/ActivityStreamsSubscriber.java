package org.apache.streams.components.activitysubscriber;

import java.util.Date;
import java.util.List;

public interface ActivityStreamsSubscriber {
    void receive(List<String> activity);
    String getStream();
    Date getLastUpdated();
    void setLastUpdated(Date lastUpdated);
}
