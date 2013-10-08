package org.apache.streams.components.activitysubscriber;


import org.apache.streams.persistence.model.ActivityStreamsSubscription;

import java.util.Date;
import java.util.List;

public interface ActivityStreamsSubscriber {
    public void receive(List<String> activity);
    public String getStream();
    public void init();
    public void setInRoute(String route);
    public String getInRoute();
    public void setSubscription(ActivityStreamsSubscription config);
    public void updateSubscription(String config);
    public boolean isAuthenticated();
    public void setAuthenticated(boolean authenticated);
    public ActivityStreamsSubscription getSubscription();
    Date getLastUpdated();
    void setLastUpdated(Date lastUpdated);
}
