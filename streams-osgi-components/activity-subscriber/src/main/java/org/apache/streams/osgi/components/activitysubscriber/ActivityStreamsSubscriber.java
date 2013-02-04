package org.apache.streams.osgi.components.activitysubscriber;


import java.util.ArrayList;
import java.util.HashMap;

public interface ActivityStreamsSubscriber {
    public void receive(String activity);
    public String getStream();
    public void init();
    public void setActivityStreamsSubscriberWarehouse(ActivityStreamsSubscriberWarehouse warehouse);
    public void addSrc(String[] src);
    public void addSrc(String src);
    public String[] getSubscriptions();
    public void setInRoute(String route);
    public String getInRoute();

}
