package org.apache.streams.osgi.components.activitysubscriber;


import java.util.ArrayList;
import java.util.HashMap;

public interface ActivityStreamsSubscriber {
    public void receive(String activity);
    public String getStream();
    public void init();
    public void setInRoute(String route);
    public String getInRoute();
    public void setActivityStreamsSubscriberConfiguration(ActivityStreamsSubscriberConfiguration config);
    public void updateActivityStreamsSubscriberConfiguration(ActivityStreamsSubscriberConfiguration config);

}
