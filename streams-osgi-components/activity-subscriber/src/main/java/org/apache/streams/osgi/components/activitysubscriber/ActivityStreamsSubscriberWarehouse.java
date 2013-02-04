package org.apache.streams.osgi.components.activitysubscriber;

import java.util.ArrayList;

/**
 * Public API representing an example OSGi service
 */
public interface ActivityStreamsSubscriberWarehouse {

    public void register(String src, ActivityStreamsSubscriber activitySubscriber);
    public void register(String[] src, ActivityStreamsSubscriber activitySubscriber);
    public ArrayList<ActivityStreamsSubscriber> findSubscribersBySrc(String src);


}

