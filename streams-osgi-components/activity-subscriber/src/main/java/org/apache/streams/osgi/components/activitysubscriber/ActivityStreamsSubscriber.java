package org.apache.streams.osgi.components.activitysubscriber;


public interface ActivityStreamsSubscriber {
    public void receive(String activity);
    public String getStream();
    public void init();
    public void setInRoute(String route);
    public String getInRoute();
    public void setActivityStreamsSubscriberConfiguration(ActivityStreamsSubscription config);
    public void updateActivityStreamsSubscriberConfiguration(String config);

}
