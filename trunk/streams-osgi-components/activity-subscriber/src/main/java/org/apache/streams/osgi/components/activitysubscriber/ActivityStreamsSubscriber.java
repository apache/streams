package org.apache.streams.osgi.components.activitysubscriber;


import java.util.Date;
import java.util.List;

public interface ActivityStreamsSubscriber {
    public void receive(List<String> activity);
    public String getStream();
    public void init();
    public void setInRoute(String route);
    public String getInRoute();
    public void setActivityStreamsSubscriberConfiguration(ActivityStreamsSubscription config);
    public void updateActivityStreamsSubscriberConfiguration(String config);
    public boolean isAuthenticated();
    public void setAuthenticated(boolean authenticated);
    public ActivityStreamsSubscription getActivityStreamsSubscriberConfiguration();
    Date getLastUpdated();
    void setLastUpdated(Date lastUpdated);
}
